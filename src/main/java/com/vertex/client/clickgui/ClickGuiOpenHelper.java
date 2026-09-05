package com.vertex.client.clickgui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import org.jetbrains.annotations.Nullable;

public final class ClickGuiOpenHelper {

    private ClickGuiOpenHelper() {}

    public static boolean inGame(MinecraftClient client) {
        return client.world != null && client.player != null;
    }

    public static boolean shouldNotKeepAsBackgroundInGame(Screen current) {
        if (current instanceof ClickGuiScreen) {
            return true;
        }
        if (current instanceof HandledScreen) {
            return true;
        }
        if (current instanceof ChatScreen) {
            return true;
        }
        if (current instanceof ConfirmLinkScreen) {
            return true;
        }
        if (current instanceof GameMenuScreen) {
            return true;
        }
        if (current instanceof OpenToLanScreen) {
            return true;
        }
        if (current instanceof OptionsScreen) {
            return true;
        }
        if (current instanceof DeathScreen) {
            return true;
        }
        if (current instanceof PackScreen) {
            return true;
        }
        if (current instanceof StatsScreen) {
            return true;
        }
        if (isVanillaPackage(current, "net.minecraft.client.gui.screen.option")) {
            return true;
        }
        if (isVanillaPackage(current, "net.minecraft.client.gui.screen.advancement")) {
            return true;
        }
        if (isVanillaPackage(current, "net.minecraft.client.gui.screen.report")) {
            return true;
        }
        if (isVanillaPackage(current, "net.minecraft.client.gui.screen.message")) {
            return true;
        }
        return false;
    }

    private static boolean isVanillaPackage(Screen screen, String packagePrefix) {
        Package p = screen.getClass().getPackage();
        if (p == null) {
            return false;
        }
        return p.getName().equals(packagePrefix) || p.getName().startsWith(packagePrefix + ".");
    }

    @Nullable
    public static Screen backgroundToUse(MinecraftClient client) {
        Screen cur = client.currentScreen;
        if (!inGame(client) || cur == null) {
            return cur;
        }
        if (shouldNotKeepAsBackgroundInGame(cur)) {
            return null;
        }
        return cur;
    }

    public static boolean canOpen(MinecraftClient client) {
        Screen cur = client.currentScreen;
        if (cur instanceof ClickGuiScreen) {
            return false;
        }
        if (!inGame(client) || cur == null) {
            return true;
        }
        return !shouldNotKeepAsBackgroundInGame(cur);
    }

    public static ClickGuiScreen open(MinecraftClient client) {
        try {
            client.options.getMenuBackgroundBlurriness().setValue(0);
        } catch (Throwable ignored) {}
        return new ClickGuiScreen(backgroundToUse(client));
    }
}