package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

/** Small client-side hit marker triggered when the focused living target loses health. */
public final class NullHitVisuals {
    private static float previousHealth = -1.0f;
    private static int ticksLeft;
    private NullHitVisuals() {}

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null
                || !(client.crosshairTarget instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof LivingEntity target) || !target.isAlive()) {
            previousHealth = -1.0f;
            return;
        }
        float health = target.getHealth();
        if (previousHealth >= 0.0f && health < previousHealth - 0.01f) ticksLeft = 9;
        previousHealth = health;
        if (ticksLeft > 0) ticksLeft--;
    }

    public static void render(DrawContext ctx, MinecraftClient client) {
        if (ticksLeft <= 0 || client.player == null) return;
        int cx = client.getWindow().getScaledWidth() / 2;
        int cy = client.getWindow().getScaledHeight() / 2;
        int alpha = Math.min(255, ticksLeft * 28);
        int color = (alpha << 24) | (NullTheme.accent() & 0x00FFFFFF);
        int shadow = (alpha << 24) | 0x00121A2A;
        for (int i = 0; i < 4; i++) {
            int dx = (i % 2 == 0) ? -1 : 1;
            int dy = (i < 2) ? -1 : 1;
            int x = cx + dx * 8;
            int y = cy + dy * 8;
            ctx.fill(x - 1, y - 1, x + 2, y + 2, shadow);
            ctx.fill(x, y, x + 1, y + 1, color);
            ctx.fill(cx + dx * 4, cy + dy * 4, cx + dx * 7, cy + dy * 5, color);
        }
    }
}
