package com.vertex;

import com.vertex.client.hud.HudEditor;
import com.vertex.client.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;

public class VertexClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModuleManager.MODULES.size();
        HudEditor.get().load();
    }
}
