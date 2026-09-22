package net.boostedbrightness.misc;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.datafixers.util.Either;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.OptionInstance;

public class BoostedSliderCallbacks implements OptionInstance.SliderableValueSet<Double> {
    public static final BoostedSliderCallbacks INSTANCE = new BoostedSliderCallbacks();

    private BoostedSliderCallbacks() {}

    @Override
    public Optional<Double> validateValue(Double value) {
        if (value >= BoostedBrightness.minBrightness && value <= BoostedBrightness.maxBrightness)
            return Optional.of(value);
        return Optional.of(Math.max(BoostedBrightness.minBrightness,
                Math.min(BoostedBrightness.maxBrightness, value)));
    }

    @Override
    public double toSliderValue(Double value) {
        double range = BoostedBrightness.maxBrightness - BoostedBrightness.minBrightness;
        return Math.max(0.0, Math.min(1.0,
                (value - BoostedBrightness.minBrightness) / range));
    }

    @Override
    public Double fromSliderValue(double sliderValue) {
        double range = BoostedBrightness.maxBrightness - BoostedBrightness.minBrightness;
        return sliderValue * range + BoostedBrightness.minBrightness;
    }

    @Override
    public Codec<Double> codec() {
        return Codec.either(
                Codec.doubleRange(BoostedBrightness.minBrightness, BoostedBrightness.maxBrightness),
                Codec.BOOL).xmap(
                        either -> either.map(v -> v, v -> v ? 1.0 : 0.0),
                        Either::left);
    }
}
