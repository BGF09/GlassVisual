package dev.glassvisual;
public final class NullTheme {
    private static final int[] COLORS = {0xFF9B7CFF,0xFF39D6C8,0xFFFF6FAE,0xFFFFB45E,0xFF7CB8FF,0xFFB7E36B};
    private static int selected = 0;
    private NullTheme() {}
    public static int accent() { return COLORS[selected]; }
    public static int colorCount() { return COLORS.length; }
    public static void select(int index) { if (index >= 0 && index < COLORS.length) selected = index; }
    public static int color(int index) { return COLORS[index]; }
}
