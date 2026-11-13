package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class NullMainMenuScreen extends Screen {
    public NullMainMenuScreen() { super(Text.literal("NULL")); }
    @Override protected void init() {
        int center = width / 2, y = height / 2 + 26, bw = Math.min(250, width - 36), x = center - bw / 2;
        addDrawableChild(menuButton("◈  Singleplayer", () -> client.setScreen(new SelectWorldScreen(this)), x, y, bw));
        addDrawableChild(menuButton("◎  Multiplayer", () -> client.setScreen(new MultiplayerScreen(this)), x, y + 36, bw));
        addDrawableChild(menuButton("♙  Account Manager", () -> client.setScreen(new NullAccountScreen(this)), x, y + 72, bw));
        addDrawableChild(menuButton("⚙  Settings", () -> client.setScreen(new OptionsScreen(this, client.options)), x, y + 108, bw));
        addDrawableChild(menuButton("⇥  Quit", () -> client.scheduleStop(), center - 48, y + 150, 96));
    }
    private ButtonWidget menuButton(String label, Runnable action, int x, int y, int w) {
        return ButtonWidget.builder(Text.literal(label), b -> action.run()).dimensions(x, y, w, 29).build();
    }
    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int accent = NullTheme.accent();
        ctx.fillGradient(0, 0, width, height, 0xFF090B11, 0xFF151A27);
        ctx.fill(width / 2 - 1, 0, width / 2 + 1, height, 0x11FFFFFF);
        ctx.fill(0, height / 2 - 1, width, height / 2 + 1, 0x223C4654);
        ctx.fill(width / 2 - 190, height / 2 - 190, width / 2 + 190, height / 2 + 205, 0x66111622);
        ctx.fill(width / 2 - 190, height / 2 - 190, width / 2 + 190, height / 2 - 187, accent);
        ctx.drawCenteredTextWithShadow(textRenderer, "N", width / 2, height / 2 - 166, 0xFF8492E8);
        ctx.drawCenteredTextWithShadow(textRenderer, "NULL", width / 2, height / 2 - 112, 0xFFF4F6FC);
        ctx.drawCenteredTextWithShadow(textRenderer, "VISUAL CLIENT", width / 2, height / 2 - 94, accent);
        ctx.drawCenteredTextWithShadow(textRenderer, "ACCOUNT  •  " + NullAccountProfile.login(), width / 2, height / 2 - 72, 0xFFB98AFF);
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "NULL CLIENT  •  1.21.4", width / 2, height - 18, 0xFF7F899C);
    }
    @Override public boolean shouldPause() { return false; }
}
