package dev.glassvisual;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public final class HudOverlayRenderer {
    private static final int PANEL = 0xDDF7FAFF;
    private static final int TEXT = 0xFF202A3B;
    private static final int MUTED = 0xFF758198;
    private static final int SOFT = 0xFF69758B;

    private HudOverlayRenderer() {}

    public static void render(DrawContext ctx, MinecraftClient client) {
        int accent = NullTheme.accent();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        renderWatermark(ctx, client, accent, width, height);

        if (GlassVisualClient.MODULES.fps) {
            String fps = client.getCurrentFps() + " FPS";
            int x = HudLayout.fpsX < 0 ? width - 92 : HudLayout.fpsX;
            int y = HudLayout.fpsY;
            panel(ctx, x, y, 80, 26, accent);
            ctx.drawText(client.textRenderer, fps, x + 10, y + 9, TEXT, true);
        }

        if (GlassVisualClient.MODULES.ping && client.player != null && client.getNetworkHandler() != null) {
            var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) {
                String value = entry.getLatency() + " ms";
                int x = HudLayout.pingX < 0 ? width - 92 : HudLayout.pingX;
                panel(ctx, x, HudLayout.pingY, 80, 24, accent);
                ctx.drawText(client.textRenderer, "PING", x + 8, HudLayout.pingY + 7, MUTED, false);
                ctx.drawText(client.textRenderer, value, x + 43, HudLayout.pingY + 7, TEXT, true);
            }
        }

        if (GlassVisualClient.MODULES.coordinates && client.player != null) {
            BlockPos pos = client.player.getBlockPos();
            String coords = "XYZ  " + pos.getX() + "  /  " + pos.getY() + "  /  " + pos.getZ();
            int x = HudLayout.coordinatesX;
            int y = HudLayout.coordinatesY < 0 ? height - 34 : HudLayout.coordinatesY;
            panel(ctx, x, y, 154, 24, accent);
            ctx.drawText(client.textRenderer, coords, x + 11, y + 8, TEXT, false);
        }

        if (GlassVisualClient.MODULES.keystrokes && client.player != null) {
            int x = HudLayout.keysX;
            int y = HudLayout.keysY < 0 ? height - 112 : HudLayout.keysY;
            drawKey(ctx, client, "W", client.options.forwardKey, x + 24, y, accent, 20);
            drawKey(ctx, client, "A", client.options.leftKey, x, y + 24, accent, 20);
            drawKey(ctx, client, "S", client.options.backKey, x + 24, y + 24, accent, 20);
            drawKey(ctx, client, "D", client.options.rightKey, x + 48, y + 24, accent, 20);
            drawKey(ctx, client, "JUMP", client.options.jumpKey, x, y + 48, accent, 72);
        }

        if (GlassVisualClient.MODULES.armorHud && client.player != null) {
            renderArmor(ctx, client, accent, width, height);
        }
        if (GlassVisualClient.MODULES.potionHud && client.player != null) {
            renderPotions(ctx, client, accent, width);
        }
        if (GlassVisualClient.MODULES.arrayList) {
            renderArrayList(ctx, client, accent, width);
        }
    }

    private static void renderWatermark(DrawContext ctx, MinecraftClient client, int accent, int width, int height) {
        int x = HudLayout.watermarkX;
        int y = HudLayout.watermarkY;
        panel(ctx, x, y, 114, 26, accent);
        ctx.drawText(client.textRenderer, "NULL", x + 11, y + 5, TEXT, true);
        ctx.drawText(client.textRenderer, "CLIENT", x + 47, y + 6, MUTED, false);
    }

    private static void renderArrayList(DrawContext ctx, MinecraftClient client, int accent, int screenWidth) {
        String[] names = {"Target HUD", "Keystrokes", "FPS", "Coordinates", "Armor HUD", "Potion HUD", "Ping"};
        boolean[] active = {
            GlassVisualClient.MODULES.targetHud, GlassVisualClient.MODULES.keystrokes,
            GlassVisualClient.MODULES.fps, GlassVisualClient.MODULES.coordinates,
            GlassVisualClient.MODULES.armorHud, GlassVisualClient.MODULES.potionHud,
            GlassVisualClient.MODULES.ping
        };
        int x = HudLayout.arrayListX;
        int y = HudLayout.arrayListY;
        for (int i = 0; i < names.length; i++) {
            if (!active[i]) continue;
            int w = client.textRenderer.getWidth(names[i]) + 20;
            int left = x < 0 ? screenWidth - w - 8 : x;
            ctx.fill(left, y, left + w, y + 17, PANEL);
            ctx.fill(left + w - 2, y, left + w, y + 17, accent);
            ctx.drawText(client.textRenderer, names[i], left + 7, y + 4, TEXT, true);
            y += 19;
        }
    }

    private static void renderArmor(DrawContext ctx, MinecraftClient client, int accent, int screenWidth, int screenHeight) {
        int x = HudLayout.armorX;
        int y = HudLayout.armorY < 0 ? screenHeight - 34 : HudLayout.armorY;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        panel(ctx, x, y, 104, 30, accent);
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = client.player.getEquippedStack(slots[i]);
            if (stack.isEmpty()) continue;
            int itemX = x + 5 + i * 25;
            ctx.drawItem(stack, itemX, y + 2);
            if (stack.isDamageable()) {
                float remaining = 1.0f - (float) stack.getDamage() / stack.getMaxDamage();
                int barWidth = Math.round(16 * Math.max(0.0f, remaining));
                ctx.fill(itemX + 1, y + 22, itemX + 17, y + 24, 0xFF3B4050);
                if (barWidth > 0) ctx.fill(itemX + 1, y + 22, itemX + 1 + barWidth, y + 24, remaining > 0.5f ? 0xFF73D99B : 0xFFFFB45F);
            }
        }
    }

    private static void renderPotions(DrawContext ctx, MinecraftClient client, int accent, int screenWidth) {
        List<StatusEffectInstance> effects = new ArrayList<>(client.player.getStatusEffects());
        if (effects.isEmpty()) return;
        int x = HudLayout.potionsX < 0 ? screenWidth - 154 : HudLayout.potionsX;
        int y = HudLayout.potionsY;
        int rows = Math.min(effects.size(), 6);
        panel(ctx, x, y, 146, 20 + rows * 17, accent);
        ctx.drawText(client.textRenderer, "EFFECTS", x + 8, y + 6, accent, true);
        for (int i = 0; i < rows; i++) {
            StatusEffectInstance effect = effects.get(i);
            String name = effect.getEffectType().value().getName().getString();
            if (effect.getAmplifier() > 0) name += " " + (effect.getAmplifier() + 1);
            int ticks = effect.getDuration();
            String time = ticks < 0 ? "∞" : String.format("%d:%02d", ticks / 1200, (ticks / 20) % 60);
            if (name.length() > 15) name = name.substring(0, 14) + "…";
            ctx.drawText(client.textRenderer, name, x + 8, y + 21 + i * 17, TEXT, false);
            ctx.drawText(client.textRenderer, time, x + 108, y + 21 + i * 17, MUTED, false);
        }
    }

    private static void panel(DrawContext ctx, int x, int y, int w, int h, int accent) {
        ctx.fill(x + 2, y + 2, x + w + 2, y + h + 2, 0x44000000);
        ctx.fill(x, y, x + w, y + h, PANEL);
        ctx.fill(x, y, x + w, y + 2, 0x55FFFFFF);
        ctx.fill(x, y, x + 2, y + h, accent);
    }

    private static void drawKey(DrawContext ctx, MinecraftClient client, String label,
                                KeyBinding key, int x, int y, int accent, int width) {
        boolean pressed = key.isPressed();
        ctx.fill(x, y, x + width, y + 20, pressed ? 0xFFDCE8FA : PANEL);
        ctx.fill(x, y, x + width, y + 2, pressed ? accent : 0x665E687F);
        int textColor = pressed ? 0xFF202A3B : MUTED;
        int textX = x + (width - client.textRenderer.getWidth(label)) / 2;
        ctx.drawText(client.textRenderer, label, textX, y + 6, textColor, true);
    }
}
