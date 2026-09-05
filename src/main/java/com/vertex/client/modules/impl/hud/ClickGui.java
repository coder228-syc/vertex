package com.vertex.client.modules.impl.hud;

import com.vertex.client.clickgui.ClickGuiOpenHelper;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(name = "ClickGui", desc = "Click GUI theme colors", type = ModuleCategory.Display, key = GLFW.GLFW_KEY_UNKNOWN)
public class ClickGui extends Module {

    public static ClickGui INSTANCE;

    public ClickGui() {
        INSTANCE = this;
    }

    @Override
    public void onEvent(Object event) {
    }

    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (ClickGuiOpenHelper.canOpen(mc)) {
            mc.setScreen(ClickGuiOpenHelper.open(mc));
        }
    }
}