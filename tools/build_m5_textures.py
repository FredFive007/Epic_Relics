from __future__ import annotations

import os
from pathlib import Path
from zipfile import ZipFile

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE_DIR = ROOT / "art-reference" / "generated-sources"
TEXTURE_ROOT = ROOT / "src" / "main" / "resources" / "assets" / "epic_relics" / "textures"
ITEM_DIR = TEXTURE_ROOT / "item"
EQUIPMENT_DIR = TEXTURE_ROOT / "entity" / "equipment"
PREVIEW_DIR = ROOT / "art-reference" / "previews"


def find_minecraft_jar() -> Path:
    override = os.environ.get("EPIC_RELICS_MINECRAFT_JAR")
    if override:
        path = Path(override).expanduser()
        if path.is_file():
            return path
        raise FileNotFoundError(f"EPIC_RELICS_MINECRAFT_JAR does not exist: {path}")

    gradle_home = Path(os.environ.get("GRADLE_USER_HOME", Path.home() / ".gradle"))
    cache_root = (
        gradle_home
        / "caches"
        / "fabric-loom"
        / "minecraftMaven"
        / "net"
        / "minecraft"
        / "minecraft-merged-deobf"
        / "26.2"
    )
    candidates = sorted(cache_root.glob("minecraft-merged-deobf-26.2*.jar"))
    if candidates:
        return candidates[-1]
    raise FileNotFoundError(
        "Minecraft 26.2 development JAR was not found. Run Gradle genSources first, "
        "or set EPIC_RELICS_MINECRAFT_JAR."
    )


MINECRAFT_JAR = find_minecraft_jar()


ITEM_SOURCES = {
    "dragon_sight_helmet": "dragon_sight_helmet-source-v1.png",
    "skywing_chestplate": "skywing_chestplate-source-v1.png",
    "voidwalker_leggings": "voidwalker_leggings-source-v1.png",
    "heavy_core_boots": "heavy_core_boots-source-v1.png",
    "gravity_blade": "gravity_blade-source-v1.png",
    "resonance_bow": "resonance_bow-source-v2-transparent.png",
    "resonance_bow_pulling_0": "resonance_bow_pulling_0-source-v1.png",
    "resonance_bow_pulling_1": "resonance_bow_pulling_1-source-v1.png",
    "resonance_bow_pulling_2": "resonance_bow_pulling_2-source-v1.png",
    "netherite_bow_blank": "netherite_bow_blank-source-v1.png",
}


