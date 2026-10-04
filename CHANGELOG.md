# Changelog

## 0.3.1-beta+26.3 - 2026-10-04

### Changed

- Redesigned armor with swept segmented horns, a closed dragon faceplate,
  layered shoulder scales, a framed violet chest core, forearm guards,
  articulated leg scales, and slimmer boots.
- Added restrained dark-gold geometry over the existing black-violet palette.
- Retained equipment stats, abilities, recipes, progression, and the absence
  of a persistent side HUD.
- Added a three-stage development roadmap covering regression testing,
  exploration guidance, and optional mastery cosmetics.

### Fixed

- Trimmed Skywing Chestplates retain their normal wing texture without trying
  to load nonexistent wing-trim textures. The real item's trim, body-armor trim,
  enchantments, and other wing items remain unchanged.

### Validation

- Source generation and the build passed; a dedicated server passed M1-M5 and
  the progression checks, stopped cleanly, and completed `runServer` successfully.
- A freshly restarted client completed nine captures with gold Spire trim and
  Protection V. Inspected front, back, and oblique bow-drawing views show body
  trim and the restored wing texture without missing wing-trim texture errors.
- All nine final untrimmed-armor captures were reviewed: normal/close views,
  front/back/oblique angles, crouching, and bow drawing. The helper completed
  its 400-tick sequence, restored temporary settings, and logged no missing
  texture errors.
- Raised plates can obscure parts of the underlying trim. Complete animation,
  active gliding, armor stands, additional trim patterns, baby/slim models,
  ordinary Elytra, Vulkan, and third-party rendering compatibility remain
  unverified.

## 0.3.0-beta+26.3 - 2026-10-03

### Added

- Brief skill feedback through the normal action bar and equipped-weapon
  cooldown-ready sounds.
- Local Void Step landing particles in the world. The original exact 54-block
  ray and teleport placement remain unchanged.
- Six optional exploration trials, one matching tradeable sigil per player, and
  six alternative smithing recipes using the sigil, original base equipment, and
  a Nether Star. Smithing consumes the sigil; the exploration materials are not
  consumed. All seven original recipes remain available.
- A 26-advancement relic tab covering material clues, exploration trials,
  acquisition, skill mastery, and carrying all six relics together.
- Native 3D Gravity Blade and Resonance Bow models, separate purple/cyan bow
  modes, and three draw stages per mode.
- Additional armor geometry that follows the wearer's bones: dragon horns and
  crest, shoulder/chest plates, segmented leggings, and reinforced boots.
- Dedicated-server development checks for all six optional smithing routes,
  correct single-sigil rewards, and advancement references.

### Changed

- Brief skill feedback explains applicable equipment/cooldown failures; client
  cooldown timing follows the server's remaining duration.
- Stomp and gravity-field particles now show brief expanding/contracting waves;
  the existing sounds for the three stomp tiers are retained.
- Skill mastery records successful server-side teleports, damage, or effects;
  missed attacks and rejected effects do not complete hit-based challenges.
- Retained existing relic damage, attributes, range, cooldowns, original recipes,
  innate armor protections, armor trims, glint, and wing rendering.

### Validation

- `genSources` and `build` passed; a dedicated server passed M1-M5 and the new
  progression checks.
- In-game inspection confirmed armor details, base weapon models, all three
  rendered bow draw stages in both modes, and landing particles without missing
  relic textures. The advancement screen opened and its relic tab was inspected.
- The Voidwalker trial awarded one sigil and did not award another while the
  player continued meeting its conditions for several minutes. Its count also
  remained one after a server restart and returning to the End with the required
  materials.
- Full combat, multiplayer, the other exploration/mastery paths, full-inventory
  reward delivery, other GUI scales/languages, complete hand/armor-animation
  coverage, and Vulkan/rendering-mod compatibility remain unverified.

## 0.2.1-beta+26.3 - 2026-09-25

### Changed

- Ported to Minecraft 26.3, Fabric Loader 0.19.5, and Fabric API 0.161.0+26.3.
- Replaced GLFW key constants with Minecraft InputConstants for SDL input compatibility.
- Updated the Enderman Mixin target and migrated recipe-unlock advancement conditions to the 26.3 registry format.
- Existing relic abilities, stats, recipes, and innate armor protections are retained.

## 0.2.0-beta - 2026-08-31

### Added

- Relic armor created at a smithing table receives Protection V, Fire
  Protection V, Blast Protection V, and Projectile Protection V.
- The four innate protections remain on relic armor after grindstone use.

### Changed

- Smithing preserves unrelated enchantments and other components inherited
  from the original netherite armor.
- Innate protections are excluded from grindstone experience calculations to
  prevent repeatable experience farming; other removable enchantments still
  behave normally.
