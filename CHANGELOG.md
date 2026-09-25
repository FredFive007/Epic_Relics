# Changelog

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
