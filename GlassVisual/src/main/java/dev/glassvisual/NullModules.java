package dev.glassvisual;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Registry of everything the ClickGUI shows: categories, modules and their real settings. */
public final class NullModules {
    private static final String[] CATEGORY_RU = {"Бой", "Движение", "Визуалы", "Игрок", "Разное"};
    private static final String[] CATEGORY_EN = {"Combat", "Movement", "Visuals", "Player", "Misc"};
    public static final int VISUALS = 2;

    private static List<GuiModule> all;
    private static List<List<GuiModule>> byCategory;

    private NullModules() {}

    public static int categoryCount() { return CATEGORY_EN.length; }
    public static String categoryName(int index) { return (NullUiSettings.russian() ? CATEGORY_RU : CATEGORY_EN)[index]; }

    public static List<GuiModule> all() { ensure(); return all; }
    public static List<GuiModule> category(int index) { ensure(); return byCategory.get(index); }

    /** Snap every animation to the real state (used when the screen opens). */
    public static void syncAnimations() {
        for (GuiModule m : all()) {
            m.onAnim = m.enabled() ? 1.0f : 0.0f;
            m.expandAnim = m.expanded ? 1.0f : 0.0f;
        }
    }

    // ------------------------------------------------------------------ model

    public abstract static class Setting {
        public final String ru, en;
        private BooleanSupplier visible = () -> true;
        Setting(String ru, String en) { this.ru = ru; this.en = en; }
        public Setting visibleWhen(BooleanSupplier supplier) { this.visible = supplier; return this; }
        public boolean isVisible() { return visible.getAsBoolean(); }
        public String label() { return NullUiSettings.russian() ? ru : en; }
        public abstract int height();
    }

    public static final class Toggle extends Setting {
        final BooleanSupplier get; final Consumer<Boolean> set;
        Toggle(String ru, String en, BooleanSupplier get, Consumer<Boolean> set) { super(ru, en); this.get = get; this.set = set; }
        public boolean value() { return get.getAsBoolean(); }
        public void flip() { set.accept(!get.getAsBoolean()); }
        @Override public int height() { return 24; }
    }

    public static final class Slider extends Setting {
        public final float min, max, step;
        final Supplier<Float> get; final Consumer<Float> set; final Function<Float, String> format;
        Slider(String ru, String en, float min, float max, float step, Supplier<Float> get, Consumer<Float> set, Function<Float, String> format) {
            super(ru, en); this.min = min; this.max = max; this.step = step; this.get = get; this.set = set; this.format = format;
        }
        public float value() { return get.get(); }
        public float fraction() { return max > min ? Math.max(0.0f, Math.min(1.0f, (value() - min) / (max - min))) : 0.0f; }
        public void setFraction(float fraction) {
            float raw = min + (max - min) * Math.max(0.0f, Math.min(1.0f, fraction));
            float snapped = step > 0.0f ? Math.round((raw - min) / step) * step + min : raw;
            set.accept(Math.max(min, Math.min(max, snapped)));
        }
        public String display() { return format.apply(value()); }
        @Override public int height() { return 32; }
    }

    public static final class Choice extends Setting {
        final String[] optionsRu, optionsEn; final int[] swatches; final IntSupplier get; final IntConsumer set;
        Choice(String ru, String en, String[] optionsRu, String[] optionsEn, int[] swatches, IntSupplier get, IntConsumer set) {
            super(ru, en); this.optionsRu = optionsRu; this.optionsEn = optionsEn; this.swatches = swatches; this.get = get; this.set = set;
        }
        public int count() { return optionsEn.length; }
        public String option(int i) { return (NullUiSettings.russian() ? optionsRu : optionsEn)[i]; }
        public int swatch(int i) { return swatches == null ? 0 : 0xFF000000 | swatches[i]; }
        public boolean hasSwatches() { return swatches != null; }
        public int selected() { return get.getAsInt(); }
        public void select(int i) { set.accept(i); }
        @Override public int height() { return 28; }
    }

    public static final class Action extends Setting {
        final Runnable run;
        Action(String ru, String en, Runnable run) { super(ru, en); this.run = run; }
        public void perform() { run.run(); }
        @Override public int height() { return 30; }
    }

