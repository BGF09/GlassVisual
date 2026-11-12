package dev.glassvisual;

public final class NullUiSettings {
    public enum LayoutMode { FIT, STRETCH, FOUR_THREE }
    private static LayoutMode mode = LayoutMode.FIT;
    private static float scale = 1.0f;
    private NullUiSettings() {}
    public static LayoutMode mode() { return mode; }
    public static float scale() { return scale; }
    public static void setMode(LayoutMode value) { mode = value; }
    public static void adjustScale(float delta) { scale = Math.max(0.75f, Math.min(1.25f, scale + delta)); }
}
