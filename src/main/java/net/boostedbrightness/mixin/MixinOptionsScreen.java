package net.boostedbrightness.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

// VideoSettingsScreen.init() is inherited from OptionsSubScreen.
// MixinSimpleOption already extends the gamma slider range, so this mixin
// is kept minimal. Add additional VideoSettings hooks here if needed.
@Mixin(VideoSettingsScreen.class)
public abstract class MixinOptionsScreen extends Screen {

    protected MixinOptionsScreen(Component title) {
        super(title);
    }
}
