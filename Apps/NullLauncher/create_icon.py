from PIL import Image, ImageDraw, ImageFilter
import math

S = 256
im = Image.new("RGBA", (S, S), (5, 4, 16, 255))
px = im.load()
for y in range(S):
    for x in range(S):
        a = max(0.0, 1.0 - math.hypot(x - 128, y - 120) / 190)
        px[x, y] = (int(9 + 34*a), int(4 + 5*a), int(27 + 85*a), 255)

card = Image.new("RGBA", (S, S), (0,0,0,0))
d = ImageDraw.Draw(card)
d.rounded_rectangle((13,13,243,243), radius=48, fill=(8,5,25,245), outline=(174,91,255,255), width=5)
im = Image.alpha_composite(im, card.filter(ImageFilter.GaussianBlur(12)))
im = Image.alpha_composite(im, card)

logo = Image.new("RGBA", (S,S), (0,0,0,0))
d = ImageDraw.Draw(logo)
# Crown from the selected first NULL logo concept
d.polygon([(78,75),(91,43),(111,65),(128,35),(146,65),(166,43),(179,76),(170,91),(87,91)], fill=(210,174,255,255), outline=(255,239,255,255))
d.rounded_rectangle((88,94,168,102), radius=4, fill=(174,100,255,255))
# Bold angular N
d.polygon([(76,118),(105,118),(145,183),(145,118),(178,118),(178,211),(148,211),(108,148),(108,211),(76,211)], fill=(151,75,255,255), outline=(225,190,255,255))
d.line([(83,124),(102,124),(151,197)], fill=(238,218,255,230), width=4)
im = Image.alpha_composite(im, logo)
orbit = Image.new("RGBA", (S,S), (0,0,0,0))
ImageDraw.Draw(orbit).arc((25,85,231,200), 195, 340, fill=(205,139,255,220), width=5)
im = Image.alpha_composite(im, orbit.filter(ImageFilter.GaussianBlur(0.5)))
im.save("Apps/NullLauncher/null.ico", format="ICO", sizes=[(256,256),(128,128),(64,64),(48,48),(32,32),(16,16)])
