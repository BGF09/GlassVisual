package dev.glassvisual;

import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * NULL World Particles: glowing motes that live in the 3D world around the player.
 *
 * <p>This is NOT Minecraft's ParticleManager and uses no ParticleTypes. State lives in primitive arrays
 * (structure of arrays, fixed capacity), simulation runs on the client tick and rendering happens in
 * {@code WorldRenderEvents.LAST} as camera-facing quads, so nothing is allocated per particle or per frame.
 */
public final class NullWorldParticles {
    private static final int MAX = 360;
    private static final byte AMBIENT = 0, SPARK = 1;

    private static final double[] x = new double[MAX], y = new double[MAX], z = new double[MAX];
    private static final double[] px = new double[MAX], py = new double[MAX], pz = new double[MAX];
    private static final float[] vx = new float[MAX], vy = new float[MAX], vz = new float[MAX];
    private static final float[] age = new float[MAX], maxAge = new float[MAX];
    private static final float[] size = new float[MAX], phase = new float[MAX];
    private static final byte[] kind = new byte[MAX];
    private static int count;

    private static final Random RNG = new Random();
    private static final Vector3f RIGHT = new Vector3f(), UP = new Vector3f();
    private static ClientWorld lastWorld;

    private NullWorldParticles() {}

    public static void clear() { count = 0; lastWorld = null; }

    // ------------------------------------------------------------------ simulation (20 Hz)

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (!NullUiSettings.worldParticles || player == null || world == null) {
            if (count != 0 || lastWorld != null) clear();
            return;
        }
        if (world != lastWorld) { count = 0; lastWorld = world; } // new dimension / world: start fresh

        double ex = player.getX(), ey = player.getY() + player.getHeight() * 0.5, ez = player.getZ();
        float range = NullVisualSettings.particleRange();
        double leash = (range * 1.35) * (range * 1.35);

        for (int i = count - 1; i >= 0; i--) {
            px[i] = x[i]; py[i] = y[i]; pz[i] = z[i];
            age[i] += 1.0f;
            if (age[i] >= maxAge[i]) { remove(i); continue; }
            float a = age[i] * 0.07f + phase[i];
            boolean spark = kind[i] == SPARK;
            vx[i] += (float) Math.sin(a * 1.3f + phase[i]) * (spark ? 0.0030f : 0.0011f);
            vy[i] += (float) Math.cos(a * 0.9f) * 0.0009f + (spark ? 0.0007f : 0.0002f);
            vz[i] += (float) Math.sin(a * 1.1f + 1.7f) * (spark ? 0.0030f : 0.0011f);
            float drag = spark ? 0.93f : 0.965f;
            vx[i] *= drag; vy[i] *= drag; vz[i] *= drag;
            x[i] += vx[i]; y[i] += vy[i]; z[i] += vz[i];
            double dx = x[i] - ex, dy = y[i] - ey, dz = z[i] - ez;
            if (dx * dx + dy * dy + dz * dz > leash && maxAge[i] > age[i] + 10) maxAge[i] = age[i] + 10; // too far: fade out
        }

        int target = Math.max(8, Math.round(NullVisualSettings.particleDensity() * 3.0f));
        for (int n = 0; n < 6 && count < target; n++) spawnAmbient(world, ex, ey, ez, range);

