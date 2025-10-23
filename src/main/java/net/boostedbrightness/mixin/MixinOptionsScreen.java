package net.boostedbrightness.mixin;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VideoOptionsScreen.class)
public abstract class MixinOptionsScreen extends Screen {

    // Konstruktor nötig, da wir Screen erweitern
    protected MixinOptionsScreen(Text title) {
        super(title);
    }

    // robustes target: "init" ohne Descriptor, require=0 macht Build tolerant, falls Mapping leicht abweicht
    @Inject(method = "init", at = @At("RETURN"), require = 0)
    private void addBoostedBrightnessSlider(CallbackInfo ci) {
        // initialer Slider-Wert: nutze den aktuellen Wert der Gamma-Option
        double initial = 0.5d;
        try {
            SimpleOption<Double> gammaOpt = MinecraftClient.getInstance().options.getGamma();
            if (gammaOpt != null) initial = gammaOpt.getValue();
        } catch (Throwable ignored) {}

        SliderWidget customSlider = new SliderWidget(10, 10, 200, 20,
                Text.literal("Boosted Brightness"), initial) {

            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal("Brightness: " + String.format("%.2f", this.value)));
            }

            @Override
            protected void applyValue() {
                // Setze den Wert sauber in die SimpleOption (keine direkten Feldzugriffe)
                try {
                    SimpleOption<Double> gammaOpt = MinecraftClient.getInstance().options.getGamma();
                    if (gammaOpt != null) {
                        // Clamp auf Werte aus BoostedBrightness, um Konsistenz zu wahren
                        double newVal = Math.min(BoostedBrightness.maxBrightness, Math.max(BoostedBrightness.minBrightness, this.value));
                        gammaOpt.setValue(newVal);
                    }
                } catch (Throwable t) {
                    // Falls etwas unerwartet ist, ignoriere — wir wollen keinen Crash hier
                }
            }
        };

        // Füge das Widget ordentlich zur Screen-Instanz hinzu.
        // Als Mixinklasse sind wir selbst eine Screen-Subclass, daher ist this.addDrawableChild(...) erlaubt.
        this.addDrawableChild(customSlider);
    }
}
