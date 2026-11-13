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
            NullTheme.select(data.theme);
            NullUiSettings.particles = data.particles;
            NullUiSettings.setScale(data.uiScale);
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
        data.theme = NullTheme.selectedIndex();
        data.particles = NullUiSettings.particles;
        data.uiScale = NullUiSettings.scale();
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
        boolean particles = true;
        int theme;
        float uiScale = 1.0f;
    }
}
