package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** In-game drag editor for HUD groups. */
public final class HudEditorScreen extends Screen {
    private static final int PANEL = 0xD91A1D29;
    private static final int OUTLINE = 0xFFB69AFF;
    private String dragging;
    private int dragOffsetX, dragOffsetY;

    public HudEditorScreen() { super(Text.literal("Glass Visual HUD Editor")); }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x66000000);
        drawElement(ctx, "WATERMARK", HudLayout.watermarkX, HudLayout.watermarkY, 114, 26);
        drawElement(ctx, "FPS", HudLayout.fpsX < 0 ? width - 92 : HudLayout.fpsX, HudLayout.fpsY, 80, 26);
        drawElement(ctx, "COORDINATES", HudLayout.coordinatesX, HudLayout.coordinatesY < 0 ? height - 34 : HudLayout.coordinatesY, 154, 26);
        drawElement(ctx, "KEYSTROKES", HudLayout.keysX, HudLayout.keysY < 0 ? height - 112 : HudLayout.keysY, 92, 76);
        drawElement(ctx, "ARRAYLIST", HudLayout.arrayListX < 0 ? width - 112 : HudLayout.arrayListX, HudLayout.arrayListY, 104, 36);
        drawElement(ctx, "ARMOR", HudLayout.armorX, HudLayout.armorY < 0 ? height - 34 : HudLayout.armorY, 104, 30);
        drawElement(ctx, "POTIONS", HudLayout.potionsX < 0 ? width - 154 : HudLayout.potionsX, HudLayout.potionsY, 146, 54);
        drawElement(ctx, "PING", HudLayout.pingX < 0 ? width - 92 : HudLayout.pingX, HudLayout.pingY, 80, 24);
        ctx.drawCenteredTextWithShadow(textRenderer, "HUD EDITOR  •  DRAG ELEMENTS  •  ESC TO SAVE",
            width / 2, 12, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "Right Alt opens this editor",
            width / 2, 26, 0xFFB9B1C8);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawElement(DrawContext ctx, String label, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, PANEL);
        ctx.fill(x, y, x + w, y + 2, OUTLINE);
        ctx.fill(x, y, x + 2, y + h, OUTLINE);
        ctx.fill(x + w - 2, y, x + w, y + h, OUTLINE);
        ctx.fill(x, y + h - 2, x + w, y + h, OUTLINE);
        ctx.drawText(textRenderer, label, x + 7, y + (h - 9) / 2, 0xFFFFFFFF, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            String[] ids = {"watermark","fps","coordinates","keys","arrayList","armor","potions","ping"};
            for (String id : ids) {
                int[] p = position(id);
                int[] s = size(id);
                if (inside(mouseX, mouseY, p[0], p[1], s[0], s[1])) {
                    dragging = id;
                    dragOffsetX = (int) mouseX - p[0];
                    dragOffsetY = (int) mouseY - p[1];
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && dragging != null) {
            int[] s = size(dragging);
            int x = clamp((int) mouseX - dragOffsetX, 0, Math.max(0, width - s[0]));
            int y = clamp((int) mouseY - dragOffsetY, 0, Math.max(0, height - s[1]));
            switch (dragging) {
                case "watermark" -> { HudLayout.watermarkX=x; HudLayout.watermarkY=y; }
                case "fps" -> { HudLayout.fpsX=x; HudLayout.fpsY=y; }
                case "coordinates" -> { HudLayout.coordinatesX=x; HudLayout.coordinatesY=y; }
                case "keys" -> { HudLayout.keysX=x; HudLayout.keysY=y; }
                case "arrayList" -> { HudLayout.arrayListX=x; HudLayout.arrayListY=y; }
                case "armor" -> { HudLayout.armorX=x; HudLayout.armorY=y; }
                case "potions" -> { HudLayout.potionsX=x; HudLayout.potionsY=y; }
                case "ping" -> { HudLayout.pingX=x; HudLayout.pingY=y; }
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging != null) {
            dragging = null;
            GlassVisualConfig.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private int[] position(String id) {
        return switch (id) {
            case "watermark" -> new int[]{HudLayout.watermarkX,HudLayout.watermarkY};
            case "fps" -> new int[]{HudLayout.fpsX<0?width-92:HudLayout.fpsX,HudLayout.fpsY};
            case "coordinates" -> new int[]{HudLayout.coordinatesX,HudLayout.coordinatesY<0?height-34:HudLayout.coordinatesY};
            case "keys" -> new int[]{HudLayout.keysX,HudLayout.keysY<0?height-112:HudLayout.keysY};
            case "arrayList" -> new int[]{HudLayout.arrayListX<0?width-112:HudLayout.arrayListX,HudLayout.arrayListY};
            case "armor" -> new int[]{HudLayout.armorX,HudLayout.armorY<0?height-34:HudLayout.armorY};
            case "potions" -> new int[]{HudLayout.potionsX<0?width-154:HudLayout.potionsX,HudLayout.potionsY};
            default -> new int[]{HudLayout.pingX<0?width-92:HudLayout.pingX,HudLayout.pingY};
        };
    }

    private int[] size(String id) {
        return switch (id) {
            case "watermark" -> new int[]{114,26};
            case "fps","ping" -> new int[]{80,26};
            case "coordinates" -> new int[]{154,26};
            case "keys" -> new int[]{92,76};
            case "arrayList" -> new int[]{104,36};
            case "armor" -> new int[]{104,30};
            default -> new int[]{146,54};
        };
    }

    private static boolean inside(double mx,double my,int x,int y,int w,int h) {
        return mx>=x&&mx<=x+w&&my>=y&&my<=y+h;
    }
    private static int clamp(int value,int min,int max) { return Math.max(min,Math.min(max,value)); }
    @Override public void close() { GlassVisualConfig.save(); super.close(); }
    @Override public boolean shouldPause() { return false; }
}
