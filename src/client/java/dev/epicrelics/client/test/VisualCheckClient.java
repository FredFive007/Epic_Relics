package dev.epicrelics.client.test;

import dev.epicrelics.EpicRelics;
import dev.epicrelics.ability.RelicEquipment;
import dev.epicrelics.item.ModComponents;
import dev.epicrelics.item.ModItems;
import dev.epicrelics.network.ResonanceModeRequestPayload;
import dev.epicrelics.network.VoidStepRequestPayload;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Explicitly enabled development-only captures on a loopback QA server. */
public final class VisualCheckClient {
	private static final Logger LOGGER = LoggerFactory.getLogger("epic_relics/visual-check");
	private static File output;
	private static final String STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS")
			.withZone(ZoneId.of("Asia/Shanghai")).format(Instant.now());
	private static int ticks;
	private static int waitingTicks;
	private static boolean finished;
	private static boolean initialized;
	private static CameraType originalCamera;
	private static boolean originalHudHidden;
	private static int originalSlot;
	private static AdvancementsScreen advancementScreen;
	private static boolean armorCheck;
	private static boolean armorStarted;
	private static int originalFov;
	private static float originalYaw;
	private static float originalPitch;
	private static float originalHeadYaw;
	private static float originalBodyYaw;
	private static boolean originalCrouching;
	private static float armorYawOffset;
	private static int armorBowSlot = -1;
	private static int armorEmptySlot = -1;

	private VisualCheckClient() {
	}

