package dev.glassvisual;

/** Screen-space anchors for draggable HUD elements. */
public final class HudLayout {
    public static int watermarkX = 12, watermarkY = 12;
    public static int fpsX = -1, fpsY = 12;
    public static int coordinatesX = 12, coordinatesY = -1;
    public static int keysX = 12, keysY = -1;
    public static int arrayListX = -1, arrayListY = 70;
    public static int armorX = 12, armorY = -1;
    public static int potionsX = -1, potionsY = 170;
    public static int pingX = -1, pingY = 42;
    public static int inventoryX = -1, inventoryY = -1;
    /** Top notification stack; negative X means horizontally centered. */
    public static int notificationsX = -1, notificationsY = 42;

    private HudLayout() {}
}
