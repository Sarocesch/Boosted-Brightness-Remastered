//package net.boostedbrightness.compat.sodium;
//
//import net.boostedbrightness.BoostedBrightness;
//import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
//import net.minecraft.text.Text;
//
//public class SodiumBrightnessFormatter {
//    public static ControlValueFormatter createBrightnessFormatter() {
//        return value -> {
//
//            double gamma = BoostedBrightness.minBrightness +
//                    (value / (BoostedBrightness.maxBrightness * 100.0)) *
//                            (BoostedBrightness.maxBrightness - BoostedBrightness.minBrightness);
//
//            long brightness = Math.round(gamma * 100);
//
//            return Text.literal(String.valueOf(brightness));
//        };
//    }
//}
