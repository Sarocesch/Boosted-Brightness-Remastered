package net.boostedbrightness.mixin;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VideoOptionsScreen.class)
public abstract class MixinOptionsScreen extends Screen {

    protected MixinOptionsScreen(Text title) {
        super(title);
    }

    @Inject(method = "addOptions", at = @At("RETURN"))
    private void addBoostedBrightnessSlider(CallbackInfo ci) {
        // Nur hinzufügen wenn Sodium nicht geladen ist
        if (BoostedBrightness.isSodiumLoaded) {
            return;
        }

        double initial = 0.5d;
        try {
            SimpleOption<Double> gammaOpt = MinecraftClient.getInstance().options.getGamma();
            if (gammaOpt != null) initial = gammaOpt.getValue();
        } catch (Throwable ignored) {}

        // Erstelle einen benutzerdefinierten Slider
        ClickableWidget customSlider = new net.minecraft.client.gui.widget.SliderWidget(10, 10, 200, 20,
                Text.literal("Boosted Brightness"), initial) {

            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal("Brightness: " + String.format("%.2f", this.value)));
            }

            @Override
            protected void applyValue() {
                try {
                    SimpleOption<Double> gammaOpt = MinecraftClient.getInstance().options.getGamma();
                    if (gammaOpt != null) {
                        double newVal = Math.min(BoostedBrightness.maxBrightness, Math.max(BoostedBrightness.minBrightness, this.value));
                        gammaOpt.setValue(newVal);
                    }
                } catch (Throwable t) {
                    // Ignoriere Fehler
                }
            }
        };

        this.addDrawableChild(customSlider);
    }
}