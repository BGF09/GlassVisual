package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** In-game drag editor for the four current HUD groups. */
public final class HudEditorScreen extends Screen {
    private static final int PANEL = 0xD91A1D29;
    private static final int OUTLINE = 0xFFB69AFF;
    private String dragging;
    private int dragOffsetX, dragOffsetY;

    public HudEditorScreen() {
        super(Text.literal("Glass Visual HUD Editor"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x66000000);
        drawElement(ctx, "WATERMARK", HudLayout.watermarkX, HudLayout.watermarkY, 114, 26);
        int fpsX = HudLayout.fpsX < 0 ? width - 92 : HudLayout.fpsX;
        drawElement(ctx, "FPS", fpsX, HudLayout.fpsY, 80, 26);
        int coordsY = HudLayout.coordinatesY < 0 ? height - 34 : HudLayout.coordinatesY;
        drawElement(ctx, "COORDINATES", HudLayout.coordinatesX, coordsY, 154, 26);
        int keysY = HudLayout.keysY < 0 ? height - 112 : HudLayout.keysY;
        drawElement(ctx, "KEYSTROKES", HudLayout.keysX, keysY, 92, 76);
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
            if (inside(mouseX, mouseY, HudLayout.watermarkX, HudLayout.watermarkY, 114, 26)) dragging = "watermark";
            else if (inside(mouseX, mouseY, HudLayout.fpsX < 0 ? width - 92 : HudLayout.fpsX, HudLayout.fpsY, 80, 26)) dragging = "fps";
            else if (inside(mouseX, mouseY, HudLayout.coordinatesX, HudLayout.coordinatesY < 0 ? height - 34 : HudLayout.coordinatesY, 154, 26)) dragging = "coordinates";
            else if (inside(mouseX, mouseY, HudLayout.keysX, HudLayout.keysY < 0 ? height - 112 : HudLayout.keysY, 92, 76)) dragging = "keys";
            if (dragging != null) {
                int[] pos = position(dragging);
                dragOffsetX = (int) mouseX - pos[0];
                dragOffsetY = (int) mouseY - pos[1];
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && dragging != null) {
            int x = clamp((int) mouseX - dragOffsetX, 0, width - 40);
            int y = clamp((int) mouseY - dragOffsetY, 0, height - 24);
            switch (dragging) {
                case "watermark" -> { HudLayout.watermarkX = x; HudLayout.watermarkY = y; }
                case "fps" -> { HudLayout.fpsX = x; HudLayout.fpsY = y; }
                case "coordinates" -> { HudLayout.coordinatesX = x; HudLayout.coordinatesY = y; }
                case "keys" -> { HudLayout.keysX = x; HudLayout.keysY = y; }
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
            case "watermark" -> new int[]{HudLayout.watermarkX, HudLayout.watermarkY};
            case "fps" -> new int[]{HudLayout.fpsX < 0 ? width - 92 : HudLayout.fpsX, HudLayout.fpsY};
            case "coordinates" -> new int[]{HudLayout.coordinatesX, HudLayout.coordinatesY < 0 ? height - 34 : HudLayout.coordinatesY};
            default -> new int[]{HudLayout.keysX, HudLayout.keysY < 0 ? height - 112 : HudLayout.keysY};
        };
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public void close() {
        GlassVisualConfig.save();
        super.close();
    }

    @Override
    public boolean shouldPause() { return false; }
}
