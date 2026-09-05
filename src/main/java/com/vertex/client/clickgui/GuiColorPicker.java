package com.vertex.client.clickgui;

import com.vertex.client.modules.ModuleManager;
import com.vertex.client.render.font.FontUtils;
import com.vertex.client.theme.GuiThemeColor;
import com.vertex.client.render.util.ColorUtil;
import com.vertex.client.util.MathUtil;
import com.vertex.client.render.util.RenderUtil;
import com.vertex.client.render.util.Scissor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Vector4f;

import java.awt.Color;

public final class GuiColorPicker {
    public static final int HEADER_BASE_H = 27;
    public static final int DESC_LINE_STEP = 10;
    public static final float DESC_MAX_W = 160f;
    public static final int ROW_GAP = 0;
    public static final int CARD_PAD_X = 8;
    public static final float CARD_ROUND = 2.5f;
    public static final int PICKER_TOP_GAP = -8;
    public static final int PICKER_BODY_H = 97;
    public static final int PICKER_PAD = 8;
    public static final int SB_H = 45;
    public static final int SLIDER_H = 5;
    public static final int SLIDER_GAP = 4;
    public static final int BTN_H = 14;
    public static final int BTN_GAP = 6;
    public static final float SWATCH_D = 9f;
    private static final float SLIDER_THUMB_W = 6.5f;
    private static final float SLIDER_THUMB_H = 4.5f;
    private static final int CARD_FILL = new Color(0, 0, 0, 255).getRGB(); // было 81 (полупрозрачно) — теперь непрозрачно
    private static final float TEXT_F_SECONDARY = 130f / 255f;
    private static final float TEXT_F_BUTTON = 150f / 255f;

    private GuiColorPicker() {
    }

    private static int themeTextRgb() {
        return ColorUtil.guiThemeTextRgb();
    }

    private static int themeTextDim(float factor, java.util.function.IntUnaryOperator ao) {
        return ao.applyAsInt(ColorUtil.scaleRgb(themeTextRgb(), factor));
    }

    public static int headerHeight(GuiThemeColor entry) {
        if (entry.description == null || entry.description.isEmpty()) {
            return HEADER_BASE_H;
        }
        var lines = FontUtils.roboto[13].splitTextToLines(entry.description, DESC_MAX_W);
        if (lines.isEmpty()) {
            return HEADER_BASE_H;
        }
        return HEADER_BASE_H + lines.size() * DESC_LINE_STEP;
    }

    public static float rowHeight(GuiThemeColor entry) {
        return headerHeight(entry) + (PICKER_BODY_H + PICKER_TOP_GAP) * entry.pickerAnim;
    }

    public static float listHeight(java.util.List<GuiThemeColor> entries) {
        float h = 0f;
        for (int i = 0; i < entries.size(); i++) {
            h += rowHeight(entries.get(i));
            if (i < entries.size() - 1) h += ROW_GAP;
        }
        return h;
    }

    public static void tickAnim(GuiThemeColor entry) {
        entry.pickerAnim = MathUtil.lerp(entry.pickerAnim, entry.pickerOpen ? 1f : 0f, 14f);
        if (entry.pickerAnim < 0.01f) entry.pickerAnim = 0f;
        if (entry.pickerAnim > 0.99f) entry.pickerAnim = 1f;
    }

