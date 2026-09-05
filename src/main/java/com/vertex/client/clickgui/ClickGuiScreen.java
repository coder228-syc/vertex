package com.vertex.client.clickgui;

import com.vertex.client.clickgui.impl.ColorSettingRenderer;
import com.vertex.client.clickgui.impl.SliderSettingRenderer;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleManager;
import com.vertex.client.modules.impl.hud.ClickGui;
import com.vertex.client.modules.setting.ColorSetting;
import com.vertex.client.modules.setting.Setting;
import com.vertex.client.modules.setting.SliderSetting;
import com.vertex.client.render.font.FontUtils;
import com.vertex.client.render.font.RenderFonts;
import com.vertex.client.render.util.KawaseBlur;
import com.vertex.client.render.util.RenderUtil;
import com.vertex.client.render.util.Scissor;
import com.vertex.client.util.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vertex layout (window, sidebar, cards, categories) with SkyCore surface theme:
 * kawase blur, dark overlay, low-alpha white cards, accent 142/187/255.
 */
public class ClickGuiScreen extends Screen implements IMinecraft {
    private static final ModuleCategory[] CATEGORIES = {
            ModuleCategory.Render,
            ModuleCategory.Display,
            ModuleCategory.Combat,
            ModuleCategory.Movement,
            ModuleCategory.Player,
            ModuleCategory.Misc
    };

    private static final int WINDOW_W = 660;
    private static final int WINDOW_H = 400;
    private static final int SIDEBAR_W = 118;
    private static final int CARD_W = 168;
    private static final int CARD_H = 52;
    private static final int SETTINGS_PAD_TOP = 6;
    private static final int SETTINGS_PAD_BOTTOM = 6;
    private static final int SETTINGS_ROW_GAP = 2;
    private static final int CARD_GAP = 12;

    private static final int SEARCH_W = 106;
    private static final int SEARCH_H = 18;
    private static final int NAV_PILL_H = 18;
    private static final int NAV_STEP = 22;

    private static final float WINDOW_RADIUS = 26.0F;
    private static final float PANEL_RADIUS = 18.0F;
    private static final float CARD_RADIUS = 12.0F;
    private static final float BLUR_OFFSET = 3.0F;
    private static final int BLUR_STEPS = 5;
    private static final float BLUR_STRENGTH = 1.0F;

    private static final int THEME_SWATCH_SIZE = 12;
    private static final int THEME_SWATCH_GAP = 3;
    private static final int[] THEME_SWATCHES = new int[]{
            0xFF159AF2,
            0xFF6747E8,
            0xFF9B43E4,
            0xFFD544C7,
            0xFFF24D72,
            0xFFFF6C57,
            0xFFFF8D50,
            0xFFFFBD50,
            0xFFFFDB65,
            0xFF8FD05D
    };

    private static final int WINDOW_OVERLAY = rgb(21, 21, 21, 217);
    private static final int PANEL_FILL = rgb(0, 0, 0, opacity(15));
    private static final int ACCENT = rgb(142, 187, 255, 255);
    private static final int TEXT = rgb(255, 255, 255, 255);
    private static final int TEXT_DIM = rgb(150, 150, 160, 255);
    private static final int TEXT_MUTED = rgb(255, 255, 255, 64);
    private static final int TEXT_SOFT = rgb(255, 255, 255, 191);
    private static final int SEARCH_BG = rgb(255, 255, 255, 8);
    private static final int ITEM_ACTIVE = rgb(255, 255, 255, opacity(10));
    private static final int DIVIDER = rgb(255, 255, 255, opacity(8));
    private static final int CARD_FILL_OFF = rgb(255, 255, 255, opacity(2));
    private static final int CARD_FILL_ON = rgb(255, 255, 255, opacity(7));
    private static final int CARD_LINE_OFF = rgb(255, 255, 255, opacity(2));
    private static final int CARD_LINE_ON = rgb(255, 255, 255, opacity(13));
    private static final int SETTINGS_FILL = rgb(255, 255, 255, opacity(1));
    private static final int SETTINGS_LINE = rgb(255, 255, 255, opacity(3));
    private static final int TOGGLE_OFF = rgb(55, 55, 65, 255);
    private static final int FOOTER_TEXT = rgb(140, 140, 140, 255);
    private static final int BLUR_TINT = rgb(255, 255, 255, 0);

