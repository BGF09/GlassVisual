package dev.glassvisual;
import java.util.ArrayDeque;
import java.util.Iterator;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
public final class NullLegitEsp {
 private static final ArrayDeque<Ghost> GHOSTS=new ArrayDeque<>();
 private static java.util.UUID targetId;
 private NullLegitEsp(){}
 public static void tick(MinecraftClient client){
  if((!GlassVisualClient.MODULES.legitEsp&&!GlassVisualClient.MODULES.targetHud)||client.player==null||client.world==null){GHOSTS.clear();targetId=null;return;}
  if(client.crosshairTarget instanceof EntityHitResult hit&&hit.getEntity() instanceof PlayerEntity p&&p!=client.player&&!p.isInvisible()&&client.player.canSee(p)){
   if(!p.getUuid().equals(targetId)){GHOSTS.clear();targetId=p.getUuid();}
   GHOSTS.addLast(new Ghost(p.getBoundingBox(),0));
   while(GHOSTS.size()>6)GHOSTS.removeFirst();
  }else{targetId=null;}
  Iterator<Ghost> it=GHOSTS.iterator();while(it.hasNext()){Ghost g=it.next();g.age++;if(g.age>9)it.remove();}
 }
 public static void render(WorldRenderContext context){
  MinecraftClient client=MinecraftClient.getInstance();
  if((!GlassVisualClient.MODULES.legitEsp&&!GlassVisualClient.MODULES.targetHud)||client.player==null||context.matrixStack()==null||context.consumers()==null)return;
  MatrixStack matrices=context.matrixStack();Vec3d camera=context.camera().getPos();matrices.push();matrices.translate(-camera.x,-camera.y,-camera.z);
  Matrix4f matrix=matrices.peek().getPositionMatrix();VertexConsumer lines=context.consumers().getBuffer(RenderLayer.getLines());
  int accent=NullTheme.accent();
  if(GlassVisualClient.MODULES.legitEsp)for(PlayerEntity p:client.world.getPlayers()){
   if(p==client.player||p.isInvisible()||!client.player.canSee(p)||!p.isAlive())continue;
   Box box=p.getBoundingBox().expand(0.025);
   drawBox(lines,matrix,box,accent,220);
   double y=box.maxY+0.22;double half=Math.max(0.28,p.getWidth()*0.55);double left=p.getX()-half,right=p.getX()+half;
   line(lines,matrix,new Vec3d(left,y,p.getZ()),new Vec3d(right,y,p.getZ()),70,78,96,210);
   float ratio=Math.max(0,Math.min(1,p.getHealth()/Math.max(1,p.getMaxHealth())));
   line(lines,matrix,new Vec3d(left,y+0.006,p.getZ()),new Vec3d(left+(right-left)*ratio,y+0.006,p.getZ()),ratio>.5f?70:ratio>.25f?255:255,ratio>.5f?220:ratio>.25f?190:85,ratio>.5f?145:ratio>.25f?75:95,245);
  }
  for(Ghost g:GHOSTS){int alpha=Math.max(0,120-g.age*13);if(alpha>0)drawBox(lines,matrix,g.box,accent,alpha);}
  matrices.pop();
 }
 private static void drawBox(VertexConsumer v,Matrix4f m,Box b,int color,int alpha){
  int r=(color>>16)&255,g=(color>>8)&255,bl=color&255;
  Vec3d[] p={new Vec3d(b.minX,b.minY,b.minZ),new Vec3d(b.maxX,b.minY,b.minZ),new Vec3d(b.maxX,b.minY,b.maxZ),new Vec3d(b.minX,b.minY,b.maxZ),new Vec3d(b.minX,b.maxY,b.minZ),new Vec3d(b.maxX,b.maxY,b.minZ),new Vec3d(b.maxX,b.maxY,b.maxZ),new Vec3d(b.minX,b.maxY,b.maxZ)};
  int[][] edges={{0,1},{1,2},{2,3},{3,0},{4,5},{5,6},{6,7},{7,4},{0,4},{1,5},{2,6},{3,7}};
  for(int[] e:edges)line(v,m,p[e[0]],p[e[1]],r,g,bl,alpha);
 }
 private static void line(VertexConsumer v,Matrix4f m,Vec3d a,Vec3d b,int r,int g,int bl,int alpha){
  v.vertex(m,(float)a.x,(float)a.y,(float)a.z).color(r,g,bl,alpha).normal(0,1,0);
  v.vertex(m,(float)b.x,(float)b.y,(float)b.z).color(r,g,bl,alpha).normal(0,1,0);
 }
 private static final class Ghost{final Box box;int age;Ghost(Box b,int a){box=b;age=a;}}
}
