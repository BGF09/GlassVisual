package dev.glassvisual;

import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public final class TargetHudRenderer {
    private static final int PANEL=0xF21A202D, MUTED=0xFF9AA8BD, TEXT=0xFFF3F5FB;
    private static UUID previousTarget;
    private static int ghostTicks;
    private static float smoothHealth=-1, ghostHealth=-1;
    private TargetHudRenderer(){}

    public static void render(DrawContext ctx,MinecraftClient client,LivingEntity target){
        int x=client.getWindow().getScaledWidth()/2+16;
        int y=client.getWindow().getScaledHeight()/2+34;
        int w=204,h=76,accent=NullTheme.accent();
        float health=Math.max(0,target.getHealth()),maxHealth=Math.max(1,target.getMaxHealth());
        float ratio=MathHelper.clamp(health/maxHealth,0.0f,1.0f);
        if(previousTarget==null||!previousTarget.equals(target.getUuid())){
            previousTarget=target.getUuid();ghostTicks=20;ghostHealth=ratio;smoothHealth=ratio;
        }
        if(smoothHealth<0)smoothHealth=ratio;
        smoothHealth+=(ratio-smoothHealth)*0.20f;
        if(ghostTicks>0)ghostTicks--;
        double distance=client.player==null?0:client.player.distanceTo(target);
        roundedFill(ctx,x-3,y-3,w+6,h+6,12,0x55000000);
        roundedFill(ctx,x,y,w,h,10,PANEL);
        roundedFill(ctx,x+1,y+1,w-2,h-2,9,0xFF202938);
        roundedFill(ctx,x,y,4,h,2,accent);
        ctx.fill(x+12,y+8,x+w-12,y+9,0x334B5B72);

        boolean skinDrawn=false;
        if(target instanceof PlayerEntity player&&client.getNetworkHandler()!=null){
            PlayerListEntry entry=client.getNetworkHandler().getPlayerListEntry(player.getUuid());
            if(entry!=null){SkinTextures skin=entry.getSkinTextures();PlayerSkinDrawer.draw(ctx,skin,x+12,y+17,38);skinDrawn=true;}
        }
        if(!skinDrawn){
            roundedFill(ctx,x+12,y+17,38,38,8,0xFF303D52);
            roundedFill(ctx,x+21,y+20,20,19,8,0xFF8796AE);
            roundedFill(ctx,x+17,y+37,28,14,6,accent);
        }
        String name=target.getName().getString();
        if(name.length()>18)name=name.substring(0,17)+"…";
        ctx.drawText(client.textRenderer,Text.literal(name),x+60,y+14,TEXT,true);
        String type=target instanceof PlayerEntity?"PLAYER":"ENTITY";
        int tagW=client.textRenderer.getWidth(type)+12;
        roundedFill(ctx,x+w-tagW-11,y+12,tagW,15,6,0xFF303B50);
        ctx.drawText(client.textRenderer,type,x+w-tagW-5,y+16,accent,true);

        int hpColor=ratio>0.6f?0xFF55D69A:ratio>0.3f?0xFFFFC56B:0xFFFF6B78;
        String hp=String.format(java.util.Locale.ROOT,"%.1f / %.0f HP",health,maxHealth);
        ctx.drawText(client.textRenderer,hp,x+60,y+34,hpColor,true);
        String range=String.format(java.util.Locale.ROOT,"%.1fm away",distance);
        ctx.drawText(client.textRenderer,range,x+60,y+48,MUTED,false);

        int bx=x+12,by=y+63,bw=w-24;
        roundedFill(ctx,bx,by,bw,6,3,0xFF354155);
        int ghostWidth=Math.round(bw*MathHelper.clamp(ghostHealth,0,1));
        if(ghostWidth>0)roundedFill(ctx,bx,by,ghostWidth,6,3,0x667F8CA8);
        int fill=Math.round(bw*MathHelper.clamp(smoothHealth,0,1));
        if(fill>0)roundedFill(ctx,bx,by,fill,6,3,hpColor);
        if(ghostTicks>0){
            int alpha=Math.min(90,ghostTicks*4);
            ctx.fill(x+w-8,y+3,x+w-5,y+6,(alpha<<24)|(accent&0x00FFFFFF));
        }
    }
    private static void roundedFill(DrawContext ctx,int x,int y,int w,int h,int radius,int color){
        if(w<=0||h<=0)return;
        int r=Math.max(1,Math.min(radius,Math.min(w,h)/2));
        ctx.fill(x+r,y,x+w-r,y+h,color);
        for(int row=0;row<r;row++){
            int dy=r-row-1;
            int inset=r-(int)Math.floor(Math.sqrt(Math.max(0,r*r-dy*dy)));
            ctx.fill(x+inset,y+row,x+w-inset,y+row+1,color);
            ctx.fill(x+inset,y+h-1-row,x+w-inset,y+h-row,color);
        }
    }
}