    private final @Nullable Screen backgroundScreen;
    private ModuleCategory currentCategory = ModuleCategory.Render;
    private Module expandedModule = null;
    private String searchText = "";
    private boolean searchFocused = false;
    private float scrollY = 0.0F;
    private float maxScroll = 0.0F;
    private float lastDelta = 0.016F;
    private float indicatorY = 0.0F;
    private Setting draggingSetting = null;
    private int dragX = 0;
    private int dragY = 0;
    private int dragW = 0;

    private final Map<Module, Float> enableAnim = new IdentityHashMap<>();

    public ClickGuiScreen() {
        this(null);
    }

    public ClickGuiScreen(@Nullable Screen backgroundScreen) {
        super(Text.literal("Vertex GUI"));
        this.backgroundScreen = backgroundScreen;
    }

    @Override
    protected void init() {
        super.init();
        this.scrollY = 0.0F;
        this.indicatorY = navY(windowY());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        if (backgroundScreen != null) {
            backgroundScreen.render(ctx, mouseX, mouseY, delta);
        }

        lastDelta = Math.max(0.001F, Math.min(0.08F, delta));
        KawaseBlur.blur.updateBlur(BLUR_OFFSET, BLUR_STEPS);

        MinecraftClient mc = MinecraftClient.getInstance();
        int x = windowX();
        int y = windowY();

        renderWindow(ctx, x, y);
        renderSidebar(ctx, x, y);
        renderContentPanel(ctx, x, y);
        renderSearch(ctx, x, y);
        renderContent(ctx, mc, x, y, mouseX, mouseY);
        renderFooter(ctx, x, y);
    }

    private void renderWindow(DrawContext ctx, int x, int y) {
        RenderUtil.drawBlurredRoundedRectangle(
                ctx.getMatrices(),
                x, y, WINDOW_W, WINDOW_H,
                WINDOW_RADIUS,
                BLUR_TINT,
                BLUR_STRENGTH
        );
        RenderUtil.drawRoundedRect(ctx.getMatrices(), x, y, WINDOW_W, WINDOW_H, WINDOW_RADIUS, WINDOW_OVERLAY);
    }

    private void renderContentPanel(DrawContext ctx, int x, int y) {
        int panelX = x + SIDEBAR_W + 6;
        int panelY = y + 40;
        int panelW = WINDOW_W - SIDEBAR_W - 14;
        int panelH = WINDOW_H - 52;
        RenderUtil.drawRoundedRect(ctx.getMatrices(), panelX, panelY, panelW, panelH, PANEL_RADIUS, PANEL_FILL);
    }

