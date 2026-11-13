package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.Vec3d;

public final class NullParticleRenderer {
    private static int tick;
    private NullParticleRenderer() {}
    public static void tick(MinecraftClient client) {
        if (!NullUiSettings.particles || client.player == null || client.world == null) return;
        if (++tick % 3 != 0) return;
        Vec3d p = client.player.getPos();
        DustParticleEffect dust = new DustParticleEffect(NullTheme.accent() & 0x00FFFFFF, 0.85f);
        for (int i = 0; i < 2; i++) {
            double angle = client.world.random.nextDouble() * Math.PI * 2.0;
            double radius = 0.35 + client.world.random.nextDouble() * 0.55;
            double x = p.x + Math.cos(angle) * radius;
            double y = p.y + 0.25 + client.world.random.nextDouble() * 1.35;
            double z = p.z + Math.sin(angle) * radius;
            client.world.addParticle(dust, x, y, z, -Math.cos(angle) * 0.012,
                0.018 + client.world.random.nextDouble() * 0.018, -Math.sin(angle) * 0.012);
        }
    }
}
