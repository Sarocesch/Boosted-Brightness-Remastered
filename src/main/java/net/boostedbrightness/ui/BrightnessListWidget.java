package net.boostedbrightness.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.boostedbrightness.BoostedBrightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

public class BrightnessListWidget extends ElementListWidget<BrightnessListWidget.BrightnessEntry> {

    @SuppressWarnings("unchecked")
    public BrightnessListWidget(MinecraftClient client, int width, int height, int top, int bottom) {
        // Neuer Konstruktor: 5 Parameter (client, width, height, top, bottom)
        super(client, width, height, top, bottom);

        // setRenderSelection wurde entfernt in neueren Mappings -> Auswahl-Rendering in Entry implementieren, falls nötig

        if (client.options.getGamma().getValue() != BoostedBrightness.getBrightness()) {
            BoostedBrightness.changeBrightness(client.options.getGamma().getValue());
        }

        for (int idx = 0; idx < BoostedBrightness.numBrightnesses(); idx++) {
            this.addEntry(BrightnessListWidget.BrightnessEntry.create(idx, this.width, this));
        }

        if (BoostedBrightness.numBrightnesses() < BoostedBrightness.MAX_BRIGHTNESSES) {
            this.addEntry(BrightnessListWidget.BrightnessEntry.create(-1, this.width, this));
        }
    }

    public void addBrightness() {
        @SuppressWarnings("unchecked")
        List<BrightnessEntry> entries = (List<BrightnessEntry>)(List<?>) this.children();

        BoostedBrightness.brightnesses.add(1.0);
        int size = BoostedBrightness.numBrightnesses();

        // füge neuen Eintrag an der vorletzten Stelle (vor dem + Button) ein
        entries.add(size - 1, BrightnessEntry.create(size - 1, this.width, this));

        if (size == BoostedBrightness.MAX_BRIGHTNESSES) {
            entries.remove(BoostedBrightness.MAX_BRIGHTNESSES);
        }
    }

    public void removeBrightness(int index) {
        @SuppressWarnings("unchecked")
        List<BrightnessEntry> entries = (List<BrightnessEntry>)(List<?>) this.children();

        int oldSize = BoostedBrightness.brightnesses.size();

        BoostedBrightness.brightnesses.remove(index);
        entries.remove(oldSize - 1);

        for (int i = index; i < oldSize - 1; i++) {
            entries.get(i).updateValue();
        }

        if (oldSize == BoostedBrightness.MAX_BRIGHTNESSES) {
            entries.add(BrightnessEntry.create(-1, this.width, this));
        }
        if (BoostedBrightness.getBrightnessIndex() == BoostedBrightness.numBrightnesses()) {
            BoostedBrightness.setBrightnessIndex(BoostedBrightness.getBrightnessIndex() - 1);
        }
    }

    @Override
    public int getRowWidth() {
        // deine feste Breite, wie vorher
        return 300;
    }

    @Override
    public int getRowLeft() {
        // Ersetzt das alte getScrollbarPositionX()-Offset
        return super.getRowLeft() + 32;
    }

    public Optional<ClickableWidget> getHoveredButton(double mouseX, double mouseY) {
        Iterator<BrightnessEntry> it = this.children().iterator();

        while (it.hasNext()) {
            BrightnessListWidget.BrightnessEntry buttonEntry = it.next();
            Iterator<ClickableWidget> inner = buttonEntry.buttons.iterator();

            while (inner.hasNext()) {
                ClickableWidget abstractButtonWidget = inner.next();
                if (abstractButtonWidget.isMouseOver(mouseX, mouseY)) {
                    return Optional.of(abstractButtonWidget);
                }
            }
        }

        return Optional.empty();
    }

    protected boolean isSelectedEntry(int index) {
        return BoostedBrightness.getBrightnessIndex() == index;
    }

    public static class BrightnessEntry extends ElementListWidget.Entry<BrightnessListWidget.BrightnessEntry> {
        private final List<ClickableWidget> buttons;
        private final BrightnessListWidget listWidget;

        private int index;

        private BrightnessEntry(List<ClickableWidget> buttons, int index, BrightnessListWidget listWidget) {
            this.buttons = buttons;
            this.listWidget = listWidget;
            this.index = index;
        }

        public static BrightnessListWidget.BrightnessEntry create(int index, int width, BrightnessListWidget listWidget) {
            ArrayList<ClickableWidget> widgets = new ArrayList<>();

            if (index >= 0) {
                widgets.add(new BrightnessSliderWidget(index, width / 2 - 120, 0, 240, 20,
                        BrightnessSliderWidget.sliderValue(BoostedBrightness.getBrightness(index))));

                if (index >= 2) {
                    widgets.add(ButtonWidget.builder(Text.literal("X"), (button) -> {
                        listWidget.removeBrightness(index);
                    }).size(20, 20).position(width / 2 + 120 + 5, 0).build());
                }
            } else {
                widgets.add(ButtonWidget.builder(Text.literal("+"), (button) -> {
                    listWidget.addBrightness();
                }).size(240, 20).position(width / 2 - 120, 0).build());
            }

            return new BrightnessEntry(widgets, index, listWidget);
        }

        public void updateValue() {
            for (ClickableWidget button : buttons)
                if (button instanceof BrightnessSliderWidget)
                    ((BrightnessSliderWidget) button).updateValue();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
                           int mouseY, boolean hovered, float tickDelta) {
            // Positioniere und rendere Buttons/Slider
            for (ClickableWidget button : this.buttons) {
                button.setY(y);
                // button.setX(...) wird durch ButtonWidget.position(...) beim Erstellen bereits gesetzt relativ zur Gesamt-Screen-Position
                button.render(context, mouseX, mouseY, tickDelta);
            }

            if (this.index >= 0) {
                context.drawTextWithShadow(listWidget.client.textRenderer, String.valueOf(this.index + 1),
                        listWidget.width / 2 - 150 + 13, y + entryHeight / 3, 0xFFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            boolean mouseOnButton = false;

            for (ClickableWidget button : this.buttons) {
                if (button.isMouseOver(mouseX, mouseY)) {
                    mouseOnButton = true;
                    break;
                }
            }
            if (!mouseOnButton && this.index >= 0) {
                BoostedBrightness.setBrightnessIndex(this.index);
            }

            return super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        public List<? extends Element> children() {
            return this.buttons;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return this.buttons;
        }
    }
}
