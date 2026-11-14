package dev.glassvisual;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFW;
public final class NullHandSettings {
 private static float x,y,z; private static int saveTicks;
 private NullHandSettings(){}
 public static float x(){return x;} public static float y(){return y;} public static float z(){return z;}
 public static void set(float nx,float ny,float nz){x=clamp(nx);y=clamp(ny);z=clamp(nz);}
 public static void apply(MatrixStack matrices){matrices.translate(x,y,z);}
 public static void tick(MinecraftClient client){
  if(!GlassVisualClient.MODULES.handPosition||client.player==null||client.currentScreen!=null)return;
  long w=client.getWindow().getHandle();float step=0.0025f;boolean changed=false;
  if(GLFW.glfwGetKey(w,GLFW.GLFW_KEY_KP_4)==GLFW.GLFW_PRESS){x-=step;changed=true;}
  if(GLFW.glfwGetKey(w,GLFW.GLFW_KEY_KP_6)==GLFW.GLFW_PRESS){x+=step;changed=true;}
  if(GLFW.glfwGetKey(w,GLFW.GLFW_KEY_KP_8)==GLFW.GLFW_PRESS){y+=step;changed=true;}
  if(GLFW.glfwGetKey(w,GLFW.GLFW_KEY_KP_2)==GLFW.GLFW_PRESS){y-=step;changed=true;}
  if(GLFW.glfwGetKey(w,GLFW.GLFW_KEY_KP_7)==GLFW.GLFW_PRESS){z-=step;changed=true;}
  if(GLFW.glfwGetKey(w,GLFW.GLFW_KEY_KP_9)==GLFW.GLFW_PRESS){z+=step;changed=true;}
  if(changed&&++saveTicks%12==0)GlassVisualConfig.save();
 }
 private static float clamp(float v){return Math.max(-0.35f,Math.min(0.35f,v));}
}
