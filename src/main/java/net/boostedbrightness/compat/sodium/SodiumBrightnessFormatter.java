package net.boostedbrightness.compat.sodium;

import net.boostedbrightness.BoostedBrightness;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.minecraft.text.Text;

public class SodiumBrightnessFormatter {
    public static ControlValueFormatter createBrightnessFormatter() {
        return value -> {
            if (value == 0) {
                return Text.translatable("options.gamma.min");
            } else {
                double extendedValue = (value / 100.0) * BoostedBrightness.maxBrightness;
                int percentage = (int) Math.round(extendedValue * 100);
                return Text.literal(percentage + "%");
            }
        };
    }
}