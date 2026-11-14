# Generator of placeholder 64x64 icons (drawn at 4x then downscaled for smooth edges).
# Replace the PNGs in src/main/resources/assets/knyui/textures/gui/ with your own art any time.
import math, os
from PIL import Image, ImageDraw

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src/main/resources/assets/knyui/textures/gui")
os.makedirs(OUT, exist_ok=True)
S = 256
W = (255, 255, 255, 255)

def canvas():
    im = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    return im, ImageDraw.Draw(im)

def save(im, name):
    im.resize((64, 64), Image.LANCZOS).save(os.path.join(OUT, name + ".png"))

def strength():
    im, d = canvas()
    d.rounded_rectangle((70, 100, 186, 200), 28, fill=W)
    for i in range(4):
        x = 70 + i * 29
        d.rounded_rectangle((x, 56 - (8 if i in (1, 2) else 0), x + 27, 118), 12, fill=W)
    d.rounded_rectangle((176, 120, 214, 170), 16, fill=W)
    d.rectangle((96, 196, 160, 232), fill=W)
    save(im, "icon_strength")

def agility():
    im, d = canvas()
    d.ellipse((140, 24, 182, 66), fill=W)
    d.line((150, 76, 118, 140), fill=W, width=22)
    d.line((150, 80, 196, 108), fill=W, width=16)
    d.line((140, 84, 100, 112), fill=W, width=16)
    d.line((118, 140, 164, 176), fill=W, width=18); d.line((164, 176, 150, 222), fill=W, width=16)
    d.line((118, 140, 82, 182), fill=W, width=18); d.line((82, 182, 44, 190), fill=W, width=16)
    for i, y in enumerate((70, 100, 130)):
        d.line((10, y, 54 - i * 8, y), fill=W, width=8)
    save(im, "icon_agility")

def endurance():
    im, d = canvas()
    pts = []
    for t in range(360):
        a = math.radians(t)
        x = 16 * math.sin(a) ** 3
        y = 13 * math.cos(a) - 5 * math.cos(2 * a) - 2 * math.cos(3 * a) - math.cos(4 * a)
        pts.append((128 + x * 6.4, 112 - y * 6.4))
    d.polygon(pts, fill=W)
    save(im, "icon_endurance")

def lock():
    im, d = canvas()
    d.arc((70, 24, 186, 150), 180, 360, fill=W, width=22)
    d.line((81, 90, 81, 118), fill=W, width=22); d.line((175, 90, 175, 118), fill=W, width=22)
    d.rounded_rectangle((44, 110, 212, 232), 22, fill=W)
    d.ellipse((112, 148, 144, 180), fill=(0, 0, 0, 255)); d.rectangle((122, 168, 134, 206), fill=(0, 0, 0, 255))
    save(im, "icon_lock")

def tint(color, name, shape):
    im, d = canvas()
    shape(d, color)
    save(im, name)

def water(d, c):
    d.polygon([(128, 24), (200, 140), (56, 140)], fill=c)
    d.ellipse((50, 100, 206, 232), fill=c)
    d.ellipse((92, 150, 120, 190), fill=(255, 255, 255, 120))

def flame(d, c):
    d.polygon([(128, 14), (196, 110), (212, 170), (128, 244), (44, 170), (62, 100), (100, 130)], fill=c)
    d.polygon([(128, 110), (166, 172), (128, 226), (92, 172)], fill=(255, 255, 255, 130))

def thunder(d, c):
    d.polygon([(150, 10), (60, 140), (118, 140), (92, 246), (200, 100), (140, 100)], fill=c)

def mark(d, c):
    d.ellipse((24, 24, 232, 232), fill=c)
    d.arc((60, 60, 196, 196), 200, 520, fill=(0, 0, 0, 200), width=16)
    d.arc((96, 96, 160, 160), 20, 330, fill=(0, 0, 0, 200), width=14)

def focus(d, c):
    d.arc((40, 40, 216, 216), 20, 300, fill=c, width=22)
    d.polygon([(200, 70), (236, 112), (176, 120)], fill=c)
    d.ellipse((104, 104, 152, 152), fill=c)

strength(); agility(); endurance(); lock()
tint((79, 195, 247, 255), "breath_water", water)
tint((255, 112, 67, 255), "breath_flame", flame)
tint((255, 238, 88, 255), "breath_thunder", thunder)
tint((229, 57, 53, 255), "passive_slayer_mark", mark)
tint((60, 140, 255, 255), "passive_constant_focus", focus)
print("ok")
