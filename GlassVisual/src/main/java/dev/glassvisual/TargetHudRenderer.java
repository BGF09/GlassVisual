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
    private static final int PANEL=0xE00D1018, MUTED=0xFFAAA6BB;
    private static UUID previousTarget;
    private static int ghostTicks;
    private static float smoothHealth=-1, ghostHealth=-1;
    private TargetHudRenderer(){}

    public static void render(DrawContext ctx,MinecraftClient client,LivingEntity target){
        int x=client.getWindow().getScaledWidth()/2+14;
        int y=client.getWindow().getScaledHeight()/2+34;
        int w=184,h=66,accent=NullTheme.accent();
        float health=Math.max(0,target.getHealth()),maxHealth=Math.max(1,target.getMaxHealth());
        float ratio=MathHelper.clamp(health/maxHealth,0.0f,1.0f);
        if(previousTarget==null||!previousTarget.equals(target.getUuid())){
            previousTarget=target.getUuid();ghostTicks=18;ghostHealth=ratio;smoothHealth=ratio;
        }
        if(smoothHealth<0)smoothHealth=ratio;
        smoothHealth+= (ratio-smoothHealth)*0.22f;
        if(ghostTicks>0)ghostTicks--;
        double distance=client.player.distanceTo(target);

        ctx.fill(x-3,y-3,x+w+3,y+h+3,0x44000000);
        ctx.fill(x,y,x+w,y+h,PANEL);
        ctx.fill(x,y,x+w,y+2,accent);
        // A soft ghost echo appears briefly when the tracked target changes.
        if(ghostTicks>0){
            int alpha=Math.min(75,ghostTicks*4);
            int ghost=(alpha<<24)|(accent&0x00FFFFFF);
            ctx.fill(x+w-43,y+8,x+w-12,y+39,ghost);
            ctx.fill(x+w-37,y+3,x+w-18,y+15,ghost);
            ctx.fill(x+w-40,y+36,x+w-15,y+43,ghost);
        }

        boolean skinDrawn=false;
        if(target instanceof PlayerEntity player&&client.getNetworkHandler()!=null){
            PlayerListEntry entry=client.getNetworkHandler().getPlayerListEntry(player.getUuid());
            if(entry!=null){SkinTextures skin=entry.getSkinTextures();PlayerSkinDrawer.draw(ctx,skin,x+9,y+13,34);skinDrawn=true;}
        }
        if(!skinDrawn){
            ctx.fill(x+9,y+13,x+43,y+47,0xFF292438);
            ctx.fill(x+18,y+17,x+34,y+32,0xFFB8A0D5);
            ctx.fill(x+14,y+33,x+38,y+44,accent);
        }
        String name=target.getName().getString();if(name.length()>17)name=name.substring(0,16)+"…";
        ctx.drawText(textRenderer(client),Text.literal(name),x+52,y+10,0xFFFFFFFF,true);
        ctx.drawText(textRenderer(client),Text.literal(String.format("%.1f HP  •  %.1fm",health,distance)),x+52,y+24,MUTED,false);
        ctx.drawText(textRenderer(client),Text.literal("TARGET"),x+w-48,y+10,accent,true);

        int bx=x+52,by=y+43,bw=119;
        ctx.fill(bx,by,bx+bw,by+7,0xFF302B3A);
        int ghostWidth=Math.round(bw*MathHelper.clamp(ghostHealth,0,1));
        if(ghostWidth>0)ctx.fill(bx,by,bx+ghostWidth,by+7,0x667F8CA8);
        int fill=Math.round(bw*MathHelper.clamp(smoothHealth,0,1));
        if(fill>0){ctx.fill(bx,by,bx+fill,by+7,accent);ctx.fill(bx,by,bx+fill,by+1,0x66FFFFFF);}
        ctx.drawText(textRenderer(client),Text.literal("NULL"),x+9,y+52,accent,false);
    }
    private static net.minecraft.client.font.TextRenderer textRenderer(MinecraftClient client){return client.textRenderer;}
}
