package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.BlockPos;

public final class HudOverlayRenderer {
    private static final int PANEL = 0xD9101420;
    private static final int TEXT = 0xFFF3F5FC;
    private static final int MUTED = 0xFF9CA7BC;

    private HudOverlayRenderer() {}

    public static void render(DrawContext ctx, MinecraftClient client) {
        int accent = NullTheme.accent();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        int wx = clamp(HudLayout.watermarkX, 0, width - 114);
        int wy = clamp(HudLayout.watermarkY, 0, height - 26);
        ctx.fill(wx, wy, wx + 114, wy + 26, PANEL);
        ctx.fill(wx, wy, wx + 3, wy + 26, accent);
        ctx.fill(wx + 3, wy, wx + 114, wy + 2, 0x55FFFFFF);
        ctx.drawText(client.textRenderer, "NULL", wx + 11, wy + 5, TEXT, true);
        ctx.drawText(client.textRenderer, "CLIENT", wx + 47, wy + 6, MUTED, false);

        if (GlassVisualClient.MODULES.fps) {
            String fps = client.getCurrentFps() + " FPS";
            int x = HudLayout.fpsX < 0 ? width - client.textRenderer.getWidth(fps) - 24 : HudLayout.fpsX;
            x = clamp(x, 0, width - 80);
            int y = clamp(HudLayout.fpsY, 0, height - 26);
            ctx.fill(x, y, x + 80, y + 26, PANEL);
            ctx.fill(x, y, x + 3, y + 26, accent);
            ctx.drawText(client.textRenderer, fps, x + 10, y + 9, TEXT, true);
        }

        if (GlassVisualClient.MODULES.coordinates && client.player != null) {
            BlockPos pos = client.player.getBlockPos();
            String coords = "XYZ  " + pos.getX() + "  /  " + pos.getY() + "  /  " + pos.getZ();
            int x = clamp(HudLayout.coordinatesX, 0, width - 154);
            int y = HudLayout.coordinatesY < 0 ? height - 34 : HudLayout.coordinatesY;
            y = clamp(y, 0, height - 24);
            ctx.fill(x, y, x + 154, y + 24, PANEL);
            ctx.fill(x, y, x + 3, y + 24, accent);
            ctx.drawText(client.textRenderer, coords, x + 11, y + 8, TEXT, false);
        }

        if (GlassVisualClient.MODULES.keystrokes && client.player != null) {
            int x = clamp(HudLayout.keysX, 0, width - 92);
            int y = HudLayout.keysY < 0 ? height - 112 : HudLayout.keysY;
            y = clamp(y, 0, height - 76);
            drawKey(ctx, client, "W", client.options.forwardKey, x + 24, y, accent);
            drawKey(ctx, client, "A", client.options.leftKey, x, y + 24, accent);
            drawKey(ctx, client, "S", client.options.backKey, x + 24, y + 24, accent);
            drawKey(ctx, client, "D", client.options.rightKey, x + 48, y + 24, accent);
            drawKey(ctx, client, "JUMP", client.options.jumpKey, x, y + 48, accent, 72);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.max(0, Math.min(value, max)));
    }

    private static void drawKey(DrawContext ctx, MinecraftClient client, String label,
                                KeyBinding key, int x, int y, int accent) {
        drawKey(ctx, client, label, key, x, y, accent, 20);
    }

    private static void drawKey(DrawContext ctx, MinecraftClient client, String label,
                                KeyBinding key, int x, int y, int accent, int width) {
        boolean pressed = key.isPressed();
        ctx.fill(x, y, x + width, y + 20, pressed ? 0xE53A4058 : PANEL);
        ctx.fill(x, y, x + width, y + 2, pressed ? accent : 0x665E687F);
        int textColor = pressed ? 0xFFFFFFFF : MUTED;
        int textX = x + (width - client.textRenderer.getWidth(label)) / 2;
        ctx.drawText(client.textRenderer, label, textX, y + 6, textColor, true);
    }
}
