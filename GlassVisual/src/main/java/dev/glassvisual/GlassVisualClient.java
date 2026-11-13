package dev.glassvisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public final class GlassVisualClient implements ClientModInitializer {
    public static final String MOD_ID="glassvisual";
    public static final ModuleState MODULES=new ModuleState();
    private static KeyBinding openClickGui;
    @Override public void onInitializeClient(){
        openClickGui=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.glassvisual.open_gui",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_SHIFT,"category.glassvisual"));
        ScreenEvents.AFTER_INIT.register((client,screen,scaledWidth,scaledHeight)->{
            if(screen instanceof TitleScreen) client.setScreen(new NullMainMenuScreen());
        });
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            NullParticleRenderer.tick(client);
            TargetNotifier.tick(client);
            while(openClickGui.wasPressed()){
                if(client.currentScreen==null)client.setScreen(new ClickGuiScreen());
                else if(client.currentScreen instanceof ClickGuiScreen)client.setScreen(null);
            }
        });
        HudRenderCallback.EVENT.register((context,tickCounter)->{
            MinecraftClient client=MinecraftClient.getInstance();
            if(client.player==null||client.world==null||client.currentScreen!=null)return;
            if(MODULES.targetHud&&client.crosshairTarget instanceof EntityHitResult hit&&hit.getEntity() instanceof LivingEntity target&&target.isAlive())
                TargetHudRenderer.render(context,client,target);
            HudOverlayRenderer.render(context,client);
            Notifier.render(context,client);
        });
    }
    public static final class ModuleState {
        public boolean targetHud=true,keystrokes=false,coordinates=false,fps=false,glowAccent=true;
    }
}
