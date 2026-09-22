package net.boostedbrightness.mixin;

import net.boostedbrightness.BoostedBrightness;
import net.boostedbrightness.misc.BoostedSliderCallbacks;
import net.minecraft.client.Options;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(Options.class)
public class MixinGammaOption {

    @Shadow
    private OptionInstance<Double> gamma;

    /**
     * Intercepts Options.gamma() getter. Ensures the OptionInstance has our
     * extended ValueSet (fallback in case MixinSimpleOption's constructor hook
     * didn't fire or failed).
     */
    @Inject(method = "gamma", at = @At("RETURN"), cancellable = true)
    private void onGetGamma(CallbackInfoReturnable<OptionInstance<Double>> cir) {
        OptionInstance<Double> opt = cir.getReturnValue();
        if (opt == null) return;

        try {
            // Check if values is already our BoostedSliderCallbacks
            Field valuesField = OptionInstance.class.getDeclaredField("values");
            valuesField.setAccessible(true);
            Object currentValues = valuesField.get(opt);

            if (!(currentValues instanceof BoostedSliderCallbacks)) {
                // MixinSimpleOption didn't apply — do it here as fallback
                System.out.println("[BoostedBrightness] Fallback: patching gamma in getter");
                valuesField.set(opt, BoostedSliderCallbacks.INSTANCE);

                Field codecField = OptionInstance.class.getDeclaredField("codec");
                codecField.setAccessible(true);
                codecField.set(opt, BoostedSliderCallbacks.INSTANCE.codec());
            }
        } catch (Exception ignored) {}

        // Clamp current value to our extended range
        try {
            double current = opt.get();
            double clamped = Math.min(BoostedBrightness.maxBrightness,
                    Math.max(BoostedBrightness.minBrightness, current));
            if (Double.compare(current, clamped) != 0) {
                // Use direct field write to bypass ValueSet validation
                Field valueField = OptionInstance.class.getDeclaredField("value");
                valueField.setAccessible(true);
                valueField.set(opt, clamped);
            }
        } catch (Throwable ignored) {}

        cir.setReturnValue(opt);
    }
}
