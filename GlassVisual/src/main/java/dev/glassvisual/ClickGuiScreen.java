package dev.glassvisual;

import dev.glassvisual.NullModules.Action;
import dev.glassvisual.NullModules.Choice;
import dev.glassvisual.NullModules.GuiModule;
import dev.glassvisual.NullModules.Setting;
import dev.glassvisual.NullModules.Slider;
import dev.glassvisual.NullModules.Toggle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * NULL ClickGUI: dark graphite glass panels, categories on the left, module search, smooth animations and
 * real per-module settings. All geometry is recomputed from the current window size every frame, so the layout
 * is always inside the screen. It does nothing to the game image - Screen Stretch is a separate world-render module.
 */
public final class ClickGuiScreen extends Screen {
    // palette: graphite glass with the violet NULL accent
    private static final int PANEL = 0xD4141620, SIDE = 0xC80D0F16, CARD = 0xA024283A, CARD_HOVER = 0xB02D3347;
    private static final int TEXT = 0xFFF1F4FC, MUTED = 0xFF9AA3B5, DIM = 0xFF6B7385;
    private static final int SWITCH_OFF = 0xFF3A4152, TRACK = 0xFF343A48, FIELD = 0xA00A0C12;

    private static final long OPEN_NS = 170_000_000L, CLOSE_NS = 120_000_000L;
    private static final int CARD_TOP = 46, GAP = 8;

    private static final Identifier FONT = Identifier.of(GlassVisualClient.MOD_ID, "gui");
    private static final Identifier FONT_BOLD = Identifier.of(GlassVisualClient.MOD_ID, "gui_bold");
    private static final Map<String, Text> TEXTS = new HashMap<>();
    private static final Map<String, String> TRIMS = new HashMap<>();
    private static final int[][] INSETS = new int[17][];

    private int category;
    private String query = "";
    private float scroll, scrollTarget, maxScroll;
    private long lastFrame, openStart, closeStart;
    private boolean closing;
    private float ga = 0.0f; // global alpha of the open/close animation

    // geometry (recomputed every frame)
    private int px, py, pw, ph, sbw, cx0, cw, hdrY, ly0, ly1;
    private boolean compact;
    private int searchX, searchW;

    // cards
    private final List<GuiModule> view = new ArrayList<>();
    private int[] rx = new int[0], ry = new int[0], rw = new int[0], rh = new int[0];
    private int contentHeight;

    // slider dragging
    private Slider dragSlider;
    private int dragX0, dragW;

    public ClickGuiScreen() {
        super(Text.literal("NULL"));
        openStart = System.nanoTime();
        NullModules.syncAnimations();
    }

    // ------------------------------------------------------------------ screen plumbing

    @Override public boolean shouldPause() { return false; }

    @Override public void close() {
        if (!closing) { closing = true; closeStart = System.nanoTime(); }
    }

    @Override public void removed() {
        GlassVisualConfig.save();
        super.removed();
    }

    // ------------------------------------------------------------------ text helpers (readable Inter font, see assets/glassvisual/font)

    private static Text txt(String s, boolean bold) {
        if (TEXTS.size() > 700) TEXTS.clear();
        String key = (bold ? "b" : "r") + s;
        Text cached = TEXTS.get(key);
        if (cached == null) {
            cached = Text.literal(s).setStyle(Style.EMPTY.withFont(bold ? FONT_BOLD : FONT));
            TEXTS.put(key, cached);
        }
        return cached;
    }

    private int tw(String s, boolean bold) { return textRenderer.getWidth(txt(s, bold)); }

    private void text(DrawContext ctx, String s, int x, int y, int color, boolean bold) {
        ctx.drawText(textRenderer, txt(s, bold), x, y, fade(color), false);
    }

    private void textRight(DrawContext ctx, String s, int right, int y, int color, boolean bold) {
        text(ctx, s, right - tw(s, bold), y, color, bold);
    }

