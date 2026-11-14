package dev.glassvisual;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
public final class HudEditorScreen extends Screen {
 private static final int PANEL=0xE9161D2A, SOFT=0xD9222B3B, WHITE=0xFFF2F5FC, MUTED=0xFF9AA8BD;
 private String dragging; private int offsetX,offsetY;
 public HudEditorScreen(){super(Text.literal("NULL HUD Editor"));}
 private static Text ui(String s){return Text.literal(s).setStyle(Style.EMPTY.withFont(Identifier.of("glassvisual","ui")));}
 @Override public void render(DrawContext ctx,int mx,int my,float delta){
  int accent=NullTheme.accent();ctx.fill(0,0,width,height,0xA6060A12);
  int pw=Math.min(540,width-28),px=(width-pw)/2;rounded(ctx,px,8,pw,38,10,PANEL);
  ctx.fill(px+12,18,px+15,36,accent);ctx.drawText(textRenderer,ui("NULL / HUD EDITOR"),px+24,16,WHITE,false);
  ctx.drawText(textRenderer,ui("Перетаскивай блоки мышкой • ESC — сохранить"),px+178,18,MUTED,false);
  element(ctx,"NULL CLIENT","watermark",HudLayout.watermarkX,HudLayout.watermarkY,118,28,accent);
  element(ctx,"FPS  240","fps",HudLayout.fpsX<0?width-94:HudLayout.fpsX,HudLayout.fpsY,82,28,accent);
  element(ctx,"XYZ  120 / 64 / -32","coordinates",HudLayout.coordinatesX,HudLayout.coordinatesY<0?height-34:HudLayout.coordinatesY,160,28,accent);
  element(ctx,"W  A  S  D","keys",HudLayout.keysX,HudLayout.keysY<0?height-112:HudLayout.keysY,96,78,accent);
  element(ctx,"ACTIVE MODULES","arrayList",HudLayout.arrayListX<0?width-116:HudLayout.arrayListX,HudLayout.arrayListY,108,38,accent);
  element(ctx,"ARMOR  98%","armor",HudLayout.armorX,HudLayout.armorY<0?height-34:HudLayout.armorY,108,30,accent);
  element(ctx,"POTIONS  0:42","potions",HudLayout.potionsX<0?width-158:HudLayout.potionsX,HudLayout.potionsY,150,56,accent);
  element(ctx,"PING  24 ms","ping",HudLayout.pingX<0?width-94:HudLayout.pingX,HudLayout.pingY,82,26,accent);
  element(ctx,"HOTBAR · 9 SLOTS","inventory",HudLayout.inventoryX<0?width/2-100:HudLayout.inventoryX,HudLayout.inventoryY<0?height-48:HudLayout.inventoryY,200,34,accent);
  element(ctx,"MODULE NOTIFICATIONS","notifications",HudLayout.notificationsX<0?width/2-105:HudLayout.notificationsX,HudLayout.notificationsY,210,38,accent);
  rounded(ctx,px,height-34,pw,26,8,PANEL);ctx.drawCenteredTextWithShadow(textRenderer,ui("LEFT ALT · HUD EDITOR · DRAG ELEMENTS"),width/2,height-26,MUTED);
  super.render(ctx,mx,my,delta);
 }
 private void element(DrawContext ctx,String preview,String id,int x,int y,int w,int h,int accent){
  rounded(ctx,x,y,w,h,7,id.equals(dragging)?0xF02A3548:SOFT);ctx.fill(x+1,y+1,x+w-1,y+3,accent);
  ctx.drawText(textRenderer,ui(preview),x+8,y+(h-8)/2,WHITE,false);ctx.drawText(textRenderer,ui(id.toUpperCase()),x+7,y+h+3,MUTED,false);
  if(id.equals(dragging)){ctx.fill(x-2,y-2,x+w+2,y-1,accent);ctx.fill(x-2,y+h+1,x+w+2,y+h+2,accent);}
 }
 private static void rounded(DrawContext ctx,int x,int y,int w,int h,int radius,int color){
  int r=Math.max(1,Math.min(radius,Math.min(w,h)/2));for(int row=0;row<h;row++){int edge=row<r?r-1-row:row>=h-r?row-(h-r):-1;int inset=edge<0?0:r-(int)Math.floor(Math.sqrt(Math.max(0,r*r-edge*edge)));ctx.fill(x+inset,y+row,x+w-inset,y+row+1,color);}
 }
 @Override public boolean mouseClicked(double mx,double my,int button){
  if(button==0){String[] ids={"watermark","fps","coordinates","keys","arrayList","armor","potions","ping","inventory","notifications"};for(String id:ids){int[] p=position(id),s=size(id);if(mx>=p[0]&&mx<=p[0]+s[0]&&my>=p[1]&&my<=p[1]+s[1]){dragging=id;offsetX=(int)mx-p[0];offsetY=(int)my-p[1];return true;}}}
  return super.mouseClicked(mx,my,button);
 }
 @Override public boolean mouseDragged(double mx,double my,int button,double dx,double dy){
  if(button==0&&dragging!=null){int[] s=size(dragging);int x=clamp((int)mx-offsetX,0,Math.max(0,width-s[0]));int y=clamp((int)my-offsetY,0,Math.max(0,height-s[1]));
   switch(dragging){case "watermark"->{HudLayout.watermarkX=x;HudLayout.watermarkY=y;}case "fps"->{HudLayout.fpsX=x;HudLayout.fpsY=y;}case "coordinates"->{HudLayout.coordinatesX=x;HudLayout.coordinatesY=y;}case "keys"->{HudLayout.keysX=x;HudLayout.keysY=y;}case "arrayList"->{HudLayout.arrayListX=x;HudLayout.arrayListY=y;}case "armor"->{HudLayout.armorX=x;HudLayout.armorY=y;}case "potions"->{HudLayout.potionsX=x;HudLayout.potionsY=y;}case "ping"->{HudLayout.pingX=x;HudLayout.pingY=y;}case "inventory"->{HudLayout.inventoryX=x;HudLayout.inventoryY=y;}case "notifications"->{HudLayout.notificationsX=x;HudLayout.notificationsY=y;}}return true;}
  return super.mouseDragged(mx,my,button,dx,dy);
 }
 @Override public boolean mouseReleased(double mx,double my,int button){if(button==0&&dragging!=null){dragging=null;GlassVisualConfig.save();return true;}return super.mouseReleased(mx,my,button);}
 private int[] position(String id){return switch(id){case "watermark"->new int[]{HudLayout.watermarkX,HudLayout.watermarkY};case "fps"->new int[]{HudLayout.fpsX<0?width-94:HudLayout.fpsX,HudLayout.fpsY};case "coordinates"->new int[]{HudLayout.coordinatesX,HudLayout.coordinatesY<0?height-34:HudLayout.coordinatesY};case "keys"->new int[]{HudLayout.keysX,HudLayout.keysY<0?height-112:HudLayout.keysY};case "arrayList"->new int[]{HudLayout.arrayListX<0?width-116:HudLayout.arrayListX,HudLayout.arrayListY};case "armor"->new int[]{HudLayout.armorX,HudLayout.armorY<0?height-34:HudLayout.armorY};case "potions"->new int[]{HudLayout.potionsX<0?width-158:HudLayout.potionsX,HudLayout.potionsY};case "ping"->new int[]{HudLayout.pingX<0?width-94:HudLayout.pingX,HudLayout.pingY};case "notifications"->new int[]{HudLayout.notificationsX<0?width/2-105:HudLayout.notificationsX,HudLayout.notificationsY};default->new int[]{HudLayout.inventoryX<0?width/2-100:HudLayout.inventoryX,HudLayout.inventoryY<0?height-48:HudLayout.inventoryY};};}
 private int[] size(String id){return switch(id){case "watermark"->new int[]{118,28};case "fps","ping"->new int[]{82,28};case "coordinates"->new int[]{160,28};case "keys"->new int[]{96,78};case "arrayList"->new int[]{108,38};case "armor"->new int[]{108,30};case "potions"->new int[]{150,56};case "notifications"->new int[]{210,38};default->new int[]{200,34};};}
 private static int clamp(int v,int min,int max){return Math.max(min,Math.min(max,v));}
 @Override public void close(){GlassVisualConfig.save();super.close();}
 @Override public boolean shouldPause(){return false;}
}