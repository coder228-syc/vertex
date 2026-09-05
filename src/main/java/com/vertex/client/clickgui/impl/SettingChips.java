package com.vertex.client.clickgui.impl;

import com.vertex.client.render.font.FontUtils;
import com.vertex.client.render.font.RenderFonts;
import com.vertex.client.clickgui.SettingRenderContext;
import com.vertex.client.render.util.ColorUtil;
import com.vertex.client.render.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

final class SettingChips {

    static final float CHIP_PAD_X = 3f;
    static final float CHIP_PAD_Y = 2f;
    static final float CHIP_GAP_X = 2f;
    static final float CHIP_GAP_Y = 2f;
    static final float CHIP_ROUND = 2f;
    static final float LABEL_GAP = 4f;

    private static RenderFonts labelFont() {
        return FontUtils.gilroy[12];
    }

    private static RenderFonts chipFont() {
        return FontUtils.gilroy[11];
    }

    private SettingChips() {
    }

    static float chipsOffsetY() {
        return labelFont().getHeight() + LABEL_GAP;
    }

    record ChipBounds(float x, float y, float w, float h, int index) {
    }

    static float chipHeight() {
        return chipFont().getHeight() + CHIP_PAD_Y * 2f;
    }

    static float chipWidth(String label) {
        return chipFont().getWidth(label) + CHIP_PAD_X * 2f;
    }

    static List<ChipBounds> layout(List<String> labels, float x, float y, float maxW) {
        List<ChipBounds> out = new ArrayList<>();
        if (labels.isEmpty()) {
            return out;
        }
        float lineH = chipHeight();
        float curX = x;
        float curY = y;
        for (int i = 0; i < labels.size(); i++) {
            float chipW = chipWidth(labels.get(i));
            float remaining = x + maxW - curX;
            if (curX > x && chipW > remaining + 0.25f) {
                curX = x;
                curY += lineH + CHIP_GAP_Y;
            }
            out.add(new ChipBounds(curX, curY, chipW, lineH, i));
            curX += chipW + CHIP_GAP_X;
        }
        return out;
    }

    static float chipsAreaHeight(List<String> labels, float maxW) {
        List<ChipBounds> chips = layout(labels, 0f, 0f, maxW);
        if (chips.isEmpty()) {
            return 0f;
        }
        float bottom = 0f;
        for (ChipBounds chip : chips) {
            bottom = Math.max(bottom, chip.y + chip.h);
        }
        return bottom;
    }

    static int totalHeight(List<String> labels, float maxW) {
        float chipsH = chipsAreaHeight(labels, maxW);
        float labelH = labelFont().getHeight();
        if (chipsH <= 0f) {
            return (int) Math.ceil(labelH);
        }
        return (int) Math.ceil(labelH + LABEL_GAP + chipsH);
    }

    static void drawChip(DrawContext ctx, SettingRenderContext env, ChipBounds chip, String text, boolean selected) {
        int fill = selected
                ? ColorUtil.setAlpha(env.fieldFill(), 90)
                : ColorUtil.setAlpha(env.fieldFill(), 50);
        int border = env.panelBorder();
        int textColor = selected ? env.ao(env.accentRgb()) : ColorUtil.setAlpha(env.textPrimary(), 190);
        RenderUtil.drawRoundedRect(ctx.getMatrices(), chip.x, chip.y, chip.w, chip.h, CHIP_ROUND, fill);
        RenderUtil.drawRoundedBorder(ctx.getMatrices(), chip.x, chip.y, chip.w, chip.h, CHIP_ROUND, -0.45f, border);
        chipFont().drawLeftAligned(
                ctx.getMatrices(),
                text,
                chip.x + CHIP_PAD_X,
                chip.y + CHIP_PAD_Y - 0.5f,
                textColor
        );
    }

    static ChipBounds findAt(List<ChipBounds> chips, double mouseX, double mouseY) {
        for (ChipBounds chip : chips) {
            if (mouseX >= chip.x && mouseX <= chip.x + chip.w && mouseY >= chip.y && mouseY <= chip.y + chip.h) {
                return chip;
            }
        }
        return null;
    }
}
