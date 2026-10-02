#!/usr/bin/env python3
"""Render the actual exported Java mesh offline. Developer tool; requires Pillow.

Usage: ./gradlew exportPreview
       python tools/render_preview.py build/preview/poses.json
"""
import json
import math
from pathlib import Path
import sys
from PIL import Image, ImageDraw, ImageFont


def font(size):
    for name in ("DejaVuSans.ttf", "arial.ttf"):
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            pass
    return ImageFont.load_default()


def main():
    poses = json.loads(Path(sys.argv[1]).read_text())
    colors = [(230, 62, 65), (246, 201, 52), (52, 174, 229)]
    frames = []
    for fi in range(len(poses)):
        im = Image.new("RGB", (1020, 540), (19, 27, 38))
        draw = ImageDraw.Draw(im)
        draw.text((40, 24), "WACKY SIRE", font=font(26), fill=(244, 246, 250))
        draw.text((40, 64), "Procedural mesh preview • actual game rendering still needs testing",
                  font=font(15), fill=(170, 186, 203))
        for mi in range(3):
            triangles = poses[(fi + mi * len(poses) // 3) % len(poses)]
            ox, oy = 170 + mi * 340, 452
            yaw, pitch = 0.28, 0.1
            projected = []
            for tri in triangles:
                points, depth = [], 0
                for vi in range(3):
                    x, y, z = tri[1 + vi * 3:4 + vi * 3]
                    xx = x * math.cos(yaw) + z * math.sin(yaw)
                    zz = -x * math.sin(yaw) + z * math.cos(yaw)
                    yy = y * math.cos(pitch) - zz * math.sin(pitch)
                    depth += y * math.sin(pitch) + zz * math.cos(pitch)
                    points.append((ox + xx * 0.88, oy + yy * 0.88))
                area = ((points[1][0] - points[0][0]) * (points[2][1] - points[0][1])
                        - (points[1][1] - points[0][1]) * (points[2][0] - points[0][0]))
                if area < 0:
                    continue  # Reverse windings are already supplied by the mesh.
                a, b, c = tri[1:4], tri[4:7], tri[7:10]
                v, q = [b[i] - a[i] for i in range(3)], [c[i] - a[i] for i in range(3)]
                normal = [v[1] * q[2] - v[2] * q[1], v[2] * q[0] - v[0] * q[2],
                          v[0] * q[1] - v[1] * q[0]]
                length = math.sqrt(sum(n * n for n in normal))
                shade = 0.62 + 0.38 * abs(sum(normal[i] * [-0.4, -0.5, -0.7][i]
                                             for i in range(3)) / length)
                rgb = [colors[mi], (246, 245, 231), (18, 24, 30), (59, 67, 77)][tri[0]]
                projected.append((depth, points, tuple(min(255, int(ch * shade)) for ch in rgb)))
            draw.ellipse((ox - 51, oy - 8, ox + 51, oy + 14), fill=(9, 17, 28))
            for _, points, rgb in sorted(projected, reverse=True):
                draw.polygon(points, fill=rgb)
        draw.text((40, 501), "Original artwork • six colors • independent looping flails",
                  font=font(15), fill=(170, 186, 203))
        frames.append(im)
    root = Path(__file__).resolve().parents[1]
    docs = root / "docs"
    docs.mkdir(exist_ok=True)
    frames[0].save(docs / "preview.png")
    frames[0].save(docs / "preview.gif", save_all=True, append_images=frames[1:],
                   duration=50, loop=0, optimize=True)
    icon = frames[0].crop((118, 112, 222, 460))
    icon.thumbnail((46, 70))
    out = Image.new("RGBA", (48, 72), (19, 27, 38, 255))
    out.paste(icon, ((48 - icon.width) // 2, (72 - icon.height) // 2))
    out.save(root / "icon.png")
    print("Generated preview.png, preview.gif and icon.png")


if __name__ == "__main__":
    main()
