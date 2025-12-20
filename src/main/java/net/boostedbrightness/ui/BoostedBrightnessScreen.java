package net.boostedbrightness.ui;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class BoostedBrightnessScreen extends Screen {
    private final Screen parent;
    private BrightnessListWidget list;

    public BoostedBrightnessScreen(Screen parent) {
        super(Component.translatable("options.boosted-brightness.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.list = new BrightnessListWidget(this.minecraft, this.width, this.height - 64, 32, 25);
        this.addRenderableWidget(this.list);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.minecraft.setScreen(this.parent);
        }).size(240, 20).pos(this.width / 2 - 120, this.height - 27).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        BoostedBrightness.saveConfig();
        this.minecraft.setScreen(this.parent);
    }
}
