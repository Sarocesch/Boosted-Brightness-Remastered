package net.boostedbrightness.misc;

import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.datafixers.util.Either;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

/**
 * Custom slider value set for extended brightness range.
 * Implements SliderableValueSet (accessible via Access Transformer).
 */
public class BoostedSliderCallbacks implements OptionInstance.SliderableValueSet<Double> {
    public static final BoostedSliderCallbacks INSTANCE = new BoostedSliderCallbacks();

    private BoostedSliderCallbacks() {
    }

    @Override
    public Optional<Double> validateValue(Double value) {
        // Accept any value within extended range
        if (value >= BoostedBrightness.minBrightness && value <= BoostedBrightness.maxBrightness) {
            return Optional.of(value);
        }
        // Clamp to valid range
        return Optional.of(Math.max(BoostedBrightness.minBrightness,
                Math.min(BoostedBrightness.maxBrightness, value)));
    }

    @Override
    public double toSliderValue(Double value) {
        // Convert gamma value to slider position (0.0 to 1.0)
        double range = BoostedBrightness.maxBrightness - BoostedBrightness.minBrightness;
        double normalized = (value - BoostedBrightness.minBrightness) / range;
        return Math.max(0.0, Math.min(1.0, normalized));
    }

    @Override
    public Double fromSliderValue(double sliderValue) {
        // Convert slider position (0.0 to 1.0) to gamma value
        double range = BoostedBrightness.maxBrightness - BoostedBrightness.minBrightness;
        return sliderValue * range + BoostedBrightness.minBrightness;
    }

    @Override
    public Codec<Double> codec() {
        return Codec.either(
                Codec.doubleRange(BoostedBrightness.minBrightness, BoostedBrightness.maxBrightness),
                Codec.BOOL).xmap(
                        either -> either.map(value -> value, value -> value ? 1.0 : 0.0),
                        Either::left);
    }
}
