package dev.glassvisual;

public final class NullTheme {
    private static final int ACCENT = 0xFF9B7CFF;
    private NullTheme() {}
    public static int accent() { return ACCENT; }
    public static int colorCount() { return 1; }
    public static int selectedIndex() { return 0; }
    public static void select(int index) { }
    public static int color(int index) { return ACCENT; }
}