    public static void render(
            DrawContext ctx,
            int x,
            float y,
            int w,
            GuiThemeColor entry,
            int mouseX,
            int mouseY,
            float tabAlpha,
            java.util.function.IntUnaryOperator ao,
            java.util.function.UnaryOperator<float[]> rectTransform
    ) {
        tickAnim(entry);

        int headerH = headerHeight(entry);
        float totalH = rowHeight(entry);
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + totalH;
        int cardBg = RenderUtil.applyOpacity(ao.applyAsInt(CARD_FILL), tabAlpha);
        RenderUtil.drawRoundedRect(ctx.getMatrices(), x, y, w, totalH, CARD_ROUND, cardBg);

        int preview = RenderUtil.applyOpacity(ao.applyAsInt(entry.color), tabAlpha);
        float swX = x + w - SWATCH_D - CARD_PAD_X;
        float swY = y + 9f;
        RenderUtil.drawCircle(ctx.getMatrices(), swX + SWATCH_D / 2f, swY + SWATCH_D / 2f, SWATCH_D, preview);
        if (entry.pickerOpen || hovered) {
            RenderUtil.drawCircleBorder(
                    ctx.getMatrices(),
                    swX + SWATCH_D / 2f,
                    swY + SWATCH_D / 2f,
                    SWATCH_D + 1.5f,
                    -0.45f,
                    RenderUtil.applyOpacity(ao.applyAsInt(ModuleManager.STYLE_MANAGER.getFirstColor()), tabAlpha * (entry.pickerOpen ? 1f : 0.55f))
            );
        }

        int titleColor = RenderUtil.applyOpacity(ao.applyAsInt(themeTextRgb()), tabAlpha);
        int descColor = RenderUtil.applyOpacity(themeTextDim(TEXT_F_SECONDARY, ao), tabAlpha);
        FontUtils.inter[16].drawLeftAligned(ctx.getMatrices(), entry.displayName, x + 8.5f, y + 8.5f, titleColor);

        if (entry.description != null && !entry.description.isEmpty()) {
            var descLines = FontUtils.roboto[13].splitTextToLines(entry.description, DESC_MAX_W);
            for (int i = 0; i < descLines.size(); i++) {
                FontUtils.roboto[13].drawLeftAligned(ctx.getMatrices(), descLines.get(i), x + 8, y + 22 + i * DESC_LINE_STEP, descColor);
            }
        }

        if (entry.pickerAnim <= 0.01f) return;

        float anim = entry.pickerAnim;
        float bodyY = y + headerH + PICKER_TOP_GAP;
        float bodyH = PICKER_BODY_H * anim;

        Scissor.push();
        {
            float[] screenRect = rectTransform.apply(new float[]{x, bodyY, w, bodyH});
            Scissor.setFromComponentCoordinates(screenRect[0], screenRect[1], screenRect[2], screenRect[3]);
        }

        int alphaInt = Math.round(255f * tabAlpha * anim);
        PickerLayout layout = PickerLayout.of(x, y, w, headerH);
        updateFromDrag(entry, mouseX, mouseY, layout);

        drawSbSquare(ctx, layout.innerX, layout.sbY, layout.innerW, SB_H, Color.HSBtoRGB(entry.hue, 1f, 1f), alphaInt);

        float selX = layout.innerX + entry.saturation * layout.innerW;
        float selY = layout.sbY + (1f - entry.brightness) * SB_H;
        selX = Math.max(layout.innerX + 2, Math.min(layout.innerX + layout.innerW - 2, selX));
        selY = Math.max(layout.sbY + 2, Math.min(layout.sbY + SB_H - 2, selY));
        RenderUtil.drawCircle(ctx.getMatrices(), selX, selY, 5f, RenderUtil.applyOpacity(-1, alphaInt));

        drawHueSlider(ctx, layout.innerX, layout.hueY, layout.innerW, SLIDER_H, alphaInt);

        float hueKnob = layout.innerX + entry.hue * layout.innerW;
        hueKnob = Math.max(layout.innerX + 2, Math.min(layout.innerX + layout.innerW - 2, hueKnob));
        drawSliderThumb(ctx, hueKnob, layout.hueY, SLIDER_H, alphaInt);

        int rgb = Color.HSBtoRGB(entry.hue, entry.saturation, entry.brightness);
        int opaque = 0xFF000000 | (rgb & 0xFFFFFF);
        int alphaLeft = RenderUtil.applyOpacity(opaque, 0f);
        int alphaRight = RenderUtil.applyOpacity(opaque, alphaInt);
        RenderUtil.rectRGB(ctx.getMatrices(), layout.innerX, layout.alphaY, layout.innerW, SLIDER_H, 1.5f, alphaLeft, alphaLeft, alphaRight, alphaRight);

        float alphaKnob = layout.innerX + entry.alpha * layout.innerW;
        alphaKnob = Math.max(layout.innerX + 2, Math.min(layout.innerX + layout.innerW - 2, alphaKnob));
        drawSliderThumb(ctx, alphaKnob, layout.alphaY, SLIDER_H, alphaInt);

        drawClipboardButtons(ctx, entry, layout, mouseX, mouseY, tabAlpha, anim, ao);

        Scissor.pop();
    }

