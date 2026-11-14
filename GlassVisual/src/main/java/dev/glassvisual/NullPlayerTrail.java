package dev.glassvisual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * NULL Player Trail: a smooth glowing ribbon that follows the player's real path in the 3D world.
 *
 * <p>The position history is recorded on the client tick in a fixed-size ring buffer. Every frame the stored
 * points plus the interpolated live position are smoothed with a Catmull-Rom spline and drawn as a camera-facing
 * ribbon (a soft wide glow ribbon and a thin bright core). History is dropped on dimension/world change and on
 * teleports, and old points age out so a stationary player never leaves a permanent line.
 */
public final class NullPlayerTrail {
    private static final int CAP = 256;                  // must be a power of two
    private static final int MASK = CAP - 1;
    private static final int SUB = 4;                    // spline subdivisions per stored segment
    private static final double MIN_STEP_SQ = 0.12 * 0.12;
    private static final double TELEPORT_SQ = 6.0 * 6.0;

    // ring buffer of recorded points (oldest first)
    private static final double[] tx = new double[CAP], ty = new double[CAP], tz = new double[CAP];
    private static final float[] tage = new float[CAP];
    private static int start, count;
    private static ClientWorld lastWorld;
    private static double lastX, lastY, lastZ;           // last recorded position (kept after points age out)
    private static boolean hasLast;

    // per-frame scratch buffers (no allocation while rendering)
    private static final float[] cpx = new float[CAP + 2], cpy = new float[CAP + 2], cpz = new float[CAP + 2], cpa = new float[CAP + 2];
    private static final int MAX_SAMPLES = (CAP + 1) * SUB + 2;
    private static final float[] spx = new float[MAX_SAMPLES], spy = new float[MAX_SAMPLES], spz = new float[MAX_SAMPLES], spa = new float[MAX_SAMPLES];
    private static final float[] sdx = new float[MAX_SAMPLES], sdy = new float[MAX_SAMPLES], sdz = new float[MAX_SAMPLES]; // side vectors (unit)
    private static final float[] sfa = new float[MAX_SAMPLES];                                                          // final alpha factor

    private NullPlayerTrail() {}

    public static void clear() { start = 0; count = 0; lastWorld = null; hasLast = false; }

    private static int idx(int i) { return (start + i) & MASK; }

    // ------------------------------------------------------------------ history (20 Hz)

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (!NullUiSettings.playerTrail || player == null || world == null) {
            if (count != 0 || lastWorld != null) clear();
            return;
        }
        if (world != lastWorld) { start = 0; count = 0; hasLast = false; lastWorld = world; } // another dimension/world: never connect points

        float fade = NullVisualSettings.trailFadeSpeed();
        int length = NullVisualSettings.trailLength();
        for (int i = 0; i < count; i++) tage[idx(i)] += fade;
        while (count > 0 && tage[idx(0)] >= length) { start = (start + 1) & MASK; count--; }