    private void renderSidebar(DrawContext ctx, int x, int y) {
        RenderFonts title = font(FontUtils.gilroy, 16);
        RenderFonts version = font(FontUtils.inter, 11);
        RenderFonts label = font(FontUtils.gilroy, 12);
        RenderFonts icons = font(FontUtils.icomoon, 12);

        if (title != null) {
            title.drawLeftAligned(ctx.getMatrices(), "Vertex", x + 36, y + 13, TEXT);
        }
        if (version != null) {
            version.drawLeftAligned(ctx.getMatrices(), "Client", x + 36, y + 24, TEXT_DIM);
        }

        renderThemeSwatches(ctx, x, y);

        int navX = navX(x);
        int navY = navY(y);
        int pillW = navW();

        int activeIndex = 0;
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i] == currentCategory) {
                activeIndex = i;
                break;
            }
        }
        float targetY = navY + activeIndex * NAV_STEP;
        indicatorY += (targetY - indicatorY) * Math.min(1.0F, lastDelta * 14.0F);

        RenderUtil.drawRoundedRect(ctx.getMatrices(), navX, indicatorY, pillW, NAV_PILL_H, 12.0F, ITEM_ACTIVE);
        RenderUtil.drawRoundedBorder(ctx.getMatrices(), navX, indicatorY, pillW, NAV_PILL_H, 12.0F, 1.2F, ITEM_ACTIVE);

        int dividerX = navX + 12;
        RenderUtil.drawRoundedRect(ctx.getMatrices(), dividerX, navY - 12, 56.0F, 1.0F, 2.0F, DIVIDER);
        RenderUtil.drawRoundedRect(ctx.getMatrices(), dividerX, navY + CATEGORIES.length * NAV_STEP + 4, 56.0F, 1.0F, 2.0F, DIVIDER);

        for (int i = 0; i < CATEGORIES.length; i++) {
            ModuleCategory category = CATEGORIES[i];
            boolean active = category == currentCategory;
            int by = navY + i * NAV_STEP;
            int textColor = active ? TEXT : TEXT_DIM;

            if (icons != null && category.icon != null) {
                icons.drawLeftAligned(ctx.getMatrices(), category.icon, navX + 6.5F, by + 3.0F, themeAccent());
            }
            if (label != null) {
                label.drawLeftAligned(ctx.getMatrices(), category.name(), navX + 22.0F, by + 3.5F, textColor);
            }
        }
    }

    private void renderSearch(DrawContext ctx, int x, int y) {
        RenderFonts regular = font(FontUtils.inter, 12);
        int searchX = searchX(x);
        int searchY = searchY(y);

        RenderUtil.drawRoundedRect(ctx.getMatrices(), searchX, searchY, SEARCH_W, SEARCH_H, 12.0F, SEARCH_BG);

        String text = searchText.isEmpty() ? (searchFocused ? "" : "Search...") : searchText;
        int color = searchText.isEmpty() ? TEXT_MUTED : TEXT_SOFT;
        if (regular != null && !text.isEmpty()) {
            regular.drawLeftAligned(ctx.getMatrices(), text, searchX + 7.0F, searchY + 4.0F, color);
        }
        if (searchFocused && searchText.isEmpty() && ((System.currentTimeMillis() / 420L) & 1L) == 0L && regular != null) {
            RenderUtil.drawRoundedRect(ctx.getMatrices(), searchX + 7.0F, searchY + 4.0F, 1.0F, 10.0F, 0.5F, TEXT_SOFT);
        }
    }

    private void renderContent(DrawContext ctx, MinecraftClient mc, int x, int y, int mouseX, int mouseY) {
        RenderFonts titleFont = font(FontUtils.gilroy, 12);
        RenderFonts descFont = font(FontUtils.inter, 10);

        List<Module> filtered = getFilteredModules();
        updateScroll(filtered);

        int leftX = cardLeftX(x);
        int rightX = cardRightX(x);
        int listY = cardListY(y);
        int leftOffset = 0;
        int rightOffset = 0;

        int panelX = x + SIDEBAR_W + 6;
        int panelY = y + 40;
        int panelW = WINDOW_W - SIDEBAR_W - 14;
        int panelH = WINDOW_H - 52;

        Scissor.push();
        try {
            Scissor.setFromComponentCoordinates(panelX, panelY, panelW, panelH);
            for (Module mod : filtered) {
                boolean leftColumn = leftOffset <= rightOffset;
                int cardX = leftColumn ? leftX : rightX;
                int cardY = listY + (leftColumn ? leftOffset : rightOffset) - (int) scrollY;
                int cardHeight = getModuleBlockHeight(mod);
                if (leftColumn) {
                    leftOffset += cardHeight + CARD_GAP;
                } else {
                    rightOffset += cardHeight + CARD_GAP;
                }
                if (cardY + cardHeight < y + 30 || cardY > y + WINDOW_H - 20) {
                    continue;
                }

                float enable = enableAnim.getOrDefault(mod, mod.isState() ? 1.0F : 0.0F);
                enable += ((mod.isState() ? 1.0F : 0.0F) - enable) * Math.min(1.0F, lastDelta * 10.0F);
                enableAnim.put(mod, enable);

                boolean expanded = expandedModule == mod && !mod.getSettings().isEmpty();
                renderModuleCard(ctx, mc, mod, cardX, cardY, enable, expanded, titleFont, descFont);

                if (expanded) {
                    renderSettings(ctx, mod, cardX, cardY);
                }
            }

            if (filtered.isEmpty() && descFont != null) {
                descFont.drawLeftAligned(ctx.getMatrices(), "No modules", cardLeftX(x) + 8, y + 80, TEXT_DIM);
            }
        } finally {
            Scissor.pop();
        }
    }

    private void renderModuleCard(
            DrawContext ctx,
            MinecraftClient mc,
            Module mod,
            int cardX,
            int cardY,
            float enable,
            boolean expanded,
            RenderFonts titleFont,
            RenderFonts descFont
    ) {
        int fill = lerpColor(CARD_FILL_OFF, CARD_FILL_ON, enable);
        int line = lerpColor(CARD_LINE_OFF, CARD_LINE_ON, enable);

        RenderUtil.drawRoundedRect(ctx.getMatrices(), cardX, cardY, CARD_W, CARD_H, CARD_RADIUS, fill);
        RenderUtil.drawRoundedBorder(ctx.getMatrices(), cardX, cardY, CARD_W, CARD_H, CARD_RADIUS, 1.2F, line);

        float textMul = opacity(25) / 255.0F + (1.0F - opacity(25) / 255.0F) * enable;
        float descMul = opacity(15) / 255.0F + (opacity(25) - opacity(15)) / 255.0F * enable;
        int titleColor = withAlpha(TEXT, (int) (255 * textMul));
        int descColor = withAlpha(TEXT, (int) (255 * descMul));

        String name = fitText(mc, mod.name, CARD_W - 52);
        if (titleFont != null) {
            titleFont.drawLeftAligned(ctx.getMatrices(), name, cardX + 10, cardY + 10, titleColor);
        }
        String desc = fitText(mc, mod.desc, CARD_W - 20);
        if (descFont != null) {
            descFont.drawLeftAligned(ctx.getMatrices(), desc, cardX + 10, cardY + 26, descColor);
        }

        drawToggle(ctx, cardX + CARD_W - 22, cardY + 12, enable);
    }

    private void renderSettings(DrawContext ctx, Module mod, int cardX, int cardY) {
        List<Setting> settings = getVisibleSettings(mod);
        int settingsY = settingsY(cardY);
        int contentW = CARD_W - 12;
        int settingsH = getExpandedSettingsHeight(mod);
        RenderUtil.drawRoundedRect(ctx.getMatrices(), cardX, settingsY, CARD_W, settingsH, 9.0F, SETTINGS_FILL);
        RenderUtil.drawRoundedBorder(ctx.getMatrices(), cardX, settingsY, CARD_W, settingsH, 9.0F, 1.2F, SETTINGS_LINE);

        SettingRenderContext env = createSettingContext();
        int rowY = settingsY + SETTINGS_PAD_TOP;
        for (Setting setting : settings) {
            int height = SettingDispatch.height(setting, contentW);
            SettingDispatch.render(ctx, setting, cardX + 6, rowY, contentW, env);
            rowY += height + SETTINGS_ROW_GAP;
        }
    }

    private void renderFooter(DrawContext ctx, int x, int y) {
        RenderFonts footer = font(FontUtils.inter, 10);
        int barX = x + SIDEBAR_W + 10;
        int barY = y + WINDOW_H - 16;
        if (footer != null) {
            footer.drawLeftAligned(ctx.getMatrices(), "Modules: " + getFilteredModules().size(), barX + 4, barY - 10, FOOTER_TEXT);
        }
    }

    private void drawToggle(DrawContext ctx, float x, float y, float progress) {
        int track = lerpColor(TOGGLE_OFF, themeAccent(), progress);
        RenderUtil.drawRoundedRect(ctx.getMatrices(), x, y, 10.0F, 6.5F, 6.5F, track);
        float knobX = x + 4.5F * progress;
        RenderUtil.drawRoundedRect(ctx.getMatrices(), knobX, y + 1.0F, 4.5F, 4.5F, 4.5F, TEXT);
    }

    private int themeAccent() {
        int c = ModuleManager.STYLE_MANAGER != null ? ModuleManager.STYLE_MANAGER.getFirstColor() : -1;
        return c == -1 ? ACCENT : withAlpha(c, 255);
    }

    private void renderThemeSwatches(DrawContext ctx, int x, int y) {
        int baseX = themeSwatchesX(x);
        int baseY = themeSwatchesY(y);
        int selected = themeSelectedIndex();
        for (int i = 0; i < THEME_SWATCHES.length; i++) {
            int sx = baseX + i * (THEME_SWATCH_SIZE + THEME_SWATCH_GAP);
            boolean sel = i == selected;
            RenderUtil.drawRoundedRect(ctx.getMatrices(), sx, baseY, THEME_SWATCH_SIZE, THEME_SWATCH_SIZE, 4.0F, THEME_SWATCHES[i]);
            RenderUtil.drawRoundedBorder(ctx.getMatrices(), sx, baseY, THEME_SWATCH_SIZE, THEME_SWATCH_SIZE, 4.0F, sel ? 2.0F : 1.0F, sel ? TEXT : withAlpha(TEXT, opacity(30)));
        }
    }

    private int themeSelectedIndex() {
        int current = ModuleManager.STYLE_MANAGER != null ? (ModuleManager.STYLE_MANAGER.getFirstColor() & 0xFFFFFF) : -1;
        if (current == -1) {
            return -1;
        }
        for (int i = 0; i < THEME_SWATCHES.length; i++) {
            if ((THEME_SWATCHES[i] & 0xFFFFFF) == current) {
                return i;
            }
        }
        return -1;
    }

    private int themeSwatchIndexAt(double mouseX, double mouseY, int x, int y) {
        int baseX = themeSwatchesX(x);
        int baseY = themeSwatchesY(y);
        for (int i = 0; i < THEME_SWATCHES.length; i++) {
            int sx = baseX + i * (THEME_SWATCH_SIZE + THEME_SWATCH_GAP);
            if (hit(mouseX, mouseY, sx, baseY, THEME_SWATCH_SIZE, THEME_SWATCH_SIZE)) {
                return i;
            }
        }
        return -1;
    }

    private int themeSwatchesX(int x) {
        int total = THEME_SWATCHES.length * THEME_SWATCH_SIZE + (THEME_SWATCHES.length - 1) * THEME_SWATCH_GAP;
        return x + (WINDOW_W - total) / 2;
    }

    private int themeSwatchesY(int y) {
        return y + 15;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollY += (float) (verticalAmount * 14.0D);
        scrollY = Math.max(0.0F, Math.min(maxScroll, scrollY));
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSetting instanceof SliderSetting ss) {
            SliderSettingRenderer.applyValueFromGuiX(ss, mouseX, dragX, dragW);
            return true;
        }
        if (draggingSetting instanceof ColorSetting cs) {
            ColorSettingRenderer.applyDrag(cs, mouseX, mouseY, dragX, dragY, dragW);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingSetting instanceof ColorSetting cs) {
            ColorSettingRenderer.releaseAll(cs);
        }
        draggingSetting = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = windowX();
        int y = windowY();

        int themeIndex = themeSwatchIndexAt(mouseX, mouseY, x, y);
        if (themeIndex >= 0) {
            ModuleManager.STYLE_MANAGER.setPrimaryColor(THEME_SWATCHES[themeIndex]);
            return true;
        }

        if (hit(mouseX, mouseY, searchX(x), searchY(y), SEARCH_W, SEARCH_H)) {
            searchFocused = true;
            return true;
        }

        int navStartX = navX(x);
        int navStartY = navY(y);
        int pillW = navW();
        for (int i = 0; i < CATEGORIES.length; i++) {
            int by = navStartY + i * NAV_STEP;
            if (hit(mouseX, mouseY, navStartX, by, pillW, NAV_PILL_H)) {
                currentCategory = CATEGORIES[i];
                expandedModule = null;
                scrollY = 0.0F;
                return true;
            }
        }

        List<Module> modules = getFilteredModules();
        int leftX = cardLeftX(x);
        int rightX = cardRightX(x);
        int listY = cardListY(y);
        int leftOffset = 0;
        int rightOffset = 0;

        for (Module mod : modules) {
            boolean leftColumn = leftOffset <= rightOffset;
            int cardX = leftColumn ? leftX : rightX;
            int cardY = listY + (leftColumn ? leftOffset : rightOffset) - (int) scrollY;
            int cardHeight = getModuleBlockHeight(mod);

            if (hit(mouseX, mouseY, cardX, cardY, CARD_W, CARD_H)) {
                if (button == 1 && !mod.getSettings().isEmpty()) {
                    expandedModule = (expandedModule == mod) ? null : mod;
                } else {
                    mod.toggle();
                }
                return true;
            }

            if (expandedModule == mod && !mod.getSettings().isEmpty()) {
                int settingsY = settingsY(cardY);
                int settingsH = getExpandedSettingsHeight(mod);
                if (mouseY >= settingsY && mouseY <= settingsY + settingsH) {
                    int contentW = CARD_W - 12;
                    int rowY = settingsY + SETTINGS_PAD_TOP;
                    for (Setting setting : getVisibleSettings(mod)) {
                        int height = SettingDispatch.height(setting, contentW);
                        if (mouseY >= rowY && mouseY <= rowY + height) {
                            boolean handled = SettingDispatch.click(setting, button, mouseX, mouseY, cardX + 6, rowY, contentW, createSettingContext());
                            if (handled) {
                                if (setting instanceof SliderSetting || setting instanceof ColorSetting) {
                                    draggingSetting = setting;
                                    dragX = cardX + 6;
                                    dragY = rowY;
                                    dragW = contentW;
                                }
                                return true;
                            }
                        }
                        rowY += height + SETTINGS_ROW_GAP;
                    }
                }
            }

            if (leftColumn) {
                leftOffset += cardHeight + CARD_GAP;
            } else {
                rightOffset += cardHeight + CARD_GAP;
            }
        }

        if (button == 1 && searchFocused) {
            searchText = "";
            searchFocused = false;
            return true;
        }

        searchFocused = false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (!searchFocused) {
            return super.charTyped(chr, keyCode);
        }
        if (chr == '\u0008' || Character.isISOControl(chr)) {
            return super.charTyped(chr, keyCode);
        }
        searchText += chr;
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == 14 || keyCode == 211 || keyCode == 52) {
                if (!searchText.isEmpty()) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                }
                return true;
            }
            if (keyCode == 1) {
                searchFocused = false;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private List<Module> getFilteredModules() {
        List<Module> filtered = new ArrayList<>();
        for (Module mod : ModuleManager.MODULES) {
            if (mod.getCategory() != currentCategory) {
                continue;
            }
            if (searchText == null || searchText.isEmpty()) {
                filtered.add(mod);
                continue;
            }
            String query = searchText.toLowerCase();
            String name = mod.name == null ? "" : mod.name.toLowerCase();
            String desc = mod.desc == null ? "" : mod.desc.toLowerCase();
            if (name.contains(query) || desc.contains(query)) {
                filtered.add(mod);
            }
        }
        return filtered;
    }

    private void updateScroll(List<Module> modules) {
        int leftOffset = 0;
        int rightOffset = 0;
        for (Module mod : modules) {
            int height = getModuleBlockHeight(mod) + CARD_GAP;
            if (leftOffset <= rightOffset) {
                leftOffset += height;
            } else {
                rightOffset += height;
            }
        }
        int y = windowY();
        int listY = cardListY(y);
        int panelBottom = y + 40 + (WINDOW_H - 52);
        int visibleH = Math.max(48, panelBottom - listY - 8);
        maxScroll = Math.max(0.0F, Math.max(leftOffset, rightOffset) - visibleH);
        scrollY = Math.max(0.0F, Math.min(maxScroll, scrollY));
    }

    private int getModuleBlockHeight(Module mod) {
        int height = CARD_H;
        if (expandedModule == mod && !mod.getSettings().isEmpty()) {
            height += 8 + getExpandedSettingsHeight(mod);
        }
        return height;
    }

    private int getExpandedSettingsHeight(Module mod) {
        if (mod == null || mod.getSettings() == null || mod.getSettings().isEmpty()) {
            return 0;
        }
        List<Setting> settings = getVisibleSettings(mod);
        if (settings.isEmpty()) {
            return 0;
        }
        int contentW = CARD_W - 12;
        int total = SETTINGS_PAD_TOP;
        for (Setting setting : settings) {
            total += SettingDispatch.height(setting, contentW) + SETTINGS_ROW_GAP;
        }
        return total + SETTINGS_PAD_BOTTOM;
    }

    private List<Setting> getVisibleSettings(Module mod) {
        List<Setting> visible = new ArrayList<>();
        if (mod == null || mod.getSettings() == null) {
            return visible;
        }
        for (Setting setting : mod.getSettings()) {
            if (setting.isVisible()) {
                visible.add(setting);
            }
        }
        return visible;
    }

    private String fitText(MinecraftClient mc, String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String value = text.trim();
        if (mc.textRenderer.getWidth(value) <= maxWidth) {
            return value;
        }
        String suffix = "...";
        int end = value.length();
        while (end > 0 && mc.textRenderer.getWidth(value.substring(0, end) + suffix) > maxWidth) {
            end--;
        }
        return end <= 0 ? suffix : value.substring(0, end) + suffix;
    }

    private int windowX() {
        return (width - WINDOW_W) / 2;
    }

    private int windowY() {
        return (height - WINDOW_H) / 2;
    }

    private int contentX(int x) {
        return x + SIDEBAR_W + 12;
    }

    private int cardLeftX(int x) {
        return contentX(x) + 12;
    }

    private int cardRightX(int x) {
        return cardLeftX(x) + CARD_W + 10;
    }

    private int cardListY(int y) {
        return y + 56;
    }

    private int searchX(int x) {
        return x + WINDOW_W - 132;
    }

    private int searchY(int y) {
        return y + 10;
    }

    private int navX(int x) {
        return x + 12;
    }

    private int navY(int y) {
        return y + 90;
    }

    private int navW() {
        return SIDEBAR_W - 24;
    }

    private int settingsY(int cardY) {
        return cardY + CARD_H + 4;
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static RenderFonts font(RenderFonts[] family, int size) {
        if (family == null || size < 0 || size >= family.length) {
            return null;
        }
        return family[size];
    }

    private static int opacity(int percent) {
        return Math.round(255.0F * percent / 100.0F);
    }

    private static int rgb(int r, int g, int b, int a) {
        return new Color(clamp(r), clamp(g), clamp(b), clamp(a)).getRGB();
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static int withAlpha(int color, int alpha) {
        alpha = clamp(alpha);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return new Color(r, g, b, alpha).getRGB();
    }

    private static int lerpColor(int from, int to, float t) {
        t = Math.max(0.0F, Math.min(1.0F, t));
        Color a = new Color(from, true);
        Color b = new Color(to, true);
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t)
        ).getRGB();
    }

    private SettingRenderContext createSettingContext() {
        return new SettingRenderContext() {
            @Override
            public int ao(int color) {
                return color;
            }

            @Override
            public int accentRgb() {
                return themeAccent();
            }

            @Override
            public int textPrimary() {
                return TEXT;
            }

            @Override
            public int textSecondary() {
                return TEXT_SOFT;
            }

            @Override
            public int textMuted() {
                return TEXT_MUTED;
            }

            @Override
            public int iconMuted() {
                return TEXT_DIM;
            }

            @Override
            public int fieldFill() {
                return rgb(255, 255, 255, opacity(7));
            }

            @Override
            public int panelBorder() {
                return rgb(255, 255, 255, opacity(8));
            }

            @Override
            public int sliderTrack() {
                return rgb(40, 38, 52, 255);
            }

            @Override
            public void drawPanel(DrawContext ctx, float x, float y, float w, float h, float rounding, float alpha) {
                RenderUtil.drawRoundedRect(ctx.getMatrices(), x, y, w, h, rounding, withAlpha(rgb(255, 255, 255, opacity(2)), (int) (alpha * 255f)));
            }

            @Override
            public void drawField(DrawContext ctx, float x, float y, float w, float h, float rounding, float alpha) {
                RenderUtil.drawRoundedRect(ctx.getMatrices(), x, y, w, h, rounding, withAlpha(rgb(255, 255, 255, opacity(7)), (int) (alpha * 255f)));
            }

            @Override
            public void drawPanelBorder(DrawContext ctx, float x, float y, float w, float h, float rounding, int borderRgb, float alpha) {
                RenderUtil.drawRoundedBorder(ctx.getMatrices(), x, y, w, h, rounding, 0.8F, withAlpha(borderRgb, (int) (alpha * 255f)));
            }

            @Override
            public void renderToggle(DrawContext ctx, int x, int y, boolean state, Object id) {
                drawToggle(ctx, x, y + 3, state ? 1.0F : 0.0F);
            }
        };
    }
}
