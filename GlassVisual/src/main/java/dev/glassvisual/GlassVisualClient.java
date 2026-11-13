package dev.glassvisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.client.world.ClientWorld;
import org.lwjgl.glfw.GLFW;

public final class GlassVisualClient implements ClientModInitializer {
    public static final String MOD_ID = "glassvisual";
    public static final ModuleState MODULES = new ModuleState();
    private static KeyBinding openClickGui;
    private static KeyBinding openHudEditor;

    @Override
    public void onInitializeClient() {
        GlassVisualConfig.load();
        openClickGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.glassvisual.open_gui", InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT, "category.glassvisual"
        ));
        openHudEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.glassvisual.hud_editor", InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_ALT, "category.glassvisual"
        ));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) client.setScreen(new NullMainMenuScreen());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            NullAimAssist.tick(client);
            if (MODULES.timeChanger && client.world != null) {
                ((ClientWorld.Properties) client.world.getLevelProperties()).setTimeOfDay(6000L);
            }
            if (MODULES.autoSprint && client.player != null) {
                var player = client.player;
                boolean canSprint = player.input.movementForward > 0.0F
                    && !player.isSneaking() && !player.horizontalCollision
                    && player.getHungerManager().getFoodLevel() > 6;
                player.setSprinting(canSprint);
            }
            NullMovementTracker.tick(client);
            NullHitVisuals.tick(client);
            NullParticleRenderer.tick(client);
            TargetNotifier.tick(client);
            while (openHudEditor.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new HudEditorScreen());
            }
            while (openClickGui.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
                else if (client.currentScreen instanceof ClickGuiScreen) client.setScreen(null);
            }
        });

        WorldRenderEvents.BLOCK_OUTLINE.register((context, outline) -> {
            if (!MODULES.blockOutline || context.matrixStack() == null || context.consumers() == null) return true;
            MatrixStack matrices = context.matrixStack();
            var pos = outline.blockPos();
            var shape = outline.blockState().getOutlineShape(context.world(), pos);
            matrices.push();
            matrices.translate(-outline.cameraX(), -outline.cameraY(), -outline.cameraZ());
            VertexRendering.drawOutline(matrices, context.consumers().getBuffer(RenderLayer.getLines()),
                shape, pos.getX(), pos.getY(), pos.getZ(), NullTheme.accent());
            matrices.pop();
            return false;
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null || client.currentScreen != null) return;
            if (MODULES.targetHud && client.crosshairTarget instanceof EntityHitResult hit
                    && hit.getEntity() instanceof LivingEntity target && target.isAlive()) {
                TargetHudRenderer.render(context, client, target);
            }
            if (MODULES.hitVisuals) NullHitVisuals.render(context, client);
            NullParticleRenderer.render(context, client);
            HudOverlayRenderer.render(context, client);
            if (MODULES.notifications) Notifier.render(context, client);
        });
    }

    public static final class ModuleState {
        public boolean targetHud = true;
        public boolean keystrokes;
        public boolean coordinates;
        public boolean fps;
        public boolean glowAccent = true;
        public boolean arrayList = true;
        public boolean armorHud;
        public boolean potionHud;
        public boolean ping;
        public boolean autoSprint;
        public boolean sprintDisplay;
        public boolean jumpStats;
        public boolean speedGraph;
        public boolean sessionInfo;
        public boolean clock;
        public boolean hitVisuals;
        public boolean combatHud;
        public boolean worldTint;
        public boolean crosshair;
        public boolean durability;
        public boolean inventory;
        public boolean notifications = true;
        public boolean armorStatus;
        public boolean timeChanger;
        public boolean blockOutline;
        public boolean aimAssist;
    }
}
