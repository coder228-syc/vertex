package com.vertex.client.clickgui;

import com.vertex.client.clickgui.impl.*;
import com.vertex.client.modules.setting.*;
import net.minecraft.client.gui.DrawContext;


final class SettingDispatch {

    private static final BooleanSettingRenderer BOOL = new BooleanSettingRenderer();
    private static final BindBooleanSettingRenderer BIND_BOOL = new BindBooleanSettingRenderer();
    private static final BindSettingRenderer BIND = new BindSettingRenderer();
    private static final SliderSettingRenderer SLIDER = new SliderSettingRenderer();
    private static final ModeSettingRenderer MODE = new ModeSettingRenderer();
    private static final MultiSettingRenderer MULTI = new MultiSettingRenderer();
    private static final ColorSettingRenderer COLOR = new ColorSettingRenderer();
    private static final TextSettingRenderer TEXT = new TextSettingRenderer();

    private SettingDispatch() {}

    static int height(Setting s, int width) {
        if (s instanceof BooleanSetting) return BooleanSettingRenderer.HEIGHT;
        if (s instanceof BindBooleanSetting) return BindBooleanSettingRenderer.HEIGHT;
        if (s instanceof BindSetting) return BindSettingRenderer.HEIGHT;
        if (s instanceof SliderSetting ss) return SliderSettingRenderer.getHeight(ss);
        if (s instanceof ModeSetting ms) return ModeSettingRenderer.getHeight(ms, width);
        if (s instanceof MultiSetting ms) return MultiSettingRenderer.getHeight(ms, width);
        if (s instanceof ColorSetting cs) return ColorSettingRenderer.getHeight(cs);
        if (s instanceof TextSetting) return TextSettingRenderer.HEIGHT;
        return 12;
    }

    static void render(DrawContext ctx, Setting s, int x, int y, int w, SettingRenderContext env) {
        if (s instanceof BooleanSetting bs) BOOL.render(ctx, bs, x, y, w, env);
        else if (s instanceof BindBooleanSetting bbs) BIND_BOOL.render(ctx, bbs, x, y, w, env);
        else if (s instanceof BindSetting bs2) BIND.render(ctx, bs2, x, y, w, env);
        else if (s instanceof SliderSetting ss) SLIDER.render(ctx, ss, x, y, w, env);
        else if (s instanceof ModeSetting ms) MODE.render(ctx, ms, x, y, w, env);
        else if (s instanceof MultiSetting ms) MULTI.render(ctx, ms, x, y, w, env);
        else if (s instanceof ColorSetting cs) COLOR.render(ctx, cs, x, y, w, env);
        else if (s instanceof TextSetting ts) TEXT.render(ctx, ts, x, y, w, env);
    }

    static boolean click(Setting s, int button, double mouseX, double mouseY, int x, int y, int w, SettingRenderContext env) {
        if (s instanceof BooleanSetting bs) return BOOL.mouseClicked(bs, button, mouseX, mouseY, x, y, w, env);
        if (s instanceof BindBooleanSetting bbs) return BIND_BOOL.mouseClicked(bbs, button, mouseX, mouseY, x, y, w, env);
        if (s instanceof BindSetting bs2) return BIND.mouseClicked(bs2, button, mouseX, mouseY, x, y, w, env);
        if (s instanceof SliderSetting ss) return SLIDER.mouseClicked(ss, button, mouseX, mouseY, x, y, w, env);
        if (s instanceof ModeSetting ms) return MODE.mouseClicked(ms, button, mouseX, mouseY, x, y, w, env);
        if (s instanceof MultiSetting ms) return MULTI.mouseClicked(ms, button, mouseX, mouseY, x, y, w, env);
        if (s instanceof ColorSetting cs) return COLOR.mouseClicked(cs, button, mouseX, mouseY, x, y, w, env);
        if (s instanceof TextSetting ts) return TEXT.mouseClicked(ts, button, mouseX, mouseY, x, y, w, env);
        return false;
    }
}
