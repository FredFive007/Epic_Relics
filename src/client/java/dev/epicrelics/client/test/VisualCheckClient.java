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

	private VisualCheckClient() {
	}

	public static void initialize() {
		if (initialized || !FabricLoader.getInstance().isDevelopmentEnvironment()
				|| !Boolean.getBoolean("epicrelics.visualCheck")) {
			return;
		}
		initialized = true;
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
		LOGGER.info("Visual QA enabled; waiting for a loopback multiplayer server and all four relic armor pieces.");
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
			client.options.keyUse.setDown(false);
			if (++waitingTicks % 100 == 1) {
				LOGGER.warn("Visual QA is waiting for screen {} to close; no automatic UI interaction.",
						client.gui.screen().getClass().getName());
			}
			return;
		}
		if (client.isPaused()) {
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
