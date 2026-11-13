package dev.glassvisual;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public final class NullUiSettings {
    public enum LayoutMode { FIT, STRETCH, FOUR_THREE }
    private static LayoutMode mode = LayoutMode.FIT;
    private static float scale = 1.0f;
    public static boolean particles = true;

    private NullUiSettings() {}
    public static LayoutMode mode() { return mode; }
    public static float scale() { return scale; }
    public static void setMode(LayoutMode value) { if (value != null) mode = value; }
    public static void setScale(float value) { scale = Math.max(0.75f, Math.min(1.25f, value)); }
    public static void adjustScale(float delta) { setScale(scale + delta); }

    // Changes the actual game window aspect ratio; GPU fullscreen stretching is controlled by the graphics driver.
    public static void setScreenRatio(int numerator, int denominator) {
        if (numerator <= 0 || denominator <= 0) return;
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        if (window.isFullscreen()) window.toggleFullscreen();
        int h = Math.max(480, window.getHeight());
        int w = Math.max(640, Math.round(h * (numerator / (float) denominator)));
        window.setWindowedSize(w, h);
    }
}