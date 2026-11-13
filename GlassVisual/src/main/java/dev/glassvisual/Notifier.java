package dev.glassvisual;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/** Thread-safe animated HUD notification queue. Timing is based on the monotonic clock. */
public final class Notifier {
    private static final int LIMIT = 5;
    private static final long MODULE_TIME = 2_000_000_000L;
    private static final long TARGET_TIME = 2_500_000_000L;
    private static final int BG = 0xF0141B29;
    private static final int BORDER = 0xAA71849D;
    private static final List<Card> ACTIVE = new ArrayList<>(LIMIT);
    private static final ArrayDeque<Card> QUEUED = new ArrayDeque<>();

    private Notifier() {}

    public static void module(Module module, boolean enabled) {
        if (module == null) return;
        String name = module.getName();
        String message = (enabled ? "Включено (" : "Выключено (") + name + ")";
        int accent = enabled ? NullTheme.accent() : 0xFFFF777F;
        synchronized (ACTIVE) {
            for (int i = 0; i < ACTIVE.size(); i++) {
                Card card = ACTIVE.get(i);
                if (card.type == Type.MODULE && card.key.equals(name)) {
                    card.message = message;
                    card.accent = accent;
                    card.started = System.nanoTime();
                    card.duration = MODULE_TIME;
                    card.leaving = false;
                    return;
                }
            }
            for (Card card : QUEUED) {
                if (card.type == Type.MODULE && card.key.equals(name)) {
                    card.message = message;
                    card.accent = accent;
                    return;
                }
            }
            enqueue(new Card(Type.MODULE, name, message, accent, null, MODULE_TIME));
        }
    }

    public static void target(net.minecraft.entity.Entity target, String action, ItemStack stack) {
        if (target == null || action == null) return;
        ItemStack icon = stack == null ? ItemStack.EMPTY : stack.copy();
        String name = target.getName().getString();
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerListEntry entry = client.getNetworkHandler() == null ? null
            : client.getNetworkHandler().getPlayerListEntry(target.getUuid());
        SkinTextures skin = entry == null ? null : entry.getSkinTextures();
        synchronized (ACTIVE) {
            Card card = new Card(Type.TARGET, "target:" + name + ":" + action,
                action + " · " + name, NullTheme.accent(), icon, TARGET_TIME);
            card.skin = skin;
            enqueue(card);
        }
    }

    /** Compatibility entry point retained for existing send(String) callers. */
    public static void send(String message) {
        if (message == null || message.isBlank()) return;
        synchronized (ACTIVE) {
            enqueue(new Card(Type.MODULE, "legacy:" + message, message,
                NullTheme.accent(), null, MODULE_TIME));
        }
    }

    private static void enqueue(Card card) {
        if (ACTIVE.size() < LIMIT) {
            card.started = System.nanoTime();
            ACTIVE.add(card);
        } else {
            QUEUED.addLast(card);
        }
    }

    public static void render(DrawContext context, MinecraftClient client) {
        if (client == null || client.player == null) return;
        long now = System.nanoTime();
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();
        int bottom = screenH - 58;
        synchronized (ACTIVE) {
            for (int i = ACTIVE.size() - 1; i >= 0; i--) {
                Card card = ACTIVE.get(i);
                if (!card.leaving && now - card.started >= card.duration) {
                    card.leaving = true;
                    card.leaveStarted = now;
                }
                if (card.leaving && now - card.leaveStarted >= 260_000_000L) ACTIVE.remove(i);
            }
            while (ACTIVE.size() < LIMIT && !QUEUED.isEmpty()) {
                Card next = QUEUED.removeFirst();
                next.started = now;
                ACTIVE.add(next);
            }
            int stackOffset = 0;
            for (int i = 0; i < ACTIVE.size(); i++) {
                Card card = ACTIVE.get(i);
                int textWidth = client.textRenderer.getWidth(card.message);
                int width = card.type == Type.MODULE ? Math.max(174, textWidth + 70)
                    : Math.max(230, textWidth + (card.icon.isEmpty() ? 76 : 112));
                width = Math.min(width, screenW - 24);
                long elapsed = now - card.started;
                float progress = Math.min(1.0f, elapsed / 260_000_000.0f);
                float shifted = progress - 1.0f;
                float back = 1.0f + 2.70158f * shifted * shifted * shifted
                    + 1.70158f * shifted * shifted;
                float alpha = 1.0f;
                float yOffset = (1.0f - back) * 24.0f;
                if (card.leaving) {
                    float out = Math.min(1.0f, (now - card.leaveStarted) / 260_000_000.0f);
                    alpha = 1.0f - out;
                    yOffset += out * 34.0f;
                }
                int x = (screenW - width) / 2;
                int y = bottom - stackOffset - 38 + Math.round(yOffset);
                drawCard(context, client, card, x, y, width, 38, alpha);
                stackOffset += 45;
            }
        }
    }

