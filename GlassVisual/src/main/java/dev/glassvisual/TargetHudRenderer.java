package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public final class TargetHudRenderer {
    private static final int PANEL=0xD910101B,MUTED=0xFFAAA6BB;
    private TargetHudRenderer(){}
    public static void render(DrawContext ctx,MinecraftClient client,LivingEntity target){
        int x=client.getWindow().getScaledWidth()/2+12,y=client.getWindow().getScaledHeight()/2+30,w=166,h=58,a=NullTheme.accent();
        float health=Math.max(0,target.getHealth()),max=Math.max(1,target.getMaxHealth()),ratio=MathHelper.clamp(health/max,0.0f,1.0f);
        double distance=client.player.distanceTo(target);
        ctx.fill(x-2,y-2,x+w+2,y+h+2,0x55434B64);ctx.fill(x,y,x+w,y+h,PANEL);ctx.fill(x,y,x+w,y+2,a);
        ctx.fill(x+8,y+9,x+40,y+41,0xFF292438);ctx.fill(x+17,y+14,x+31,y+28,0xFFB8A0D5);ctx.fill(x+13,y+29,x+35,y+38,a);
        String name=target.getName().getString();if(name.length()>16)name=name.substring(0,16)+"…";
        ctx.drawText(client.textRenderer,Text.literal(name),x+47,y+9,0xFFFFFFFF,true);
        ctx.drawText(client.textRenderer,Text.literal(String.format("%.1f HP  •  %.1fm",health,distance)),x+47,y+22,MUTED,false);
        int bx=x+47,by=y+39,bw=108,fill=Math.round(bw*ratio);
        ctx.fill(bx,by,bx+bw,by+7,0xFF342B3E);if(fill>0)ctx.fill(bx,by,bx+fill,by+7,a);
        ctx.drawText(client.textRenderer,Text.literal("NULL"),x+w-38,y+9,a,false);
    }
}