    public static final class GuiModule {
        public final int category;
        public final String name, descRu, descEn;
        final BooleanSupplier get; final Consumer<Boolean> set;
        public final List<Setting> settings = new ArrayList<>();
        public float onAnim, expandAnim;
        public boolean expanded;

        GuiModule(int category, String name, String descRu, String descEn, BooleanSupplier get, Consumer<Boolean> set) {
            this.category = category; this.name = name; this.descRu = descRu; this.descEn = descEn; this.get = get; this.set = set;
        }
        public boolean enabled() { return get.getAsBoolean(); }
        public void setEnabled(boolean value) { set.accept(value); }
        public String description() { return NullUiSettings.russian() ? descRu : descEn; }
        public boolean hasSettings() { return !settings.isEmpty(); }

        public int settingsHeight() {
            int total = 0;
            for (Setting s : settings) if (s.isVisible()) total += s.height();
            return total + 12;
        }

        public boolean matches(String lowerQuery) {
            return name.toLowerCase(Locale.ROOT).contains(lowerQuery)
                || descRu.toLowerCase(Locale.ROOT).contains(lowerQuery)
                || descEn.toLowerCase(Locale.ROOT).contains(lowerQuery);
        }

        GuiModule with(Setting... items) { for (Setting s : items) settings.add(s); return this; }
    }

    // ------------------------------------------------------------------ registry

