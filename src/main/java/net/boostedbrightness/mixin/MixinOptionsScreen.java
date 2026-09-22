package net.boostedbrightness.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VideoOptionsScreen.class)
public abstract class MixinOptionsScreen extends Screen {

    protected MixinOptionsScreen(Text title) {
        super(title);
    }
}