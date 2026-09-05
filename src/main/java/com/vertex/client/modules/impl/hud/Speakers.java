package com.vertex.client.modules.impl.hud;

import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(name = "Speakers", desc = "Отображение громкости игроков", type = ModuleCategory.Display, key = GLFW.GLFW_KEY_UNKNOWN)
public class Speakers extends Module {

    public Speakers() {
    }

    @Override
    public void onEvent(Object event) {
    }
}