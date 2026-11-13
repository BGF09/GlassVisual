package dev.glassvisual;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Detects remote item use from synchronized inventory stack decrements.
 * This avoids hard-coding packet class names and works for throwables and consumables.
 */
public final class TargetNotifier {
    private static final double RANGE_SQUARED = 24.0 * 24.0;
    private static final long COOLDOWN = 1_500_000_000L;
    private static final Map<UUID, Snapshot> LAST = new HashMap<>();
    private static final Map<CooldownKey, Long> COOLDOWNS = new HashMap<>();

    private TargetNotifier() {}

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            LAST.clear();
            COOLDOWNS.clear();
            return;
        }
        long now = System.nanoTime();
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player || player.squaredDistanceTo(client.player) > RANGE_SQUARED) continue;
            UUID id = player.getUuid();
            ItemStack main = player.getMainHandStack();
            ItemStack off = player.getOffHandStack();
            Snapshot old = LAST.get(id);
            if (old != null) {
                check(player, old.mainItem, old.mainCount, main, now);
                check(player, old.offItem, old.offCount, off, now);
            }
            LAST.put(id, new Snapshot(main.getItem(), main.getCount(), off.getItem(), off.getCount()));
        }
        Iterator<UUID> players = LAST.keySet().iterator();
        while (players.hasNext()) {
            UUID id = players.next();
            boolean present = false;
            for (PlayerEntity player : client.world.getPlayers()) {
                if (player.getUuid().equals(id) && player.squaredDistanceTo(client.player) <= RANGE_SQUARED) {
                    present = true;
                    break;
                }
            }
            if (!present) players.remove();
        }
        COOLDOWNS.entrySet().removeIf(entry -> now - entry.getValue() > COOLDOWN * 4);
    }

    private static void check(PlayerEntity player, Item previous, int previousCount,
                              ItemStack current, long now) {
        if (previousCount <= current.getCount() || !tracked(previous)) return;
        CooldownKey key = new CooldownKey(player.getUuid(), previous);
        Long last = COOLDOWNS.get(key);
        if (last != null && now - last < COOLDOWN) return;
        COOLDOWNS.put(key, now);
        Notifier.target(player, action(previous), new ItemStack(previous));
    }

    private static boolean tracked(Item item) {
        return item == Items.WIND_CHARGE || item == Items.ENDER_PEARL
            || item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE
            || item == Items.TOTEM_OF_UNDYING || item == Items.POTION
            || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION;
    }

    private static String action(Item item) {
        if (item == Items.WIND_CHARGE) return "использовал заряд ветра";
        if (item == Items.ENDER_PEARL) return "бросил эндер-жемчуг";
        if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) return "съел золотое яблоко";
        if (item == Items.TOTEM_OF_UNDYING) return "использовал тотем";
        return "использовал зелье";
    }

    private record Snapshot(Item mainItem, int mainCount, Item offItem, int offCount) {}
    private record CooldownKey(UUID player, Item item) {}
}
