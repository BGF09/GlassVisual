package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NullAccountScreen extends Screen {
    private final Screen parent;
    public NullAccountScreen(Screen parent){super(Text.literal("Account Manager"));this.parent=parent;}
    @Override protected void init(){
        addDrawableChild(ButtonWidget.builder(Text.literal("BACK"),b->client.setScreen(parent))
            .dimensions(width/2-55,height/2+35,110,24).build());
    }
    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta){
        ctx.fillGradient(0,0,width,height,0xFF090B11,0xFF151A27);
        ctx.fill(width/2-170,height/2-55,width/2+170,height/2+75,0xEE111722);
        ctx.fill(width/2-170,height/2-55,width/2+170,height/2-52,NullTheme.accent());
        ctx.drawCenteredTextWithShadow(textRenderer,"ACCOUNT MANAGER",width/2,height/2-35,0xFFF4F6FC);
        ctx.drawCenteredTextWithShadow(textRenderer,"Manage accounts through your Minecraft launcher.",width/2,height/2-12,0xFF9AA6B2);
        super.render(ctx,mouseX,mouseY,delta);
    }
    @Override public boolean shouldPause(){return false;}
}
