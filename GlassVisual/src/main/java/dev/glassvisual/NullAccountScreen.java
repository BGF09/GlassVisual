package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NullAccountScreen extends Screen {
    private final Screen parent;
    public NullAccountScreen(Screen parent) { super(Text.literal("NULL Account")); this.parent = parent; }
    @Override protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("BACK"), b -> client.setScreen(parent))
            .dimensions(width / 2 - 55, height / 2 + 48, 110, 26).build());
    }
    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fillGradient(0, 0, width, height, 0xFF090B11, 0xFF151A27);
        int left = width / 2 - 175, top = height / 2 - 72;
        ctx.fill(left, top, left + 350, top + 150, 0xEE111722);
        ctx.fill(left, top, left + 350, top + 3, NullTheme.accent());
        ctx.drawCenteredTextWithShadow(textRenderer, "NULL ACCOUNT", width / 2, top + 22, 0xFFF4F6FC);
        ctx.drawCenteredTextWithShadow(textRenderer, "Logged in as", width / 2, top + 57, 0xFF9AA6B2);
        ctx.drawCenteredTextWithShadow(textRenderer, NullAccountProfile.login(), width / 2, top + 76, 0xFFB98AFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "Manage sign-in in NULL Launcher", width / 2, top + 112, 0xFF7F899C);
        super.render(ctx, mouseX, mouseY, delta);
    }
    @Override public boolean shouldPause() { return false; }
}
