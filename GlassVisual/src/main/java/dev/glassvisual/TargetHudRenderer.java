package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public final class TargetHudRenderer {
    private static final int PANEL = 0xD910101B;
    private static final int MUTED = 0xFFAAA6BB;

    private TargetHudRenderer() {}

    public static void render(DrawContext ctx, MinecraftClient client, LivingEntity target) {
        int x = client.getWindow().getScaledWidth() / 2 + 12;
        int y = client.getWindow().getScaledHeight() / 2 + 30;
        int w = 166;
        int h = 58;
        int accent = NullTheme.accent();

        float health = Math.max(0, target.getHealth());
        float maxHealth = Math.max(1, target.getMaxHealth());
        float ratio = MathHelper.clamp(health / maxHealth, 0.0f, 1.0f);
        double distance = client.player.distanceTo(target);

        ctx.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x55434B64);
        ctx.fill(x, y, x + w, y + h, PANEL);
        ctx.fill(x, y, x + w, y + 2, accent);

        boolean skinDrawn = false;
        if (target instanceof PlayerEntity player && client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
            if (entry != null) {
                SkinTextures skin = entry.getSkinTextures();
                PlayerSkinDrawer.draw(ctx, skin, x + 8, y + 9, 32);
                skinDrawn = true;
            }
        }
        if (!skinDrawn) {
            ctx.fill(x + 8, y + 9, x + 40, y + 41, 0xFF292438);
            ctx.fill(x + 17, y + 14, x + 31, y + 28, 0xFFB8A0D5);
            ctx.fill(x + 13, y + 29, x + 35, y + 38, accent);
        }

        String name = target.getName().getString();
        if (name.length() > 16) name = name.substring(0, 16) + "…";
        ctx.drawText(client.textRenderer, Text.literal(name), x + 47, y + 9, 0xFFFFFFFF, true);
        ctx.drawText(client.textRenderer,
            Text.literal(String.format("%.1f HP  •  %.1fm", health, distance)),
            x + 47, y + 22, MUTED, false);

        int barX = x + 47;
        int barY = y + 39;
        int barW = 108;
        int filled = Math.round(barW * ratio);
        ctx.fill(barX, barY, barX + barW, barY + 7, 0xFF342B3E);
        if (filled > 0) {
            ctx.fill(barX, barY, barX + filled, barY + 7, accent);
            ctx.fill(barX, barY, barX + filled, barY + 1, 0x66FFFFFF);
        }
        ctx.drawText(client.textRenderer, Text.literal("NULL"), x + w - 38, y + 9, accent, false);
    }
}