    private static void drawClipboardButtons(DrawContext ctx, GuiThemeColor entry, PickerLayout layout, int mouseX, int mouseY, float tabAlpha, float anim, java.util.function.IntUnaryOperator ao) {
        float a = tabAlpha * anim;
        boolean copyH = in(mouseX, mouseY, layout.innerX, layout.btnY, layout.btnW, BTN_H);
        boolean pasteH = in(mouseX, mouseY, layout.pasteBtnX(), layout.btnY, layout.btnW, BTN_H);

        entry.copyHoverAnim = MathUtil.lerp(entry.copyHoverAnim, copyH ? 1f : 0f, 18f);
        entry.pasteHoverAnim = MathUtil.lerp(entry.pasteHoverAnim, pasteH ? 1f : 0f, 18f);

        drawActionButton(ctx, layout.innerX, layout.btnY, layout.btnW, "Копировать", entry.copyHoverAnim, a, ao);
        drawActionButton(ctx, layout.pasteBtnX(), layout.btnY, layout.btnW, "Вставить", entry.pasteHoverAnim, a, ao);
    }

    private static void drawActionButton(DrawContext ctx, float x, float y, float w, String label, float hoverAnim, float alpha, java.util.function.IntUnaryOperator ao) {
        float cx = x + w / 2f;
        float cy = y + BTN_H / 2f;
        float scale = 1f + hoverAnim * 0.034f;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(cx, cy, 0);
        ctx.getMatrices().scale(scale, scale, 1f);
        ctx.getMatrices().translate(-cx, -cy, 0);

        int bg = RenderUtil.applyOpacity(ao.applyAsInt(new Color(14, 14, 14, 77).getRGB()), alpha);
        int border = RenderUtil.applyOpacity(ao.applyAsInt(new Color(58, 58, 62, 39).getRGB()), alpha);
        int text = RenderUtil.applyOpacity(themeTextDim(TEXT_F_BUTTON, ao), alpha);

        RenderUtil.drawRoundedRect(ctx.getMatrices(), x, y, w, BTN_H, 2.5f, bg);
        RenderUtil.drawRoundedBorder(ctx.getMatrices(), x, y, w, BTN_H, 2.5f, -0.45f, border);

        float textW = FontUtils.inter[11].getWidth(label);
        float textY = y + (BTN_H - FontUtils.inter[11].getHeight()) / 2f;
        FontUtils.inter[11].drawLeftAligned(ctx.getMatrices(), label, x + (w - textW) / 2f, textY, text);

        ctx.getMatrices().pop();
    }

    private record PickerLayout(float innerX, float innerW, float sbY, float hueY, float alphaY, float btnY, float btnW) {
        static PickerLayout of(int x, float rowY, int w, int headerH) {
            float innerX = x + CARD_PAD_X;
            float innerW = w - CARD_PAD_X * 2;
            float sbY = rowY + headerH + PICKER_TOP_GAP + PICKER_PAD;
            float hueY = sbY + SB_H + SLIDER_GAP;
            float alphaY = hueY + SLIDER_H + SLIDER_GAP;
            float btnY = alphaY + SLIDER_H + 5;
            float btnW = (innerW - BTN_GAP) / 2f;
            return new PickerLayout(innerX, innerW, sbY, hueY, alphaY, btnY, btnW);
        }