        double px = player.getX(), py = player.getY() + 0.12, pz = player.getZ();
        if (hasLast) {
            double dx = px - lastX, dy = py - lastY, dz = pz - lastZ;
            double d2 = dx * dx + dy * dy + dz * dz;
            if (d2 > TELEPORT_SQ) { start = 0; count = 0; }   // teleport / respawn: drop the old path completely
            else if (d2 < MIN_STEP_SQ) return;                 // standing still: no new points, old ones keep fading
        }
        if (count == CAP) { start = (start + 1) & MASK; count--; }
        int k = idx(count);
        tx[k] = px; ty[k] = py; tz[k] = pz; tage[k] = 0.0f;
        lastX = px; lastY = py; lastZ = pz; hasLast = true;
        count++;
    }

    // ------------------------------------------------------------------ rendering (every frame)

    public static void render(WorldRenderContext context) {
        if (!NullUiSettings.playerTrail || count == 0) return;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world != lastWorld || player.isSpectator() || player.isInvisible()) return;
        VertexConsumerProvider consumers = context.consumers();
        MatrixStack matrices = context.matrixStack();
        if (consumers == null || matrices == null) return;

        Vec3d cam = context.camera().getPos();
        float tickDelta = context.tickCounter().getTickDelta(false);
        Vec3d live = player.getLerpedPos(tickDelta);
        int length = NullVisualSettings.trailLength();

        // 1. control points: recorded history followed by the interpolated live position
        int n = 0;
        for (int i = 0; i < count; i++) {
            int k = idx(i);
            float t = 1.0f - (tage[k] + NullVisualSettings.trailFadeSpeed() * tickDelta) / length;
            float a = clamp01(t);
            cpx[n] = (float) (tx[k] - cam.x); cpy[n] = (float) (ty[k] - cam.y); cpz[n] = (float) (tz[k] - cam.z);
            cpa[n] = a * a * (3.0f - 2.0f * a);
            n++;
        }
        float lx = (float) (live.x - cam.x), ly = (float) (live.y + 0.12 - cam.y), lz = (float) (live.z - cam.z);
        float ddx = lx - cpx[n - 1], ddy = ly - cpy[n - 1], ddz = lz - cpz[n - 1];
        if (ddx * ddx + ddy * ddy + ddz * ddz > 1.0e-6f) {
            cpx[n] = lx; cpy[n] = ly; cpz[n] = lz; cpa[n] = 1.0f; n++;
        } else {
            cpa[n - 1] = 1.0f;
        }
        if (n < 2) return;

        // 2. Catmull-Rom smoothing
        int s = 0;
        for (int seg = 0; seg < n - 1; seg++) {
            int i0 = Math.max(seg - 1, 0), i1 = seg, i2 = seg + 1, i3 = Math.min(seg + 2, n - 1);
            for (int j = 0; j < SUB; j++) {
                float t = j / (float) SUB;
                spx[s] = catmull(cpx[i0], cpx[i1], cpx[i2], cpx[i3], t);
                spy[s] = catmull(cpy[i0], cpy[i1], cpy[i2], cpy[i3], t);
                spz[s] = catmull(cpz[i0], cpz[i1], cpz[i2], cpz[i3], t);
                spa[s] = cpa[i1] + (cpa[i2] - cpa[i1]) * t;
                s++;
            }
        }
        spx[s] = cpx[n - 1]; spy[s] = cpy[n - 1]; spz[s] = cpz[n - 1]; spa[s] = cpa[n - 1];
        s++;

        // 3. camera-facing side vectors + near-camera fade
        float prevX = 0.0f, prevY = 1.0f, prevZ = 0.0f;
        for (int k = 0; k < s; k++) {
            int a = Math.max(k - 1, 0), b = Math.min(k + 1, s - 1);
            float tdx = spx[b] - spx[a], tdy = spy[b] - spy[a], tdz = spz[b] - spz[a];
            // side = tangent x view, where view is the vector from the camera (origin) to the sample
            float sx = tdy * spz[k] - tdz * spy[k];
            float sy = tdz * spx[k] - tdx * spz[k];
            float sz = tdx * spy[k] - tdy * spx[k];
            float len = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
            if (len < 1.0e-5f) { sx = prevX; sy = prevY; sz = prevZ; }
            else { sx /= len; sy /= len; sz /= len; }
            // keep the ribbon orientation continuous along the path
            if (k > 0 && sx * prevX + sy * prevY + sz * prevZ < 0.0f) { sx = -sx; sy = -sy; sz = -sz; }
            sdx[k] = sx; sdy[k] = sy; sdz[k] = sz;
            prevX = sx; prevY = sy; prevZ = sz;
            float dist = (float) Math.sqrt(spx[k] * spx[k] + spy[k] * spy[k] + spz[k] * spz[k]);
            sfa[k] = spa[k] * clamp01((dist - 0.9f) / 1.6f);
        }

        // 4. draw
        Matrix4f m = matrices.peek().getPositionMatrix();
        VertexConsumer vc = consumers.getBuffer(RenderLayer.getLightning());
        int rgb = NullVisualSettings.trailColorRgb();
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        float width = NullVisualSettings.trailWidth();
        boolean glow = NullVisualSettings.trailGlow();

        if (glow) {
            float brightness = NullVisualSettings.trailGlowBrightness() / 100.0f;
            ribbon3(vc, m, s, width * 3.4f, r, g, b, 0.55f * brightness);
        }
        int coreR = (r + 255) / 2, coreG = (g + 255) / 2, coreB = (b + 255) / 2;
        float coreHalf = width * (glow ? 0.28f : 0.20f);
        ribbon1(vc, m, s, Math.max(coreHalf, 0.012f), coreR, coreG, coreB, glow ? 0.95f : 0.90f);

        if (consumers instanceof VertexConsumerProvider.Immediate immediate) immediate.draw(RenderLayer.getLightning());
    }

    /** Soft ribbon: transparent edges and a bright centre line, which gives the glow its falloff. */
    private static void ribbon3(VertexConsumer vc, Matrix4f m, int s, float fullWidth, int r, int g, int b, float strength) {
        for (int k = 0; k < s - 1; k++) {
            float w0 = fullWidth * 0.5f * (0.35f + 0.65f * spa[k]);
            float w1 = fullWidth * 0.5f * (0.35f + 0.65f * spa[k + 1]);
            int a0 = (int) (sfa[k] * strength * 255.0f), a1 = (int) (sfa[k + 1] * strength * 255.0f);
            if (a0 <= 0 && a1 <= 0) continue;
            float l0x = spx[k] + sdx[k] * w0, l0y = spy[k] + sdy[k] * w0, l0z = spz[k] + sdz[k] * w0;
            float r0x = spx[k] - sdx[k] * w0, r0y = spy[k] - sdy[k] * w0, r0z = spz[k] - sdz[k] * w0;
            float l1x = spx[k + 1] + sdx[k + 1] * w1, l1y = spy[k + 1] + sdy[k + 1] * w1, l1z = spz[k + 1] + sdz[k + 1] * w1;
            float r1x = spx[k + 1] - sdx[k + 1] * w1, r1y = spy[k + 1] - sdy[k + 1] * w1, r1z = spz[k + 1] - sdz[k + 1] * w1;
            quad(vc, m, l0x, l0y, l0z, 0, spx[k], spy[k], spz[k], a0, spx[k + 1], spy[k + 1], spz[k + 1], a1, l1x, l1y, l1z, 0, r, g, b);
            quad(vc, m, spx[k], spy[k], spz[k], a0, r0x, r0y, r0z, 0, r1x, r1y, r1z, 0, spx[k + 1], spy[k + 1], spz[k + 1], a1, r, g, b);
        }
    }

    /** Solid thin ribbon of constant alpha across its width (the bright core line). */
    private static void ribbon1(VertexConsumer vc, Matrix4f m, int s, float halfWidth, int r, int g, int b, float strength) {
        for (int k = 0; k < s - 1; k++) {
            float w0 = halfWidth * (0.45f + 0.55f * spa[k]);
            float w1 = halfWidth * (0.45f + 0.55f * spa[k + 1]);
            int a0 = (int) (sfa[k] * strength * 255.0f), a1 = (int) (sfa[k + 1] * strength * 255.0f);
            if (a0 <= 0 && a1 <= 0) continue;
            quad(vc, m,
                spx[k] + sdx[k] * w0, spy[k] + sdy[k] * w0, spz[k] + sdz[k] * w0, a0,
                spx[k] - sdx[k] * w0, spy[k] - sdy[k] * w0, spz[k] - sdz[k] * w0, a0,
                spx[k + 1] - sdx[k + 1] * w1, spy[k + 1] - sdy[k + 1] * w1, spz[k + 1] - sdz[k + 1] * w1, a1,
                spx[k + 1] + sdx[k + 1] * w1, spy[k + 1] + sdy[k + 1] * w1, spz[k + 1] + sdz[k + 1] * w1, a1,
                r, g, b);
        }
    }

    /** One quad, emitted double-sided so it is visible whatever the face culling state of the render layer is. */
    private static void quad(VertexConsumer vc, Matrix4f m,
                             float x0, float y0, float z0, int a0, float x1, float y1, float z1, int a1,
                             float x2, float y2, float z2, int a2, float x3, float y3, float z3, int a3,
                             int r, int g, int b) {
        vc.vertex(m, x0, y0, z0).color(r, g, b, a0);
        vc.vertex(m, x1, y1, z1).color(r, g, b, a1);
        vc.vertex(m, x2, y2, z2).color(r, g, b, a2);
        vc.vertex(m, x3, y3, z3).color(r, g, b, a3);
        vc.vertex(m, x3, y3, z3).color(r, g, b, a3);
        vc.vertex(m, x2, y2, z2).color(r, g, b, a2);
        vc.vertex(m, x1, y1, z1).color(r, g, b, a1);
        vc.vertex(m, x0, y0, z0).color(r, g, b, a0);
    }

    private static float catmull(float p0, float p1, float p2, float p3, float t) {
        float t2 = t * t, t3 = t2 * t;
        return 0.5f * ((2.0f * p1) + (-p0 + p2) * t + (2.0f * p0 - 5.0f * p1 + 4.0f * p2 - p3) * t2 + (-p0 + 3.0f * p1 - 3.0f * p2 + p3) * t3);
    }

    private static float clamp01(float v) { return v < 0.0f ? 0.0f : Math.min(v, 1.0f); }
}