    private static void drawCard(DrawContext ctx, MinecraftClient client, Card card,
                                 int x, int y, int width, int height, float alpha) {
        int fade = Math.max(0, Math.min(255, Math.round(alpha * 255)));
        ctx.fill(x - 2, y - 2, x + width + 2, y + height + 2, (Math.round(alpha * 35) << 24) | 0x00101828);
        ctx.fill(x, y, x + width, y + height, (fade << 24) | (BG & 0x00FFFFFF));
        int border = (Math.round(alpha * 170) << 24) | (BORDER & 0x00FFFFFF);
        ctx.fill(x, y, x + width, y + 1, border);
        ctx.fill(x, y + height - 1, x + width, y + height, border);
        ctx.fill(x, y, x + 1, y + height, border);
        ctx.fill(x + width - 1, y, x + width, y + height, border);
        int accent = (fade << 24) | (card.accent & 0x00FFFFFF);

        if (card.type == Type.MODULE) {
            drawBell(ctx, x + 10, y + 9, accent, alpha);
            ctx.fill(x + 36, y + 7, x + 37, y + height - 7, (Math.round(alpha * 90) << 24) | 0x00A9C7DA);
            ctx.drawText(client.textRenderer, card.text, x + 47, y + 15,
                (fade << 24) | 0x00F5F8FF, true);
        } else {
            if (card.skin != null) PlayerSkinDrawer.draw(ctx, card.skin, x + 8, y + 6, 22);
            else {
                ctx.fill(x + 8, y + 6, x + 30, y + 29, (fade << 24) | 0x00313C50);
                ctx.fill(x + 13, y + 10, x + 25, y + 21, (fade << 24) | 0x00A8B9CC);
                ctx.fill(x + 11, y + 21, x + 27, y + 27, (fade << 24) | 0x007D91AA);
            }
            ctx.fill(x + 37, y + 6, x + 38, y + height - 6, (Math.round(alpha * 90) << 24) | 0x00A9B6C7);
            ctx.drawText(client.textRenderer, card.text, x + 48, y + 15,
                (fade << 24) | 0x00F1F3F7, true);
            if (!card.icon.isEmpty()) {
                int ix = x + width - 28;
                ctx.fill(ix - 5, y + 5, x + width - 5, y + height - 5, (Math.round(alpha * 90) << 24) | 0x00A9B6C7);
                ctx.drawItem(card.icon, ix, y + 10);
            }
        }
    }

    private static void drawBell(DrawContext ctx, int x, int y, int color, float alpha) {
        int glow = (Math.round(alpha * 48) << 24) | 0x003FC8F2;
        ctx.fill(x + 2, y + 1, x + 17, y + 17, glow);
        ctx.fill(x + 6, y + 2, x + 13, y + 4, color);
        ctx.fill(x + 4, y + 4, x + 15, y + 13, color);
        ctx.fill(x + 2, y + 12, x + 17, y + 15, color);
        ctx.fill(x + 7, y + 15, x + 12, y + 17, color);
        ctx.fill(x + 9, y, x + 10, y + 2, color);
        ctx.fill(x, y + 5, x + 3, y + 6, glow);
        ctx.fill(x + 16, y + 5, x + 19, y + 6, glow);
    }

    private enum Type { MODULE, TARGET }
    private static final class Card {
        final Type type;
        final String key;
        final ItemStack icon;
        SkinTextures skin;
        String message;
        int accent;
        long duration, started, leaveStarted;
        boolean leaving;
        Card(Type type, String key, String message, int accent, ItemStack icon, long duration) {
            this.type = type; this.key = key; this.message = message; this.text = Text.literal(message); this.accent = accent;
            this.icon = icon == null ? ItemStack.EMPTY : icon; this.duration = duration;
        }
    }
}
