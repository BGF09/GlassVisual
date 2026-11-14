package dev.glassvisual.mixin;
import dev.glassvisual.GlassVisualClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
 @Inject(method="renderCrosshair",at=@At("HEAD"),cancellable=true)
 private void null$replaceVanillaCrosshair(DrawContext context,RenderTickCounter tickCounter,CallbackInfo ci){if(GlassVisualClient.MODULES.crosshair)ci.cancel();}
 @Inject(method="renderHotbar",at=@At("HEAD"),cancellable=true)
 private void null$replaceVanillaHotbar(DrawContext context,RenderTickCounter tickCounter,CallbackInfo ci){if(GlassVisualClient.MODULES.inventory)ci.cancel();}
}
