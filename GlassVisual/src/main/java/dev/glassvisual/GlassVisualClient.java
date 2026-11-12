package dev.glassvisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public final class GlassVisualClient implements ClientModInitializer {
    public static final String MOD_ID = "glassvisual";
    public static final ModuleState MODULES = new ModuleState();

    private static KeyBinding openClickGui;

    @Override
    public void onInitializeClient() {
        openClickGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.glassvisual.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.glassvisual"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openClickGui.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGuiScreen());
                } else if (client.currentScreen instanceof ClickGuiScreen) {
                    client.setScreen(null);
                }
            }
        });

        // HudRenderCallback is deprecated in newer Fabric API versions, but remains
        // available in Fabric API 0.119.2 for Minecraft 1.21.4.
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!MODULES.targetHud || client.player == null || client.world == null
                    || client.currentScreen != null) {
                return;
            }

            if (client.crosshairTarget instanceof EntityHitResult hit
                    && hit.getEntity() instanceof LivingEntity target
                    && target.isAlive()) {
                TargetHudRenderer.render(context, client, target);
            }
        });
    }

    public static final class ModuleState {
        public boolean targetHud = true;
        public boolean clickGui = true;
        public boolean keystrokes = false;
        public boolean coordinates = false;
        public boolean fps = false;
        public boolean glowAccent = true;
    }
}
