package dev.glassvisual;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Persists client-side visual settings in config/glassvisual.json. */
public final class GlassVisualConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("glassvisual.json");

    private GlassVisualConfig() {}

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;
        try {
            Data data = GSON.fromJson(Files.readString(FILE), Data.class);
            if (data == null) return;
            GlassVisualClient.MODULES.targetHud = data.targetHud;
            GlassVisualClient.MODULES.keystrokes = data.keystrokes;
            GlassVisualClient.MODULES.coordinates = data.coordinates;
            GlassVisualClient.MODULES.fps = data.fps;
            GlassVisualClient.MODULES.glowAccent = data.glowAccent;
            GlassVisualClient.MODULES.arrayList = data.arrayList;
            GlassVisualClient.MODULES.armorHud = data.armorHud;
            GlassVisualClient.MODULES.potionHud = data.potionHud;
            GlassVisualClient.MODULES.ping = data.ping;
            NullTheme.select(data.theme);
            NullUiSettings.particles = data.particles;
            NullUiSettings.setScale(data.uiScale);
            HudLayout.watermarkX = data.watermarkX;
            HudLayout.watermarkY = data.watermarkY;
            HudLayout.fpsX = data.fpsX;
            HudLayout.fpsY = data.fpsY;
            HudLayout.coordinatesX = data.coordinatesX;
            HudLayout.coordinatesY = data.coordinatesY;
            HudLayout.keysX = data.keysX;
            HudLayout.keysY = data.keysY;
            HudLayout.arrayListX = data.arrayListX;
            HudLayout.arrayListY = data.arrayListY;
            HudLayout.armorX = data.armorX;
            HudLayout.armorY = data.armorY;
            HudLayout.potionsX = data.potionsX;
            HudLayout.potionsY = data.potionsY;
            HudLayout.pingX = data.pingX;
            HudLayout.pingY = data.pingY;
            try {
                NullUiSettings.setMode(data.layoutMode == null
                    ? NullUiSettings.LayoutMode.FIT
                    : NullUiSettings.LayoutMode.valueOf(data.layoutMode));
            } catch (IllegalArgumentException ignored) {
                NullUiSettings.setMode(NullUiSettings.LayoutMode.FIT);
            }
        } catch (Exception exception) {
            System.err.println("[GlassVisual] Could not load config: " + exception.getMessage());
        }
    }

    public static void save() {
        Data data = new Data();
        data.targetHud = GlassVisualClient.MODULES.targetHud;
        data.keystrokes = GlassVisualClient.MODULES.keystrokes;
        data.coordinates = GlassVisualClient.MODULES.coordinates;
        data.fps = GlassVisualClient.MODULES.fps;
        data.glowAccent = GlassVisualClient.MODULES.glowAccent;
        data.arrayList = GlassVisualClient.MODULES.arrayList;
        data.armorHud = GlassVisualClient.MODULES.armorHud;
        data.potionHud = GlassVisualClient.MODULES.potionHud;
        data.ping = GlassVisualClient.MODULES.ping;
        data.theme = NullTheme.selectedIndex();
        data.particles = NullUiSettings.particles;
        data.uiScale = NullUiSettings.scale();
        data.layoutMode = NullUiSettings.mode().name();
        data.watermarkX = HudLayout.watermarkX;
        data.watermarkY = HudLayout.watermarkY;
        data.fpsX = HudLayout.fpsX;
        data.fpsY = HudLayout.fpsY;
        data.coordinatesX = HudLayout.coordinatesX;
        data.coordinatesY = HudLayout.coordinatesY;
        data.keysX = HudLayout.keysX;
        data.keysY = HudLayout.keysY;
        data.arrayListX = HudLayout.arrayListX;
        data.arrayListY = HudLayout.arrayListY;
        data.armorX = HudLayout.armorX;
        data.armorY = HudLayout.armorY;
        data.potionsX = HudLayout.potionsX;
        data.potionsY = HudLayout.potionsY;
        data.pingX = HudLayout.pingX;
        data.pingY = HudLayout.pingY;
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(data));
        } catch (IOException exception) {
            System.err.println("[GlassVisual] Could not save config: " + exception.getMessage());
        }
    }

    private static final class Data {
        boolean targetHud = true;
        boolean keystrokes;
        boolean coordinates;
        boolean fps;
        boolean glowAccent = true;
        boolean arrayList = true;
        boolean armorHud;
        boolean potionHud;
        boolean ping;
        boolean particles = true;
        int theme;
        float uiScale = 1.0f;
        String layoutMode = "FIT";
        int watermarkX = 12, watermarkY = 12;
        int fpsX = -1, fpsY = 12;
        int coordinatesX = 12, coordinatesY = -1;
        int keysX = 12, keysY = -1;
        int arrayListX = -1, arrayListY = 70;
        int armorX = 12, armorY = -1;
        int potionsX = -1, potionsY = 170;
        int pingX = -1, pingY = 42;
    }
}
