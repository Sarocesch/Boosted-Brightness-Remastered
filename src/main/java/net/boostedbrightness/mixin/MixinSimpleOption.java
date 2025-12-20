package net.boostedbrightness.mixin;

import net.boostedbrightness.BoostedBrightness;
import net.boostedbrightness.misc.BoostedSliderCallbacks;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.client.OptionInstance;

import java.util.function.Consumer;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to modify the gamma OptionInstance to support extended brightness
 * range.
 * Uses Access Transformer to make fields accessible.
 */
@Mixin(OptionInstance.class)
public class MixinSimpleOption<T> {

    @Shadow
    @Final
    Component caption;

    @Shadow
    @Final
    @Mutable
    OptionInstance.ValueSet<T> values;

    @Shadow
    @Final
    @Mutable
    Function<T, Component> toString;

    @Shadow
    @Final
    @Mutable
    Consumer<T> onValueUpdate;

    @SuppressWarnings("unchecked")
    @Inject(at = @At("RETURN"), method = "<init>*")
    private void init(CallbackInfo info) {
        if (this.caption == null)
            return;

        var content = this.caption.getContents();
        if (!(content instanceof TranslatableContents translatable))
            return;

        String key = translatable.getKey();
        if (!key.equals("options.gamma"))
            return;

        // Replace the values field with our extended range slider callbacks
        this.values = (OptionInstance.ValueSet<T>) BoostedSliderCallbacks.INSTANCE;

        // Update the text getter for extended range display
        this.toString = (Function<T, Component>) (Function<Double, Component>) this::textGetter;

        // Update the value change callback
        this.onValueUpdate = (Consumer<T>) (Consumer<Double>) this::changeCallback;

        System.out.println("[BoostedBrightness] Gamma option modified for extended range: "
                + BoostedBrightness.minBrightness + " to " + BoostedBrightness.maxBrightness);
    }

    /**
     * Creates the display text for the brightness slider.
     */
    private Component textGetter(Double gamma) {
        long brightness = Math.round(gamma * 100);
        String display;
        if (brightness < 0) {
            display = brightness + "%";
        } else if (brightness == 0) {
            display = "0%";
        } else if (brightness == 100) {
            display = "100%";
        } else if (brightness > 100) {
            display = "+" + brightness + "%";
        } else {
            display = brightness + "%";
        }
        return Component.translatable("options.gamma").append(": ").append(Component.literal(display));
    }

    /**
     * Callback when brightness value changes.
     */
    private void changeCallback(Double gamma) {
        BoostedBrightness.changeBrightness(gamma);
    }
}