        float pasteBtnX() {
            return innerX + btnW + BTN_GAP;
        }
    }

    private static void drawSliderThumb(DrawContext ctx, float centerX, float barY, float barH, int alphaInt) {
        float thumbX = centerX - SLIDER_THUMB_W * 0.5f;
        float thumbY = barY + (barH - SLIDER_THUMB_H) * 0.5f;
        RenderUtil.drawRoundedRect(
                ctx.getMatrices(),
                thumbX,
                thumbY,
                SLIDER_THUMB_W,
                SLIDER_THUMB_H,
                1f,
                RenderUtil.applyOpacity(-1, alphaInt)
        );
    }

    private static void drawSbSquare(DrawContext ctx, float x, float y, float w, float h, int hueRgb, int alphaInt) {
        int white = RenderUtil.applyOpacity(-1, alphaInt);
        int hue = RenderUtil.applyOpacity(hueRgb, alphaInt);
        RenderUtil.rectRGB(ctx.getMatrices(), x, y, w, h, 5f, white, white, hue, hue);

        int transparent = RenderUtil.applyOpacity(Color.BLACK.getRGB(), 0f);
        int black = RenderUtil.applyOpacity(Color.BLACK.getRGB(), alphaInt);
        RenderUtil.rectRGB(ctx.getMatrices(), x, y, w, h, 5f, transparent, black, black, transparent);
    }

    private static void drawHueSlider(DrawContext ctx, float x, float y, float w, float h, int alphaInt) {
        int segments = 6;
        float segW = w / segments;
        for (int i = 0; i < segments; i++) {
            int c1 = RenderUtil.applyOpacity(Color.HSBtoRGB(i / (float) segments, 1f, 1f), alphaInt);
            int c2 = RenderUtil.applyOpacity(Color.HSBtoRGB((i + 1f) / segments, 1f, 1f), alphaInt);
            float sx = x + i * segW;
            float r = 1.5f;
            Vector4f rounding = i == 0
                    ? new Vector4f(r, r, 0, 0)
                    : i == segments - 1
                      ? new Vector4f(0, 0, r, r)
                      : new Vector4f(0, 0, 0, 0);
            RenderUtil.rectRGB(ctx.getMatrices(), sx, y, segW + (i == segments - 1 ? 0.5f : 1.5f), h, rounding, c1, c1, c2, c2);
        }
    }

    private static void updateFromDrag(GuiThemeColor entry, int mouseX, int mouseY, PickerLayout layout) {
        if (entry.sbDragging) {
            entry.saturation = clamp01((mouseX - layout.innerX) / layout.innerW);
            entry.brightness = clamp01(1f - (mouseY - layout.sbY) / SB_H);
            entry.syncColorFromHsb();
            ModuleManager.STYLE_MANAGER.scheduleSave();
        }
        if (entry.hueDragging) {
            entry.hue = clamp01((mouseX - layout.innerX) / layout.innerW);
            entry.syncColorFromHsb();
            ModuleManager.STYLE_MANAGER.scheduleSave();
        }
        if (entry.alphaDragging) {
            entry.alpha = clamp01((mouseX - layout.innerX) / layout.innerW);
            entry.syncColorFromHsb();
            ModuleManager.STYLE_MANAGER.scheduleSave();
        }
    }

