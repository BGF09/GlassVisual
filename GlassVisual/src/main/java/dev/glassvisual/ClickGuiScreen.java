package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class ClickGuiScreen extends Screen {
    private static final int SHADE=0x99060A12, PANEL=0xF21A202D, SIDE=0xFF141A25, CARD=0xFF242D3B;
    private static final int ROW=0xFF242D3B, WHITE=0xFFF1F4FC, MUTED=0xFF9AA8BD;
    private static final String[][] RU={
        {"Бой","Aim Assist","Hit Visuals","Target HUD","Combat HUD","Particles"},
        {"Движение","Auto Sprint","Sprint Display","Jump Stats","Speed Graph","Keystrokes"},
        {"Визуалы","World Tint","Block Outline","Crosshair","FPS Counter","Potion HUD","Time Changer","World Particles","Player Trail","Screen Stretch"},
        {"Игрок","Armor Status","Armor HUD","Coordinates","Durability","Inventory","Totem Counter","Food HUD","XP HUD"},
        {"Разное","Session Info","Clock","ArrayList","Notifications","Ping","Direction"}
    };
    private static final String[][] EN={
        {"Combat","Aim Assist","Hit Visuals","Target HUD","Combat HUD","Particles"},
        {"Movement","Auto Sprint","Sprint Display","Jump Stats","Speed Graph","Keystrokes"},
        {"Visuals","World Tint","Block Outline","Crosshair","FPS Counter","Potion HUD","Time Changer","World Particles","Player Trail","Screen Stretch"},
        {"Player","Armor Status","Armor HUD","Coordinates","Durability","Inventory","Totem Counter","Food HUD","XP HUD"},
        {"Misc","Session Info","Clock","ArrayList","Notifications","Ping","Direction"}
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
        w=Math.max(420,w);h=Math.max(300,h);x=(width-w)/2;y=(height-h)/2;
        search=new TextFieldWidget(textRenderer,x+178,y+22,Math.min(250,w-410),22,Text.literal("Search"));
        search.setMaxLength(40);search.setPlaceholder(Text.literal(tr("Поиск модулей...","Search modules...")));
        addDrawableChild(search);
    }
    @Override public void render(DrawContext ctx,int mx,int my,float delta){
        int accent=NullTheme.accent();ctx.fill(0,0,width,height,SHADE);
        ctx.fill(x-4,y-4,x+w+4,y+h+4,0x220D1424);roundedFill(ctx,x,y,w,h,14,PANEL);
        roundedFill(ctx,x,y,145,h,14,SIDE);ctx.fill(x,y,x+w,y+3,accent);
        ctx.drawText(textRenderer,"N",x+17,y+17,accent,true);
        ctx.drawText(textRenderer,"NULL",x+34,y+17,WHITE,true);
        ctx.drawText(textRenderer,"CLIENT",x+72,y+18,MUTED,false);
        ctx.fill(x+12,y+43,x+133,y+44,0xFF282E3A);
        String[] cats=categoryNames();
        for(int i=0;i<cats.length;i++){
            int cy=y+57+i*37;boolean active=i==selected;
            if(active){roundedFill(ctx,x+8,cy-5,129,30,8,0xFF293447);ctx.fill(x+8,cy-5,x+11,cy+25,accent);}
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
        int listX=x+164,listY=y+62,listW=w-184;
        String q=search.getText().toLowerCase();
        int rowY=listY,shown=0;
        String[][] cat=categories();
        for(int i=1;i<cat[selected].length;i++){
            String name=cat[selected][i];if(!q.isBlank()&&!name.toLowerCase().contains(q))continue;
            int rh=48;roundedFill(ctx,listX,rowY,listW,rh,9,CARD);
            ctx.fill(listX,rowY,listX+3,rowY+rh,accent);
            ctx.drawText(textRenderer,name,listX+14,rowY+8,WHITE,true);
            String desc=description(name);
            ctx.drawText(textRenderer,desc,listX+14,rowY+24,MUTED,false);
            boolean supported=isSupported(selected,i);
            if(supported){
                boolean enabled=enabled(selected,i);String state=enabled?"ON":"OFF";
                int sw=textRenderer.getWidth(state);
                roundedFill(ctx,listX+listW-48,rowY+13,36,21,8,enabled?0xFF174B3B:0xFF343D4D);
                ctx.drawText(textRenderer,state,listX+listW-30-sw/2,rowY+19,enabled?0xFF66E0A7:0xFFB0BBD0,true);
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
            case "Particles" -> tr("Декоративные частицы интерфейса HUD","Decorative particles on the HUD");
            case "World Particles" -> tr("Собственные 3D-частицы вокруг игрока","Custom 3D particles around the player");
            case "Player Trail" -> tr("Светящийся след движения игрока в мире","Glowing movement trail in the world");
            case "Screen Stretch" -> tr("Растяжение игрового мира без растяжения HUD","Stretch the 3D world without stretching the HUD");
            case "Aim Assist" -> tr("Мягкая доводка прицела при зажатой атаке","Gentle aim correction while attack is held");
            case "Target HUD" -> tr("Цель под прицелом: здоровье, дистанция и имя","Target under crosshair: health, distance and name");
            case "Hit Visuals" -> tr("Короткий маркер при потере здоровья целью","Brief marker when the focused target loses health");
            case "Combat HUD" -> tr("Предмет в руке и индикатор перезарядки удара","Held item and attack cooldown indicator");
            case "World Tint" -> tr("Лёгкий цветовой оттенок игрового экрана","Subtle color tint over the game view");
            case "Block Outline" -> tr("Цветная обводка блока под прицелом","Accent outline around the targeted block");
            case "Time Changer" -> tr("Фиксирует локальное время мира на полдень","Locks the local world time to noon");
            case "Crosshair" -> tr("Акцентный прицел NULL поверх стандартного","NULL accent reticle over the vanilla crosshair");
            case "Armor Status" -> tr("Процент прочности каждой части брони","Durability percentage for each armor piece");
            case "Totem Counter" -> tr("Считает тотемы во всём инвентаре","Counts totems in your inventory");
            case "Food HUD" -> tr("Показывает голод и насыщение","Shows hunger and saturation");
            case "XP HUD" -> tr("Уровень и прогресс опыта","Experience level and progress");
            case "Direction" -> tr("Показывает направление взгляда","Shows your current facing direction");
            case "Durability" -> tr("Прочность предмета в основной руке","Main-hand item durability");
            case "Inventory" -> tr("Название выбранного предмета и количество","Selected item name and stack count");
            case "Notifications" -> tr("Уведомления о переключении модулей","Module toggle notifications");
            case "Auto Sprint" -> tr("Автоматически бежать при движении вперёд","Automatically sprint while moving forward");
            case "Keystrokes" -> tr("Отображение нажатых клавиш","Show pressed movement keys");
            case "Sprint Display" -> tr("Показывает, включён ли спринт","Shows whether sprint is active");
            case "Jump Stats" -> tr("Считает прыжки за текущую сессию","Counts jumps in this session");
            case "Speed Graph" -> tr("График текущей скорости игрока","Live player movement speed graph");
            case "Session Info" -> tr("Время текущей игровой сессии","Current play session duration");
            case "Clock" -> tr("Локальные часы в HUD","Local clock in the HUD");
            case "FPS Counter" -> tr("Текущий FPS в компактной панели","Current FPS in a compact panel");
            case "Armor HUD" -> tr("Броня и прочность предметов","Equipped armor and durability");
            case "Potion HUD" -> tr("Эффекты и время действия","Active effects and remaining time");
            case "Coordinates" -> tr("Координаты игрока","Player coordinates");
            case "ArrayList" -> tr("Список активных визуальных модулей","List of enabled visual modules");
            case "Ping" -> tr("Задержка до сервера","Server latency");
            default -> tr("Визуальный модуль NULL","NULL visual module");
        };
    }
    private boolean isSupported(int c,int i){
        String[][] groups = categories();
        return c >= 0 && c < groups.length && i > 0 && i < groups[c].length;
    }
    private boolean enabled(int c,int i){var m=GlassVisualClient.MODULES;
        if(c==0&&i==5)return NullUiSettings.particles;if(c==0&&i==1)return m.aimAssist;if(c==0&&i==2)return m.hitVisuals;if(c==0&&i==3)return m.targetHud;if(c==0&&i==4)return m.combatHud;if(c==1&&i==1)return m.autoSprint;if(c==1&&i==5)return m.keystrokes;
        if(c==2&&i==1)return m.worldTint;if(c==2&&i==2)return m.blockOutline;if(c==2&&i==3)return m.crosshair;if(c==2&&i==6)return m.timeChanger;if(c==2&&i==4)return m.fps;if(c==2&&i==5)return m.potionHud;
        if(c==2&&i==7)return NullUiSettings.worldParticles;if(c==2&&i==8)return NullUiSettings.playerTrail;if(c==2&&i==9)return NullUiSettings.stretchMode()!=NullUiSettings.StretchMode.OFF;
        if(c==3&&i==6)return m.totemCounter;if(c==3&&i==7)return m.foodHud;if(c==3&&i==8)return m.xpHud;if(c==3&&i==1)return m.armorStatus;if(c==3&&i==2)return m.armorHud;if(c==3&&i==3)return m.coordinates;if(c==3&&i==4)return m.durability;if(c==3&&i==5)return m.inventory;
        if(c==1&&i==2)return m.sprintDisplay;if(c==1&&i==3)return m.jumpStats;if(c==1&&i==4)return m.speedGraph;
        if(c==4&&i==1)return m.sessionInfo;if(c==4&&i==2)return m.clock;if(c==4&&i==4)return m.notifications;
        if(c==4&&i==3)return m.arrayList;if(c==4&&i==5)return m.ping;if(c==4&&i==6)return m.directionHud;return false;
    }
    private void toggle(int c,int i){var m=GlassVisualClient.MODULES;
        if(c==0&&i==5)NullUiSettings.particles=!NullUiSettings.particles;else if(c==0&&i==1)m.aimAssist=!m.aimAssist;else if(c==0&&i==2)m.hitVisuals=!m.hitVisuals;else if(c==0&&i==3)m.targetHud=!m.targetHud;else if(c==0&&i==4)m.combatHud=!m.combatHud;else if(c==1&&i==1)m.autoSprint=!m.autoSprint;else if(c==1&&i==5)m.keystrokes=!m.keystrokes;
        else if(c==2&&i==1)m.worldTint=!m.worldTint;else if(c==2&&i==2)m.blockOutline=!m.blockOutline;else if(c==2&&i==3)m.crosshair=!m.crosshair;else if(c==2&&i==6)m.timeChanger=!m.timeChanger;else if(c==2&&i==4)m.fps=!m.fps;else if(c==2&&i==5)m.potionHud=!m.potionHud;
        else if(c==2&&i==7)NullUiSettings.worldParticles=!NullUiSettings.worldParticles;else if(c==2&&i==8)NullUiSettings.playerTrail=!NullUiSettings.playerTrail;else if(c==2&&i==9)NullUiSettings.toggleScreenStretch();
        else if(c==3&&i==6)m.totemCounter=!m.totemCounter;else if(c==3&&i==7)m.foodHud=!m.foodHud;else if(c==3&&i==8)m.xpHud=!m.xpHud;else if(c==3&&i==1)m.armorStatus=!m.armorStatus;else if(c==3&&i==2)m.armorHud=!m.armorHud;else if(c==3&&i==3)m.coordinates=!m.coordinates;else if(c==3&&i==4)m.durability=!m.durability;else if(c==3&&i==5)m.inventory=!m.inventory;
        else if(c==1&&i==2)m.sprintDisplay=!m.sprintDisplay;else if(c==1&&i==3)m.jumpStats=!m.jumpStats;else if(c==1&&i==4)m.speedGraph=!m.speedGraph;
        else if(c==4&&i==1)m.sessionInfo=!m.sessionInfo;else if(c==4&&i==2)m.clock=!m.clock;else if(c==4&&i==4)m.notifications=!m.notifications;
        else if(c==4&&i==3)m.arrayList=!m.arrayList;else if(c==4&&i==5)m.ping=!m.ping;else if(c==4&&i==6)m.directionHud=!m.directionHud;
        GlassVisualConfig.save();Notifier.module(new Module(categories()[c][i]),enabled(c,i));
    }
    private static void roundedFill(DrawContext ctx,int x,int y,int w,int h,int radius,int color){
        int r=Math.max(1,Math.min(radius,Math.min(w,h)/2));
        for(int row=0;row<h;row++){
            int edge=row<r?r-1-row:(row>=h-r?row-(h-r):-1);
            int inset=edge<0?0:r-(int)Math.floor(Math.sqrt(Math.max(0,r*r-edge*edge)));
            ctx.fill(x+inset,y+row,x+w-inset,y+row+1,color);
        }
    }
    private void drawSmallButton(DrawContext ctx,int bx,int by,int bw,int bh,String label,int accent){
        roundedFill(ctx,bx,by,bw,bh,6,0xFF293447);ctx.fill(bx,by,bx+2,by+bh,accent);
        ctx.drawCenteredTextWithShadow(textRenderer,label,bx+bw/2,by+7,WHITE);
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(super.mouseClicked(mx,my,button))return true;
        if(button!=0)return false;
        for(int i=0;i<categories().length;i++){int cy=y+57+i*37;if(mx>=x+8&&mx<=x+137&&my>=cy-5&&my<=cy+25){selected=i;return true;}}
        if(mx>=x+14&&mx<=x+126&&my>=y+h-89&&my<=y+h-66){NullUiSettings.toggleLanguage();GlassVisualConfig.save();init();return true;}
        if(mx>=x+14&&mx<=x+126&&my>=y+h-60&&my<=y+h-37){NullUiSettings.particles=!NullUiSettings.particles;GlassVisualConfig.save();return true;}
        if(mx>=x+164&&mx<=x+w-20&&my>=y+62&&my<=y+h-20){
            int row=(int)(my-(y+62))/54;int i=row+1;String q=search.getText().toLowerCase();
            int actual=0;for(int n=1;n<categories()[selected].length;n++){if(!q.isBlank()&&!categories()[selected][n].toLowerCase().contains(q))continue;if(actual++==row){if(isSupported(selected,n)){toggle(selected,n);return true;}break;}}
        }
        if(my>=y+20&&my<=y+42&&mx>=x+w-190&&mx<=x+w-142){NullUiSettings.setStretchMode(NullUiSettings.StretchMode.FOUR_THREE);GlassVisualConfig.save();return true;}
        if(my>=y+20&&my<=y+42&&mx>=x+w-138&&mx<=x+w-90){NullUiSettings.setStretchMode(NullUiSettings.StretchMode.SIXTEEN_NINE);GlassVisualConfig.save();return true;}
        if(my>=y+20&&my<=y+42&&mx>=x+w-86&&mx<=x+w-18){NullUiSettings.setStretchMode(NullUiSettings.StretchMode.MANUAL);GlassVisualConfig.save();return true;}
        return false;
    }
    @Override public boolean shouldPause(){return false;}
}
