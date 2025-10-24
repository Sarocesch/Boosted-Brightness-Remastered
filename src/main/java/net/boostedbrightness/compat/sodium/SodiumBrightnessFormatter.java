package net.boostedbrightness.compat.sodium;

import net.boostedbrightness.BoostedBrightness;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.minecraft.text.Text;

public class SodiumBrightnessFormatter {
    public static ControlValueFormatter createBrightnessFormatter() {
        return value -> {

            double extendedValue = BoostedBrightness.minBrightness +
                    (value / 100.0) * (BoostedBrightness.maxBrightness - BoostedBrightness.minBrightness);
            int percentage = (int) Math.round(extendedValue * 100);
            return Text.literal(percentage + "%");
        };
    }
}