    public static boolean mouseClicked(double gx, double gy, int button, int x, float rowY, int w, GuiThemeColor entry) {
        int headerH = headerHeight(entry);
        if (in(gx, gy, x, rowY, w, headerH)) {
            if (button == 1) {
                boolean next = !entry.pickerOpen;
                ModuleManager.STYLE_MANAGER.closeAllPickers();
                entry.pickerOpen = next;
                return true;
            }
            return button == 0;
        }

        if (entry.pickerAnim < 0.4f) {
            return false;
        }

        PickerLayout layout = PickerLayout.of(x, rowY, w, headerH);

        if (button != 0) {
            return in(gx, gy, x, rowY, w, rowHeight(entry));
        }

        if (in(gx, gy, layout.innerX, layout.sbY, layout.innerW, SB_H)) {
            entry.sbDragging = true;
            applySb(entry, gx, gy, layout.innerX, layout.sbY, layout.innerW);
            return true;
        }
        if (in(gx, gy, layout.innerX, layout.hueY, layout.innerW, SLIDER_H)) {
            entry.hueDragging = true;
            entry.hue = clamp01((float) ((gx - layout.innerX) / layout.innerW));
            entry.syncColorFromHsb();
            ModuleManager.STYLE_MANAGER.scheduleSave();
            return true;
        }
        if (in(gx, gy, layout.innerX, layout.alphaY, layout.innerW, SLIDER_H)) {
            entry.alphaDragging = true;
            entry.alpha = clamp01((float) ((gx - layout.innerX) / layout.innerW));
            entry.syncColorFromHsb();
            ModuleManager.STYLE_MANAGER.scheduleSave();
            return true;
        }
        if (in(gx, gy, layout.innerX, layout.btnY, layout.btnW, BTN_H)) {
            MinecraftClient.getInstance().keyboard.setClipboard(colorToHex(entry.color));
            return true;
        }
        if (in(gx, gy, layout.pasteBtnX(), layout.btnY, layout.btnW, BTN_H)) {
            int pasted = hexToColor(MinecraftClient.getInstance().keyboard.getClipboard());
            if (pasted != -1) {
                entry.setColor(pasted);
                ModuleManager.STYLE_MANAGER.scheduleSave();
            }
            return true;
        }
        return in(gx, gy, x, rowY, w, rowHeight(entry));
    }

    public static boolean isDragging(GuiThemeColor entry) {
        return entry.sbDragging || entry.hueDragging || entry.alphaDragging;
    }

    public static void applyDrag(int mouseX, int mouseY, int x, float rowY, int w, GuiThemeColor entry) {
        if (!isDragging(entry)) return;
        PickerLayout layout = PickerLayout.of(x, rowY, w, headerHeight(entry));
        if (entry.sbDragging) {
            applySb(entry, mouseX, mouseY, layout.innerX, layout.sbY, layout.innerW);
        }
        if (entry.hueDragging) {
            entry.hue = clamp01((mouseX - layout.innerX) / layout.innerW);
            entry.syncColorFromHsb();
            ModuleManager.STYLE_MANAGER.scheduleSave();
        }
        if (entry.alphaDragging) {
            entry.alpha = clamp01((mouseX - layout.innerX) / layout.innerW);
            entry.syncColorFromHsb();
            ModuleManager.STYLE_MANAGER.scheduleSave();
        }
    }

    private static void applySb(GuiThemeColor entry, double gx, double gy, float innerX, float sbY, float innerW) {
        entry.saturation = clamp01((float) ((gx - innerX) / innerW));
        entry.brightness = clamp01(1f - (float) ((gy - sbY) / SB_H));
        entry.syncColorFromHsb();
        ModuleManager.STYLE_MANAGER.scheduleSave();
    }

    public static void releaseAll() {
        if (ModuleManager.STYLE_MANAGER == null) return;
        for (GuiThemeColor c : ModuleManager.STYLE_MANAGER.getColors()) {
            c.sbDragging = false;
            c.hueDragging = false;
            c.alphaDragging = false;
        }
    }

    private static boolean in(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static String colorToHex(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;
        return a == 255 ? String.format("#%02X%02X%02X", r, g, b) : String.format("#%02X%02X%02X%02X", r, g, b, a);
    }

    private static int hexToColor(String raw) {
        if (raw == null) return -1;
        String hex = raw.trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        try {
            if (hex.length() == 6) {
                return 0xFF000000 | Integer.parseInt(hex, 16);
            }
            if (hex.length() == 8) {
                return (int) Long.parseLong(hex, 16);
            }
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }
}