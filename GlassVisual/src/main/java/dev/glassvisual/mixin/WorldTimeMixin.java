package dev.glassvisual.mixin;

import dev.glassvisual.GlassVisualClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps the visual time fixed without writing to client world properties every tick.
 * Server time packets and the world's internal clock remain untouched.
 */
@Mixin(World.class)
public abstract class WorldTimeMixin {
    @Inject(method = "getTimeOfDay", at = @At("RETURN"), cancellable = true)
    private void null$keepNight(CallbackInfoReturnable<Long> cir) {
        if ((Object) this instanceof ClientWorld && GlassVisualClient.MODULES.timeChanger) {
            cir.setReturnValue(18000L);
        }
    }
}
