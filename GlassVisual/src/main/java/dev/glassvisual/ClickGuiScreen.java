package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ClickGuiScreen extends Screen {
    private static final int PANEL=0xF011121B, CARD=0xE51B1E2B, ROW=0xC9272B3B, MUTED=0xFF9CA3B8;
    private static final String[][] CATEGORIES={
        {"COMBAT","Aim Assist","Hit Visuals","Target Info","Reach Display","Combat HUD","Particles"},
        {"MOVEMENT","Sprint Display","Jump Stats","Motion Info","Speed Graph","Elytra HUD","Key Overlay"},
        {"RENDER","World Tint","Block Outline","Crosshair","Item Display","Effects HUD","Time Overlay"},
        {"PLAYER","Armor Status","Inventory View","Coordinates","Durability","Status Effects","Totem Counter"},
        {"MISC","Session Info","Clock","Brand Tag","Notifications","Server Info","Music Widget"}
    };
    public ClickGuiScreen(){super(Text.literal("NULL"));}
    @Override protected void init(){
        clearChildren();
        int pw=Math.min(width-32,900), left=(width-pw)/2, top=Math.max(24,height/2-225), sx=left+pw-250;
        for(int i=0;i<NullTheme.colorCount();i++){final int index=i;
            addDrawableChild(ButtonWidget.builder(Text.literal("●"),b->NullTheme.select(index)).dimensions(sx+i*35,top+13,30,22).build());}
        addDrawableChild(ButtonWidget.builder(Text.literal("CLOSE  ×"),b->close()).dimensions(left+pw-94,top+48,78,22).build());
    }
    @Override public void render(DrawContext ctx,int mouseX,int mouseY,float delta){
        int accent=NullTheme.accent(), pw=Math.min(width-32,900), left=(width-pw)/2, top=Math.max(24,height/2-225);
        int ph=Math.min(height-top-18,450), gap=10, cw=(pw-36-gap*4)/5;
        ctx.fill(0,0,width,height,0x99070A12);
        ctx.fill(0,top+64,width,top+68,0x552D344B); ctx.fill(0,top+64,width,top+66,accent);
        ctx.fill(left-3,top-3,left+pw+3,top+ph+3,0x55252B42); ctx.fill(left,top,left+pw,top+ph,PANEL);
        ctx.fill(left,top,left+pw,top+3,accent); ctx.fill(left+1,top+1,left+pw-1,top+2,0x66FFFFFF);
        ctx.drawText(textRenderer,"NULL",left+20,top+17,0xFFFFFFFF,true);
        ctx.drawText(textRenderer,"VISUAL CLIENT  /  1.21.4",left+69,top+18,MUTED,false);
        ctx.drawText(textRenderer,"THEME",left+pw-282,top+19,MUTED,false);
        ctx.drawText(textRenderer,"ACCENT COLOR",left+20,top+52,accent,true);
        ctx.drawText(textRenderer,"VISUAL LAYOUT  •  DISPLAY ONLY",left+126,top+52,MUTED,false);
        int contentTop=top+82;
        for(int c=0;c<CATEGORIES.length;c++){
            int x=left+12+c*(cw+gap); ctx.fill(x,contentTop,x+cw,top+ph-14,CARD);
            ctx.fill(x,contentTop,x+cw,contentTop+27,0xFF202536); ctx.fill(x,contentTop,x+2,top+ph-14,accent);
            ctx.drawText(textRenderer,CATEGORIES[c][0],x+10,contentTop+10,accent,true);
            for(int m=1;m<CATEGORIES[c].length;m++){
                int y=contentTop+37+(m-1)*42; if(y+32>top+ph-18)break;
                ctx.fill(x+7,y,x+cw-7,y+34,ROW); ctx.fill(x+7,y,x+9,y+34,0x553D455C);
                ctx.drawText(textRenderer,CATEGORIES[c][m],x+15,y+12,0xFFE7EAF4,false);
                ctx.drawText(textRenderer,"›",x+cw-20,y+10,MUTED,false);
            }
        }
        super.render(ctx,mouseX,mouseY,delta);
        int swatchX=left+pw-250;
        for(int i=0;i<NullTheme.colorCount();i++){
            int sx=swatchX+i*35;
            ctx.fill(sx+10,top+19,sx+20,top+29,NullTheme.color(i));
        }
    }
    @Override public boolean shouldPause(){return false;}
}