def pixelize_source(source: Path, size: int = 32, margin: int = 1) -> Image.Image:
    image = Image.open(source).convert("RGBA")
    alpha = image.getchannel("A")
    bbox = alpha.point(lambda value: 255 if value >= 32 else 0).getbbox()
    if bbox is None:
        raise ValueError(f"No visible pixels in {source}")
    image = image.crop(bbox)
    max_inner = size - margin * 2
    scale = min(max_inner / image.width, max_inner / image.height)
    width = max(1, round(image.width * scale))
    height = max(1, round(image.height * scale))
    image = image.resize((width, height), Image.Resampling.NEAREST)
    image.putalpha(image.getchannel("A").point(lambda value: 255 if value >= 80 else 0))
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    canvas.alpha_composite(image, ((size - width) // 2, (size - height) // 2))
    return canvas


def read_jar_image(path: str) -> Image.Image:
    with ZipFile(MINECRAFT_JAR) as jar:
        with jar.open(path) as stream:
            return Image.open(stream).convert("RGBA")


def netherite_tint(image: Image.Image, accent: str) -> Image.Image:
    result = Image.new("RGBA", image.size)
    pixels = result.load()
    source = image.load()
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = source[x, y]
            if a == 0:
                continue
            light = int(0.25 * r + 0.55 * g + 0.20 * b)
            base = (
                min(255, int(light * 0.72 + 15)),
                min(255, int(light * 0.64 + 10)),
                min(255, int(light * 0.78 + 18)),
                a,
            )
            if accent == "relic" and (x * 7 + y * 11) % 41 == 0:
                base = (126, 31, 210, a)
            elif accent == "void" and ((x + y * 3) % 17 == 0 or (x * 5 + y) % 29 == 0):
                base = (174, 24, 235, a)
            pixels[x, y] = base
    return result


def paint_if_opaque(image: Image.Image, points: list[tuple[int, int]], color: tuple[int, int, int, int]) -> None:
    pixels = image.load()
    for x, y in points:
        if 0 <= x < image.width and 0 <= y < image.height and pixels[x, y][3] > 0:
            pixels[x, y] = color


def build_equipment_textures() -> dict[str, Image.Image]:
    humanoid = netherite_tint(Image.open(ROOT / "art-reference" / "netherite_armor_humanoid.png").convert("RGBA"), "relic")
    baby = netherite_tint(Image.open(ROOT / "art-reference" / "netherite_armor_baby.png").convert("RGBA"), "relic")
    leggings = netherite_tint(Image.open(ROOT / "art-reference" / "netherite_armor_leggings.png").convert("RGBA"), "void")

    paint_if_opaque(humanoid, [(9, 10), (10, 10), (13, 10), (14, 10)], (226, 60, 255, 255))
    paint_if_opaque(humanoid, [(22, 20), (23, 20), (22, 21), (23, 21)], (113, 45, 226, 255))
    paint_if_opaque(humanoid, [(4, 26), (5, 26), (12, 26), (13, 26)], (244, 128, 26, 255))

    wings = read_jar_image("assets/minecraft/textures/entity/equipment/wings/elytra.png")
    wing_result = Image.new("RGBA", wings.size)
    src = wings.load()
    dst = wing_result.load()
    for y in range(wings.height):
        for x in range(wings.width):
            r, g, b, a = src[x, y]
            if a == 0:
                continue
            light = int(0.25 * r + 0.55 * g + 0.20 * b)
            color = (max(18, int(light * 0.42)), max(15, int(light * 0.35)), max(25, int(light * 0.55)), a)
            if (x + y * 2) % 19 == 0:
                color = (40, 170, 220, a)
            elif (x * 3 + y) % 23 == 0:
                color = (115, 40, 210, a)
            dst[x, y] = color

    outputs = {
        "humanoid/relic_armor.png": humanoid,
        "humanoid_baby/relic_armor.png": baby,
        "humanoid_leggings/voidwalker_leggings.png": leggings,
        "wings/skywing.png": wing_result,
    }
    for relative, image in outputs.items():
        path = EQUIPMENT_DIR / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        image.save(path, optimize=True)
    return outputs


def build_item_textures() -> dict[str, Image.Image]:
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    outputs: dict[str, Image.Image] = {}
    for name, source_name in ITEM_SOURCES.items():
        image = pixelize_source(SOURCE_DIR / source_name)
        image.save(ITEM_DIR / f"{name}.png", optimize=True)
        outputs[name] = image
    return outputs


def build_preview(items: dict[str, Image.Image], equipment: dict[str, Image.Image]) -> Path:
    PREVIEW_DIR.mkdir(parents=True, exist_ok=True)
    scale = 6
    cell_w, cell_h = 220, 240
    columns = 4
    entries = list(items.items()) + [(name.replace("/", "_"), image) for name, image in equipment.items()]
    rows = (len(entries) + columns - 1) // columns
    preview = Image.new("RGBA", (columns * cell_w, rows * cell_h), (14, 12, 20, 255))
    draw = ImageDraw.Draw(preview)
    font = ImageFont.load_default()
    for index, (name, image) in enumerate(entries):
        col, row = index % columns, index // columns
        x, y = col * cell_w, row * cell_h
        draw.rounded_rectangle((x + 8, y + 8, x + cell_w - 8, y + cell_h - 8), radius=12,
                               fill=(27, 23, 38, 255), outline=(89, 56, 128, 255), width=2)
        fit_scale = max(1, min(scale, 190 // image.width, 180 // image.height))
        shown = image.resize((image.width * fit_scale, image.height * fit_scale), Image.Resampling.NEAREST)
        px = x + (cell_w - shown.width) // 2
        py = y + 18 + max(0, (190 - shown.height) // 2)
        preview.alpha_composite(shown, (px, py))
        label = name.replace("_", " ")
        box = draw.textbbox((0, 0), label, font=font)
        draw.text((x + (cell_w - (box[2] - box[0])) // 2, y + cell_h - 30), label,
                  font=font, fill=(226, 216, 242, 255))
    path = PREVIEW_DIR / "epic-relics-m5-final-textures-v2-20260829.png"
    preview.save(path, optimize=True)
    return path


def main() -> None:
    items = build_item_textures()
    equipment = build_equipment_textures()
    preview = build_preview(items, equipment)
    print(f"Generated {len(items)} item textures and {len(equipment)} equipment textures")
    print(preview)


if __name__ == "__main__":
    main()

