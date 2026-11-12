package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public final class TargetHudRenderer {
    private static final int PURPLE = 0xFFB34CFF;
    private static final int PANEL = 0xD910101B;
    private static final int MUTED = 0xFFAAA6BB;

    private TargetHudRenderer() {}

    public static void render(DrawContext ctx, MinecraftClient client, LivingEntity target) {
        int x = client.getWindow().getScaledWidth() / 2 + 12;
        int y = client.getWindow().getScaledHeight() / 2 + 30;
        int w = 166;
        int h = 58;

        float health = Math.max(0, target.getHealth());
        float maxHealth = Math.max(1, target.getMaxHealth());
        float ratio = MathHelper.clamp(health / maxHealth, 0.0f, 1.0f);
        double distance = client.player.distanceTo(target);

        // Glass panel and subtle purple edge.
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x66B34CFF);
        ctx.fill(x, y, x + w, y + h, PANEL);
        ctx.fill(x + 1, y + 1, x + w - 1, y + 2, 0x55FFFFFF);

        // Four neon corner brackets, matching the reference screenshot.
        int c = GlassVisualClient.MODULES.glowAccent ? PURPLE : 0xFFB9B9C8;
        int t = 3;
        ctx.fill(x - 3, y + 7, x, y + 23, c);
        ctx.fill(x - 3, y + 7, x + 14, y + 10, c);
        ctx.fill(x + w, y + 7, x + w + 3, y + 23, c);
        ctx.fill(x + w - 14, y + 7, x + w + 3, y + 10, c);
        ctx.fill(x - 3, y + h - 23, x, y + h - 7, c);
        ctx.fill(x - 3, y + h - 10, x + 14, y + h - 7, c);
        ctx.fill(x + w, y + h - 23, x + w + 3, y + h - 7, c);
        ctx.fill(x + w - 14, y + h - 10, x + w + 3, y + h - 7, c);

        // Portrait placeholder: stylized avatar tile. Skin rendering can be added
        // later using the target's skin texture / player model renderer.
        ctx.fill(x + 8, y + 9, x + 40, y + 41, 0xFF292438);
        ctx.fill(x + 17, y + 14, x + 31, y + 28, 0xFFB8A0D5);
        ctx.fill(x + 13, y + 29, x + 35, y + 38, 0xFF6C4A9B);

        String name = target.getName().getString();
        if (name.length() > 16) name = name.substring(0, 16) + "…";
        ctx.drawText(client.textRenderer, Text.literal(name), x + 47, y + 9, 0xFFFFFFFF, true);
        ctx.drawText(client.textRenderer,
                Text.literal(String.format("%.1f HP  •  %.1fm", health, distance)),
                x + 47, y + 22, MUTED, false);

        int barX = x + 47;
        int barY = y + 39;
        int barW = 108;
        ctx.fill(barX, barY, barX + barW, barY + 7, 0xFF342B3E);
        int fill = Math.round(barW * ratio);
        int healthColor = ratio > 0.5f ? 0xFFB34CFF : (ratio > 0.25f ? 0xFFFFB347 : 0xFFFF526B);
        if (fill > 0) ctx.fill(barX, barY, barX + fill, barY + 7, healthColor);
        ctx.fill(barX, barY, barX + fill, barY + 1, 0x66FFFFFF);

        ctx.drawText(client.textRenderer,
                Text.literal("TARGET"),
                x + w - 42, y + 9, 0xFFCEB5FF, false);
    }
}
