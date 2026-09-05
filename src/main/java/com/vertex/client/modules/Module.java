package com.vertex.client.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vertex.client.modules.setting.*;
import com.vertex.client.util.IMinecraft;
import com.vertex.client.util.KeyMappings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("All")
public abstract class Module implements IMinecraft {
    private final ModuleInfo initerFunctions = this.getClass().getAnnotation(ModuleInfo.class);

    public String name;
    public String keywords;
    private ModuleCategory category;
    public int bind;
    public String desc;
    public boolean state;
    public boolean expanded;
    public float expandProgress = 0.0F;
    public float toggleProgress = 0.0F;
    public boolean favorite = false;
    public int customColor = -1;
    private final ArrayList<Setting> settings = new ArrayList<>();

    public Module() {
        initializeProperties();
    }

    public Module(String name, ModuleCategory category) {
        this.name = name;
        this.category = category;
        this.state = false;
        this.bind = 0;
    }

    private void initializeProperties() {
        name = initerFunctions.name();
        desc = initerFunctions.desc();
        category = initerFunctions.type();
        keywords = Arrays.toString(initerFunctions.keywords());
        state = false;
        bind = initerFunctions.key();
    }

    public abstract void onEvent(final Object event);

    public final void setState(final boolean enabled) {
        if (state == enabled) return;

        state = enabled;
        try {
            if (state) onEnable();
            else onDisable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public void addSettings(Setting... options) {
        settings.addAll(Arrays.asList(options));
    }

    public void toggle() {
        state = !state;
        try {
            if (state) {
                onEnable();
            } else {
                onDisable();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public boolean isState() {
        return state;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public String getTag() {
        return bind > 0 ? KeyMappings.keyMappings(bind) : "None";
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean hasDropdown() {
        return !settings.isEmpty();
    }

    public float getExpandProgress() {
        return expandProgress;
    }

    public void setExpandProgress(float progress) {
        this.expandProgress = progress;
    }

    public float getToggleProgress() {
        return toggleProgress;
    }

    public void setToggleProgress(float progress) {
        this.toggleProgress = progress;
    }

    public int getBindCode() {
        return bind;
    }

    public void setBindCode(int key) {
        bind = key;
    }

    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.addProperty("state", state);
        object.addProperty("keyIndex", bind);
        object.addProperty("clickguiExpanded", expanded);

        JsonObject propertiesObject = new JsonObject();
        for (Setting set : settings) {
            if (set instanceof BindBooleanSetting bbs) {
                JsonObject bbsObject = new JsonObject();
                bbsObject.addProperty("state", bbs.get());
                bbsObject.addProperty("bindKey", bbs.getBindKey());
                propertiesObject.add(bbs.getName(), bbsObject);
            } else if (set instanceof BooleanSetting bs) {
                propertiesObject.addProperty(set.getName(), bs.get());
            } else if (set instanceof MultiSetting ms) {
                propertiesObject.addProperty(set.getName(), ms.getConfigValue());
            } else if (set instanceof ModeSetting ms) {
                propertiesObject.addProperty(set.getName(), ms.get());
            } else if (set instanceof SliderSetting ss) {
                propertiesObject.addProperty(set.getName(), ss.get());
            } else if (set instanceof BindSetting bs) {
                propertiesObject.addProperty(set.getName(), bs.getKey());
            } else if (set instanceof TextSetting ts) {
                propertiesObject.addProperty(set.getName(), ts.getValue());
            } else if (set instanceof ColorSetting cs) {
                propertiesObject.addProperty(set.getName(), cs.get());
            }
        }

        object.add("Settings", propertiesObject);
        return object;
    }

    public void load(JsonObject object) {
        if (object == null) return;

        if (object.has("state")) {
            setState(object.get("state").getAsBoolean());
        }
        if (object.has("keyIndex")) {
            setBindCode(object.get("keyIndex").getAsInt());
        }
        if (object.has("clickguiExpanded")) {
            expanded = object.get("clickguiExpanded").getAsBoolean();
        }

        JsonElement settingsElement = object.get("Settings");
        if (settingsElement == null || !settingsElement.isJsonObject()) return;

        JsonObject propertiesObject = settingsElement.getAsJsonObject();

        for (Setting set : settings) {
            String name = set.getName();
            if (!propertiesObject.has(name)) continue;

            if (set instanceof BindBooleanSetting bbs && propertiesObject.get(name).isJsonObject()) {
                JsonObject bbsObject = propertiesObject.getAsJsonObject(name);
                if (bbsObject.has("state")) bbs.set(bbsObject.get("state").getAsBoolean());
                if (bbsObject.has("bindKey")) bbs.setKey(bbsObject.get("bindKey").getAsInt());
            } else {
                if (set instanceof BooleanSetting bs) {
                    bs.set(propertiesObject.get(name).getAsBoolean());
                } else if (set instanceof MultiSetting ms) {
                    ms.setConfigValue(propertiesObject.get(name).getAsString());
                } else if (set instanceof ModeSetting ms) {
                    ms.set(propertiesObject.get(name).getAsString());
                } else if (set instanceof SliderSetting ss) {
                    ss.set(propertiesObject.get(name).getAsFloat());
                } else if (set instanceof BindSetting bs) {
                    bs.setKey(propertiesObject.get(name).getAsInt());
                } else if (set instanceof TextSetting ts) {
                    ts.setValue(propertiesObject.get(name).getAsString());
                } else if (set instanceof ColorSetting cs) {
                    cs.set(propertiesObject.get(name).getAsInt());
                }
            }
        }
    }
}
