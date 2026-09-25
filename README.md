# Epic Relics

Epic Relics is a Fabric mod for Minecraft Java Edition 26.3.

It adds six unbreakable endgame relics with distinct armor, movement, melee,
area-control, and ranged abilities.

## Beta 0.2.0 armor upgrade

Upgrading netherite armor into relic armor now preserves its existing name,
trim, components, and unrelated enchantments while granting Protection V,
Fire Protection V, Blast Protection V, and Projectile Protection V.

These four protections are innate to relic armor. A grindstone removes other
eligible enchantments normally, but retains the innate protections and does not
award experience for them.

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
