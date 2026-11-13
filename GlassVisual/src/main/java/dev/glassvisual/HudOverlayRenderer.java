package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.BlockPos;

public final class HudOverlayRenderer {
    private static final int PANEL=0xD9101420,TEXT=0xFFF3F5FC,MUTED=0xFF9CA7BC;
    private HudOverlayRenderer(){}
    public static void render(DrawContext ctx,MinecraftClient client){
        int accent=NullTheme.accent(),w=client.getWindow().getScaledWidth(),h=client.getWindow().getScaledHeight();
        ctx.fill(12,12,126,38,PANEL); ctx.fill(12,12,15,38,accent); ctx.fill(15,12,126,14,0x55FFFFFF);
        ctx.drawText(client.textRenderer,"NULL",23,17,TEXT,true); ctx.drawText(client.textRenderer,"CLIENT",59,18,MUTED,false);
        if(GlassVisualClient.MODULES.fps){String fps=client.getCurrentFps()+" FPS";int x=w-client.textRenderer.getWidth(fps)-24;
            ctx.fill(x-10,12,w-12,38,PANEL);ctx.fill(x-10,12,x-7,38,accent);ctx.drawText(client.textRenderer,fps,x,21,TEXT,true);}
        if(GlassVisualClient.MODULES.coordinates&&client.player!=null){BlockPos p=client.player.getBlockPos();
            String s="XYZ  "+p.getX()+"  /  "+p.getY()+"  /  "+p.getZ();int bw=client.textRenderer.getWidth(s)+24;
            ctx.fill(12,h-34,12+bw,h-10,PANEL);ctx.fill(12,h-34,15,h-10,accent);ctx.drawText(client.textRenderer,s,23,h-26,TEXT,false);}
        if(GlassVisualClient.MODULES.keystrokes&&client.player!=null){int x=12,y=h-102;
            drawKey(ctx,client,"W",client.options.forwardKey,x+24,y,accent);drawKey(ctx,client,"A",client.options.leftKey,x,y+24,accent);
            drawKey(ctx,client,"S",client.options.backKey,x+24,y+24,accent);drawKey(ctx,client,"D",client.options.rightKey,x+48,y+24,accent);
            drawKey(ctx,client,"JUMP",client.options.jumpKey,x,y+48,accent,72);}
    }
    private static void drawKey(DrawContext c,MinecraftClient m,String l,KeyBinding k,int x,int y,int a){drawKey(c,m,l,k,x,y,a,20);}
    private static void drawKey(DrawContext c,MinecraftClient m,String l,KeyBinding k,int x,int y,int a,int w){
        boolean p=k.isPressed();c.fill(x,y,x+w,y+20,p?0xE53A4058:PANEL);c.fill(x,y,x+w,y+2,p?a:0x665E687F);
        c.drawText(m.textRenderer,l,x+(w-m.textRenderer.getWidth(l))/2,y+6,p?0xFFFFFFFF:MUTED,true);
    }
}
