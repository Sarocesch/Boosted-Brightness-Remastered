package net.boostedbrightness.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.boostedbrightness.BoostedBrightness;

@Mixin(GameOptions.class)
public class MixinGammaOption {

    // Shadow das Feld, so wie es in GameOptions deklariert ist (meist private)
    @Shadow private SimpleOption<Double> gamma;

    /**
     * getGamma() gibt in modernen Mappings ein SimpleOption<Double> zurück.
     * Wir injecten hier auf RETURN und clampen den aktuellen Wert der Option,
     * sodass keine ungültigen Werte durchkommen.
     */
    @Inject(method = "getGamma", at = @At("RETURN"), cancellable = true)
    private void onGetGamma(CallbackInfoReturnable<SimpleOption<Double>> cir) {
        SimpleOption<Double> opt = cir.getReturnValue();
        if (opt == null) return;

        // hole aktuellen numerischen Wert, clamp ihn und schreibe ihn zurück in die Option
        try {
            double current = opt.getValue();
            double clamped = Math.min(BoostedBrightness.maxBrightness, Math.max(BoostedBrightness.minBrightness, current));
            if (Double.compare(current, clamped) != 0) {
                opt.setValue(clamped);
            }
        } catch (Throwable ignored) {
            // Falls irgendwas schiefgeht, nichts verderben — leave option as-is
        }

        // wir geben die gleiche Option zurück (wir modifizieren nur ihren internen Wert)
        cir.setReturnValue(opt);
    }
}
