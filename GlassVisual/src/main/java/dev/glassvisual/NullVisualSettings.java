package dev.glassvisual;

/**
 * Persistent tuning of the NULL world-visual modules: World Particles and Player Trail (enable flags live in NullUiSettings).
 * This class intentionally has no Minecraft dependencies so the math can be tested in isolation.
 */
public final class NullVisualSettings {
    private NullVisualSettings() {}

    // ---------------------------------------------------------------- World Particles

    private static int particleDensity = 45;
    private static int particleRange = 14;
    private static float particleSize = 1.0f;
    private static boolean particleMoveSparks = true;

    public static int particleDensity() { return particleDensity; }
    public static void setParticleDensity(int v) { particleDensity = Math.round(clamp(v, 5, 100)); }
    public static int particleRange() { return particleRange; }
    public static void setParticleRange(int v) { particleRange = Math.round(clamp(v, 4, 32)); }
    public static float particleSize() { return particleSize; }
    public static void setParticleSize(float v) { particleSize = clamp(v, 0.5f, 2.0f); }
    public static boolean particleMoveSparks() { return particleMoveSparks; }
    public static void setParticleMoveSparks(boolean v) { particleMoveSparks = v; }

    // ---------------------------------------------------------------- Player Trail

    public static final int[] TRAIL_COLORS = {0x9B7CFF, 0x4FD8FF, 0xFF6FB5, 0x6BFFB8, 0xFFC857, 0xF1F4FC};
    public static final String[] TRAIL_COLOR_NAMES_EN = {"NULL Violet", "Cyan", "Rose", "Mint", "Gold", "White"};
    public static final String[] TRAIL_COLOR_NAMES_RU = {"Фиолетовый NULL", "Бирюзовый", "Розовый", "Мятный", "Золотой", "Белый"};

    private static int trailLength = 50;
    private static float trailWidth = 0.08f;
    private static boolean trailGlow = true;
    private static int trailGlowBrightness = 60;
    private static float trailFadeSpeed = 1.0f;
    private static int trailColor = 0;

    public static int trailLength() { return trailLength; }
    public static void setTrailLength(int v) { trailLength = Math.round(clamp(v, 10, 200)); }
    public static float trailWidth() { return trailWidth; }
    public static void setTrailWidth(float v) { trailWidth = clamp(v, 0.02f, 0.30f); }
    public static boolean trailGlow() { return trailGlow; }
    public static void setTrailGlow(boolean v) { trailGlow = v; }
    public static int trailGlowBrightness() { return trailGlowBrightness; }
    public static void setTrailGlowBrightness(int v) { trailGlowBrightness = Math.round(clamp(v, 10, 100)); }
    public static float trailFadeSpeed() { return trailFadeSpeed; }
    public static void setTrailFadeSpeed(float v) { trailFadeSpeed = clamp(v, 0.5f, 3.0f); }
    public static int trailColorIndex() { return trailColor; }
    public static void setTrailColorIndex(int v) { trailColor = Math.max(0, Math.min(TRAIL_COLORS.length - 1, v)); }
    public static int trailColorRgb() { return TRAIL_COLORS[trailColor]; }

    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}
