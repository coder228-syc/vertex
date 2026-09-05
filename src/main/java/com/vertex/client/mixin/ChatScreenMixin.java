package com.vertex.client.mixin;

import com.vertex.client.hud.HudEditor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void vertex$hudEditor(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HudEditor.get().updateAndRender(context, mouseX, mouseY);
    }
}