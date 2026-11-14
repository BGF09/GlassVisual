package dev.glassvisual.mixin;
import dev.glassvisual.GlassVisualClient;
import dev.glassvisual.NullHandSettings;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
 @Inject(method="renderFirstPersonItem",at=@At("HEAD"))
 private void null$moveFirstPersonHand(AbstractClientPlayerEntity player,float tickProgress,float pitch,Hand hand,float swingProgress,ItemStack item,float equipProgress,MatrixStack matrices,VertexConsumerProvider vertexConsumers,int light,CallbackInfo ci){
  if(GlassVisualClient.MODULES.handPosition)NullHandSettings.apply(matrices);
 }
}
