package net.boostedbrightness.mixin;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoSettingsScreen.class)
public abstract class MixinOptionsScreen extends Screen {

    protected MixinOptionsScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"), require = 0)
    private void addBoostedBrightnessSlider(CallbackInfo ci) {
        double initial = 0.5d;
        try {
            OptionInstance<Double> gammaOpt = Minecraft.getInstance().options.gamma();
            if (gammaOpt != null)
                initial = gammaOpt.get();
        } catch (Throwable ignored) {
        }

        AbstractSliderButton customSlider = new AbstractSliderButton(10, 10, 200, 20,
                Component.literal("Boosted Brightness"), initial) {

            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Brightness: " + String.format("%.2f", this.value)));
            }

            @Override
            protected void applyValue() {
                try {
                    OptionInstance<Double> gammaOpt = Minecraft.getInstance().options.gamma();
                    if (gammaOpt != null) {
                        double newVal = Math.min(BoostedBrightness.maxBrightness,
                                Math.max(BoostedBrightness.minBrightness, this.value));
                        gammaOpt.set(newVal);
                    }
                } catch (Throwable t) {
                }
            }
        };

        this.addRenderableWidget(customSlider);
    }
}
