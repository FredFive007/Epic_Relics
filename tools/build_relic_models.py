"""Generate native 26.3 cuboid models. Reuses existing PNGs without modifying them.

Run with Python 3. All geometry, UV swatches and display transforms live here so
the resting/pulling frames and the two synchronized bow modes stay consistent.
"""
from __future__ import annotations

import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/epic_relics"
MODELS = ASSETS / "models/item"
PALETTE = {
    "dark": ("armor", 8, 2, 64, 32),
    "metal": ("armor", 11, 1, 64, 32),
    "edge": ("armor", 11, 0, 64, 32),
    "violet": ("armor", 9, 10, 64, 32),
    "cyan": ("bow", 15, 9, 32, 32),
    "white": ("blade", 13, 18, 32, 32),
    "amber": ("armor", 4, 26, 64, 32),
}
TEXTURES = {
    # 26.3 separates block and item atlases. The alias below is explicitly added
    # to the item atlas from the unmodified armor PNG by atlases/items.json.
    "armor": "epic_relics:item/relic_palette",
    "blade": "epic_relics:item/gravity_blade",
    "bow": "epic_relics:item/resonance_bow",
}


def cube(name, low, high, material="metal", light=0, rotation=None):
    texture, px, py, width, height = PALETTE[material]
    uv = [round((px + .25) * 16 / width, 5), round((py + .25) * 16 / height, 5),
          round((px + .75) * 16 / width, 5), round((py + .75) * 16 / height, 5)]
    result = {"name": name, "from": list(low), "to": list(high),
              "faces": {face: {"uv": uv, "texture": "#" + texture}
                        for face in ["north", "south", "east", "west", "up", "down"]}}
    if light:
        result["light_emission"] = light
    if rotation:
        result["rotation"] = rotation
    return result


def beam(name, start, end, width, depth, material, light=0):
    """A cuboid aligned with an arbitrary 26.3 Euler-Z rotation in the XY plane."""
    cx, cy = [(a + b) / 2 for a, b in zip(start, end)]
    length = math.dist(start, end)
    angle = math.degrees(math.atan2(end[1] - start[1], end[0] - start[0])) - 90
    return cube(name, [cx - width / 2, cy - length / 2, 8 - depth / 2],
                [cx + width / 2, cy + length / 2, 8 + depth / 2], material, light,
                {"origin": [cx, cy, 8], "z": round(angle, 4)})


def transforms(bow=False):
    display = {
        "gui": {"rotation": [0, -18, -35], "translation": [0, 0, 0], "scale": [.83, .83, .83]},
        "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [.5, .5, .5]},
        "fixed": {"rotation": [0, 180, 0], "scale": [.75, .75, .75]},
        "thirdperson_righthand": {"rotation": [0, -90, -45], "translation": [0, 3.3, 3.3], "scale": [.95, .95, .95]},
        "thirdperson_lefthand": {"rotation": [0, 90, 45], "translation": [0, 3.3, 3.3], "scale": [.95, .95, .95]},
        "firstperson_righthand": {"rotation": [0, -90, 10], "translation": [-1.1, 4.2, .7], "scale": [.72, .72, .72]},
        "firstperson_lefthand": {"rotation": [0, 90, -10], "translation": [-1.1, 4.2, .7], "scale": [.72, .72, .72]},
    }
    if bow:
        display.update({
            "gui": {"rotation": [0, -20, -25], "translation": [.3, 0, 0], "scale": [.85, .85, .85]},
            "thirdperson_righthand": {"rotation": [-80, 260, -40], "translation": [-1, -2, 2.5], "scale": [.9, .9, .9]},
            "thirdperson_lefthand": {"rotation": [-80, -280, 40], "translation": [-1, -2, 2.5], "scale": [.9, .9, .9]},
            "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [.85, .85, .85]},
            "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [.85, .85, .85]},
        })
    return display


def model(elements, particle, bow=False):
    return {"credit": "Epic Relics native cuboid geometry v0.3", "ambientocclusion": False,
            "textures": dict(TEXTURES, particle=particle), "display": transforms(bow), "elements": elements}


def blade():
    parts = [
        cube("wrapped_grip", [7.45, 1.1, 7.35], [8.55, 5, 8.65], "dark"),
        cube("pommel", [7.05, .45, 7.0], [8.95, 1.5, 9.0], "metal"),
        cube("pommel_core", [7.5, .15, 7.5], [8.5, .8, 8.5], "violet", 8),
        cube("guard", [5.0, 4.5, 7.0], [11.0, 5.3, 9.0], "dark"),
        cube("guard_lip", [5.1, 5.1, 6.9], [10.9, 5.45, 9.1], "edge"),
        cube("gravity_core", [7.25, 4.7, 6.65], [8.75, 6.2, 9.35], "violet", 10),
        cube("blade_body", [6.3, 5.65, 7.5], [9.7, 14.2, 8.5], "metal"),
        cube("blade_spine", [7.6, 6.0, 7.4], [8.4, 14.2, 8.6], "dark"),
        cube("left_edge", [5.95, 6.25, 7.76], [6.31, 14.2, 8.24], "edge"),
        cube("right_edge", [9.69, 6.25, 7.76], [10.05, 14.2, 8.24], "edge"),
        # The upper half of a rotated square is a true pointed silhouette.
        # The lower half sits inside the blade body; a thinner bright outer
        # diamond forms the cutting bevel rather than a white square end cap.
        cube("point_bevel", [6.55, 12.75, 7.78], [9.45, 15.65, 8.22], "edge",
             rotation={"origin": [8, 14.2, 8], "z": 45}),
        cube("point_face", [6.79, 12.99, 7.52], [9.21, 15.41, 8.48], "metal",
             rotation={"origin": [8, 14.2, 8], "z": 45}),
    ]
    for i in range(4):
        parts.append(cube("grip_band_" + str(i), [7.36, 1.65 + i * .8, 7.26], [8.64, 1.88 + i * .8, 8.74], "edge"))
    for side, x in [("left", 5.15), ("right", 10.85)]:
        parts.append(beam(side + "_guard_talon", [x, 4.85], [x + (-.65 if side == "left" else .65), 6.2], .65, 1.4, "metal"))
    for i in range(6):
        for face, z in [("front", 7.32), ("back", 8.6)]:
            parts.append(cube(face + "_rune_" + str(i), [7.72, 6.55 + i * 1.25, z], [8.28, 7.4 + i * 1.25, z + .08], "violet", 12))
    return model(parts, TEXTURES["blade"])