    private static void ensure() {
        if (all != null) return;
        all = new ArrayList<>();
        byCategory = new ArrayList<>();
        for (int i = 0; i < CATEGORY_EN.length; i++) byCategory.add(new ArrayList<>());
        var m = GlassVisualClient.MODULES;

        // Combat
        add(0, "Aim Assist", "Мягкая доводка прицела при зажатой атаке", "Gentle aim correction while attack is held", () -> m.aimAssist, v -> m.aimAssist = v);
        add(0, "Hit Visuals", "Короткий маркер при потере здоровья целью", "Brief marker when the focused target loses health", () -> m.hitVisuals, v -> m.hitVisuals = v);
        add(0, "Target HUD", "Цель под прицелом: здоровье, дистанция и имя", "Target under crosshair: health, distance and name", () -> m.targetHud, v -> m.targetHud = v);
        add(0, "Combat HUD", "Предмет в руке и индикатор перезарядки удара", "Held item and attack cooldown indicator", () -> m.combatHud, v -> m.combatHud = v);
        add(0, "HUD Particles", "Лёгкие 2D-частицы NULL поверх HUD", "Lightweight 2D NULL particles over the HUD", () -> NullUiSettings.particles, v -> NullUiSettings.particles = v);

        // Movement
        add(1, "Auto Sprint", "Автоматически бежать при движении вперёд", "Automatically sprint while moving forward", () -> m.autoSprint, v -> m.autoSprint = v);
        add(1, "Sprint Display", "Показывает, включён ли спринт", "Shows whether sprint is active", () -> m.sprintDisplay, v -> m.sprintDisplay = v);
        add(1, "Jump Stats", "Считает прыжки за текущую сессию", "Counts jumps in this session", () -> m.jumpStats, v -> m.jumpStats = v);
        add(1, "Speed Graph", "График текущей скорости игрока", "Live player movement speed graph", () -> m.speedGraph, v -> m.speedGraph = v);
        add(1, "Keystrokes", "Отображение нажатых клавиш", "Show pressed movement keys", () -> m.keystrokes, v -> m.keystrokes = v);

        // Visuals: the three new world modules come first
        add(VISUALS, "Screen Stretch", "Растягивает изображение игрового мира. HUD, текст и меню не меняются",
            "Stretches the rendered 3D world. HUD, text and menus stay untouched",
            () -> NullUiSettings.stretchMode() != NullUiSettings.StretchMode.OFF, NullUiSettings::setScreenStretchEnabled)
            .with(
                new Choice("Режим", "Mode", new String[]{"4:3", "16:9", "Растяжка"}, new String[]{"4:3", "16:9", "Stretch"}, null,
                    NullUiSettings::stretchPresetIndex, NullUiSettings::selectStretchPreset),
                new Slider("Растяжение по горизонтали", "Horizontal stretch", NullUiSettings.STRETCH_MIN, NullUiSettings.STRETCH_MAX, 0.01f,
                    NullUiSettings::manualStretch, NullUiSettings::setManualStretch, v -> String.format(Locale.ROOT, "%.2fx", v))
                    .visibleWhen(() -> NullUiSettings.stretchPresetIndex() == 2));
        add(VISUALS, "World Particles", "Светящиеся 3D-частицы NULL вокруг игрока", "Glowing NULL 3D particles around the player",
            () -> NullUiSettings.worldParticles, v -> NullUiSettings.worldParticles = v)
            .with(
                new Slider("Плотность", "Density", 5, 100, 1, () -> (float) NullVisualSettings.particleDensity(),
                    v -> NullVisualSettings.setParticleDensity(Math.round(v)), v -> String.format(Locale.ROOT, "%d%%", Math.round(v))),
                new Slider("Дальность", "Range", 4, 32, 1, () -> (float) NullVisualSettings.particleRange(),
                    v -> NullVisualSettings.setParticleRange(Math.round(v)), v -> Math.round(v) + (NullUiSettings.russian() ? " бл." : " bl.")),
                new Slider("Размер", "Size", 0.5f, 2.0f, 0.1f, NullVisualSettings::particleSize,
                    NullVisualSettings::setParticleSize, v -> String.format(Locale.ROOT, "%.1fx", v)),
                new Toggle("Искры при движении", "Movement sparks", NullVisualSettings::particleMoveSparks, NullVisualSettings::setParticleMoveSparks));
        add(VISUALS, "Player Trail", "Светящийся шлейф за игроком в игровом мире", "Glowing 3D trail behind the player",
            () -> NullUiSettings.playerTrail, v -> NullUiSettings.playerTrail = v)
            .with(
                new Slider("Длина", "Length", 10, 200, 1, () -> (float) NullVisualSettings.trailLength(),
                    v -> NullVisualSettings.setTrailLength(Math.round(v)), v -> String.format(Locale.ROOT, "%.1f s", Math.round(v) * 0.05f)),
                new Slider("Толщина", "Thickness", 0.02f, 0.30f, 0.01f, NullVisualSettings::trailWidth,
                    NullVisualSettings::setTrailWidth, v -> String.format(Locale.ROOT, "%.2f", v)),
                new Slider("Скорость исчезновения", "Fade speed", 0.5f, 3.0f, 0.1f, NullVisualSettings::trailFadeSpeed,
                    NullVisualSettings::setTrailFadeSpeed, v -> String.format(Locale.ROOT, "%.1fx", v)),
                new Toggle("Свечение", "Glow", NullVisualSettings::trailGlow, NullVisualSettings::setTrailGlow),
                new Slider("Яркость свечения", "Glow brightness", 10, 100, 1, () -> (float) NullVisualSettings.trailGlowBrightness(),
                    v -> NullVisualSettings.setTrailGlowBrightness(Math.round(v)), v -> String.format(Locale.ROOT, "%d%%", Math.round(v)))
                    .visibleWhen(NullVisualSettings::trailGlow),
                new Choice("Цвет", "Color", NullVisualSettings.TRAIL_COLOR_NAMES_RU, NullVisualSettings.TRAIL_COLOR_NAMES_EN,
                    NullVisualSettings.TRAIL_COLORS, NullVisualSettings::trailColorIndex, NullVisualSettings::setTrailColorIndex));
        add(VISUALS, "World Tint", "Лёгкий цветовой оттенок игрового экрана", "Subtle color tint over the game view", () -> m.worldTint, v -> m.worldTint = v);
        add(VISUALS, "Block Outline", "Цветная обводка блока под прицелом", "Accent outline around the targeted block", () -> m.blockOutline, v -> m.blockOutline = v);
        add(VISUALS, "Crosshair", "Акцентный прицел NULL поверх стандартного", "NULL accent reticle over the vanilla crosshair", () -> m.crosshair, v -> m.crosshair = v);
        add(VISUALS, "FPS Counter", "Текущий FPS в компактной панели", "Current FPS in a compact panel", () -> m.fps, v -> m.fps = v);
        add(VISUALS, "Potion HUD", "Эффекты и время действия", "Active effects and remaining time", () -> m.potionHud, v -> m.potionHud = v);
        add(VISUALS, "Always Night", "Постоянная ночь только на твоём клиенте", "Always night on your client only", () -> m.timeChanger, v -> m.timeChanger = v);
        add(VISUALS, "Legit ESP", "Обводка игроков только при прямой видимости + HP над головой", "Player outline and health above head only when visible", () -> m.legitEsp, v -> m.legitEsp = v);

        // Player
        add(3, "Armor Status", "Процент прочности каждой части брони", "Durability percentage for each armor piece", () -> m.armorStatus, v -> m.armorStatus = v);
        add(3, "Armor HUD", "Броня и прочность предметов", "Equipped armor and durability", () -> m.armorHud, v -> m.armorHud = v);
        add(3, "Coordinates", "Координаты игрока", "Player coordinates", () -> m.coordinates, v -> m.coordinates = v);
        add(3, "Durability", "Прочность предмета в основной руке", "Main-hand item durability", () -> m.durability, v -> m.durability = v);
        add(3, "Inventory", "Название выбранного предмета и количество", "Selected item name and stack count", () -> m.inventory, v -> m.inventory = v);
        add(3, "Hand Position", "Положение руки и предмета: смещение по осям (NumPad 4/6, 8/2, 7/9)", "Hand and held-item offset per axis (NumPad 4/6, 8/2, 7/9)", () -> m.handPosition, v -> m.handPosition = v)
            .with(
                new Slider("Смещение X", "Offset X", -0.35f, 0.35f, 0.005f, NullHandSettings::x,
                    v -> NullHandSettings.set(v, NullHandSettings.y(), NullHandSettings.z()), v -> String.format(Locale.ROOT, "%.3f", v)),
                new Slider("Смещение Y", "Offset Y", -0.35f, 0.35f, 0.005f, NullHandSettings::y,
                    v -> NullHandSettings.set(NullHandSettings.x(), v, NullHandSettings.z()), v -> String.format(Locale.ROOT, "%.3f", v)),
                new Slider("Смещение Z", "Offset Z", -0.35f, 0.35f, 0.005f, NullHandSettings::z,
                    v -> NullHandSettings.set(NullHandSettings.x(), NullHandSettings.y(), v), v -> String.format(Locale.ROOT, "%.3f", v)),
                new Action("Сбросить XYZ", "Reset XYZ", () -> NullHandSettings.set(0.0f, 0.0f, 0.0f)));
        add(3, "Totem Counter", "Считает тотемы во всём инвентаре", "Counts totems in your inventory", () -> m.totemCounter, v -> m.totemCounter = v);
        add(3, "Food HUD", "Показывает голод и насыщение", "Shows hunger and saturation", () -> m.foodHud, v -> m.foodHud = v);
        add(3, "XP HUD", "Уровень и прогресс опыта", "Experience level and progress", () -> m.xpHud, v -> m.xpHud = v);

        // Misc
        add(4, "Session Info", "Время текущей игровой сессии", "Current play session duration", () -> m.sessionInfo, v -> m.sessionInfo = v);
        add(4, "Clock", "Локальные часы в HUD", "Local clock in the HUD", () -> m.clock, v -> m.clock = v);
        add(4, "ArrayList", "Список активных визуальных модулей", "List of enabled visual modules", () -> m.arrayList, v -> m.arrayList = v);
        add(4, "Notifications", "Уведомления о переключении модулей", "Module toggle notifications", () -> m.notifications, v -> m.notifications = v);
        add(4, "Ping", "Задержка до сервера", "Server latency", () -> m.ping, v -> m.ping = v);
        add(4, "Direction", "Показывает направление взгляда", "Shows your current facing direction", () -> m.directionHud, v -> m.directionHud = v);
    }

    private static GuiModule add(int category, String name, String ru, String en, BooleanSupplier get, Consumer<Boolean> set) {
        GuiModule module = new GuiModule(category, name, ru, en, get, set);
        all.add(module);
        byCategory.get(category).add(module);
        return module;
    }
}
