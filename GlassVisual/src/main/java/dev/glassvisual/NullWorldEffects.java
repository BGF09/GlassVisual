package dev.glassvisual;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/** NULL-owned world effects rendered through Fabric's world buffers. */
public final class NullWorldEffects {
    private static final Random RANDOM = new Random();
    private static final ArrayDeque<TrailPoint> TRAIL = new ArrayDeque<>();
    private static final List<Spark> SPARKS = new ArrayList<>();
    private static ClientWorld lastWorld;
    private static Vec3d lastPlayerPos;
    private static int ticks;
    private NullWorldEffects() {}

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) { clear(); return; }
        if (lastWorld != client.world) { clear(); lastWorld = client.world; }
        Vec3d playerPos = client.player.getPos();
        if (NullUiSettings.playerTrail) {
            if (lastPlayerPos == null || playerPos.distanceTo(lastPlayerPos) > 8.0) TRAIL.clear();
            if (lastPlayerPos == null || playerPos.squaredDistanceTo(lastPlayerPos) > 0.0064) {
                TRAIL.addLast(new TrailPoint(playerPos.add(0, 0.12, 0), 0));
                while (TRAIL.size() > 100) TRAIL.removeFirst();
            }
        } else TRAIL.clear();
        lastPlayerPos = playerPos;

        if (NullUiSettings.worldParticles && ++ticks % 2 == 0 && SPARKS.size() < 110) {
            for (int i = 0; i < 2 && SPARKS.size() < 110; i++) {
                double angle = RANDOM.nextDouble() * Math.PI * 2.0;
                double radius = 0.7 + RANDOM.nextDouble() * 2.8;
                Vec3d spawn = playerPos.add(Math.cos(angle) * radius, 0.25 + RANDOM.nextDouble() * 2.1,
                    Math.sin(angle) * radius);
                SPARKS.add(new Spark(spawn, (RANDOM.nextDouble() - 0.5) * 0.012,
                    0.008 + RANDOM.nextDouble() * 0.018, (RANDOM.nextDouble() - 0.5) * 0.012,
                    50 + RANDOM.nextInt(55), 0.025 + RANDOM.nextDouble() * 0.025));
            }
        }
        Iterator<Spark> iterator = SPARKS.iterator();
        while (iterator.hasNext()) {
            Spark spark = iterator.next();
            spark.position = spark.position.add(spark.vx, spark.vy, spark.vz);
            spark.vy -= 0.00035; spark.age++;
            if (spark.age >= spark.life || spark.position.squaredDistanceTo(playerPos) > 144.0) iterator.remove();
        }
        Iterator<TrailPoint> trailIterator = TRAIL.iterator();
        while (trailIterator.hasNext()) {
            TrailPoint point = trailIterator.next();
            point.age++;
            if (point.age > 115) trailIterator.remove();
        }
    }

    public static void render(WorldRenderContext context) {
        if (context.matrixStack() == null || context.consumers() == null) return;
        if ((!NullUiSettings.playerTrail || TRAIL.size() < 2)
                && (!NullUiSettings.worldParticles || SPARKS.isEmpty())) return;
        MatrixStack matrices = context.matrixStack();
        Vec3d camera = context.camera().getPos();
        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
        int accent = NullTheme.accent();
        int red = (accent >> 16) & 255, green = (accent >> 8) & 255, blue = accent & 255;

        if (NullUiSettings.playerTrail && TRAIL.size() >= 2) {
            TrailPoint previous = null;
            for (TrailPoint point : TRAIL) {
                if (previous != null) {
                    int alpha = Math.max(0, Math.min(210,
                        (int) (210.0 * (1.0 - Math.max(previous.age, point.age) / 116.0))));
                    if (alpha > 0) line(lines, matrix, previous.position, point.position, red, green, blue, alpha);
                }
                previous = point;
            }
        }
        if (NullUiSettings.worldParticles) {
            for (Spark spark : SPARKS) {
                float fade = 1.0f - spark.age / (float) spark.life;
                int alpha = Math.max(0, Math.min(190, (int) (190 * fade)));
                if (alpha == 0) continue;
                double s = spark.size; Vec3d p = spark.position;
                line(lines, matrix, p.add(-s, 0, 0), p.add(s, 0, 0), red, green, blue, alpha);
                line(lines, matrix, p.add(0, -s, 0), p.add(0, s, 0), red, green, blue, alpha);
                line(lines, matrix, p.add(0, 0, -s), p.add(0, 0, s), red, green, blue, alpha);
            }
        }
        matrices.pop();
    }

    private static void line(VertexConsumer consumer, Matrix4f matrix, Vec3d a, Vec3d b,
                             int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, (float) a.x, (float) a.y, (float) a.z).color(red, green, blue, alpha).normal(0, 1, 0);
        consumer.vertex(matrix, (float) b.x, (float) b.y, (float) b.z).color(red, green, blue, alpha).normal(0, 1, 0);
    }
    private static void clear() { TRAIL.clear(); SPARKS.clear(); lastPlayerPos = null; lastWorld = null; ticks = 0; }
    private static final class TrailPoint {
        final Vec3d position; int age;
        TrailPoint(Vec3d position, int age) { this.position = position; this.age = age; }
    }
    private static final class Spark {
        Vec3d position; final double vx, vz, size; double vy; final int life; int age;
        Spark(Vec3d position, double vx, double vy, double vz, int life, double size) {
            this.position = position; this.vx = vx; this.vy = vy; this.vz = vz; this.life = life; this.size = size;
        }
    }
}