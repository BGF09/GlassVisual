package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/** Optional, gently smoothed aim correction while the attack key is held. */
public final class NullAimAssist {
    private static final double MAX_DISTANCE = 4.5;
    private static final double MAX_ANGLE = 18.0;
    private static final float SMOOTHING = 0.12f;

    private NullAimAssist() {}

    public static void tick(MinecraftClient client) {
        if (!GlassVisualClient.MODULES.aimAssist || client.player == null || client.world == null
                || !client.options.attackKey.isPressed()) return;

        PlayerEntity player = client.player;
        Vec3d look = player.getRotationVec(1.0f);
        PlayerEntity best = null;
        double bestAngle = MAX_ANGLE;

        for (PlayerEntity candidate : client.world.getPlayers()) {
            if (candidate == player || !candidate.isAlive() || candidate.isSpectator()
                    || player.isTeammate(candidate) || candidate.isInvisibleTo(player)
                    || !player.canSee(candidate, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, candidate.getEyeY())) continue;

            Vec3d direction = candidate.getEyePos().subtract(player.getEyePos());
            double distance = direction.length();
            if (distance < 0.1 || distance > MAX_DISTANCE) continue;

            double dot = MathHelper.clamp((float) look.dotProduct(direction.normalize()), -1.0f, 1.0f);
            double angle = Math.toDegrees(Math.acos(dot));
            if (angle < bestAngle) {
                bestAngle = angle;
                best = candidate;
            }
        }

        if (best == null) return;
        Vec3d delta = best.getEyePos().subtract(player.getEyePos());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float targetYaw = (float) (Math.atan2(delta.z, delta.x) * 180.0 / Math.PI) - 90.0f;
        float targetPitch = (float) -(Math.atan2(delta.y, horizontal) * 180.0 / Math.PI);

        float yaw = player.getYaw() + MathHelper.wrapDegrees(targetYaw - player.getYaw()) * SMOOTHING;
        float pitch = player.getPitch() + MathHelper.wrapDegrees(targetPitch - player.getPitch()) * SMOOTHING;
        player.setYaw(yaw);
        player.setPitch(MathHelper.clamp(pitch, -90.0f, 90.0f));
    }
}
