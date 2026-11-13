package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class ClickGuiScreen extends Screen {
    private static final int SHADE=0x440D1424, PANEL=0xEEF3F6FC, SIDE=0xFFE8EEF7, CARD=0xFFFFFFFF;
    private static final int ROW=0xFFF1F4FA, WHITE=0xFF20283A, MUTED=0xFF748099;
    private static final String[][] RU={
        {"Бой","Aim Assist","Hit Visuals","Target HUD","Combat HUD","Particles"},
        {"Движение","Auto Sprint","Sprint Display","Jump Stats","Speed Graph","Keystrokes"},
        {"Визуалы","World Tint","Block Outline","Crosshair","FPS Counter","Potion HUD","Time Changer"},
        {"Игрок","Armor Status","Armor HUD","Coordinates","Durability","Inventory"},
        {"Разное","Session Info","Clock","ArrayList","Notifications","Ping"}
    };
    private static final String[][] EN={
        {"Combat","Aim Assist","Hit Visuals","Target HUD","Combat HUD","Particles"},
        {"Movement","Auto Sprint","Sprint Display","Jump Stats","Speed Graph","Keystrokes"},
        {"Visuals","World Tint","Block Outline","Crosshair","FPS Counter","Potion HUD","Time Changer"},
        {"Player","Armor Status","Armor HUD","Coordinates","Durability","Inventory"},
        {"Misc","Session Info","Clock","ArrayList","Notifications","Ping"}
    };
    private int x,y,w,h,selected;
    private TextFieldWidget search;
    public ClickGuiScreen(){super(Text.literal("NULL"));}
    private String[][] categories(){return NullUiSettings.russian()?RU:EN;}
    private String tr(String ru,String en){return NullUiSettings.russian()?ru:en;}
    @Override public void renderBackground(DrawContext ctx,int mx,int my,float delta){}
    @Override protected void init(){
        float s=NullUiSettings.scale()-0.75f;
        w=Math.min(width-24,Math.round(790*(0.88f+s*0.24f)));
        h=Math.min(height-24,Math.round(470*(0.88f+s*0.24f)));
        if(NullUiSettings.mode()==NullUiSettings.LayoutMode.STRETCH){w=width-24;h=height-24;}
        else if(NullUiSettings.mode()==NullUiSettings.LayoutMode.FOUR_THREE){
            w=Math.min(width-24,Math.round(h*4f/3f));h=Math.min(height-24,Math.round(w*3f/4f));
        }
        w=Math.max(420,w);h=Math.max(300,h);x=(width-w)/2;y=(height-h)/2;
        search=new TextFieldWidget(textRenderer,x+178,y+22,Math.min(250,w-410),22,Text.literal("Search"));
        search.setMaxLength(40);search.setPlaceholder(Text.literal(tr("Поиск модулей...","Search modules...")));
        addDrawableChild(search);
    }
    @Override public void render(DrawContext ctx,int mx,int my,float delta){
        int accent=NullTheme.accent();ctx.fill(0,0,width,height,SHADE);
        ctx.fill(x-4,y-4,x+w+4,y+h+4,0x220D1424);ctx.fill(x,y,x+w,y+h,PANEL);
        ctx.fill(x,y,x+145,y+h,SIDE);ctx.fill(x,y,x+w,y+3,accent);
        ctx.drawText(textRenderer,"N",x+17,y+17,accent,true);
        ctx.drawText(textRenderer,"NULL",x+34,y+17,WHITE,true);
        ctx.drawText(textRenderer,"CLIENT",x+72,y+18,MUTED,false);
        ctx.fill(x+12,y+43,x+133,y+44,0xFF282E3A);
        String[] cats=categoryNames();
        for(int i=0;i<cats.length;i++){
            int cy=y+57+i*37;boolean active=i==selected;
            if(active){ctx.fill(x+8,cy-5,x+137,cy+25,0xFFDCE8FA);ctx.fill(x+8,cy-5,x+11,cy+25,accent);}
            ctx.drawText(textRenderer,cats[i],x+20,cy+5,active?WHITE:MUTED,active);
        }
        ctx.drawText(textRenderer,tr("НАСТРОЙКИ","SETTINGS"),x+17,y+h-111,MUTED,true);
        drawSmallButton(ctx,x+14,y+h-89,112,23,NullUiSettings.russian()?"ЯЗЫК: RU":"LANG: EN",accent);
        drawSmallButton(ctx,x+14,y+h-60,112,23,NullUiSettings.particles?tr("ЧАСТИЦЫ: ВКЛ","PARTICLES: ON"):tr("ЧАСТИЦЫ: ВЫКЛ","PARTICLES: OFF"),accent);
        ctx.drawText(textRenderer,"NULL VISUAL CLIENT",x+17,y+h-22,0xFF596273,false);

        ctx.drawText(textRenderer,cats[selected].toUpperCase(),x+164,y+17,WHITE,true);
        ctx.drawText(textRenderer,tr("ВИЗУАЛЬНЫЙ КЛИЕНТ  •  1.21.4","VISUAL CLIENT  •  1.21.4"),x+164,y+32,MUTED,false);
        search.render(ctx,mx,my,delta);
        drawSmallButton(ctx,x+w-190,y+20,48,22,"4:3",accent);
        drawSmallButton(ctx,x+w-138,y+20,48,22,"16:9",accent);
        drawSmallButton(ctx,x+w-86,y+20,68,22,"STRETCH",accent);
        ctx.drawText(textRenderer,tr("ЦВЕТ","ACCENT"),x+164,y+62,MUTED,true);
        for(int i=0;i<NullTheme.colorCount();i++){
            int bx=x+218+i*24;ctx.fill(bx,y+59,bx+17,y+76,NullTheme.color(i));
            if(i==NullTheme.selectedIndex()){ctx.fill(bx-2,y+57,bx+19,y+78,0xFFFFFFFF);ctx.fill(bx,y+59,bx+17,y+76,NullTheme.color(i));}
        }
        int listX=x+164,listY=y+91,listW=w-184;
        String q=search.getText().toLowerCase();
        int rowY=listY,shown=0;
        String[][] cat=categories();
        for(int i=1;i<cat[selected].length;i++){
            String name=cat[selected][i];if(!q.isBlank()&&!name.toLowerCase().contains(q))continue;
            int rh=48;ctx.fill(listX,rowY,listX+listW,rowY+rh,CARD);
            ctx.fill(listX,rowY,listX+3,rowY+rh,0xFF303746);
            ctx.drawText(textRenderer,name,listX+14,rowY+8,WHITE,true);
            String desc=description(name);
            ctx.drawText(textRenderer,desc,listX+14,rowY+24,MUTED,false);
            boolean supported=isSupported(selected,i);
            if(supported){
                boolean enabled=enabled(selected,i);String state=enabled?"ON":"OFF";
                int sw=textRenderer.getWidth(state);
                ctx.fill(listX+listW-48,rowY+13,listX+listW-12,rowY+34,enabled?0xFFDDF6E8:0xFFE9EDF4);
                ctx.drawText(textRenderer,state,listX+listW-30-sw/2,rowY+19,enabled?0xFF168A58:0xFF7E899B,true);
            }else{
                ctx.drawText(textRenderer,tr("СКОРО","SOON"),listX+listW-50,rowY+19,0xFF9AA4B5,false);
            }
            rowY+=54;shown++;
            if(rowY>y+h-24)break;
        }
        if(shown==0)ctx.drawText(textRenderer,tr("Ничего не найдено","No modules found"),listX+12,listY+14,MUTED,false);
        super.render(ctx,mx,my,delta);
    }
    private String[] categoryNames(){String[][] c=categories();String[] r=new String[c.length];for(int i=0;i<c.length;i++)r[i]=c[i][0];return r;}
    private String description(String name){
        return switch(name){
            case "Target HUD" -> tr("Цель под прицелом: здоровье, дистанция и имя","Target under crosshair: health, distance and name");
            case "Auto Sprint" -> tr("Автоматически бежать при движении вперёд","Automatically sprint while moving forward");
            case "Keystrokes" -> tr("Отображение нажатых клавиш","Show pressed movement keys");
            case "FPS Counter" -> tr("Текущий FPS в компактной панели","Current FPS in a compact panel");
            case "Armor HUD" -> tr("Броня и прочность предметов","Equipped armor and durability");
            case "Potion HUD" -> tr("Эффекты и время действия","Active effects and remaining time");
            case "Coordinates" -> tr("Координаты игрока","Player coordinates");
            case "ArrayList" -> tr("Список активных визуальных модулей","List of enabled visual modules");
            case "Ping" -> tr("Задержка до сервера","Server latency");
            default -> tr("Визуальный модуль NULL","NULL visual module");
        };
    }
    private boolean isSupported(int c,int i){return(c==0&&i==3)||(c==1&&(i==1||i==5))||(c==2&&(i==4||i==5))||(c==3&&(i==2||i==3))||(c==4&&(i==3||i==5));}
    private boolean enabled(int c,int i){var m=GlassVisualClient.MODULES;
        if(c==0&&i==3)return m.targetHud;if(c==1&&i==1)return m.autoSprint;if(c==1&&i==5)return m.keystrokes;
        if(c==2&&i==4)return m.fps;if(c==2&&i==5)return m.potionHud;
        if(c==3&&i==2)return m.armorHud;if(c==3&&i==3)return m.coordinates;
        if(c==4&&i==3)return m.arrayList;if(c==4&&i==5)return m.ping;return false;
    }
    private void toggle(int c,int i){var m=GlassVisualClient.MODULES;
        if(c==0&&i==3)m.targetHud=!m.targetHud;else if(c==1&&i==1)m.autoSprint=!m.autoSprint;else if(c==1&&i==5)m.keystrokes=!m.keystrokes;
        else if(c==2&&i==4)m.fps=!m.fps;else if(c==2&&i==5)m.potionHud=!m.potionHud;
        else if(c==3&&i==2)m.armorHud=!m.armorHud;else if(c==3&&i==3)m.coordinates=!m.coordinates;
        else if(c==4&&i==3)m.arrayList=!m.arrayList;else if(c==4&&i==5)m.ping=!m.ping;
        GlassVisualConfig.save();Notifier.module(new Module(categories()[c][i]),enabled(c,i));
    }
    private void drawSmallButton(DrawContext ctx,int bx,int by,int bw,int bh,String label,int accent){
        ctx.fill(bx,by,bx+bw,by+bh,0xFF202631);ctx.fill(bx,by,bx+2,by+bh,accent);
        ctx.drawCenteredTextWithShadow(textRenderer,label,bx+bw/2,by+7,WHITE);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(super.mouseClicked(mx,my,button))return true;
        if(button!=0)return false;
        for(int i=0;i<categories().length;i++){int cy=y+57+i*37;if(mx>=x+8&&mx<=x+137&&my>=cy-5&&my<=cy+25){selected=i;return true;}}
        if(mx>=x+14&&mx<=x+126&&my>=y+h-89&&my<=y+h-66){NullUiSettings.toggleLanguage();GlassVisualConfig.save();init();return true;}
        if(mx>=x+14&&mx<=x+126&&my>=y+h-60&&my<=y+h-37){NullUiSettings.particles=!NullUiSettings.particles;GlassVisualConfig.save();return true;}
        if(mx>=x+164&&my>=y+57&&my<=y+80){for(int i=0;i<NullTheme.colorCount();i++){int bx=x+218+i*24;if(mx>=bx-2&&mx<=bx+19){NullTheme.select(i);GlassVisualConfig.save();return true;}}}
        if(mx>=x+164&&mx<=x+w-20&&my>=y+91&&my<=y+h-20){
            int row=(int)(my-(y+91))/54;int i=row+1;String q=search.getText().toLowerCase();
            int actual=0;for(int n=1;n<categories()[selected].length;n++){if(!q.isBlank()&&!categories()[selected][n].toLowerCase().contains(q))continue;if(actual++==row){if(isSupported(selected,n)){toggle(selected,n);return true;}break;}}
        }
        if(my>=y+20&&my<=y+42&&mx>=x+w-190&&mx<=x+w-142){NullUiSettings.setMode(NullUiSettings.LayoutMode.FOUR_THREE);NullUiSettings.setScreenRatio(4,3);GlassVisualConfig.save();init();return true;}
        if(my>=y+20&&my<=y+42&&mx>=x+w-138&&mx<=x+w-90){NullUiSettings.setMode(NullUiSettings.LayoutMode.FIT);NullUiSettings.setScreenRatio(16,9);GlassVisualConfig.save();init();return true;}
        if(my>=y+20&&my<=y+42&&mx>=x+w-86&&mx<=x+w-18){NullUiSettings.setMode(NullUiSettings.LayoutMode.STRETCH);GlassVisualConfig.save();init();return true;}
        return false;
    }
    @Override public boolean shouldPause(){return false;}
}