    /** Shortens a string with an ellipsis so that it fits into maxWidth using the real font metrics. */
    private String fit(String s, int maxWidth, boolean bold) {
        if (maxWidth <= 0) return "";
        String key = (bold ? "b" : "r") + maxWidth + "|" + s;
        String cached = TRIMS.get(key);
        if (cached != null) return cached;
        if (TRIMS.size() > 700) TRIMS.clear();
        String result = s;
        if (tw(s, bold) > maxWidth) {
            int end = s.length();
            while (end > 0 && tw(s.substring(0, end) + "…", bold) > maxWidth) end--;
            result = end <= 0 ? "" : s.substring(0, end) + "…";
        }
        TRIMS.put(key, result);
        return result;
    }

    // ------------------------------------------------------------------ drawing helpers

    private int fade(int color) {
        int a = (int) ((color >>> 24) * ga);
        if (a < 4) a = 4; // vanilla treats alpha below 4 as opaque
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private static int alpha(int rgb, int a) { return (Math.max(0, Math.min(255, a)) << 24) | (rgb & 0x00FFFFFF); }

    private static int mix(int c1, int c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int a = (int) ((c1 >>> 24) + ((c2 >>> 24) - (c1 >>> 24)) * t);
        int r = (int) (((c1 >> 16) & 0xFF) + (((c2 >> 16) & 0xFF) - ((c1 >> 16) & 0xFF)) * t);
        int g = (int) (((c1 >> 8) & 0xFF) + (((c2 >> 8) & 0xFF) - ((c1 >> 8) & 0xFF)) * t);
        int b = (int) ((c1 & 0xFF) + ((c2 & 0xFF) - (c1 & 0xFF)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int[] insets(int r) {
        if (INSETS[r] == null) {
            int[] a = new int[r];
            for (int i = 0; i < r; i++) {
                double dy = r - i - 0.5;
                double dx = Math.sqrt(Math.max(0.0, r * (double) r - dy * dy));
                a[i] = Math.max(0, (int) Math.round(r - dx));
            }
            INSETS[r] = a;
        }
        return INSETS[r];
    }

    /** Rounded rectangle: one fill for the body, one row per corner line. */
    private void rr(DrawContext c, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        int col = fade(color);
        r = Math.min(Math.min(r, 16), Math.min(w, h) / 2);
        if (r <= 1) { c.fill(x, y, x + w, y + h, col); return; }
        int[] in = insets(r);
        for (int i = 0; i < r; i++) {
            c.fill(x + in[i], y + i, x + w - in[i], y + i + 1, col);
            c.fill(x + in[i], y + h - 1 - i, x + w - in[i], y + h - i, col);
        }
        if (h > 2 * r) c.fill(x, y + r, x + w, y + h - r, col);
    }

    private void fillRect(DrawContext c, int x1, int y1, int x2, int y2, int color) { c.fill(x1, y1, x2, y2, fade(color)); }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static float clamp01(float v) { return v < 0.0f ? 0.0f : Math.min(v, 1.0f); }

    // ------------------------------------------------------------------ layout

    private void computeGeometry() {
        float scale = NullUiSettings.scale();
        pw = Math.min(Math.round(640 * scale), Math.max(width - 16, Math.min(width, 200)));
        ph = Math.min(Math.round(400 * scale), Math.max(height - 16, Math.min(height, 150)));
        px = (width - pw) / 2;
        py = (height - ph) / 2 + (int) ((1.0f - ga) * 14.0f);
        compact = pw < 440;
        sbw = compact ? 44 : 116;
        cx0 = px + sbw + 12;
        cw = Math.max(40, pw - sbw - 24);
        hdrY = py + 12;
        ly0 = py + 50;
        ly1 = py + ph - 12;
        searchW = Math.max(60, Math.min(170, cw / 2));
        searchX = cx0 + cw - searchW;
    }

    private void rebuildView() {
        view.clear();
        String q = query.trim().toLowerCase(java.util.Locale.ROOT);
        if (q.isEmpty()) view.addAll(NullModules.category(category));
        else for (GuiModule m : NullModules.all()) if (m.matches(q)) view.add(m);
        int n = view.size();
        if (rx.length < n) { rx = new int[n]; ry = new int[n]; rw = new int[n]; rh = new int[n]; }
    }

    private int cardHeight(GuiModule m) { return CARD_TOP + Math.round(m.settingsHeight() * m.expandAnim); }

    private void layoutCards() {
        int cols = cw >= 340 ? 2 : 1;
        int colW = (cw - GAP * (cols - 1)) / cols;
        int y = 0, col = 0, rowH = 0;
        for (int i = 0; i < view.size(); i++) {
            GuiModule m = view.get(i);
            int h = cardHeight(m);
            boolean span = cols == 1 || m.expanded || m.expandAnim > 0.01f;
            if (span) {
                if (col != 0) { y += rowH + GAP; col = 0; rowH = 0; }
                rx[i] = 0; ry[i] = y; rw[i] = cw; rh[i] = h;
                y += h + GAP;
            } else {
                rx[i] = col * (colW + GAP); ry[i] = y; rw[i] = colW; rh[i] = h;
                rowH = Math.max(rowH, h);
                if (++col == cols) { y += rowH + GAP; col = 0; rowH = 0; }
            }
        }
        if (col != 0) y += rowH + GAP;
        contentHeight = Math.max(0, y - GAP);
        maxScroll = Math.max(0, contentHeight - (ly1 - ly0));
        scrollTarget = Math.max(0.0f, Math.min(maxScroll, scrollTarget));
    }

    // ------------------------------------------------------------------ rendering

    @Override public void render(DrawContext ctx, int mx, int my, float delta) {
        long now = System.nanoTime();
        float dt = lastFrame == 0 ? 0.016f : Math.min(0.1f, (now - lastFrame) / 1.0e9f);
        lastFrame = now;

        float p;
        if (closing) {
            p = 1.0f - clamp01((now - closeStart) / (float) CLOSE_NS);
            if (p <= 0.0f) { if (client != null) client.setScreen(null); return; }
        } else {
            p = clamp01((now - openStart) / (float) OPEN_NS);
        }
        ga = 1.0f - (1.0f - p) * (1.0f - p) * (1.0f - p);

        super.render(ctx, mx, my, delta); // vanilla blurred + darkened world as the "glass" backdrop
        ctx.fill(0, 0, width, height, fade(0x38050610));

        computeGeometry();
        rebuildView();
        animate(dt);
        scroll += (scrollTarget - scroll) * (1.0f - (float) Math.exp(-dt * 16.0f));
        if (Math.abs(scrollTarget - scroll) < 0.05f) scroll = scrollTarget;
        layoutCards();

        int accent = NullTheme.accent();
        drawPanel(ctx, accent);
        drawSidebar(ctx, mx, my, accent);
        drawHeader(ctx, accent);
        drawCards(ctx, mx, my, accent);
    }

    private void animate(float dt) {
        float kOn = 1.0f - (float) Math.exp(-dt * 14.0f), kExp = 1.0f - (float) Math.exp(-dt * 12.0f);
        for (GuiModule m : NullModules.all()) {
            float tOn = m.enabled() ? 1.0f : 0.0f, tExp = m.expanded ? 1.0f : 0.0f;
            m.onAnim += (tOn - m.onAnim) * kOn;
            m.expandAnim += (tExp - m.expandAnim) * kExp;
            if (Math.abs(tOn - m.onAnim) < 0.004f) m.onAnim = tOn;
            if (Math.abs(tExp - m.expandAnim) < 0.004f) m.expandAnim = tExp;
        }
    }

    private void drawPanel(DrawContext ctx, int accent) {
        for (int i = 1; i <= 6; i++) rr(ctx, px - i, py - i + 4, pw + 2 * i, ph + 2 * i, 14 + i, 0x0C000000);   // soft shadow
        rr(ctx, px - 2, py - 2, pw + 4, ph + 4, 16, alpha(accent, 0x16));                                         // faint violet glow
        rr(ctx, px, py, pw, ph, 14, PANEL);
        ctx.fillGradient(px + 14, py + 1, px + pw - 14, py + 22, fade(0x16FFFFFF), fade(0x00FFFFFF));            // glass sheen
        fillRect(ctx, px + 14, py, px + pw - 14, py + 1, 0x38FFFFFF);                                             // top edge highlight
    }

    private void drawSidebar(DrawContext ctx, int mx, int my, int accent) {
        int sx = px + 8, sy = py + 8, sw = sbw - 8, sh = ph - 16;
        rr(ctx, sx, sy, sw, sh, 10, SIDE);
        if (compact) {
            text(ctx, "N", sx + (sw - tw("N", true)) / 2, sy + 12, accent, true);
        } else {
            text(ctx, "NULL", sx + 12, sy + 11, accent, true);
            text(ctx, "CLIENT", sx + 12 + tw("NULL", true) + 5, sy + 11, MUTED, false);
        }
        fillRect(ctx, sx + 10, sy + 30, sx + sw - 10, sy + 31, 0x22FFFFFF);

        int count = NullModules.categoryCount();
        for (int i = 0; i < count; i++) {
            int by = sy + 40 + i * 32;
            boolean active = query.isBlank() && i == category;
            boolean hover = inside(mx, my, sx + 6, by, sw - 12, 28);
            if (active) {
                rr(ctx, sx + 6, by, sw - 12, 28, 8, alpha(accent, 0x38));
                fillRect(ctx, sx + 6, by + 7, sx + 9, by + 21, accent);
            } else if (hover) {
                rr(ctx, sx + 6, by, sw - 12, 28, 8, 0x1CFFFFFF);
            }
            String name = NullModules.categoryName(i);
            int color = active ? TEXT : (hover ? TEXT : MUTED);
            if (compact) {
                String letter = name.substring(0, 1);
                text(ctx, letter, sx + (sw - tw(letter, active)) / 2, by + 10, color, active);
            } else {
                text(ctx, name, sx + 18, by + 10, color, active);
            }
        }

        int ly = sy + sh - 34;
        boolean hover = inside(mx, my, sx + 6, ly, sw - 12, 26);
        rr(ctx, sx + 6, ly, sw - 12, 26, 8, hover ? 0x30FFFFFF : 0x18FFFFFF);
        String lang = NullUiSettings.russian() ? "RU" : "EN";
        String label = compact ? lang : (NullUiSettings.russian() ? "Язык: " : "Language: ") + lang;
        text(ctx, label, sx + 6 + (sw - 12 - tw(label, false)) / 2, ly + 9, TEXT, false);
        if (showScale()) {
            int sy2 = ly - 32;
            rr(ctx, sx + 6, sy2, 24, 24, 7, inside(mx, my, sx + 6, sy2, 24, 24) ? 0x30FFFFFF : 0x18FFFFFF);
            rr(ctx, sx + sw - 30, sy2, 24, 24, 7, inside(mx, my, sx + sw - 30, sy2, 24, 24) ? 0x30FFFFFF : 0x18FFFFFF);
            text(ctx, "−", sx + 6 + (24 - tw("−", true)) / 2, sy2 + 8, TEXT, true);
            text(ctx, "+", sx + sw - 30 + (24 - tw("+", true)) / 2, sy2 + 8, TEXT, true);
            String pct = Math.round(NullUiSettings.scale() * 100) + "%";
            text(ctx, pct, sx + (sw - tw(pct, false)) / 2, sy2 + 8, MUTED, false);
        }
        if (!compact) text(ctx, "v1.2 · 1.21.4", sx + 12, ly - (showScale() ? 46 : 14), DIM, false);
    }

    /** The GUI scale control needs vertical room below the category list. */
    private boolean showScale() { return !compact && ph >= 330; }

    private void drawHeader(DrawContext ctx, int accent) {
        boolean searching = !query.isBlank();
        String title = searching ? (NullUiSettings.russian() ? "Поиск" : "Search") : NullModules.categoryName(category);
        int titleMax = Math.max(20, cw - searchW - 12);
        text(ctx, fit(title, titleMax, true), cx0, hdrY + 2, TEXT, true);
        int n = view.size();
        String sub = NullUiSettings.russian() ? n + " модулей" : n + (n == 1 ? " module" : " modules");
        text(ctx, fit(sub, titleMax, false), cx0, hdrY + 15, MUTED, false);

        boolean cursorOn = (System.nanoTime() / 450_000_000L) % 2 == 0;
        rr(ctx, searchX, hdrY, searchW, 26, 8, FIELD);
        rr(ctx, searchX, hdrY + 24, searchW, 2, 1, query.isEmpty() ? 0x22FFFFFF : alpha(accent, 0xB0));
        int innerW = searchW - 20;
        if (query.isEmpty()) {
            text(ctx, fit(NullUiSettings.russian() ? "Поиск модулей…" : "Search modules…", innerW, false), searchX + 10, hdrY + 9, DIM, false);
            if (cursorOn) fillRect(ctx, searchX + 9, hdrY + 7, searchX + 10, hdrY + 19, accent);
        } else {
            String shown = query;
            while (shown.length() > 1 && tw(shown, false) > innerW) shown = shown.substring(1);
            text(ctx, shown, searchX + 10, hdrY + 9, TEXT, false);
            if (cursorOn) fillRect(ctx, searchX + 10 + tw(shown, false) + 1, hdrY + 7, searchX + 10 + tw(shown, false) + 2, hdrY + 19, accent);
        }
    }

    private void drawCards(DrawContext ctx, int mx, int my, int accent) {
        ctx.enableScissor(cx0 - 3, ly0, cx0 + cw + 3, ly1);
        int off = ly0 - Math.round(scroll);
        boolean mouseInList = inside(mx, my, cx0, ly0, cw, ly1 - ly0);
        for (int i = 0; i < view.size(); i++) {
            int x = cx0 + rx[i], y = off + ry[i], w = rw[i], h = rh[i];
            if (y + h < ly0 || y > ly1) continue;
            drawCard(ctx, view.get(i), x, y, w, h, mouseInList && inside(mx, my, x, y, w, CARD_TOP), mx, my, mouseInList, accent);
        }
        if (view.isEmpty()) {
            text(ctx, NullUiSettings.russian() ? "Ничего не найдено" : "No modules found", cx0 + 4, ly0 + 8, MUTED, false);
        }
        ctx.disableScissor();

        if (maxScroll > 1.0f) { // scrollbar
            int trackH = ly1 - ly0;
            int thumbH = Math.max(18, (int) (trackH * (trackH / (float) (trackH + maxScroll))));
            int thumbY = ly0 + (int) ((trackH - thumbH) * (scroll / maxScroll));
            rr(ctx, cx0 + cw + 4, thumbY, 3, thumbH, 1, alpha(accent, 0x90));
        }
    }

    private void drawCard(DrawContext ctx, GuiModule m, int x, int y, int w, int h, boolean hover, int mx, int my, boolean mouseInList, int accent) {
        float on = m.onAnim;
        if (on > 0.02f) rr(ctx, x - 1, y - 1, w + 2, h + 2, 10, alpha(accent, (int) (on * 34)));
        rr(ctx, x, y, w, h, 9, mix(hover ? CARD_HOVER : CARD, alpha(accent, 0x70), on * 0.30f));
        if (on > 0.02f) rr(ctx, x, y + 9, 3, CARD_TOP - 18, 1, alpha(accent, (int) (on * 255)));

        boolean gear = m.hasSettings();
        int switchW = 30, switchX = x + w - 14 - switchW, switchY = y + (CARD_TOP - 16) / 2;
        int gearX = switchX - 10 - 18;
        int labelRight = (gear ? gearX : switchX) - 10;

        text(ctx, fit(m.name, labelRight - (x + 14), true), x + 14, y + 10, TEXT, true);
        text(ctx, fit(m.description(), labelRight - (x + 14), false), x + 14, y + 25, MUTED, false);

        rr(ctx, switchX, switchY, switchW, 16, 8, mix(SWITCH_OFF, accent, on));
        rr(ctx, switchX + 2 + Math.round(on * 14), switchY + 2, 12, 12, 6, 0xFFFFFFFF);

        if (gear) {
            boolean gearHover = mouseInList && inside(mx, my, gearX, y + (CARD_TOP - 18) / 2, 18, 18);
            int gy = y + (CARD_TOP - 18) / 2;
            rr(ctx, gearX, gy, 18, 18, 5, m.expanded ? alpha(accent, 0x55) : (gearHover ? 0x38FFFFFF : 0x1EFFFFFF));
            for (int l = 0; l < 3; l++) fillRect(ctx, gearX + 4, gy + 5 + l * 3, gearX + 14, gy + 6 + l * 3, m.expanded ? TEXT : MUTED);
        }

        if (gear && m.expandAnim > 0.02f) {
            ctx.enableScissor(x, Math.max(y + CARD_TOP, ly0), x + w, Math.min(y + h, ly1));
            fillRect(ctx, x + 12, y + CARD_TOP - 1, x + w - 12, y + CARD_TOP, 0x1CFFFFFF);
            int oy = y + CARD_TOP + 6;
            for (Setting s : m.settings) {
                if (!s.isVisible()) continue;
                drawSetting(ctx, s, x, oy, w, accent);
                oy += s.height();
            }
            ctx.disableScissor();
        }
    }

    private void drawSetting(DrawContext ctx, Setting s, int x, int oy, int w, int accent) {
        int left = x + 14, right = x + w - 14;
        if (s instanceof Toggle t) {
            text(ctx, fit(s.label(), right - left - 40, false), left, oy + 8, TEXT, false);
            boolean on = t.value();
            rr(ctx, right - 26, oy + 5, 26, 14, 7, on ? accent : SWITCH_OFF);
            rr(ctx, right - 26 + 2 + (on ? 12 : 0), oy + 7, 10, 10, 5, 0xFFFFFFFF);
        } else if (s instanceof Slider sl) {
            String value = sl.display();
            textRight(ctx, value, right, oy + 4, MUTED, false);
            text(ctx, fit(s.label(), right - left - tw(value, false) - 10, false), left, oy + 4, TEXT, false);
            int trackW = right - left, ty = oy + 21;
            int filled = Math.round(trackW * sl.fraction());
            rr(ctx, left, ty, trackW, 4, 2, TRACK);
            if (filled > 0) rr(ctx, left, ty, filled, 4, 2, accent);
            rr(ctx, left + filled - 5, ty - 3, 10, 10, 5, 0xFFFFFFFF);
        } else if (s instanceof Action) {
            rr(ctx, left, oy + 3, right - left, 24, 7, alpha(accent, 0x55));
            text(ctx, fit(s.label(), right - left - 10, true), left + (right - left - tw(fit(s.label(), right - left - 10, true), true)) / 2, oy + 10, TEXT, true);
        } else if (s instanceof Choice c) {
            int[] lay = choiceLayout(c, right);
            int labelMax = lay[0] - left - 10;
            text(ctx, fit(s.label(), labelMax, false), left, oy + 9, TEXT, false);
            for (int i = 0; i < c.count(); i++) {
                int bx = lay[i * 2], bw = lay[i * 2 + 1];
                boolean sel = i == c.selected();
                if (c.hasSwatches()) {
                    if (sel) rr(ctx, bx - 2, oy + 4, bw + 4, 18, 5, 0xFFFFFFFF);
                    rr(ctx, bx, oy + 6, bw, 14, 4, c.swatch(i));
                } else {
                    rr(ctx, bx, oy + 3, bw, 20, 6, sel ? alpha(accent, 0x60) : 0x22FFFFFF);
                    text(ctx, c.option(i), bx + (bw - tw(c.option(i), sel)) / 2, oy + 9, sel ? TEXT : MUTED, sel);
                }
            }
        }
    }

    /** x/width pairs of the option buttons of a choice, packed right-aligned against {@code right}. */
    private int[] choiceLayout(Choice c, int right) {
        int n = c.count();
        int[] out = new int[n * 2];
        int total = 0;
        for (int i = 0; i < n; i++) {
            int bw = c.hasSwatches() ? 14 : tw(c.option(i), true) + 16;
            out[i * 2 + 1] = bw;
            total += bw + (i < n - 1 ? (c.hasSwatches() ? 6 : 4) : 0);
        }
        int x = right - total;
        for (int i = 0; i < n; i++) {
            out[i * 2] = x;
            x += out[i * 2 + 1] + (c.hasSwatches() ? 6 : 4);
        }
        return out;
    }

    // ------------------------------------------------------------------ input

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (closing) return true;
        if (button != 0 && button != 1) return false;

        // sidebar
        int sx = px + 8, sy = py + 8, sw = sbw - 8, sh = ph - 16;
        if (button == 0) {
            for (int i = 0; i < NullModules.categoryCount(); i++) {
                if (inside(mx, my, sx + 6, sy + 40 + i * 32, sw - 12, 28)) {
                    category = i; query = ""; scroll = 0.0f; scrollTarget = 0.0f;
                    return true;
                }
            }
            if (showScale()) {
                int sy2 = sy + sh - 34 - 32;
                if (inside(mx, my, sx + 6, sy2, 24, 24)) { NullUiSettings.adjustScale(-0.05f); GlassVisualConfig.save(); return true; }
                if (inside(mx, my, sx + sw - 30, sy2, 24, 24)) { NullUiSettings.adjustScale(0.05f); GlassVisualConfig.save(); return true; }
            }
            if (inside(mx, my, sx + 6, sy + sh - 34, sw - 12, 26)) {
                NullUiSettings.toggleLanguage();
                GlassVisualConfig.save();
                return true;
            }
        }

        // module cards
        if (!inside(mx, my, cx0, ly0, cw, ly1 - ly0)) return true;
        int off = ly0 - Math.round(scroll);
        for (int i = 0; i < view.size(); i++) {
            int x = cx0 + rx[i], y = off + ry[i], w = rw[i], h = rh[i];
            if (!inside(mx, my, x, y, w, h)) continue;
            GuiModule m = view.get(i);
            if (my < y + CARD_TOP) {
                int switchX = x + w - 14 - 30, gearX = switchX - 10 - 18;
                boolean onGear = m.hasSettings() && inside(mx, my, gearX, y + (CARD_TOP - 18) / 2, 18, 18);
                if (button == 1 || onGear) { if (m.hasSettings()) m.expanded = !m.expanded; }
                else toggleModule(m);
                return true;
            }
            if (m.hasSettings() && m.expandAnim > 0.5f) clickSetting(m, x, y, w, mx, my);
            return true;
        }
        return true;
    }

    private void toggleModule(GuiModule m) {
        boolean value = !m.enabled();
        m.setEnabled(value);
        GlassVisualConfig.save();
        Notifier.module(new Module(m.name), value);
    }

    private void clickSetting(GuiModule m, int x, int y, int w, double mx, double my) {
        int oy = y + CARD_TOP + 6;
        int left = x + 14, right = x + w - 14;
        for (Setting s : m.settings) {
            if (!s.isVisible()) continue;
            int rowH = s.height();
            if (my >= oy && my < oy + rowH) {
                if (s instanceof Toggle t) {
                    t.flip();
                    GlassVisualConfig.save();
                } else if (s instanceof Action act) {
                    act.perform();
                    GlassVisualConfig.save();
                } else if (s instanceof Slider sl) {
                    dragSlider = sl; dragX0 = left; dragW = Math.max(1, right - left);
                    sl.setFraction((float) ((mx - dragX0) / dragW));
                } else if (s instanceof Choice c) {
                    int[] lay = choiceLayout(c, right);
                    for (int i = 0; i < c.count(); i++) {
                        int pad = c.hasSwatches() ? 3 : 0;
                        if (inside(mx, my, lay[i * 2] - pad, oy, lay[i * 2 + 1] + pad * 2, rowH)) {
                            c.select(i);
                            GlassVisualConfig.save();
                            break;
                        }
                    }
                }
                return;
            }
            oy += rowH;
        }
    }

    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragSlider != null) {
            dragSlider.setFraction((float) ((mx - dragX0) / dragW));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override public boolean mouseReleased(double mx, double my, int button) {
        if (dragSlider != null) {
            dragSlider = null;
            GlassVisualConfig.save();
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        scrollTarget = Math.max(0.0f, Math.min(maxScroll, scrollTarget - (float) vertical * 28.0f));
        return true;
    }

    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && !query.isEmpty()) { query = ""; scrollTarget = 0.0f; return true; }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !query.isEmpty()) {
            query = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 ? "" : query.substring(0, query.length() - 1);
            scrollTarget = 0.0f;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override public boolean charTyped(char chr, int modifiers) {
        if (chr < 32 || chr == 127 || query.length() >= 32) return super.charTyped(chr, modifiers);
        query += chr;
        scrollTarget = 0.0f;
        return true;
    }
}
