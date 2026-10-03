# Epic Relics

Epic Relics is a Fabric mod for Minecraft Java Edition 26.3.

It adds six unbreakable endgame relics with distinct armor, movement, melee,
area-control, and ranged abilities.

## Beta 0.3.0: exploration, skill feedback, and 3D relics

Version `0.3.0-beta+26.3` adds brief skill feedback through the normal action bar
and equipped-weapon cooldown-ready sounds. Void Step marks its estimated landing
point with particles in the world. It retains the original 54-block eye ray and
exact hit placement; the marker does not move the destination, prevent a cast,
or guarantee a safe landing.

The Gravity Blade and both Resonance Bow modes now have native 3D item models.
The bow has purple Darkness Arrow and cyan Sonic Boom details, with three draw
stages for each mode. Armor gains moving dragon horns, a brow crest, shoulder and
chest plates, segmented leg plates, and reinforced boots that follow the wearer.
Existing armor textures, trims, enchantment glint, and Skywing wings remain in use.
Stomp shockwaves expand over a few frames, while the gravity field contracts
visually and its sound and particle feedback distinguish it from other skills.

Existing damage, armor, range, cooldown, and innate protection values are retained.

### Optional exploration trials

All seven original smithing recipes remain available. Six additional recipes
accept a trial sigil in the template slot, the original corresponding base item,
and a Nether Star. A sigil is an optional alternative to the original template
material; it is consumed by smithing.

Each trial awards its matching sigil once per player. The exploration materials
are checked, not consumed. Sigils can be traded and are not bound to their first
owner. Ordinary advancement progress records the award; this is not a repeatable
material source or a mandatory crafting gate.

| Sigil | Trial requirement | Smithing base | Result |
| --- | --- | --- | --- |
| Dragon Sight | Carry a dragon head and dragon breath together in the End. | Netherite helmet | Dragon Sight Helmet |
| Skywing | While in the End, complete a continuous glide lasting at least 10 seconds. | Netherite chestplate | Skywing Chestplate |
| Voidwalker | Carry dragon breath and chorus fruit together in the End. | Netherite leggings | Voidwalker Leggings |
| Heavy Core | Personally unlock an ominous vault, then carry a heavy core. | Netherite boots | Heavy Core Boots |
| Gravity | Earn vanilla Over-Overkill by dealing at least 50 hearts of damage in one mace hit, then carry a mace. | Netherite sword | Gravity Blade |
| Resonance | Earn vanilla Sneak 100 by avoiding a sculk sensor through sneaking, then carry an echo shard in the Deep Dark. | Netherite Bow Blank | Resonance Bow |

Every row also requires a **Nether Star** in the addition slot. The Voidwalker
trial provides a personal route to the leggings without consuming the world's
dragon egg. The original dragon-egg recipe remains available.

### Advancement paths

The Epic Relics tab contains **26 visible advancements**: a root, six material
clues, six exploration trials, six relic acquisition milestones, six mastery
challenges, and a collection challenge. Acquisition milestones also accept relics
obtained through the original recipes. The tab's parent connections guide the
journey; they do not lock smithing recipes.

| Mastery challenge | Requirement |
| --- | --- |
| Eyes Through Darkness | Wear the Dragon Sight Helmet in the Nether, the End, and the Deep Dark. |
| Ride the Wind | Glide continuously for 10 seconds while wearing the Skywing Chestplate. Landing or removing it resets this attempt. |
| Walker of Three Worlds | Successfully use Void Step in the Overworld, the Nether, and the End. |
| Three Echoes of the Earth | Damage a valid target with each stomp tier: falls of `4 <= distance < 8`, `8 <= distance < 16`, and `distance >= 16` blocks. |
| Master of Gravity | Land a Gravity Blade plunge attack and successfully slow a valid target with its gravity field. |
| Dual Resonance | Successfully apply Darkness with a dark arrow and damage a target with a sonic boom. |
| The Six United | Carry all six final relics at the same time; equipped armor counts. |

### Controls

- **V:** toggle Dragon Sight while wearing its helmet.
- **R:** use Void Step while wearing Voidwalker Leggings.
- **G:** switch the Resonance Bow's held mode.
- **Use item / right-click:** cast the Gravity Blade's field or draw the bow;
  Sonic Boom requires a full draw.

Bindings can be changed in Minecraft's controls menu. The landing marker respects
the vanilla particle setting: Decreased reduces its particle count and Minimal
disables it.

## Innate armor protections

Upgrading netherite armor into relic armor now preserves its existing name,
trim, components, and unrelated enchantments while granting Protection V,
Fire Protection V, Blast Protection V, and Projectile Protection V.

These four protections are innate to relic armor. A grindstone removes other
eligible enchantments normally, but retains the innate protections and does not
award experience for them.

## Validation status

On 2026-10-03, source generation, the build, dedicated-server M1-M5 checks, and
the new checks for six alternative recipes, exact sigil rewards, and the
advancement tree passed. In-game inspection confirmed the armor details, base
weapon models, all three rendered draw stages in each bow mode, and landing
marker; the client reported no missing relic textures. The advancement screen
was opened and its relic tab inspected. The Voidwalker trial awarded
one sigil in the End, did not repeat while its conditions remained satisfied for
several minutes, and remained at one sigil after a server restart and returning
to the End with the required materials.

Full combat, multiplayer, the other trial/mastery conditions, inventory-overflow
rewards, Vulkan/rendering-mod compatibility, other GUI scales and languages, and
all hand/armor-animation combinations still need verification.

## Requirements

- Minecraft Java Edition 26.3
- Fabric Loader 0.19.5 or newer compatible release
- Fabric API 0.161.0+26.3
- Java 25

## Build from source

Use JDK 25 and the Gradle Wrapper:

```powershell
.\gradlew.bat genSources
.\gradlew.bat build
.\gradlew.bat runClient
```
