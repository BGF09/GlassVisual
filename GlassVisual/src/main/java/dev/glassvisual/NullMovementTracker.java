package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/** Lightweight client-side movement/session statistics for NULL HUD widgets. */
public final class NullMovementTracker {
    private static final float[] SPEED = new float[48];
    private static int cursor;
    private static int samples;
    private static int jumps;
    private static boolean wasOnGround = true;
    private static long sessionStarted;
    private static Object lastWorld;

    private NullMovementTracker() {}

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            lastWorld = null;
            sessionStarted = 0L;
            wasOnGround = true;
            return;
        }
        if (lastWorld != client.world || sessionStarted == 0L) {
            lastWorld = client.world;
            sessionStarted = System.nanoTime();
            jumps = 0;
            cursor = 0;
            samples = 0;
            wasOnGround = client.player.isOnGround();
        }
        boolean grounded = client.player.isOnGround();
        if (wasOnGround && !grounded && client.player.getVelocity().y > 0.0) jumps++;
        wasOnGround = grounded;

        Vec3d velocity = client.player.getVelocity();
        SPEED[cursor] = (float) velocity.horizontalLength() * 20.0f;
        cursor = (cursor + 1) % SPEED.length;
        samples = Math.min(samples + 1, SPEED.length);
    }

    public static int jumps() { return jumps; }
    public static float speed() {
        if (samples == 0) return 0.0f;
        return SPEED[(cursor - 1 + SPEED.length) % SPEED.length];
    }
    public static float speedAt(int offsetFromOldest) {
        if (offsetFromOldest < 0 || offsetFromOldest >= samples) return 0.0f;
        int oldest = (cursor - samples + SPEED.length) % SPEED.length;
        return SPEED[(oldest + offsetFromOldest) % SPEED.length];
    }
    public static int samples() { return samples; }
    public static long sessionSeconds() {
        return sessionStarted == 0L ? 0L : Math.max(0L, (System.nanoTime() - sessionStarted) / 1_000_000_000L);
    }
}
