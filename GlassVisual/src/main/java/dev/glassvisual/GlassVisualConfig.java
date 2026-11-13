package dev.glassvisual;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class GlassVisualConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("glassvisual.json");
    private GlassVisualConfig() {}

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;
        try {
            Data d = GSON.fromJson(Files.readString(FILE), Data.class);
            if (d == null) return;
            var m = GlassVisualClient.MODULES;
            m.targetHud=d.targetHud; m.keystrokes=d.keystrokes; m.coordinates=d.coordinates; m.fps=d.fps;
            m.glowAccent=d.glowAccent; m.arrayList=d.arrayList; m.armorHud=d.armorHud; m.potionHud=d.potionHud; m.ping=d.ping; m.autoSprint=d.autoSprint;
            m.sprintDisplay=d.sprintDisplay; m.jumpStats=d.jumpStats; m.speedGraph=d.speedGraph; m.sessionInfo=d.sessionInfo; m.clock=d.clock;
            m.hitVisuals=d.hitVisuals; m.combatHud=d.combatHud; m.worldTint=d.worldTint; m.crosshair=d.crosshair;
            m.durability=d.durability; m.inventory=d.inventory; m.notifications=d.notifications; m.armorStatus=d.armorStatus; m.timeChanger=d.timeChanger; m.blockOutline=d.blockOutline; m.aimAssist=d.aimAssist; m.directionHud=d.directionHud; m.totemCounter=d.totemCounter; m.foodHud=d.foodHud; m.xpHud=d.xpHud;
            NullTheme.select(d.theme);
            NullUiSettings.particles=d.particles;
            NullUiSettings.setScale(d.uiScale);
            NullUiSettings.setLanguage(d.language == null ? "ru" : d.language);
            HudLayout.watermarkX=d.watermarkX; HudLayout.watermarkY=d.watermarkY;
            HudLayout.fpsX=d.fpsX; HudLayout.fpsY=d.fpsY;
            HudLayout.coordinatesX=d.coordinatesX; HudLayout.coordinatesY=d.coordinatesY;
            HudLayout.keysX=d.keysX; HudLayout.keysY=d.keysY;
            HudLayout.arrayListX=d.arrayListX; HudLayout.arrayListY=d.arrayListY;
            HudLayout.armorX=d.armorX; HudLayout.armorY=d.armorY;
            HudLayout.potionsX=d.potionsX; HudLayout.potionsY=d.potionsY;
            HudLayout.pingX=d.pingX; HudLayout.pingY=d.pingY;
            try { NullUiSettings.setMode(d.layoutMode == null ? NullUiSettings.LayoutMode.FIT : NullUiSettings.LayoutMode.valueOf(d.layoutMode)); }
            catch (IllegalArgumentException ignored) { NullUiSettings.setMode(NullUiSettings.LayoutMode.FIT); }
        } catch (Exception e) { System.err.println("[NULL] Config load failed: " + e.getMessage()); }
    }

    public static void save() {
        Data d=new Data(); var m=GlassVisualClient.MODULES;
        d.targetHud=m.targetHud; d.keystrokes=m.keystrokes; d.coordinates=m.coordinates; d.fps=m.fps;
        d.glowAccent=m.glowAccent; d.arrayList=m.arrayList; d.armorHud=m.armorHud; d.potionHud=m.potionHud; d.ping=m.ping; d.autoSprint=m.autoSprint;
        d.sprintDisplay=m.sprintDisplay; d.jumpStats=m.jumpStats; d.speedGraph=m.speedGraph; d.sessionInfo=m.sessionInfo; d.clock=m.clock;
        d.hitVisuals=m.hitVisuals; d.combatHud=m.combatHud; d.worldTint=m.worldTint; d.crosshair=m.crosshair;
        d.durability=m.durability; d.inventory=m.inventory; d.notifications=m.notifications; d.armorStatus=m.armorStatus; d.timeChanger=m.timeChanger; d.blockOutline=m.blockOutline; d.aimAssist=m.aimAssist; d.directionHud=m.directionHud; d.totemCounter=m.totemCounter; d.foodHud=m.foodHud; d.xpHud=m.xpHud;
        d.theme=NullTheme.selectedIndex(); d.particles=NullUiSettings.particles; d.uiScale=NullUiSettings.scale();
        d.layoutMode=NullUiSettings.mode().name(); d.language=NullUiSettings.language();
        d.watermarkX=HudLayout.watermarkX; d.watermarkY=HudLayout.watermarkY;
        d.fpsX=HudLayout.fpsX; d.fpsY=HudLayout.fpsY; d.coordinatesX=HudLayout.coordinatesX; d.coordinatesY=HudLayout.coordinatesY;
        d.keysX=HudLayout.keysX; d.keysY=HudLayout.keysY; d.arrayListX=HudLayout.arrayListX; d.arrayListY=HudLayout.arrayListY;
        d.armorX=HudLayout.armorX; d.armorY=HudLayout.armorY; d.potionsX=HudLayout.potionsX; d.potionsY=HudLayout.potionsY;
        d.pingX=HudLayout.pingX; d.pingY=HudLayout.pingY;
        try { Files.createDirectories(FILE.getParent()); Files.writeString(FILE,GSON.toJson(d)); }
        catch(IOException e) { System.err.println("[NULL] Config save failed: " + e.getMessage()); }
    }

    private static final class Data {
        boolean targetHud=true, keystrokes, coordinates, fps, glowAccent=true, arrayList=true, armorHud, potionHud, ping, autoSprint, sprintDisplay, jumpStats, speedGraph, sessionInfo, clock, hitVisuals, combatHud, worldTint, crosshair, durability, inventory;
        boolean notifications=true, armorStatus, timeChanger, blockOutline, aimAssist, directionHud, totemCounter, foodHud, xpHud;
        boolean particles=true; int theme; float uiScale=1.0f; String layoutMode="FIT", language="ru";
        int watermarkX=12,watermarkY=12,fpsX=-1,fpsY=12,coordinatesX=12,coordinatesY=-1,keysX=12,keysY=-1;
        int arrayListX=-1,arrayListY=70,armorX=12,armorY=-1,potionsX=-1,potionsY=170,pingX=-1,pingY=42;
    }
}
