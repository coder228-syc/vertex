package com.vertex.client.clickgui.impl;

import com.vertex.client.render.font.FontUtils;
import com.vertex.client.modules.setting.SliderSetting;
import com.vertex.client.clickgui.SettingRenderContext;
import com.vertex.client.clickgui.SettingRenderer;
import com.vertex.client.render.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class SliderSettingRenderer implements SettingRenderer<SliderSetting> {

    public static final int HEIGHT = 20;
    public static final int HEIGHT_WITH_DESC = 30;
    public static final int BAR_OFFSET_Y = 13;
    public static final int BAR_OFFSET_Y_WITH_DESC = 23;
    public static final int BAR_HEIGHT = 3;

    private static final float THUMB_W = 6.5f;
    private static final float THUMB_H = 4.5f;
    private static final float THUMB_PAD_TOP = 0.75f;

    @Override
    public void render(DrawContext ctx, SliderSetting ss, int x, int y, int w, SettingRenderContext env) {
        FontUtils.gilroy[12].drawLeftAligned(ctx.getMatrices(), ss.getName(), x, y - 1, env.textPrimary());
        boolean hasDesc = hasDesc(ss);
        if (hasDesc) {
            FontUtils.inter[10].drawLeftAligned(ctx.getMatrices(), ss.getDesc(), x, y + 8.5f, env.textSecondary());
        }
        String val = formatValueText(ss);
        FontUtils.gilroy[12].drawLeftAligned(ctx.getMatrices(), val, x + w - 1 - (int) FontUtils.gilroy[12].getWidth(val), y - 1, env.ao(env.accentRgb()));
        int barY = y + getBarOffset(ss);
        RenderUtil.drawRoundedRect(ctx.getMatrices(), x, barY, w, BAR_HEIGHT, 0.5f, env.ao(env.sliderTrack()));
        float fill = (float) ((ss.get().doubleValue() - ss.getMin()) / (ss.getMax() - ss.getMin()));
        RenderUtil.drawRoundedRect(ctx.getMatrices(), x, barY, w * fill, BAR_HEIGHT, 0.5f, env.ao(env.accentRgb()));

        float thumbW = Math.min(THUMB_W, w);
        float centerX = x + w * fill;
        float thumbX = centerX - thumbW * 0.5f;
        float minX = x;
        float maxX = x + w - thumbW;
        thumbX = MathHelper.clamp(thumbX, minX, Math.max(minX, maxX));

        RenderUtil.drawRoundedRect(ctx.getMatrices(), thumbX, barY - THUMB_PAD_TOP, thumbW, THUMB_H, 1f, env.ao(-1));
    }

    public static void applyValueFromGuiX(SliderSetting ss, double guiMouseX, int x, int barWidth) {
        if (barWidth <= 0) return;
        double min = ss.getMin();
        double max = ss.getMax();
        double t = (guiMouseX - x) / (double) barWidth;
        t = MathHelper.clamp(t, 0.0, 1.0);
        double raw = min + t * (max - min);
        double inc = ss.getIncrement();
        if (inc > 0) {
            raw = min + Math.round((raw - min) / inc) * inc;
            raw = MathHelper.clamp(raw, min, max);
        }
        ss.set(raw);
    }

    private static boolean isInRow(SliderSetting ss, double gx, double gy, int x, int y, int w) {
        return gx >= x && gx <= x + w && gy >= y && gy <= y + getHeight(ss);
    }

    @Override
    public boolean mouseClicked(SliderSetting ss, int button, double mouseX, double mouseY, int x, int y, int w, SettingRenderContext env) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && isInRow(ss, mouseX, mouseY, x, y, w)) {
            ss.resetToDefault();
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isOnBar(ss, mouseX, mouseY, x, y, w)) {
            applyValueFromGuiX(ss, mouseX, x, w);
            env.beginSliderDrag(ss, x, w);
            return true;
        }
        return false;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    public static int getHeight(SliderSetting ss) {
        return hasDesc(ss) ? HEIGHT_WITH_DESC : HEIGHT;
    }

    private static int getBarOffset(SliderSetting ss) {
        return hasDesc(ss) ? BAR_OFFSET_Y_WITH_DESC : BAR_OFFSET_Y;
    }

    private static boolean isOnBar(SliderSetting ss, double gx, double gy, int x, int y, int w) {
        int barTop = y + getBarOffset(ss);
        return gx >= x && gx <= x + w && gy >= barTop && gy <= barTop + BAR_HEIGHT;
    }

    private static boolean hasDesc(SliderSetting ss) {
        String desc = ss.getDesc();
        return desc != null && !desc.isEmpty();
    }

    private static String formatValueText(SliderSetting ss) {
        double v = ss.get().doubleValue();
        double inc = ss.getIncrement();
        if (inc <= 0) {
            return String.format(Locale.US, "%.6g", v);
        }
        if (inc >= 1.0 && Math.abs(inc - Math.rint(inc)) < 1e-9) {
            return String.valueOf((long) Math.rint(v));
        }
        int decimals = decimalsForIncrement(inc);
        return String.format(Locale.US, "%." + decimals + "f", v);
    }

    private static int decimalsForIncrement(double inc) {
        int max = 8;
        for (int d = 1; d <= max; d++) {
            double scaled = inc * Math.pow(10, d);
            if (Math.abs(scaled - Math.rint(scaled)) < 1e-6) {
                return d;
            }
        }
        return max;
    }
}
