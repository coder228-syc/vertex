package com.vertex.client.modules;

import com.vertex.client.clickgui.ClickGuiKeyListener;
import com.vertex.client.clickgui.ClickGuiScreen;
import com.vertex.client.modules.impl.hud.*;
import com.vertex.client.modules.impl.render.NameTags;
import com.vertex.client.modules.impl.render.ShaderBlock;
import com.vertex.client.modules.impl.render.ShaderHand;
import com.vertex.client.modules.impl.render.ShaderSky;
import com.vertex.client.render.font.FontUtils;
import com.vertex.client.theme.StyleManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModuleManager {

    public static final StyleManager STYLE_MANAGER = new StyleManager();
    public static final List<Module> MODULES = new CopyOnWriteArrayList<>();

    public interface ConfigManager {
        void saveConfiguration(String name);
    }

    public static final ConfigManager CONFIG_MANAGER = name -> {
    };

    static {
        new FontUtils().init();
        STYLE_MANAGER.init();
MODULES.add(new InventoryHud());
        MODULES.add(new TargetHud());
        MODULES.add(new ArmorHud());
        MODULES.add(new Watermark());
        MODULES.add(new PotionsHud());
        MODULES.add(new Cooldowns());
        MODULES.add(new Hotkeys());
        MODULES.add(new BetterNear());
        MODULES.add(new Speakers());
        MODULES.add(new NameTags());
        MODULES.add(new ShaderHand());
        MODULES.add(new ShaderBlock());
        MODULES.add(new ShaderSky());
        MODULES.add(new ClickGui());

        ClickGuiKeyListener.register();
    }

    public static List<Module> getModules(ModuleCategory category) {
        List<Module> out = new ArrayList<>();
        for (Module m : MODULES) {
            if (m.getCategory() == category) out.add(m);
        }
        return out;
    }

    public static Module get(String name) {
        for (Module m : MODULES) {
            if (m.name.equalsIgnoreCase(name)) return m;
        }
        return null;
    }
}