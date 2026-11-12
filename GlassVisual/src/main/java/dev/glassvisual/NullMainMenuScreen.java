package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NullMainMenuScreen extends Screen {
    public NullMainMenuScreen() { super(Text.literal("NULL")); }
    @Override protected void init() {
        int center = width / 2, y = height / 2 + 28;
        int bw = Math.min(230, width - 40), x = center - bw / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("SINGLEPLAYER"), b -> client.setScreen(new SelectWorldScreen(this))).dimensions(x,y,bw,24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("MULTIPLAYER"), b -> client.setScreen(new MultiplayerScreen(this))).dimensions(x,y+31,bw,24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("OPTIONS"), b -> client.setScreen(new OptionsScreen(this,client.options))).dimensions(x,y+62,bw,24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("QUIT"), b -> client.scheduleStop()).dimensions(x,y+93,bw,24).build());
    }
    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        int accent=NullTheme.accent();
        ctx.fillGradient(0,0,width,height,0xFF0A0C14,0xFF151A29);
        ctx.fill(0,height/2-90,width,height/2-86,0x443B4560);
        ctx.fill(0,height/2-90,width,height/2-88,accent);
        ctx.fill(width/2-150,height/2-126,width/2+150,height/2+151,0xAA101521);
        ctx.fill(width/2-150,height/2-126,width/2+150,height/2-123,accent);
        ctx.drawCenteredTextWithShadow(textRenderer,"NULL",width/2,height/2-100,0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,"VISUAL CLIENT",width/2,height/2-78,accent);
        ctx.drawCenteredTextWithShadow(textRenderer,"CLEAN  •  MINIMAL  •  YOUR STYLE",width/2,height/2-60,0xFF9CA3B8);
        super.render(ctx,mouseX,mouseY,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,"NULL CLIENT  /  1.21.4",width/2,height-18,0xFF737D94);
    }
    @Override public boolean shouldPause(){return false;}
}
