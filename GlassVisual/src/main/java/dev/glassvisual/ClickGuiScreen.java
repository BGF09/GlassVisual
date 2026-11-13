package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ClickGuiScreen extends Screen {
    private static final int BACKDROP=0xA6000000, PANEL=0xF00B0D12, CARD=0xF0141820;
    private static final int ROW=0xFF202630, ROW_ALT=0xFF171D25, WHITE=0xFFF4F7FA, MUTED=0xFF9AA6B2;
    private static final int GREEN=0xFF65E69A;
    private static final String[][] CATEGORIES={
        {"COMBAT","Aim Assist","Hit Visuals","Target Info","Target HUD","Combat HUD","Particles"},
        {"MOVEMENT","Sprint Display","Jump Stats","Motion Info","Speed Graph","Elytra HUD","Keystrokes"},
        {"RENDER","World Tint","Block Outline","Crosshair","FPS Counter","Effects HUD","Time Overlay"},
        {"PLAYER","Armor Status","Armor HUD","Coordinates","Durability","Potion HUD","Inventory View"},
        {"MISC","Session Info","Clock","ArrayList","Notifications","Ping Display","Music Widget"}
    };
    private int panelX,panelY,panelW,panelH;
    public ClickGuiScreen(){super(Text.literal("NULL"));}

    @Override public void renderBackground(DrawContext context,int mouseX,int mouseY,float delta) {}
    private void calculateBounds(){
        float scale=NullUiSettings.scale();
        int availableW=Math.max(280,width-28),availableH=Math.max(240,height-28);
        int baseW=Math.min(1120,Math.round((width-28)*0.94f*scale));
        int baseH=Math.min(680,Math.round((height-28)*0.88f*scale));
        if(NullUiSettings.mode()==NullUiSettings.LayoutMode.STRETCH){panelW=availableW;panelH=availableH;}
        else if(NullUiSettings.mode()==NullUiSettings.LayoutMode.FOUR_THREE){panelW=Math.min(baseW,Math.round(baseH*4f/3f));panelH=Math.min(baseH,Math.round(panelW*3f/4f));}
        else{panelW=Math.min(baseW,availableW);panelH=Math.min(baseH,availableH);}
        panelW=Math.max(260,Math.min(panelW,width-12));panelH=Math.max(220,Math.min(panelH,height-12));
        panelX=(width-panelW)/2;panelY=(height-panelH)/2;
    }
    @Override protected void init(){
        clearChildren();calculateBounds();
        int x=panelX+panelW-294,y=panelY+13;
        addDrawableChild(ButtonWidget.builder(Text.literal("−"),b->{NullUiSettings.adjustScale(-0.05f);GlassVisualConfig.save();init();}).dimensions(x,y,22,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"),b->{NullUiSettings.adjustScale(0.05f);GlassVisualConfig.save();init();}).dimensions(x+24,y,22,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("FIT"),b->{NullUiSettings.setMode(NullUiSettings.LayoutMode.FIT);GlassVisualConfig.save();init();}).dimensions(x+50,y,38,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("4:3"),b->NullUiSettings.setScreenRatio(4,3)).dimensions(x+92,y,42,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("16:9"),b->NullUiSettings.setScreenRatio(16,9)).dimensions(x+138,y,46,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("FULL"),b->MinecraftClient.getInstance().getWindow().toggleFullscreen()).dimensions(x+188,y,48,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(NullUiSettings.particles?"PARTICLES ON":"PARTICLES OFF"),b->{NullUiSettings.particles=!NullUiSettings.particles;GlassVisualConfig.save();init();})
            .dimensions(panelX+16,panelY+43,112,20).build());
        int swatchX=panelX+panelW-92;
        for(int i=0;i<NullTheme.colorCount();i++){final int index=i;
            addDrawableChild(ButtonWidget.builder(Text.literal(" "),b->{NullTheme.select(index);GlassVisualConfig.save();}).dimensions(swatchX+(i%3)*25,panelY+43+(i/3)*22,21,18).build());}
        addDrawableChild(ButtonWidget.builder(Text.literal("×"),b->close()).dimensions(panelX+panelW-30,panelY+panelH-29,20,20).build());
    }
    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta){
        calculateBounds();int accent=NullTheme.accent();
        ctx.fill(0,0,width,height,BACKDROP);
        ctx.fill(0,panelY+panelH/2-2,width,panelY+panelH/2+2,0x553C4654);
        ctx.fill(0,panelY+panelH/2-1,width,panelY+panelH/2+1,accent);
        ctx.fill(panelX-2,panelY-2,panelX+panelW+2,panelY+panelH+2,0xAA050608);
        ctx.fill(panelX,panelY,panelX+panelW,panelY+panelH,PANEL);ctx.fill(panelX,panelY,panelX+panelW,panelY+3,accent);
        ctx.drawText(textRenderer,"NULL",panelX+16,panelY+15,WHITE,true);
        ctx.drawText(textRenderer,"VISUAL CLIENT",panelX+62,panelY+16,MUTED,false);
        ctx.drawText(textRenderer,"ACCENT",panelX+142,panelY+48,GREEN,true);
        ctx.drawText(textRenderer,"SCALE "+Math.round(NullUiSettings.scale()*100)+"%  •  "+NullUiSettings.mode().name(),panelX+198,panelY+48,MUTED,false);
        int contentX=panelX+12,contentY=panelY+91,contentW=panelW-24,contentH=panelH-127;
        int columns=contentW>=900?5:contentW>=610?3:contentW>=390?2:1;
        int gap=9,rows=(CATEGORIES.length+columns-1)/columns,cardW=(contentW-gap*(columns-1))/columns,cardH=(contentH-gap*(rows-1))/rows;
        for(int i=0;i<CATEGORIES.length;i++){
            int col=i%columns,row=i/columns,x=contentX+col*(cardW+gap),y=contentY+row*(cardH+gap),right=x+cardW,bottom=y+cardH;
            if(cardW<120||cardH<64)continue;
            ctx.fill(x,y,right,bottom,CARD);ctx.fill(x,y,right,y+25,0xFF1C242D);ctx.fill(x,y,x+3,bottom,accent);
            ctx.drawText(textRenderer,CATEGORIES[i][0],x+11,y+9,GREEN,true);
            int count=CATEGORIES[i].length-1,rowGap=4,rowHeight=Math.min(27,Math.max(17,(cardH-35-(count-1)*rowGap)/count));
            for(int m=1;m<CATEGORIES[i].length;m++){
                int iy=y+31+(m-1)*(rowHeight+rowGap);if(iy+rowHeight>bottom-5)break;
                ctx.fill(x+7,iy,right-7,iy+rowHeight,(m%2==0)?ROW:ROW_ALT);
                ctx.drawText(textRenderer,CATEGORIES[i][m],x+13,iy+(rowHeight-9)/2,WHITE,false);
                if(isSupported(i,m)){
                    String state=moduleEnabled(i,m)?"ON":"OFF";
                    int stateColor=moduleEnabled(i,m)?GREEN:0xFFFF7777;
                    ctx.drawText(textRenderer,state,right-13-textRenderer.getWidth(state),iy+(rowHeight-9)/2,stateColor,true);
                } else ctx.drawText(textRenderer,"—",right-19,iy+(rowHeight-9)/2,MUTED,false);
            }
        }
        super.render(ctx,mouseX,mouseY,delta);
        int sx=panelX+panelW-92;
        for(int i=0;i<NullTheme.colorCount();i++){int bx=sx+(i%3)*25,by=panelY+43+(i/3)*22;ctx.fill(bx+6,by+5,bx+15,by+14,NullTheme.color(i));}
    }

    private boolean isSupported(int c,int i){
        return (c==0&&i==3)||(c==1&&i==6)||(c==2&&i==4)||(c==3&&(i==1||i==3||i==5))||(c==4&&(i==3||i==5));
    }
    private boolean moduleEnabled(int c,int i){
        var m=GlassVisualClient.MODULES;
        if(c==0&&i==3)return m.targetHud;
        if(c==1&&i==6)return m.keystrokes;
        if(c==2&&i==4)return m.fps;
        if(c==3&&i==1)return m.armorHud;
        if(c==3&&i==3)return m.coordinates;
        if(c==3&&i==5)return m.potionHud;
        if(c==4&&i==3)return m.arrayList;
        if(c==4&&i==5)return m.ping;
        return false;
    }
    private void toggleModule(int c,int i){
        var m=GlassVisualClient.MODULES;
        if(c==0&&i==3)m.targetHud=!m.targetHud;
        else if(c==1&&i==6)m.keystrokes=!m.keystrokes;
        else if(c==2&&i==4)m.fps=!m.fps;
        else if(c==3&&i==1)m.armorHud=!m.armorHud;
        else if(c==3&&i==3)m.coordinates=!m.coordinates;
        else if(c==3&&i==5)m.potionHud=!m.potionHud;
        else if(c==4&&i==3)m.arrayList=!m.arrayList;
        else if(c==4&&i==5)m.ping=!m.ping;
        GlassVisualConfig.save();
        Notifier.module(new Module(CATEGORIES[c][i]),moduleEnabled(c,i));
    }
    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(button==0){
            calculateBounds();
            int contentX=panelX+12,contentY=panelY+91,contentW=panelW-24,contentH=panelH-127;
            int columns=contentW>=900?5:contentW>=610?3:contentW>=390?2:1;
            int gap=9,rows=(CATEGORIES.length+columns-1)/columns,cardW=(contentW-gap*(columns-1))/columns,cardH=(contentH-gap*(rows-1))/rows;
            for(int c=0;c<CATEGORIES.length;c++){
                int col=c%columns,row=c/columns,x=contentX+col*(cardW+gap),y=contentY+row*(cardH+gap);
                int count=CATEGORIES[c].length-1,rowGap=4,rowHeight=Math.min(27,Math.max(17,(cardH-35-(count-1)*rowGap)/count));
                for(int i=1;i<CATEGORIES[c].length;i++){
                    int iy=y+31+(i-1)*(rowHeight+rowGap);
                    if(isSupported(c,i)&&mouseX>=x+7&&mouseX<x+cardW-7&&mouseY>=iy&&mouseY<iy+rowHeight){toggleModule(c,i);return true;}
                }
            }
        }
        return super.mouseClicked(mouseX,mouseY,button);
    }
    @Override public boolean shouldPause(){return false;}
}
