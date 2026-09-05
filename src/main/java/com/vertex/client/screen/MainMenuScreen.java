package com.vertex.client.screen;

import com.vertex.client.render.font.FontUtils;
import com.vertex.client.render.util.KawaseBlur;
import com.vertex.client.render.util.RenderUtil;
import com.vertex.client.util.IMinecraft;
import com.vertex.client.util.animations.Animation;
import com.vertex.client.util.animations.Easing;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import org.joml.Vector4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class MainMenuScreen extends Screen implements IMinecraft {

    private final Animation alpha = new Animation(500L, 0f, Easing.CUBIC_OUT);
    private final List<MenuButton> buttons = new ArrayList<>();

    private MenuButton singleplayerButton;
    private MenuButton multiplayerButton;
    private MenuButton settingsButton;
    private MenuButton modsButton;
    private MenuButton exitButton;

    private double lastMouseX;
    private double lastMouseY;

    public MainMenuScreen() {
        super(Text.literal("Vertex"));
    }

    @Override
    protected void init() {
        alpha.setValue(0f);
        alpha.animateTo(1f);
        buildButtons();
    }

    private void buildButtons() {
        buttons.clear();

        singleplayerButton = new MenuButton("Одиночная игра", () -> mc.setScreen(new SelectWorldScreen(this)));
        multiplayerButton = new MenuButton("Сетевая игра", () -> mc.setScreen(new MultiplayerScreen(this)));
        buttons.add(singleplayerButton);
        buttons.add(multiplayerButton);

        settingsButton = new MenuButton("Настройки", () -> mc.setScreen(new OptionsScreen(this, mc.options)));
        buttons.add(settingsButton);

        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            modsButton = new MenuButton("Моды", () -> {
                try {
                    Class<?> modMenuClass = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
                    Screen modsScreen = (Screen) modMenuClass.getConstructor(Screen.class).newInstance(this);
                    mc.setScreen(modsScreen);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            buttons.add(modsButton);
        } else {
            modsButton = null;
        }

        exitButton = new MenuButton("Выход", true, () -> mc.scheduleStop());
        buttons.add(exitButton);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        alpha.update();

        float a = alpha.getValue();
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        KawaseBlur.blur.updateBlur(8.0F, 5);

        RenderUtil.drawRoundedRect(context.getMatrices(), 0, 0, width, height, 0f, new Color(5, 8, 17, 255).getRGB());
        RenderUtil.drawBlurredRoundedRectangle(context.getMatrices(), width * 0.06f, height * 0.06f, width * 0.88f, height * 0.88f, 54f, new Color(97, 120, 255, 84).getRGB(), 1.2f);
        RenderUtil.drawBlurredRoundedRectangle(context.getMatrices(), width * 0.14f, height * 0.09f, width * 0.72f, height * 0.72f, 56f, new Color(110, 196, 255, 62).getRGB(), 1.0f);
        RenderUtil.drawBlurredRoundedRectangle(context.getMatrices(), width * 0.23f, height * 0.17f, width * 0.54f, height * 0.24f, 32f, new Color(255, 255, 255, 20).getRGB(), 0.7f);

        RenderUtil.drawLiquidRect(
            context.getMatrices(),
            width * 0.12f, height * 0.12f, width * 0.76f, height * 0.76f,
            new Vector4f(30f, 30f, 30f, 30f),
            3.8f,
            4.6f,
            0.92f,
            0.16f,
            false,
            1.12f,
            0.05f,
            com.vertex.client.render.util.ColorRGBA.of(new Color(12, 17, 30, 210).getRGB()),
            true
        );
        RenderUtil.drawRoundedBorder(context.getMatrices(), width * 0.12f, height * 0.12f, width * 0.76f, height * 0.76f, 30f, 1.6f, new Color(182, 200, 255, 130).getRGB());

        var titleFont = FontUtils.roboto[24];
        var subFont = FontUtils.roboto[13];

        float titleY = height * 0.23f;
        titleFont.centeredDraw(context.getMatrices(), "Vertex", width / 2f, titleY, argb(255, a));
        subFont.centeredDraw(context.getMatrices(), "VertexClient", width / 2f, titleY + titleFont.getHeight() + 6, argb(210, a * 0.85f));

        for (MenuButton b : buttons) {
            b.update(lastMouseX, lastMouseY);
        }

        drawButtons(context, width, height, a);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawButtons(DrawContext ctx, int width, int height, float a) {
        float gap = 6f;
        float panelWidth = Math.min(260f, width - 40f);
        float cardWidth = (panelWidth - gap) / 2f;
        float cardHeight = 26f;

        int smallCount = modsButton == null ? 1 : 2;
        float smallGap = 6f;
        float smallTotal = cardWidth * 2f + gap;
        float smallWidth = (smallTotal - smallGap * smallCount) / (smallCount + 1);
        float smallHeight = 24f;

        float exitHeight = 24f;

        float totalHeight = cardHeight + gap + smallHeight + gap + exitHeight;
        float startX = width / 2f - (cardWidth * 2f + gap) / 2f;
        float startY = height / 2f - totalHeight / 2f + height * 0.06f;
        float appear = (1f - a) * 24f;
        float y = startY + appear;

        singleplayerButton.draw(ctx, startX, y, cardWidth, cardHeight, a);
        multiplayerButton.draw(ctx, startX + cardWidth + gap, y, cardWidth, cardHeight, a);

        y += cardHeight + gap;

        float sx = startX;
        settingsButton.draw(ctx, sx, y, smallWidth, smallHeight, a);
        sx += smallWidth + smallGap;
        if (modsButton != null) {
            modsButton.draw(ctx, sx, y, smallWidth, smallHeight, a);
            sx += smallWidth + smallGap;
        }

        y += smallHeight + gap;
        exitButton.draw(ctx, startX, y, cardWidth * 2f + gap, exitHeight, a);
    }

    private static int argb(int rgb, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
        return (a << 24) | (rgb << 16) | (rgb << 8) | rgb;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (MenuButton b : buttons) {
                if (b.contains(mouseX, mouseY)) {
                    b.onClick();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private static final class MenuButton {
        private final String text;
        private final boolean danger;
        private final Runnable action;
        private final Animation hover = new Animation(180L, 0f, Easing.SMOOTH_STEP);

        private float x, y, w, h;

        MenuButton(String text, Runnable action) {
            this(text, false, action);
        }

        MenuButton(String text, boolean danger, Runnable action) {
            this.text = text;
            this.danger = danger;
            this.action = action;
        }

        void update(double mouseX, double mouseY) {
            boolean hovered = contains(mouseX, mouseY);
            hover.update(hovered ? 1f : 0f);
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        }

        void draw(DrawContext ctx, float x, float y, float w, float h, float globalAlpha) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

            float hp = hover.getValue();
            int base = danger ? new Color(130, 33, 33, 180).getRGB() : new Color(28, 34, 48, 168).getRGB();
            int fillAlpha = Math.round((100 + hp * 80) * globalAlpha);
            int fill = new Color((base >> 16) & 0xFF, (base >> 8) & 0xFF, base & 0xFF, Math.min(255, fillAlpha)).getRGB();

            RenderUtil.drawLiquidRect(
                ctx.getMatrices(),
                x, y, w, h,
                new Vector4f(9f, 9f, 9f, 9f),
                2.8f,
                3.4f,
                0.9f,
                0.18f,
                false,
                1.0f,
                0.03f,
                com.vertex.client.render.util.ColorRGBA.of(fill),
                true
            );

            int borderAlpha = Math.round((60 + hp * 120) * globalAlpha);
            int border = new Color(220, 228, 255, Math.min(255, borderAlpha)).getRGB();
            RenderUtil.drawRoundedBorder(ctx.getMatrices(), x, y, w, h, 9f, 1.0f, border);

            var font = FontUtils.roboto[13];
            int textAlpha = Math.round((185 + hp * 70) * globalAlpha);
            int textColor = new Color(255, 255, 255, Math.min(255, textAlpha)).getRGB();
            font.centeredDraw(ctx.getMatrices(), text, x + w / 2f, y + h / 2f - font.getHeight() / 2f, textColor);
        }

        void onClick() {
            if (action != null) action.run();
        }
    }
}