def bow(frame, sonic):
    accent = "cyan" if sonic else "violet"
    tension = max(0, frame + 1) / 3
    tip_x = 4.0 + tension * 1.1
    tip_y = 7.3 - tension * .75
    nock_x = 4.0 - tension * 3.1
    parts = [cube("grip", [8.6, 6.5, 7.25], [9.7, 9.5, 8.75], "dark"),
             cube("grip_ridge", [9.55, 6.7, 7.35], [10.0, 9.3, 8.65], "edge")]
    for side in [-1, 1]:
        points = [[9.1, 8 + side * 1.6], [8.9, 8 + side * 3.2],
                  [7.4 + tension * .45, 8 + side * 5.25], [tip_x, 8 + side * tip_y]]
        for i, (start, end) in enumerate(zip(points, points[1:])):
            parts.append(beam(f"limb_{side}_{i}", start, end, 1.05 - i * .12, 1.55 - i * .15, "metal"))
            # A raised colored rail on both sides remains visible from either hand.
            rail = beam(f"channel_{side}_{i}", start, end, .25, 1.73 - i * .15, accent, 9 + frame)
            parts.append(rail)
        end = points[-1]
        parts.append(cube(f"tip_socket_{side}", [end[0]-.5, end[1]-.45, 7.2], [end[0]+.5, end[1]+.45, 8.8], "dark"))
        parts.append(beam(f"string_{side}", end, [nock_x, 8], .10, .12, accent, 12))
        parts.append(cube(f"resonator_{side}", [8.15, 8+side*2.1-.45, 6.7], [9.95, 8+side*2.1+.45, 9.3], "dark"))
        parts.append(cube(f"resonator_core_{side}", [8.55, 8+side*2.1-.25, 6.6], [9.55, 8+side*2.1+.25, 9.4], accent, 12))
    for i in range(3):
        parts.append(cube(f"grip_wrap_{i}", [8.52, 6.8+i*.9, 7.18], [9.78, 7.0+i*.9, 8.82], "edge"))
    if frame >= 0:
        parts.append(beam("drawn_arrow", [nock_x, 8], [12.2, 8], .17, .17, "edge"))
        parts.append(cube("arrowhead", [11.8, 7.64, 7.64], [12.6, 8.36, 8.36], accent, 8))
        for z in [7.6, 8.25]:
            parts.append(cube("fletching_"+str(z), [nock_x+.3, 7.55, z], [nock_x+1.2, 8.45, z+.15], "white"))
    return model(parts, TEXTURES["bow"], True)


def item_ref(name):
    return {"type": "minecraft:model", "model": "epic_relics:item/" + name}


def bow_dispatch(prefix):
    return {"type": "minecraft:condition", "property": "minecraft:using_item",
            "on_false": item_ref(prefix), "on_true": {
                "type": "minecraft:range_dispatch", "property": "minecraft:use_duration", "scale": .05,
                "fallback": item_ref(prefix + "_pulling_0"), "entries": [
                    {"threshold": .65, "model": item_ref(prefix + "_pulling_1")},
                    {"threshold": .9, "model": item_ref(prefix + "_pulling_2")}]}}


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def main():
    write(ASSETS.parent / "minecraft/atlases/items.json", {"sources": [{
        "type": "minecraft:single",
        "resource": "epic_relics:entity/equipment/humanoid/relic_armor",
        "sprite": "epic_relics:item/relic_palette",
    }]})
    write(MODELS / "gravity_blade.json", blade())
    for sonic in [False, True]:
        prefix = "resonance_bow_sonic" if sonic else "resonance_bow"
        for frame in range(-1, 3):
            name = prefix + ("_pulling_" + str(frame) if frame >= 0 else "")
            write(MODELS / (name + ".json"), bow(frame, sonic))
    write(ASSETS / "items/resonance_bow.json", {"model": {
        "type": "minecraft:condition", "property": "epic_relics:sonic_mode",
        "on_true": bow_dispatch("resonance_bow_sonic"), "on_false": bow_dispatch("resonance_bow")}})
    print("Generated 9 native cuboid weapon models and synchronized bow mode dispatch; no PNGs changed.")


if __name__ == "__main__":
    main()
