package net.boostedbrightness.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.boostedbrightness.BoostedBrightness.saveConfig;

@Mixin(Minecraft.class)
public class MixinMinecraftClient {
    private final long SAVE_INTERVAL = 2000;

    @Shadow
    public Options options;
    private long lastSaveTime = 0;

    @Inject(at = @At("HEAD"), method = "close")
    private void close(CallbackInfo info) {
        options.save();
        saveConfig();
    }

    @Inject(at = @At("HEAD"), method = "setScreen")
    private void setScreen(Screen screen, CallbackInfo info) {
        if (screen instanceof OptionsScreen && System.currentTimeMillis() - lastSaveTime > SAVE_INTERVAL) {
            saveConfig();
            lastSaveTime = System.currentTimeMillis();
        }
    }
}