package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ClickGuiScreen extends Screen {
    private static final int ACCENT = 0xFFB56BFF;
    private static final int PANEL = 0xF012111D;
    private static final int CARD = 0xE91E1B2B;
    private static final int MUTED = 0xFFAAA4BC;

    public ClickGuiScreen() {
        super(Text.literal("Glass Visual"));
    }

    @Override
    protected void init() {
        clearChildren();
        int left = width / 2 - 174;
        int top = height / 2 - 116;

        addToggle("Target HUD", GlassVisualClient.MODULES.targetHud,
                left + 18, top + 91, () -> GlassVisualClient.MODULES.targetHud = !GlassVisualClient.MODULES.targetHud);
        addToggle("Keystrokes", GlassVisualClient.MODULES.keystrokes,
                left + 184, top + 91, () -> GlassVisualClient.MODULES.keystrokes = !GlassVisualClient.MODULES.keystrokes);
        addToggle("Coordinates", GlassVisualClient.MODULES.coordinates,
                left + 18, top + 139, () -> GlassVisualClient.MODULES.coordinates = !GlassVisualClient.MODULES.coordinates);
        addToggle("FPS Counter", GlassVisualClient.MODULES.fps,
                left + 184, top + 139, () -> GlassVisualClient.MODULES.fps = !GlassVisualClient.MODULES.fps);
        addToggle("Purple Accent", GlassVisualClient.MODULES.glowAccent,
                left + 18, top + 187, () -> GlassVisualClient.MODULES.glowAccent = !GlassVisualClient.MODULES.glowAccent);

        addDrawableChild(ButtonWidget.builder(Text.literal("DONE  •  ESC"), button -> close())
                .dimensions(left + 184, top + 187, 150, 24).build());
    }

    private void addToggle(String name, boolean enabled, int x, int y, Runnable action) {
        String state = enabled ? "§d●  ON" : "§7○  OFF";
        addDrawableChild(ButtonWidget.builder(Text.literal(name + "   " + state), button -> {
            action.run();
            init();
        }).dimensions(x, y, 150, 28).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int left = width / 2 - 174;
        int top = height / 2 - 116;

        ctx.fill(left - 2, top - 2, left + 350, top + 238, 0x665D347A);
        ctx.fill(left, top, left + 346, top + 234, PANEL);
        ctx.fill(left + 1, top + 1, left + 345, top + 2, 0x55FFFFFF);
        ctx.fill(left, top + 56, left + 346, top + 57, 0x887D4AA6);

        ctx.drawText(textRenderer, "GLASS", left + 18, top + 15, 0xFFFFFFFF, true);
        ctx.drawText(textRenderer, "VISUAL", left + 67, top + 15, ACCENT, true);
        ctx.drawText(textRenderer, "CLIENT EDITION  /  1.21.4", left + 192, top + 17, MUTED, false);
        ctx.drawText(textRenderer, "VISUAL MODULES", left + 18, top + 70, ACCENT, true);
        ctx.drawText(textRenderer, "Toggle an element to show or hide it in-game", left + 18, top + 80, MUTED, false);

        ctx.fill(left + 12, top + 112, left + 170, top + 169, CARD);
        ctx.fill(left + 178, top + 112, left + 336, top + 169, CARD);
        ctx.fill(left + 12, top + 160, left + 170, top + 217, CARD);
        ctx.fill(left + 178, top + 160, left + 336, top + 217, CARD);
        ctx.fill(left + 12, top + 208, left + 170, top + 226, CARD);
        ctx.drawText(textRenderer, "HUD & COMBAT", left + 18, top + 117, 0xFFD8C7F0, false);
        ctx.drawText(textRenderer, "SCREEN INFO", left + 184, top + 117, 0xFFD8C7F0, false);
        ctx.drawText(textRenderer, "THEME", left + 18, top + 165, 0xFFD8C7F0, false);
        ctx.drawText(textRenderer, "Glass Visual  •  Right Shift", left + 18, top + 220, MUTED, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
