package dev.glassvisual;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;

public final class HudOverlayRenderer {
    private static final int PANEL = 0xE91A202D;
    private static final int TEXT = 0xFFF1F3FA;
    private static final int MUTED = 0xFF9AA8BD;
    private static final int SOFT = 0xFF7D8AA0;

    private HudOverlayRenderer() {}

    public static void render(DrawContext ctx, MinecraftClient client) {
        int accent = NullTheme.accent();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        if (GlassVisualClient.MODULES.worldTint) ctx.fill(0, 0, width, height, 0x169B7CFF);
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
        if (GlassVisualClient.MODULES.armorStatus && client.player != null) {
            renderArmorStatus(ctx, client, accent, height);
        }
        if (GlassVisualClient.MODULES.potionHud && client.player != null) {
            renderPotions(ctx, client, accent, width);
        }
        if (GlassVisualClient.MODULES.arrayList) {
            renderArrayList(ctx, client, accent, width);
        }
        renderUtilityWidgets(ctx, client, accent, width, height);
        renderCombatWidgets(ctx, client, accent, width, height);
    }

    private static void renderUtilityWidgets(DrawContext ctx, MinecraftClient client, int accent, int width, int height) {
        var modules = GlassVisualClient.MODULES;
        if (modules.sprintDisplay && client.player != null) {
            int x = 12, y = height - 62;
            panel(ctx, x, y, 112, 24, accent);
            boolean sprinting = client.player.isSprinting();
            ctx.drawText(client.textRenderer, "SPRINT", x + 8, y + 8, MUTED, true);
            String state = sprinting ? "ON" : "OFF";
            int color = sprinting ? 0xFF36A875 : 0xFF8792A5;
            ctx.drawText(client.textRenderer, state, x + 69, y + 8, color, true);
        }
        if (modules.jumpStats && client.player != null) {
            int x = 12, y = height - 92;
            panel(ctx, x, y, 112, 24, accent);
            ctx.drawText(client.textRenderer, "JUMPS", x + 8, y + 8, MUTED, true);
            ctx.drawText(client.textRenderer, Integer.toString(NullMovementTracker.jumps()), x + 69, y + 8, TEXT, true);
        }
        if (modules.speedGraph && client.player != null) {
            int x = width - 154, y = height - 72;
            panel(ctx, x, y, 142, 58, accent);
            ctx.drawText(client.textRenderer, "SPEED", x + 8, y + 6, MUTED, true);
            String speed = String.format(java.util.Locale.ROOT, "%.1f m/s", NullMovementTracker.speed());
            ctx.drawText(client.textRenderer, speed, x + 76, y + 6, TEXT, true);
            int graphX = x + 8, graphY = y + 24, graphW = 126, graphH = 27;
            ctx.fill(graphX, graphY + graphH - 1, graphX + graphW, graphY + graphH, 0x55313C50);
            for (int i = 0; i < NullMovementTracker.samples(); i++) {
                float value = Math.max(0.0f, Math.min(12.0f, NullMovementTracker.speedAt(i)));
                int bar = Math.max(1, Math.round((value / 12.0f) * (graphH - 2)));
                int bx = graphX + i * graphW / 48;
                ctx.fill(bx, graphY + graphH - bar, bx + 2, graphY + graphH, accent);
            }
        }
        if (modules.sessionInfo) {
            long seconds = NullMovementTracker.sessionSeconds();
            String elapsed = String.format(java.util.Locale.ROOT, "%02d:%02d:%02d",
                seconds / 3600, (seconds / 60) % 60, seconds % 60);
            panel(ctx, 12, 44, 132, 24, accent);
            ctx.drawText(client.textRenderer, "SESSION", 20, 52, MUTED, true);
            ctx.drawText(client.textRenderer, elapsed, 74, 52, TEXT, true);
        }
        if (modules.clock) {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            int x = width - 92, y = 12;
            panel(ctx, x, y, 80, 24, accent);
            ctx.drawText(client.textRenderer, time, x + 10, y + 8, TEXT, true);
        }

        if (modules.totemCounter && client.player != null) {
            int count = 0;
            for (ItemStack stack : client.player.getInventory().main) {
                if (stack.isOf(Items.TOTEM_OF_UNDYING)) count += stack.getCount();
            }
            if (client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING))
                count += client.player.getOffHandStack().getCount();
            int x = 12, y = height - 152;
            panel(ctx, x, y, 112, 24, accent);
            ctx.drawText(client.textRenderer, "TOTEMS", x + 8, y + 8, MUTED, true);
            ctx.drawText(client.textRenderer, Integer.toString(count), x + 76, y + 8, TEXT, true);
        }
        if (modules.foodHud && client.player != null) {
            int food = client.player.getHungerManager().getFoodLevel();
            float saturation = client.player.getHungerManager().getSaturationLevel();
            int x = 12, y = height - 182;
            panel(ctx, x, y, 142, 24, accent);
            ctx.drawText(client.textRenderer, "FOOD", x + 8, y + 8, MUTED, true);
            ctx.drawText(client.textRenderer, food + "/20", x + 54, y + 8, TEXT, true);
            ctx.drawText(client.textRenderer, String.format(java.util.Locale.ROOT, "%.1f sat", saturation),
                x + 91, y + 8, accent, true);
        }
        if (modules.xpHud && client.player != null) {
            int level = client.player.experienceLevel;
            float progress = client.player.experienceProgress;
            int x = 12, y = height - 212;
            panel(ctx, x, y, 142, 24, accent);
            ctx.drawText(client.textRenderer, "LEVEL", x + 8, y + 8, MUTED, true);
            ctx.drawText(client.textRenderer, Integer.toString(level), x + 54, y + 8, TEXT, true);
            ctx.fill(x + 84, y + 10, x + 133, y + 15, 0xFF303B4D);
            int fill = Math.round(49 * MathHelper.clamp(progress, 0.0f, 1.0f));
            if (fill > 0) ctx.fill(x + 84, y + 10, x + 84 + fill, y + 15, accent);
        }
        if (modules.directionHud && client.player != null) {
            int facing = MathHelper.floor(client.player.getYaw() / 90.0f + 0.5f) & 3;
            String[] directions = {"SOUTH", "WEST", "NORTH", "EAST"};
            int x = width / 2 - 42, y = 12;
            panel(ctx, x, y, 84, 24, accent);
            ctx.drawCenteredTextWithShadow(client.textRenderer, directions[facing], x + 42, y + 8, TEXT);
        }
    }

    private static void renderCombatWidgets(DrawContext ctx, MinecraftClient client, int accent, int width, int height) {
        var modules = GlassVisualClient.MODULES;
        if (modules.crosshair) {
            int cx = width / 2, cy = height / 2;
            int dark = 0xB0182232;
            ctx.fill(cx - 1, cy - 8, cx + 2, cy - 3, dark);
            ctx.fill(cx - 1, cy + 4, cx + 2, cy + 9, dark);
            ctx.fill(cx - 8, cy - 1, cx - 3, cy + 2, dark);
            ctx.fill(cx + 4, cy - 1, cx + 9, cy + 2, dark);
            ctx.fill(cx, cy - 7, cx + 1, cy - 3, accent);
            ctx.fill(cx, cy + 4, cx + 1, cy + 8, accent);
            ctx.fill(cx - 7, cy, cx - 3, cy + 1, accent);
            ctx.fill(cx + 4, cy, cx + 8, cy + 1, accent);
        }
        if (client.player == null) return;
        if (modules.combatHud) {
            int x = width / 2 - 86, y = height - 52;
            panel(ctx, x, y, 172, 40, accent);
            String held = client.player.getMainHandStack().isEmpty() ? "EMPTY"
                : client.player.getMainHandStack().getName().getString();
            if (held.length() > 18) held = held.substring(0, 17) + "…";
            ctx.drawText(client.textRenderer, "COMBAT", x + 9, y + 7, MUTED, true);
            ctx.drawText(client.textRenderer, held, x + 58, y + 7, TEXT, true);
            float cooldown = client.player.getAttackCooldownProgress(0.0f);
            int barWidth = Math.round(154 * Math.max(0.0f, Math.min(1.0f, cooldown)));
            ctx.fill(x + 9, y + 25, x + 163, y + 31, 0xFF303B4D);
            if (barWidth > 0) ctx.fill(x + 9, y + 25, x + 9 + barWidth, y + 31, accent);
        }
        if (modules.durability) {
            var stack = client.player.getMainHandStack();
            if (stack.isDamageable()) {
                int x = width / 2 + 96, y = height - 34;
                float remaining = 1.0f - (float) stack.getDamage() / stack.getMaxDamage();
                panel(ctx, x, y, 110, 24, accent);
                ctx.drawText(client.textRenderer, "DURABILITY", x + 7, y + 4, MUTED, true);
                ctx.drawText(client.textRenderer, Math.round(remaining * 100) + "%", x + 72, y + 4,
                    remaining > 0.5f ? 0xFF36A875 : 0xFFE18B55, true);
                ctx.fill(x + 7, y + 16, x + 103, y + 19, 0xFFDCE3EF);
                int bar = Math.round(96 * Math.max(0.0f, remaining));
                if (bar > 0) ctx.fill(x + 7, y + 16, x + 7 + bar, y + 19, accent);
            }
        }
        if (modules.inventory) {
            int x = width / 2 - 94, y = height - 82;
            panel(ctx, x, y, 188, 27, accent);
            for (int slot = 0; slot < 9; slot++) {
                int sx = x + 3 + slot * 20;
                boolean selected = client.player.getInventory().selectedSlot == slot;
                ctx.fill(sx, y + 3, sx + 19, y + 24, selected ? 0xFF303B4D : 0x22313C50);
                if (selected) ctx.fill(sx, y + 3, sx + 19, y + 5, accent);
                var stack = client.player.getInventory().getStack(slot);
                if (!stack.isEmpty()) ctx.drawItem(stack, sx + 1, y + 4);
            }
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
        String[] names = {"Aim Assist", "Hit Visuals", "Target HUD", "Combat HUD", "Particles",
            "Auto Sprint", "Sprint Display", "Jump Stats", "Speed Graph", "Keystrokes",
            "World Tint", "Block Outline", "Crosshair", "FPS", "Potion HUD", "Time Changer",
            "Armor Status", "Armor HUD", "Coordinates", "Durability", "Inventory", "Totem Counter", "Food HUD", "XP HUD",
            "Session Info", "Clock", "Notifications", "Ping", "Direction"};
        var m = GlassVisualClient.MODULES;
        boolean[] active = {
            m.aimAssist, m.hitVisuals, m.targetHud, m.combatHud, NullUiSettings.particles,
            m.autoSprint, m.sprintDisplay, m.jumpStats, m.speedGraph, m.keystrokes,
            m.worldTint, m.blockOutline, m.crosshair, m.fps, m.potionHud, m.timeChanger,
            m.armorStatus, m.armorHud, m.coordinates, m.durability, m.inventory, m.totemCounter, m.foodHud, m.xpHud,
            m.sessionInfo, m.clock, m.notifications, m.ping, m.directionHud
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

    private static void renderArmorStatus(DrawContext ctx, MinecraftClient client, int accent, int screenHeight) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        String[] labels = {"HELMET", "CHEST", "LEGS", "BOOTS"};
        int x = 12, y = Math.max(82, screenHeight / 2 + 62);
        panel(ctx, x, y, 132, 82, accent);
        ctx.drawText(client.textRenderer, "ARMOR STATUS", x + 8, y + 6, accent, true);
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = client.player.getEquippedStack(slots[i]);
            int remaining = stack.isEmpty() || !stack.isDamageable() ? 0
                : Math.round(100.0f * (1.0f - (float) stack.getDamage() / stack.getMaxDamage()));
            int color = remaining > 50 ? 0xFF36A875 : remaining > 20 ? 0xFFE0A04E : 0xFFE16F78;
            ctx.drawText(client.textRenderer, labels[i], x + 8, y + 21 + i * 14, MUTED, false);
            ctx.drawText(client.textRenderer, stack.isEmpty() ? "—" : remaining + "%",
                x + 91, y + 21 + i * 14, color, true);
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
        roundedFill(ctx, x + 2, y + 2, w, h, 7, 0x33000000);
        roundedFill(ctx, x, y, w, h, 7, PANEL);
        ctx.fill(x + 7, y, x + w - 7, y + 2, 0x22FFFFFF);
        ctx.fill(x, y + 6, x + 2, y + h - 6, accent);
    }

    private static void roundedFill(DrawContext ctx, int x, int y, int w, int h, int radius, int color) {
        int r = Math.max(1, Math.min(radius, Math.min(w, h) / 2));
        for (int row = 0; row < h; row++) {
            int edge = row < r ? r - 1 - row : (row >= h - r ? row - (h - r) : -1);
            int inset = edge < 0 ? 0 : r - (int) Math.floor(Math.sqrt(Math.max(0, r * r - edge * edge)));
            ctx.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
        }
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
