package com.vertex.client.clickgui.impl;

import com.vertex.client.util.ClientManager;
import com.vertex.client.render.font.FontUtils;
import com.vertex.client.modules.setting.BindBooleanSetting;
import com.vertex.client.clickgui.SettingRenderContext;
import com.vertex.client.clickgui.SettingRenderer;
import net.minecraft.client.gui.DrawContext;

public final class BindBooleanSettingRenderer implements SettingRenderer<BindBooleanSetting> {

    public static final int HEIGHT = 12;

    @Override
    public void render(DrawContext ctx, BindBooleanSetting bbs, int x, int y, int w, SettingRenderContext env) {
        FontUtils.gilroy[10].drawLeftAligned(ctx.getMatrices(), bbs.getName(), x, y + 1, env.textPrimary());
        String state = bbs.isListeningForBind() ? "..." : (bbs.getBindKey() == 0 ? "None" : ClientManager.getKey(bbs.getBindKey()));
        FontUtils.gilroy[10].drawLeftAligned(ctx.getMatrices(), state, x + w - 25 - (int) FontUtils.gilroy[10].getWidth(state), y + 1, env.ao(env.accentRgb()));
        env.renderToggle(ctx, x + w - 18, y, bbs.get(), bbs);
    }

    @Override
    public boolean mouseClicked(BindBooleanSetting bbs, int button, double mouseX, double mouseY, int x, int y, int w, SettingRenderContext env) {
        if (button == 0) {
            bbs.set(!bbs.get());
            return true;
        }
        if (button == 1) {
            bbs.setListeningForBind(true);
            return true;
        }
        return false;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }
}