	public static void initialize() {
		if (initialized || !FabricLoader.getInstance().isDevelopmentEnvironment()
				|| !(Boolean.getBoolean("epicrelics.visualCheck") || Boolean.getBoolean("epicrelics.armorVisualCheck"))) {
			return;
		}
		initialized = true;
		armorCheck = Boolean.getBoolean("epicrelics.armorVisualCheck");
		String configuredOutput = System.getProperty("epicrelics.visualCheckOutput");
		output = configuredOutput == null || configuredOutput.isBlank()
				? new File(Minecraft.getInstance().gameDirectory, "epic-relics-visual-qa")
				: new File(configuredOutput);
		try {
			Files.createDirectories(output.toPath());
		} catch (IOException exception) {
			LOGGER.error("Cannot create visual QA output directory", exception);
			return;
		}
		ClientTickEvents.END_CLIENT_TICK.register(VisualCheckClient::tick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			restoreSettings(client);
			if (ticks > 0 && !finished) {
				finished = true;
				LOGGER.warn("Visual QA interrupted by disconnect after {} ticks", ticks);
			}
		});
		LOGGER.info("Visual QA enabled ({}); waiting for a loopback multiplayer server and all four relic armor pieces.",
				armorCheck ? "armor-only sequence" : "skills and progression sequence");
	}

	private static void tick(Minecraft client) {
		if (finished || client.player == null || client.level == null) {
			return;
		}
		var server = client.getCurrentServer();
		if (server == null || !(server.ip.equals("localhost") || server.ip.startsWith("localhost:")
				|| server.ip.equals("127.0.0.1") || server.ip.startsWith("127.0.0.1:"))) {
			return;
		}
		if (client.gui.screen() != null && client.gui.screen() != advancementScreen) {
			setHeld(client.options.keyUse, false, client.options.toggleUse().get());
			if (armorCheck) {
				setHeld(client.options.keyShift, false, client.options.toggleCrouch().get());
			}
			if (++waitingTicks % 100 == 1) {
				LOGGER.warn("Visual QA is waiting for screen {} to close; no automatic UI interaction.",
						client.gui.screen().getClass().getName());
			}
			return;
		}
		if (client.isPaused()) {
			return;
		}
		if (armorCheck) {
			tickArmor(client);
			return;
		}
		if (ticks == 0) {
			if (!RelicEquipment.hasHelmet(client.player) || !RelicEquipment.hasChestplate(client.player)
					|| !RelicEquipment.hasLeggings(client.player) || !RelicEquipment.hasBoots(client.player)
					|| !client.player.getMainHandItem().is(ModItems.GRAVITY_BLADE)) {
				if (++waitingTicks % 100 == 1) {
					LOGGER.info("Visual QA is waiting for the complete armor set and the Gravity Blade.");
				}
				return;
			}
			originalCamera = client.options.getCameraType();
			originalHudHidden = client.gui.hud.isHidden();
			originalSlot = client.player.getInventory().getSelectedSlot();
			setCamera(client, CameraType.FIRST_PERSON);
			LOGGER.info("Visual QA ready: gravity tick 40, teleport tick 50; captures 100/200/300, bow 380/385/392 and 450/455/462, armor 540, advancements 620; complete at 640.");
		}
		ticks++;
		if (ticks == 40) {
			client.options.keyUse.setDown(true);
		} else if (ticks == 42) {
			client.options.keyUse.setDown(false);
		} else if (ticks == 50 && ClientPlayNetworking.canSend(VoidStepRequestPayload.TYPE)) {
			ClientPlayNetworking.send(VoidStepRequestPayload.INSTANCE);
		} else if (ticks == 100) {
			capture(client, "00-skill-cooldown");
		} else if (ticks == 200) {
			capture(client, "01-first-person");
		} else if (ticks == 280) {
			setCamera(client, CameraType.THIRD_PERSON_FRONT);
		} else if (ticks == 300) {
			capture(client, "02-third-person-front");
		} else if (ticks == 360) {
			setCamera(client, CameraType.FIRST_PERSON);
			if (!client.player.getInventory().getItem(1).is(ModItems.RESONANCE_BOW)) {
				LOGGER.error("Visual QA requires a Resonance Bow in hotbar slot 1; stopping without moving any items.");
				restoreSettings(client);
				finished = true;
				return;
			}
			client.player.getInventory().setSelectedSlot(1);
		} else if (ticks == 365 && client.player.getMainHandItem().getOrDefault(ModComponents.RESONANCE_MODE, false)) {
			requestBowMode();
		} else if (ticks == 370 || ticks == 440) {
			client.options.keyUse.setDown(true);
		} else if (ticks == 380) {
			capture(client, "03-dark-bow-pulling-0");
		} else if (ticks == 385) {
			capture(client, "04-dark-bow-pulling-1");
		} else if (ticks == 392) {
			capture(client, "05-dark-bow-pulling-2");
		} else if (ticks == 410 || ticks == 480) {
			client.options.keyUse.setDown(false);
		} else if (ticks == 430) {
			requestBowMode();
		} else if (ticks == 450) {
			capture(client, "06-sonic-bow-pulling-0");
		} else if (ticks == 455) {
			capture(client, "07-sonic-bow-pulling-1");
		} else if (ticks == 462) {
			capture(client, "08-sonic-bow-pulling-2");
		} else if (ticks == 500) {
			KeyMapping.releaseAll();
			setCamera(client, originalCamera);
			client.player.getInventory().setSelectedSlot(originalSlot);
		} else if (ticks == 530) {
			setCamera(client, CameraType.THIRD_PERSON_FRONT);
			setHudHidden(client, true);
		} else if (ticks == 540) {
			capture(client, "09-armor-no-hud");
		} else if (ticks == 560) {
			restoreSettings(client);
		} else if (ticks == 600 && client.getConnection() != null) {
			var advancements = client.getConnection().getAdvancements();
			advancementScreen = new AdvancementsScreen(advancements);
			client.gui.setScreen(advancementScreen);
			var root = advancements.get(EpicRelics.id("relics/root"));
			if (root != null) {
				advancements.setSelectedTab(root, true);
			} else {
				LOGGER.warn("Visual QA could not find the Epic Relics advancement root; capturing the available advancement tab.");
			}
		} else if (ticks == 620) {
			capture(client, "10-advancement-tree");
		} else if (ticks == 640) {
			if (client.gui.screen() == advancementScreen) {
				client.gui.setScreen(null);
			}
			advancementScreen = null;
			KeyMapping.releaseAll();
			finished = true;
			LOGGER.info("Visual QA capture sequence complete at tick 640. Camera, HUD, selected slot restored; advancement screen closed; all keys released; no options saved.");
		}
	}

	/** A short photo sequence; never equips, teleports, gives items, or sends commands. */
	private static void tickArmor(Minecraft client) {
		var player = client.player;
		if (ticks == 0) {
			if (!RelicEquipment.hasHelmet(player) || !RelicEquipment.hasChestplate(player)
					|| !RelicEquipment.hasLeggings(player) || !RelicEquipment.hasBoots(player)) {
				if (++waitingTicks % 100 == 1) {
					LOGGER.info("Armor QA is waiting for all four equipped relic armor pieces; no weapon is required.");
				}
				return;
			}
			originalCamera = client.options.getCameraType();
			originalHudHidden = client.gui.hud.isHidden();
			originalSlot = player.getInventory().getSelectedSlot();
			originalFov = client.options.fov().get();
			originalYaw = player.getYRot();
			originalPitch = player.getXRot();
			originalHeadYaw = player.yHeadRot;
			originalBodyYaw = player.yBodyRot;
			originalCrouching = client.options.keyShift.isDown();
			armorStarted = true;
			KeyMapping.releaseAll();
			setHudHidden(client, true);
			setCamera(client, CameraType.THIRD_PERSON_FRONT);
			client.options.fov().set(70);
			for (int slot = 0; slot < 9; slot++) {
				var stack = player.getInventory().getItem(slot);
				if (armorEmptySlot < 0 && stack.isEmpty()) {
					armorEmptySlot = slot;
				}
				if (armorBowSlot < 0 && stack.is(ModItems.RESONANCE_BOW)) {
					armorBowSlot = slot;
				}
			}
			if (armorEmptySlot >= 0) {
				player.getInventory().setSelectedSlot(armorEmptySlot);
			}
			LOGGER.info("Armor QA ready: standard-FOV front 60; close front/back/left/right 100/140/180/220; crouch front/back 260/300; bow 350/370 if available; restore at 400. Original FOV={}", originalFov);
		}
		ticks++;
		if (ticks == 60) {
			capture(client, "armor-01-standard-fov70-front");
		} else if (ticks == 80) {
			client.options.fov().set(50);
		} else if (ticks == 100) {
			capture(client, "armor-02-close-fov50-front");
		} else if (ticks == 120) {
			setCamera(client, CameraType.THIRD_PERSON_BACK);
		} else if (ticks == 140) {
			capture(client, "armor-03-close-back");
		} else if (ticks == 160) {
			setCamera(client, CameraType.THIRD_PERSON_FRONT);
			armorYawOffset = -45.0F;
		} else if (ticks == 180) {
			capture(client, "armor-04-close-left-oblique");
		} else if (ticks == 200) {
			armorYawOffset = 45.0F;
		} else if (ticks == 220) {
			capture(client, "armor-05-close-right-oblique");
		} else if (ticks == 240) {
			armorYawOffset = 0.0F;
			setHeld(client.options.keyShift, true, client.options.toggleCrouch().get());
		} else if (ticks == 260) {
			capture(client, "armor-06-crouch-front");
		} else if (ticks == 280) {
			setCamera(client, CameraType.THIRD_PERSON_BACK);
		} else if (ticks == 300) {
			capture(client, "armor-07-crouch-back");
		} else if (ticks == 310) {
			setHeld(client.options.keyShift, false, client.options.toggleCrouch().get());
			setCamera(client, CameraType.THIRD_PERSON_FRONT);
			if (armorBowSlot >= 0) {
				player.getInventory().setSelectedSlot(armorBowSlot);
			} else {
				LOGGER.warn("Armor QA bow pose skipped: no Resonance Bow in the hotbar.");
			}
		} else if (ticks == 320 && armorBowSlot >= 0) {
			if (player.getProjectile(player.getMainHandItem()).isEmpty()) {
				LOGGER.warn("Armor QA bow pose skipped: no usable arrow.");
				armorBowSlot = -1;
			} else {
				setHeld(client.options.keyUse, true, client.options.toggleUse().get());
			}
		} else if (ticks == 350 && armorBowSlot >= 0) {
			capture(client, player.isUsingItem() ? "armor-08-bow-drawn-front" : "armor-08-bow-pose-not-active");
		} else if (ticks == 360 && armorBowSlot >= 0) {
			armorYawOffset = -35.0F;
		} else if (ticks == 370 && armorBowSlot >= 0) {
			capture(client, player.isUsingItem() ? "armor-09-bow-drawn-oblique" : "armor-09-bow-pose-not-active");
		} else if (ticks == 380) {
			// Switching away cancels a bow draw without intentionally firing a projectile.
			int restingSlot = armorEmptySlot >= 0 ? armorEmptySlot
					: originalSlot != armorBowSlot ? originalSlot : (originalSlot + 1) % 9;
			if (restingSlot != player.getInventory().getSelectedSlot()) {
				player.getInventory().setSelectedSlot(restingSlot);
				player.stopUsingItem();
			}
			setHeld(client.options.keyUse, false, client.options.toggleUse().get());
			armorYawOffset = 0.0F;
		} else if (ticks == 400) {
			restoreSettings(client);
			finished = true;
			LOGGER.info("Armor QA complete at tick 400. Camera, HUD, hotbar slot, FOV, view and body/head rotations restored; no options saved.");
			return;
		}
		if (!finished) {
			// LocalPlayer's camera reads view yaw, while its rendered body/head read these fields.
			// Keep the pose aligned and move only the temporary view angle for oblique photographs.
			setArmorRotations(client, originalYaw + armorYawOffset, 0.0F, originalYaw, originalYaw);
		}
	}

	private static void setHeld(KeyMapping key, boolean held, boolean toggleMode) {
		if (key.isDown() != held) {
			key.setDown(toggleMode || held);
		}
	}

	private static void setArmorRotations(Minecraft client, float yaw, float pitch, float headYaw, float bodyYaw) {
		var player = client.player;
		if (player != null) {
			player.setYRot(yaw);
			player.yRotO = yaw;
			player.setXRot(pitch);
			player.xRotO = pitch;
			player.yHeadRot = headYaw;
			player.yHeadRotO = headYaw;
			player.yBodyRot = bodyYaw;
			player.yBodyRotO = bodyYaw;
		}
	}

	private static void requestBowMode() {
		if (ClientPlayNetworking.canSend(ResonanceModeRequestPayload.TYPE)) {
			ClientPlayNetworking.send(ResonanceModeRequestPayload.INSTANCE);
		}
	}

	private static void capture(Minecraft client, String label) {
		String filename = "epic-relics-" + STAMP + "-" + label + ".png";
		LOGGER.info("Visual QA capture requested: {}", new File(new File(output, "screenshots"), filename));
		if (client.player != null) {
			LOGGER.info("Visual QA state: tick={}, item={}, useTicks={}, sonic={}", ticks,
					client.player.getMainHandItem(), client.player.getTicksUsingItem(),
					client.player.getMainHandItem().getOrDefault(ModComponents.RESONANCE_MODE, false));
		}
		try {
			Screenshot.grab(output, filename, client.gameRenderer.mainRenderTarget(), 1,
					message -> LOGGER.info("Visual QA capture result: {}", message.getString()));
		} catch (RuntimeException exception) {
			LOGGER.error("Visual QA capture failed: " + label, exception);
			restoreSettings(client);
			finished = true;
		}
	}

	private static void setCamera(Minecraft client, CameraType camera) {
		client.options.setCameraType(camera);
		client.gameRenderer.checkEntityPostEffect(camera.isFirstPerson() ? client.getCameraEntity() : null);
	}

	private static void setHudHidden(Minecraft client, boolean hidden) {
		if (client.gui.hud.isHidden() != hidden) {
			client.gui.hud.toggle();
		}
	}

	private static void restoreSettings(Minecraft client) {
		KeyMapping.releaseAll();
		if (armorStarted) {
			client.options.fov().set(originalFov);
			setArmorRotations(client, originalYaw, originalPitch, originalHeadYaw, originalBodyYaw);
			setHeld(client.options.keyShift, originalCrouching, client.options.toggleCrouch().get());
			armorStarted = false;
		}
		if (advancementScreen != null && client.gui.screen() == advancementScreen) {
			client.gui.setScreen(null);
		}
		advancementScreen = null;
		if (originalCamera != null) {
			setCamera(client, originalCamera);
			setHudHidden(client, originalHudHidden);
			if (client.player != null) {
				client.player.getInventory().setSelectedSlot(originalSlot);
			}
			originalCamera = null;
		}
	}
}
