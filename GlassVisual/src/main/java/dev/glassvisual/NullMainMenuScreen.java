package dev.glassvisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public final class NullMainMenuScreen extends Screen {
    public NullMainMenuScreen() {
        super(Text.literal("NULL"));
    }

    @Override
    protected void init() {
        clearChildren();

        int buttonWidth = Math.max(100, Math.min(190, width - 32));
        int buttonHeight = height < 430 ? 24 : 27;
        int gap = height < 430 ? 5 : 7;
        int totalHeight = buttonHeight * 5 + gap * 4;
        int buttonX = (width - buttonWidth) / 2;
        int buttonY = Math.max(96, height / 2 - 140);

        if (buttonY + totalHeight > height - 34) {
            buttonY = Math.max(70, height - totalHeight - 34);
        }

        addDrawableChild(new MenuButton(buttonX, buttonY, buttonWidth, buttonHeight,
            "Singleplayer", 0xFF8595F7, textRenderer,
            () -> client.setScreen(new SelectWorldScreen(this))));
        addDrawableChild(new MenuButton(buttonX, buttonY + buttonHeight + gap, buttonWidth, buttonHeight,
            "Multiplayer", 0xFF8E9CFB, textRenderer,
            () -> client.setScreen(new MultiplayerScreen(this))));
        addDrawableChild(new MenuButton(buttonX, buttonY + (buttonHeight + gap) * 2, buttonWidth, buttonHeight,
            "Account Manager", 0xFFB39BFF, textRenderer,
            () -> client.setScreen(new NullAccountScreen(this))));
        addDrawableChild(new MenuButton(buttonX, buttonY + (buttonHeight + gap) * 3, buttonWidth, buttonHeight,
            "Settings", 0xFF9BAAC6, textRenderer,
            () -> client.setScreen(new OptionsScreen(this, client.options))));
        addDrawableChild(new MenuButton(buttonX + (buttonWidth - 104) / 2,
            buttonY + (buttonHeight + gap) * 4, Math.min(104, buttonWidth), buttonHeight,
            "Quit", 0xFFE58A96, textRenderer, () -> client.scheduleStop()));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int centerX = width / 2;
        int accent = NullTheme.accent();

        context.fillGradient(0, 0, width, height, 0xFF080B13, 0xFF111A2B);
        context.fillGradient(centerX - 250, 0, centerX + 250, height, 0x001B2B50, 0x241B2B50);
        context.fillGradient(0, height / 3, width, height, 0x0010182B, 0x3310182B);

        int buttonY = Math.max(96, height / 2 - 140);
        int totalHeight = 5 * (height < 430 ? 24 : 27) + 4 * (height < 430 ? 5 : 7);
        if (buttonY + totalHeight > height - 34) {
            buttonY = Math.max(70, height - totalHeight - 34);
        }
        int logoY = Math.max(18, buttonY - 112);

        rounded(context, centerX - 24, logoY, 48, 48, 14, 0x553C4B83);
        rounded(context, centerX - 22, logoY + 2, 44, 44, 12, 0xC0141B2B);

        // Compact geometric NULL mark.
        context.fill(centerX - 10, logoY + 11, centerX - 6, logoY + 37, accent);
        context.fill(centerX + 7, logoY + 11, centerX + 11, logoY + 37, accent);
        for (int i = 0; i <= 20; i++) {
            int x = centerX - 6 + (i * 13 / 20);
            int y = logoY + 33 - i;
            context.fill(x, y, x + 3, y + 3, accent);
        }

        context.drawCenteredTextWithShadow(textRenderer, "NULL", centerX, logoY + 52, 0xFFF0F2FA);
        context.drawCenteredTextWithShadow(textRenderer, "VISUAL CLIENT", centerX, logoY + 65, 0xFF8793AE);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, "NULL CLIENT  ·  FABRIC 1.21.4",
            centerX, height - 18, 0xFF77839C);
    }

    private static void rounded(DrawContext context, int x, int y, int width, int height,
                                int radius, int color) {
        for (int row = 0; row < height; row++) {
            int inset = 0;
            if (row < radius) {
                int dy = radius - row;
                inset = radius - (int) Math.floor(Math.sqrt(radius * radius - dy * dy));
            } else if (row >= height - radius) {
                int dy = row - (height - radius - 1);
                inset = radius - (int) Math.floor(Math.sqrt(radius * radius - dy * dy));
            }
            context.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static final class MenuButton extends ButtonWidget {
        private final int iconColor;
        private final TextRenderer renderer;

        private MenuButton(int x, int y, int width, int height, String label, int iconColor,
                           TextRenderer renderer, Runnable action) {
            super(x, y, width, height, Text.literal(label), button -> action.run(),
                DEFAULT_NARRATION_SUPPLIER);
            this.iconColor = iconColor;
            this.renderer = renderer;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean hover = isHovered();
            int x = getX();
            int y = getY();
            int w = getWidth();
            int h = getHeight();

            rounded(context, x, y + 2, w, h, 6, hover ? 0x30242D48 : 0x2010182B);
            rounded(context, x, y, w, h, 6, hover ? 0xD52B3550 : 0x9C20283C);
            rounded(context, x + 1, y + 1, w - 2, h - 2, 5,
                hover ? 0xB52B3551 : 0x80232B40);

            rounded(context, x + 11, y + (h - 6) / 2, 6, 6, 3,
                hover ? 0xFFFFFFFF : iconColor);

            int textColor = hover ? 0xFFFFFFFF : 0xFFD8DEED;
            int textX = x + (w - renderer.getWidth(getMessage())) / 2 + 5;
            context.drawTextWithShadow(renderer, getMessage(), textX, y + (h - 9) / 2, textColor);

            if (hover) {
                rounded(context, x + 2, y + 2, w - 4, 1, 1, 0x557D8BFF);
            }
        }
    }
}
