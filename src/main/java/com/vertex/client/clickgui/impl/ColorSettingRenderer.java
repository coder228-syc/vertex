package com.vertex.client.clickgui.impl;

import com.vertex.client.render.font.FontUtils;
import com.vertex.client.modules.setting.ColorSetting;
import com.vertex.client.clickgui.SettingRenderContext;
import com.vertex.client.clickgui.SettingRenderer;
import com.vertex.client.render.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class ColorSettingRenderer implements SettingRenderer<ColorSetting> {

    public static final int COLLAPSED_HEIGHT = 14;
    public static final int EXPANDED_HEIGHT = 88;

    private static final Map<ColorSetting, DragState> STATES = new HashMap<>();

    private static DragState state(ColorSetting cs) {
        return STATES.computeIfAbsent(cs, ignored -> new DragState());
    }

    @Override
    public void render(DrawContext ctx, ColorSetting cs, int x, int y, int w, SettingRenderContext env) {
        DragState drag = state(cs);
        syncHsbFromColor(cs, drag);

        FontUtils.gilroy[12].drawLeftAligned(ctx.getMatrices(), cs.getName(), x, y + 1, env.textPrimary());
        int preview = cs.get() | 0xFF000000;
        float previewX = x + w - 10;
        RenderUtil.drawRoundedRect(ctx.getMatrices(), previewX, y + 1, 8, 8, 2f, env.ao(preview));

        if (!cs.expanded) {
            return;
        }

        int pickerX = x;
        int pickerY = y + 14;
        int pickerW = w;
        int pickerH = 54;

        int baseRgb = Color.HSBtoRGB(drag.hue, 1f, 1f) | 0xFF000000;
        RenderUtil.drawRoundedRect(ctx.getMatrices(), pickerX, pickerY, pickerW, pickerH, 2f, env.ao(baseRgb));
        RenderUtil.drawRoundedRect(ctx.getMatrices(), pickerX, pickerY, pickerW, pickerH / 2f, 2f, env.ao(0xFFFFFFFF));
        RenderUtil.drawRoundedRect(ctx.getMatrices(), pickerX, pickerY + pickerH / 2f, pickerW, pickerH / 2f, 2f, env.ao(0xFF000000));

        float selX = pickerX + drag.saturation * pickerW;
        float selY = pickerY + (1f - drag.brightness) * pickerH;
        RenderUtil.drawCircle(ctx.getMatrices(), selX, selY, 3f, env.ao(-1));

        int hueY = pickerY + pickerH + 6;
        int hueH = 4;
        int segments = 6;
        float segW = pickerW / (float) segments;
        for (int i = 0; i < segments; i++) {
            int c = Color.HSBtoRGB(i / (float) segments, 1f, 1f) | 0xFF000000;
            RenderUtil.drawRoundedRect(ctx.getMatrices(), pickerX + i * segW, hueY, segW + 0.5f, hueH, 1f, env.ao(c));
        }

        float hueX = pickerX + drag.hue * pickerW;
        RenderUtil.drawRoundedRect(ctx.getMatrices(), hueX - 2, hueY - 1, 4, hueH + 2, 1f, env.ao(-1));
    }

    @Override
    public boolean mouseClicked(ColorSetting cs, int button, double mouseX, double mouseY, int x, int y, int w, SettingRenderContext env) {
        DragState drag = state(cs);
        syncHsbFromColor(cs, drag);

        float previewX = x + w - 10;
        if (button == 1 && mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 12) {
            cs.expanded = !cs.expanded;
            return false;
        }
        if (button == 0 && mouseX >= previewX && mouseX <= previewX + 8 && mouseY >= y + 1 && mouseY <= y + 9) {
            cs.expanded = !cs.expanded;
            return false;
        }
        if (!cs.expanded || button != 0) {
            return false;
        }

        int pickerX = x;
        int pickerY = y + 14;
        int pickerW = w;
        int pickerH = 54;
        int hueY = pickerY + pickerH + 6;

        if (mouseX >= pickerX && mouseX <= pickerX + pickerW && mouseY >= pickerY && mouseY <= pickerY + pickerH) {
            drag.draggingSatBright = true;
            applySatBright(cs, drag, mouseX, mouseY, pickerX, pickerY, pickerW, pickerH);
            return true;
        }
        if (mouseX >= pickerX && mouseX <= pickerX + pickerW && mouseY >= hueY && mouseY <= hueY + 4) {
            drag.draggingHue = true;
            applyHue(cs, drag, mouseX, pickerX, pickerW);
            return true;
        }
        return false;
    }

    public static void applyDrag(ColorSetting cs, double mouseX, double mouseY, int x, int y, int w) {
        DragState drag = state(cs);
        if (!cs.expanded) {
            return;
        }
        int pickerX = x;
        int pickerY = y + 14;
        int pickerW = w;
        int pickerH = 54;
        int hueY = pickerY + pickerH + 6;

        if (drag.draggingSatBright) {
            applySatBright(cs, drag, mouseX, mouseY, pickerX, pickerY, pickerW, pickerH);
        } else if (drag.draggingHue) {
            applyHue(cs, drag, mouseX, pickerX, pickerW);
        } else if (mouseY >= pickerY && mouseY <= pickerY + pickerH) {
            drag.draggingSatBright = true;
            applySatBright(cs, drag, mouseX, mouseY, pickerX, pickerY, pickerW, pickerH);
        } else if (mouseY >= hueY && mouseY <= hueY + 4) {
            drag.draggingHue = true;
            applyHue(cs, drag, mouseX, pickerX, pickerW);
        }
    }

    public static void releaseAll(ColorSetting cs) {
        DragState drag = state(cs);
        drag.draggingSatBright = false;
        drag.draggingHue = false;
    }

    private static void applySatBright(ColorSetting cs, DragState drag, double mouseX, double mouseY,
                                       int pickerX, int pickerY, int pickerW, int pickerH) {
        drag.saturation = MathHelper.clamp((float) (mouseX - pickerX) / pickerW, 0f, 1f);
        drag.brightness = MathHelper.clamp(1f - (float) (mouseY - pickerY) / pickerH, 0f, 1f);
        cs.set(applyHsb(drag.hue, drag.saturation, drag.brightness, cs.get()));
    }

    private static void applyHue(ColorSetting cs, DragState drag, double mouseX, int pickerX, int pickerW) {
        drag.hue = MathHelper.clamp((float) (mouseX - pickerX) / pickerW, 0f, 1f);
        cs.set(applyHsb(drag.hue, drag.saturation, drag.brightness, cs.get()));
    }

    private static void syncHsbFromColor(ColorSetting cs, DragState drag) {
        float[] hsb = Color.RGBtoHSB((cs.get() >> 16) & 0xFF, (cs.get() >> 8) & 0xFF, cs.get() & 0xFF, null);
        drag.hue = hsb[0];
        drag.saturation = hsb[1];
        drag.brightness = hsb[2];
    }

    private static int applyHsb(float h, float s, float b, int oldColor) {
        int rgb = Color.HSBtoRGB(h, s, b);
        int alpha = (oldColor >> 24) & 0xFF;
        if (alpha == 0) {
            alpha = 255;
        }
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    @Override
    public int getHeight() {
        return COLLAPSED_HEIGHT;
    }

    public static int getHeight(ColorSetting cs) {
        return cs.expanded ? EXPANDED_HEIGHT : COLLAPSED_HEIGHT;
    }

    private static final class DragState {
        private float hue;
        private float saturation = 1f;
        private float brightness = 1f;
        private boolean draggingSatBright;
        private boolean draggingHue;
    }
}
