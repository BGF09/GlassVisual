package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.BlockPos;

public final class HudOverlayRenderer {
    private static final int ACCENT = 0xFFB56BFF;
    private static final int PANEL = 0xC910101A;
    private static final int TEXT = 0xFFF5F0FF;
    private static final int MUTED = 0xFFB9B1C8;

    private HudOverlayRenderer() {}

    public static void render(DrawContext ctx, MinecraftClient client) {
        int accent = GlassVisualClient.MODULES.glowAccent ? ACCENT : 0xFFB9B9C8;
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        if (GlassVisualClient.MODULES.fps) {
            String fps = client.getCurrentFps() + " FPS";
            int x = width - client.textRenderer.getWidth(fps) - 14;
            ctx.fill(x - 7, 10, width - 7, 29, PANEL);
            ctx.fill(x - 7, 10, x - 5, 29, accent);
            ctx.drawText(client.textRenderer, fps, x, 16, TEXT, true);
        }

        if (GlassVisualClient.MODULES.coordinates && client.player != null) {
            BlockPos pos = client.player.getBlockPos();
            String coords = "XYZ  " + pos.getX() + "  /  " + pos.getY() + "  /  " + pos.getZ();
            ctx.fill(10, height - 28, 18 + client.textRenderer.getWidth(coords), height - 9, PANEL);
            ctx.fill(10, height - 28, 12, height - 9, accent);
            ctx.drawText(client.textRenderer, coords, 17, height - 22, TEXT, false);
        }

        if (GlassVisualClient.MODULES.keystrokes && client.player != null) {
            int x = 12;
            int y = height - 88;
            drawKey(ctx, client, "W", client.options.forwardKey, x + 24, y, accent);
            drawKey(ctx, client, "A", client.options.leftKey, x, y + 24, accent);
            drawKey(ctx, client, "S", client.options.backKey, x + 24, y + 24, accent);
            drawKey(ctx, client, "D", client.options.rightKey, x + 48, y + 24, accent);
            drawKey(ctx, client, "JUMP", client.options.jumpKey, x, y + 48, accent, 72);
        }
    }

    private static void drawKey(DrawContext ctx, MinecraftClient client, String label,
                                KeyBinding key, int x, int y, int accent) {
        drawKey(ctx, client, label, key, x, y, accent, 20);
    }

    private static void drawKey(DrawContext ctx, MinecraftClient client, String label,
                                KeyBinding key, int x, int y, int accent, int w) {
        boolean pressed = key.isPressed();
        int background = pressed ? 0xE54B315F : PANEL;
        ctx.fill(x, y, x + w, y + 20, background);
        ctx.fill(x, y, x + w, y + 2, pressed ? accent : 0x665E526D);
        int color = pressed ? 0xFFFFFFFF : MUTED;
        int textX = x + (w - client.textRenderer.getWidth(label)) / 2;
        ctx.drawText(client.textRenderer, label, textX, y + 6, color, true);
    }
}