        if (NullVisualSettings.particleMoveSparks() && !player.isSpectator()) {
            Vec3d v = player.getVelocity();
            double speed = Math.sqrt(v.x * v.x + v.z * v.z);
            if (speed > 0.09) {
                int sparks = 1 + NullVisualSettings.particleDensity() / 45 + (speed > 0.22 ? 1 : 0);
                for (int n = 0; n < sparks && count < MAX; n++) spawnSpark(player, v);
            }
        }
    }

    private static void spawnAmbient(ClientWorld world, double ex, double ey, double ez, float range) {
        for (int attempt = 0; attempt < 3; attempt++) {
            double angle = RNG.nextDouble() * Math.PI * 2.0;
            double radius = range * (0.2 + 0.8 * Math.sqrt(RNG.nextDouble()));
            double sx = ex + Math.cos(angle) * radius;
            double sy = ey + (RNG.nextDouble() - 0.35) * range * 0.55;
            double sz = ez + Math.sin(angle) * radius;
            if (!world.getBlockState(BlockPos.ofFloored(sx, sy, sz)).isAir()) continue; // never spawn inside blocks
            int i = count++;
            x[i] = px[i] = sx; y[i] = py[i] = sy; z[i] = pz[i] = sz;
            vx[i] = (RNG.nextFloat() - 0.5f) * 0.012f;
            vy[i] = (RNG.nextFloat() - 0.3f) * 0.010f;
            vz[i] = (RNG.nextFloat() - 0.5f) * 0.012f;
            age[i] = 0.0f;
            maxAge[i] = 70.0f + RNG.nextFloat() * 120.0f;
            size[i] = 0.045f + RNG.nextFloat() * 0.075f;
            phase[i] = RNG.nextFloat() * 6.2831f;
            kind[i] = AMBIENT;
            return;
        }
    }

    private static void spawnSpark(ClientPlayerEntity player, Vec3d velocity) {
        int i = count++;
        double sx = player.getX() - velocity.x * 1.5 + (RNG.nextDouble() - 0.5) * 0.5;
        double sy = player.getY() + 0.1 + RNG.nextDouble() * 0.5;
        double sz = player.getZ() - velocity.z * 1.5 + (RNG.nextDouble() - 0.5) * 0.5;
        x[i] = px[i] = sx; y[i] = py[i] = sy; z[i] = pz[i] = sz;
        vx[i] = (float) (-velocity.x * 0.12) + (RNG.nextFloat() - 0.5f) * 0.05f;
        vy[i] = 0.01f + RNG.nextFloat() * 0.035f;
        vz[i] = (float) (-velocity.z * 0.12) + (RNG.nextFloat() - 0.5f) * 0.05f;
        age[i] = 0.0f;
        maxAge[i] = 14.0f + RNG.nextFloat() * 18.0f;
        size[i] = 0.030f + RNG.nextFloat() * 0.040f;
        phase[i] = RNG.nextFloat() * 6.2831f;
        kind[i] = SPARK;
    }

    private static void remove(int i) {
        int last = --count;
        if (i == last) return;
        x[i] = x[last]; y[i] = y[last]; z[i] = z[last];
        px[i] = px[last]; py[i] = py[last]; pz[i] = pz[last];
        vx[i] = vx[last]; vy[i] = vy[last]; vz[i] = vz[last];
        age[i] = age[last]; maxAge[i] = maxAge[last];
        size[i] = size[last]; phase[i] = phase[last]; kind[i] = kind[last];
    }

    // ------------------------------------------------------------------ rendering (every frame)

    public static void render(WorldRenderContext context) {
        if (!NullUiSettings.worldParticles || count == 0) return;
        VertexConsumerProvider consumers = context.consumers();
        MatrixStack matrices = context.matrixStack();
        if (consumers == null || matrices == null) return;

        Camera camera = context.camera();
        Vec3d cam = camera.getPos();
        float tickDelta = context.tickCounter().getTickDelta(false);
        RIGHT.set(1.0f, 0.0f, 0.0f).rotate(camera.getRotation());
        UP.set(0.0f, 1.0f, 0.0f).rotate(camera.getRotation());

        Matrix4f m = matrices.peek().getPositionMatrix();
        VertexConsumer vc = consumers.getBuffer(RenderLayer.getLightning());
        int rgb = NullTheme.accent();
        int cr = (rgb >> 16) & 0xFF, cg = (rgb >> 8) & 0xFF, cb = rgb & 0xFF;
        float sizeScale = NullVisualSettings.particleSize();
        float range = NullVisualSettings.particleRange();

        for (int i = 0; i < count; i++) {
            float ix = (float) (px[i] + (x[i] - px[i]) * tickDelta - cam.x);
            float iy = (float) (py[i] + (y[i] - py[i]) * tickDelta - cam.y);
            float iz = (float) (pz[i] + (z[i] - pz[i]) * tickDelta - cam.z);
            float dist = (float) Math.sqrt(ix * ix + iy * iy + iz * iz);

            float a = age[i] + tickDelta;
            float fadeIn = Math.min(1.0f, a / 12.0f);
            float fadeOut = Math.min(1.0f, (maxAge[i] - a) / Math.min(30.0f, maxAge[i] * 0.5f));
            float fade = smooth(Math.max(0.0f, fadeIn)) * smooth(Math.max(0.0f, fadeOut));
            float edge = clamp01((range + 3.0f - dist) / 4.0f);   // soft cut at the configured range
            float near = clamp01((dist - 0.7f) / 1.6f);           // never blind the camera
            float twinkle = 0.78f + 0.22f * (float) Math.sin(a * 0.28f + phase[i]);
            float alpha = fade * edge * near * twinkle;
            if (alpha < 0.012f) continue;

            float half = size[i] * sizeScale;
            glow(vc, m, ix, iy, iz, half * 2.8f, cr, cg, cb, (int) (alpha * 200.0f));
            core(vc, m, ix, iy, iz, half * 0.55f,
                (cr + 255 * 2) / 3, (cg + 255 * 2) / 3, (cb + 255 * 2) / 3, (int) (alpha * 255.0f));
        }

        if (consumers instanceof VertexConsumerProvider.Immediate immediate) immediate.draw(RenderLayer.getLightning());
    }

    /** Soft radial glow: four quads fanning from a bright centre to fully transparent edges. */
    private static void glow(VertexConsumer vc, Matrix4f m, float cx, float cy, float cz, float h,
                             int r, int g, int b, int alpha) {
        float rx = RIGHT.x * h, ry = RIGHT.y * h, rz = RIGHT.z * h;
        float ux = UP.x * h, uy = UP.y * h, uz = UP.z * h;
        // edge midpoints
        float e0x = cx + rx, e0y = cy + ry, e0z = cz + rz;
        float e1x = cx + ux, e1y = cy + uy, e1z = cz + uz;
        float e2x = cx - rx, e2y = cy - ry, e2z = cz - rz;
        float e3x = cx - ux, e3y = cy - uy, e3z = cz - uz;
        // corners
        float k0x = e0x + ux, k0y = e0y + uy, k0z = e0z + uz;
        float k1x = e2x + ux, k1y = e2y + uy, k1z = e2z + uz;
        float k2x = e2x - ux, k2y = e2y - uy, k2z = e2z - uz;
        float k3x = e0x - ux, k3y = e0y - uy, k3z = e0z - uz;
        fanQuad(vc, m, cx, cy, cz, e0x, e0y, e0z, k0x, k0y, k0z, e1x, e1y, e1z, r, g, b, alpha);
        fanQuad(vc, m, cx, cy, cz, e1x, e1y, e1z, k1x, k1y, k1z, e2x, e2y, e2z, r, g, b, alpha);
        fanQuad(vc, m, cx, cy, cz, e2x, e2y, e2z, k2x, k2y, k2z, e3x, e3y, e3z, r, g, b, alpha);
        fanQuad(vc, m, cx, cy, cz, e3x, e3y, e3z, k3x, k3y, k3z, e0x, e0y, e0z, r, g, b, alpha);
    }

    /** One quad with an opaque centre vertex and three transparent outer vertices. Emitted double-sided. */
    private static void fanQuad(VertexConsumer vc, Matrix4f m,
                                float ax, float ay, float az, float bx, float by, float bz,
                                float cx, float cy, float cz, float dx, float dy, float dz,
                                int r, int g, int b, int alpha) {
        vc.vertex(m, ax, ay, az).color(r, g, b, alpha);
        vc.vertex(m, bx, by, bz).color(r, g, b, 0);
        vc.vertex(m, cx, cy, cz).color(r, g, b, 0);
        vc.vertex(m, dx, dy, dz).color(r, g, b, 0);
        vc.vertex(m, ax, ay, az).color(r, g, b, alpha);
        vc.vertex(m, dx, dy, dz).color(r, g, b, 0);
        vc.vertex(m, cx, cy, cz).color(r, g, b, 0);
        vc.vertex(m, bx, by, bz).color(r, g, b, 0);
    }

    /** Small bright core, a camera-facing square. Emitted double-sided. */
    private static void core(VertexConsumer vc, Matrix4f m, float cx, float cy, float cz, float h,
                             int r, int g, int b, int alpha) {
        float rx = RIGHT.x * h, ry = RIGHT.y * h, rz = RIGHT.z * h;
        float ux = UP.x * h, uy = UP.y * h, uz = UP.z * h;
        float ax = cx - rx - ux, ay = cy - ry - uy, az = cz - rz - uz;
        float bx = cx + rx - ux, by = cy + ry - uy, bz = cz + rz - uz;
        float qx = cx + rx + ux, qy = cy + ry + uy, qz = cz + rz + uz;
        float dx = cx - rx + ux, dy = cy - ry + uy, dz = cz - rz + uz;
        vc.vertex(m, ax, ay, az).color(r, g, b, alpha);
        vc.vertex(m, bx, by, bz).color(r, g, b, alpha);
        vc.vertex(m, qx, qy, qz).color(r, g, b, alpha);
        vc.vertex(m, dx, dy, dz).color(r, g, b, alpha);
        vc.vertex(m, ax, ay, az).color(r, g, b, alpha);
        vc.vertex(m, dx, dy, dz).color(r, g, b, alpha);
        vc.vertex(m, qx, qy, qz).color(r, g, b, alpha);
        vc.vertex(m, bx, by, bz).color(r, g, b, alpha);
    }

    private static float clamp01(float v) { return v < 0.0f ? 0.0f : Math.min(v, 1.0f); }
    private static float smooth(float t) { return t * t * (3.0f - 2.0f * t); }
}
