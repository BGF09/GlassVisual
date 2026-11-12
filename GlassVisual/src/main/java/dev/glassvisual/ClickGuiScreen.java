package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ClickGuiScreen extends Screen {
    private static final int PURPLE = 0xFFB34CFF;
    private static final int PANEL = 0xE610111D;
    private static final int CARD = 0xCC242238;

    public ClickGuiScreen() {
        super(Text.literal("Glass Visual"));
    }

    @Override
    protected void init() {
        clearChildren();
        int left = width / 2 - 150;
        int top = height / 2 - 100;

        addDrawableChild(ButtonWidget.builder(moduleLabel("Target HUD", GlassVisualClient.MODULES.targetHud),
                b -> GlassVisualClient.MODULES.targetHud = !GlassVisualClient.MODULES.targetHud)
                .dimensions(left + 18, top + 65, 126, 22).build());

        addDrawableChild(ButtonWidget.builder(moduleLabel("Keystrokes", GlassVisualClient.MODULES.keystrokes),
                b -> GlassVisualClient.MODULES.keystrokes = !GlassVisualClient.MODULES.keystrokes)
                .dimensions(left + 156, top + 65, 126, 22).build());

        addDrawableChild(ButtonWidget.builder(moduleLabel("Coordinates", GlassVisualClient.MODULES.coordinates),
                b -> GlassVisualClient.MODULES.coordinates = !GlassVisualClient.MODULES.coordinates)
                .dimensions(left + 18, top + 94, 126, 22).build());

        addDrawableChild(ButtonWidget.builder(moduleLabel("FPS Counter", GlassVisualClient.MODULES.fps),
                b -> GlassVisualClient.MODULES.fps = !GlassVisualClient.MODULES.fps)
                .dimensions(left + 156, top + 94, 126, 22).build());

        addDrawableChild(ButtonWidget.builder(moduleLabel("Purple Accent", GlassVisualClient.MODULES.glowAccent),
                b -> GlassVisualClient.MODULES.glowAccent = !GlassVisualClient.MODULES.glowAccent)
                .dimensions(left + 18, top + 123, 126, 22).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),
                b -> close())
                .dimensions(left + 156, top + 123, 126, 22).build());
    }

    private static Text moduleLabel(String name, boolean enabled) {
        return Text.literal((enabled ? "§d● " : "§7○ ") + name);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int left = width / 2 - 150;
        int top = height / 2 - 100;

        ctx.fill(left - 2, top - 2, left + 302, top + 202, 0x66B34CFF);
        ctx.fill(left, top, left + 300, top + 200, PANEL);
        ctx.fill(left + 1, top + 1, left + 299, top + 2, 0x55FFFFFF);
        ctx.fill(left, top + 35, left + 300, top + 36, 0x66B34CFF);

        ctx.drawText(textRenderer, "GLASS VISUAL", left + 18, top + 13, 0xFFFFFFFF, true);
        ctx.drawText(textRenderer, "1.21.4  /  FABRIC  /  CLIENT", left + 166, top + 14, 0xFFBDA7D8, false);
        ctx.drawText(textRenderer, "VISUAL MODULES", left + 18, top + 47, PURPLE, false);
        ctx.drawText(textRenderer, "RIGHT SHIFT  •  CLOSE", left + 172, top + 180, 0xFFAAA6BB, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
