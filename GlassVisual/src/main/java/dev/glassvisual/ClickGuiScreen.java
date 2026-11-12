package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ClickGuiScreen extends Screen {
    private static final int BACKDROP=0xA6000000, PANEL=0xF00B0D12, CARD=0xF0141820;
    private static final int ROW=0xFF202630, ROW_ALT=0xFF171D25, WHITE=0xFFF4F7FA, MUTED=0xFF9AA6B2;
    private static final int GREEN=0xFF65E69A;
    private static final String[][] CATEGORIES={
        {"COMBAT","Aim Assist","Hit Visuals","Target Info","Reach Display","Combat HUD","Particles"},
        {"MOVEMENT","Sprint Display","Jump Stats","Motion Info","Speed Graph","Elytra HUD","Key Overlay"},
        {"RENDER","World Tint","Block Outline","Crosshair","Item Display","Effects HUD","Time Overlay"},
        {"PLAYER","Armor Status","Inventory View","Coordinates","Durability","Status Effects","Totem Counter"},
        {"MISC","Session Info","Clock","Brand Tag","Notifications","Server Info","Music Widget"}
    };

    private int panelX,panelY,panelW,panelH;
    public ClickGuiScreen(){ super(Text.literal("NULL")); }

    private void calculateBounds() {
        float scale=NullUiSettings.scale();
        int availableW=Math.max(280,width-28), availableH=Math.max(240,height-28);
        int baseW=Math.min(1120,Math.round((width-28)*0.94f*scale));
        int baseH=Math.min(680,Math.round((height-28)*0.88f*scale));
        if(NullUiSettings.mode()==NullUiSettings.LayoutMode.STRETCH) {
            panelW=availableW; panelH=availableH;
        } else if(NullUiSettings.mode()==NullUiSettings.LayoutMode.FOUR_THREE) {
            panelW=Math.min(baseW,Math.round(baseH*4f/3f));
            panelH=Math.min(baseH,Math.round(panelW*3f/4f));
        } else {
            panelW=Math.min(baseW,availableW); panelH=Math.min(baseH,availableH);
        }
        panelW=Math.max(260,Math.min(panelW,width-12));
        panelH=Math.max(220,Math.min(panelH,height-12));
        panelX=(width-panelW)/2; panelY=(height-panelH)/2;
    }

    @Override protected void init() {
        clearChildren(); calculateBounds();
        int x=panelX+panelW-318, y=panelY+13;
        addDrawableChild(ButtonWidget.builder(Text.literal("−"),b->{NullUiSettings.adjustScale(-0.05f);init();})
            .dimensions(x,y,24,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"),b->{NullUiSettings.adjustScale(0.05f);init();})
            .dimensions(x+27,y,24,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("FIT"),b->{NullUiSettings.setMode(NullUiSettings.LayoutMode.FIT);init();})
            .dimensions(x+58,y,42,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("STRETCH"),b->{NullUiSettings.setMode(NullUiSettings.LayoutMode.STRETCH);init();})
            .dimensions(x+104,y,65,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("4:3"),b->{NullUiSettings.setMode(NullUiSettings.LayoutMode.FOUR_THREE);init();})
            .dimensions(x+173,y,42,20).build());
        int swatchX=panelX+panelW-92;
        for(int i=0;i<NullTheme.colorCount();i++) {
            final int index=i;
            addDrawableChild(ButtonWidget.builder(Text.literal(" "),b->{NullTheme.select(index);})
                .dimensions(swatchX+(i%3)*25,panelY+45+(i/3)*22,21,18).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("×"),b->close())
            .dimensions(panelX+panelW-30,panelY+panelH-29,20,20).build());
    }

    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta) {
        calculateBounds();
        int accent=NullTheme.accent();
        ctx.fill(0,0,width,height,BACKDROP);
        // Full-width accent ribbon behind the floating interface.
        ctx.fill(0,panelY+panelH/2-2,width,panelY+panelH/2+2,0x553C4654);
        ctx.fill(0,panelY+panelH/2-1,width,panelY+panelH/2+1,accent);
        ctx.fill(panelX-2,panelY-2,panelX+panelW+2,panelY+panelH+2,0xAA050608);
        ctx.fill(panelX,panelY,panelX+panelW,panelY+panelH,PANEL);
        ctx.fill(panelX,panelY,panelX+panelW,panelY+3,accent);
        ctx.drawText(textRenderer,"NULL",panelX+16,panelY+15,WHITE,true);
        ctx.drawText(textRenderer,"VISUAL CLIENT",panelX+62,panelY+16,MUTED,false);
        ctx.drawText(textRenderer,"ACCENT",panelX+16,panelY+47,GREEN,true);
        ctx.drawText(textRenderer,"SCALE  "+Math.round(NullUiSettings.scale()*100)+"%   •   "+NullUiSettings.mode().name(),
            panelX+82,panelY+47,MUTED,false);

        int contentX=panelX+12, contentY=panelY+91, contentW=panelW-24, contentH=panelH-127;
        int columns=contentW>=900?5:contentW>=610?3:contentW>=390?2:1;
        int gap=9, rows=(CATEGORIES.length+columns-1)/columns;
        int cardW=(contentW-gap*(columns-1))/columns;
        int cardH=(contentH-gap*(rows-1))/rows;
        for(int i=0;i<CATEGORIES.length;i++) {
            int col=i%columns, row=i/columns;
            int x=contentX+col*(cardW+gap), y=contentY+row*(cardH+gap);
            int right=x+cardW, bottom=y+cardH;
            if(cardW<120 || cardH<64) continue;
            ctx.fill(x,y,right,bottom,CARD);
            ctx.fill(x,y,right,y+25,0xFF1C242D);
            ctx.fill(x,y,x+3,bottom,accent);
            ctx.drawText(textRenderer,CATEGORIES[i][0],x+11,y+9,GREEN,true);
            int itemCount=CATEGORIES[i].length-1;
            int rowGap=4, rowHeight=Math.min(27,Math.max(17,(cardH-35-(itemCount-1)*rowGap)/itemCount));
            for(int m=1;m<CATEGORIES[i].length;m++) {
                int iy=y+31+(m-1)*(rowHeight+rowGap);
                if(iy+rowHeight>bottom-5) break;
                ctx.fill(x+7,iy,right-7,iy+rowHeight,(m%2==0)?ROW:ROW_ALT);
                ctx.drawText(textRenderer,CATEGORIES[i][m],x+13,iy+(rowHeight-9)/2,WHITE,false);
                ctx.drawText(textRenderer,"›",right-19,iy+(rowHeight-9)/2,MUTED,false);
            }
        }
        super.render(ctx,mouseX,mouseY,delta);
        // Draw actual selected-color dots on top of the clickable swatches.
        int sx=panelX+panelW-92;
        for(int i=0;i<NullTheme.colorCount();i++) {
            int bx=sx+(i%3)*25, by=panelY+45+(i/3)*22;
            ctx.fill(bx+6,by+5,bx+15,by+14,NullTheme.color(i));
        }
    }
    @Override public boolean shouldPause(){return false;}
}
