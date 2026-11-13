package dev.glassvisual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** NULL's own 2D HUD particles. These are rendered by DrawContext, not Minecraft world particles. */
public final class NullParticleRenderer {
    private static final Random RANDOM=new Random();
    private static final List<Spark> SPARKS=new ArrayList<>();
    private static int ticks;
    private NullParticleRenderer(){}
    public static void tick(MinecraftClient client){
        if(!NullUiSettings.particles||client.player==null||client.world==null){SPARKS.clear();return;}
        int w=client.getWindow().getScaledWidth(),h=client.getWindow().getScaledHeight();
        if(++ticks%2==0&&SPARKS.size()<54){
            for(int i=0;i<2&&SPARKS.size()<54;i++){
                SPARKS.add(new Spark(RANDOM.nextFloat()*w,h+RANDOM.nextInt(16),-0.12f-RANDOM.nextFloat()*0.34f,
                    -0.35f-RANDOM.nextFloat()*0.55f,1.0f,1+RANDOM.nextInt(3)));
            }
        }
        Iterator<Spark> it=SPARKS.iterator();
        while(it.hasNext()){
            Spark p=it.next();p.x+=p.vx;p.y+=p.vy;p.life-=0.009f;
            if(p.life<=0||p.y<-8||p.x<-8)it.remove();
        }
    }
    public static void render(DrawContext ctx,MinecraftClient client){
        if(!NullUiSettings.particles||client.player==null)return;
        int accent=NullTheme.accent();
        for(Spark p:SPARKS){
            int alpha=Math.max(0,Math.min(255,(int)(p.life*145)));
            int color=(alpha<<24)|(accent&0x00FFFFFF);
            int s=p.size;
            ctx.fill((int)p.x,(int)p.y,(int)p.x+s,(int)p.y+s,color);
            if(s>=3)ctx.fill((int)p.x-1,(int)p.y+1,(int)p.x+s+1,(int)p.y+s-1,(alpha/3<<24)|0x00FFFFFF);
        }
    }
    private static final class Spark{
        float x,y,vx,vy,life;int size;
        Spark(float x,float y,float vx,float vy,float life,int size){this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.life=life;this.size=size;}
    }
}
