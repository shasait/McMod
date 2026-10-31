package de.hasait.mcmod.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import de.hasait.mcmod.McMod;
import de.hasait.mcmod.McModConfig;

public class McModConfigScreen extends Screen {

    private static final int COLUMN_WIDTH = 150;
    private static final int COLUMN_GAP = 10;
    private static final int LABEL_WIDTH = 90;
    private static final int ROW_HEIGHT = 22;
    private static final int FIELD_HEIGHT = 18;
    private static final int INVALID_COLOR = 0xFFFF5555;
    private static final int VALID_COLOR = 0xFFE0E0E0;

    private final Screen parent;
    private final McModConfig config = McMod.CONFIG;
    private final List<Entry> entries = new ArrayList<>();

    public McModConfigScreen(Screen parent) {
        super(Component.literal("McMod Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        entries.clear();

        int leftX = width / 2 - COLUMN_WIDTH - COLUMN_GAP / 2;
        int rightX = width / 2 + COLUMN_GAP / 2;
        int top = 30;

        addRenderableWidget(new StringWidget(0, 10, width, 9, title, font));

        addRenderableWidget(new StringWidget(leftX, top, COLUMN_WIDTH, 9, Component.literal("Repair Golem"), font));
        addRenderableWidget(new StringWidget(rightX, top, COLUMN_WIDTH, 9, Component.literal("Heal Villager"), font));
        top += 14;

        int y = top;
        addDouble(leftX, y, "Scan X", 0.0, 50.0, config::getRepairGolemScanX, config::setRepairGolemScanX);
        y += ROW_HEIGHT;
        addDouble(leftX, y, "Scan Y", 0.0, 50.0, config::getRepairGolemScanY, config::setRepairGolemScanY);
        y += ROW_HEIGHT;
        addDouble(leftX, y, "Scan Z", 0.0, 50.0, config::getRepairGolemScanZ, config::setRepairGolemScanZ);
        y += ROW_HEIGHT;
        addFloat(leftX, y, "Start Health %", 0.0F, 100.0F, config::getRepairGolemStartHealthPercentage, config::setRepairGolemStartHealthPercentage);
        y += ROW_HEIGHT;
        addFloat(leftX, y, "Health Step %", 0.1F, 100.0F, config::getRepairGolemHealthStepPercentage, config::setRepairGolemHealthStepPercentage);

        y = top;
        addDouble(rightX, y, "Scan X", 0.0, 50.0, config::getHealVillagerScanX, config::setHealVillagerScanX);
        y += ROW_HEIGHT;
        addDouble(rightX, y, "Scan Y", 0.0, 50.0, config::getHealVillagerScanY, config::setHealVillagerScanY);
        y += ROW_HEIGHT;
        addDouble(rightX, y, "Scan Z", 0.0, 50.0, config::getHealVillagerScanZ, config::setHealVillagerScanZ);
        y += ROW_HEIGHT;
        addFloat(rightX, y, "Start Health %", 0.0F, 100.0F, config::getHealVillagerStartHealthPercentage, config::setHealVillagerStartHealthPercentage);
        y += ROW_HEIGHT;
        addFloat(rightX, y, "Health Step %", 0.1F, 100.0F, config::getHealVillagerHealthStepPercentage, config::setHealVillagerHealthStepPercentage);

        int buttonY = top + 5 * ROW_HEIGHT + 14;
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onDone())
                .bounds(width / 2 - COLUMN_WIDTH - COLUMN_GAP / 2, buttonY, COLUMN_WIDTH, 20)
                .build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .bounds(width / 2 + COLUMN_GAP / 2, buttonY, COLUMN_WIDTH, 20)
                .build());
    }

    private void addDouble(int x, int y, String label, double min, double max, Supplier<Double> getter, Consumer<Double> setter) {
        addEntry(x, y, label, String.valueOf(getter.get()), text -> {
            try {
                double value = Double.parseDouble(text.trim());
                return value >= min && value <= max && Double.isFinite(value) ? () -> setter.accept(value) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    private void addFloat(int x, int y, String label, float min, float max, Supplier<Float> getter, Consumer<Float> setter) {
        addEntry(x, y, label, String.valueOf(getter.get()), text -> {
            try {
                float value = Float.parseFloat(text.trim());
                return value >= min && value <= max && Float.isFinite(value) ? () -> setter.accept(value) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    private void addEntry(int x, int y, String label, String initialValue, java.util.function.Function<String, Runnable> parser) {
        addRenderableWidget(new StringWidget(x, y + 5, LABEL_WIDTH, 9, Component.literal(label), font));

        EditBox editBox = new EditBox(font, x + LABEL_WIDTH, y, COLUMN_WIDTH - LABEL_WIDTH, FIELD_HEIGHT, Component.literal(label));
        editBox.setMaxLength(16);
        editBox.setValue(initialValue);
        Entry entry = new Entry(editBox, parser);
        editBox.setResponder(text -> entry.validate());
        entry.validate();
        entries.add(entry);
        addRenderableWidget(editBox);
    }

    private void onDone() {
        for (Entry entry : entries) {
            if (entry.applier == null) {
                return; // keep screen open while there are invalid values
            }
        }
        for (Entry entry : entries) {
            entry.applier.run();
        }
        McMod.CONFIG_STORE.save();
        onClose();
    }

    @Override
    public void onClose() {
        minecraft.setScreenAndShow(parent);
    }

    private static final class Entry {
        private final EditBox editBox;
        private final java.util.function.Function<String, Runnable> parser;
        private Runnable applier;

        private Entry(EditBox editBox, java.util.function.Function<String, Runnable> parser) {
            this.editBox = editBox;
            this.parser = parser;
        }

        private void validate() {
            applier = parser.apply(editBox.getValue());
            editBox.setTextColor(applier == null ? INVALID_COLOR : VALID_COLOR);
        }
    }

}
