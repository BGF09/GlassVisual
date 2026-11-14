package dev.glassvisual.mixin;

import dev.glassvisual.NullUiSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * NULL Screen Stretch.
 *
 * <p>Minecraft builds the 3D perspective projection in {@code GameRenderer#getBasicProjectionMatrix(float)}.
 * The GUI, HUD, inventory and menus use their own orthographic matrices and never touch this method, so scaling
 * the X axis of this matrix stretches only the rendered world (and the held item), exactly like a stretched
 * resolution in competitive shooters. The window size, framebuffer size, fullscreen state and GUI scale are untouched.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"))
    private void glassvisual$stretchWorldProjection(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        float stretch = NullUiSettings.stretchFactor(MinecraftClient.getInstance());
        if (Math.abs(stretch - 1.0f) < 1.0e-3f) return;
        cir.getReturnValue().scale(stretch, 1.0f, 1.0f);
    }
}
