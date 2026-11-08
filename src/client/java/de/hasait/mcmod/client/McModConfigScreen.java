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
import de.hasait.mcmod.config.McModConfig;
import de.hasait.mcmod.config.McModConfigBuilder;

public class McModConfigScreen extends Screen {

    private static final int COLUMN_WIDTH = 150;
    private static final int COLUMN_GAP = 10;
    private static final int LABEL_WIDTH = 90;
    private static final int ROW_HEIGHT = 22;
    private static final int FIELD_HEIGHT = 18;
    private static final int INVALID_COLOR = 0xFFFF5555;
    private static final int VALID_COLOR = 0xFFE0E0E0;

    private final Screen parent;
    private final McModConfigBuilder configBuilder;
    private final boolean remote;
    private final boolean editable;
    private final List<Entry> entries = new ArrayList<>();

    public McModConfigScreen(Screen parent) {
        super(Component.literal("McMod Config"));
        this.parent = parent;

        McModConfig serverConfig = McModClient.getServerConfig();
        if (serverConfig != null) {
            // edit a copy, it is sent to the server on Done
            remote = true;
            editable = McModClient.isServerConfigEditable();
            configBuilder = new McModConfigBuilder(serverConfig);
        } else {
            remote = false;
            editable = true;
            configBuilder = new McModConfigBuilder();
        }
    }

    @Override
    protected void init() {
        entries.clear();

        int leftX = width / 2 - COLUMN_WIDTH - COLUMN_GAP / 2;
        int rightX = width / 2 + COLUMN_GAP / 2;
        int top = 30;

        addRenderableWidget(new StringWidget(0, 10, width, 9, title, font));
        addRenderableWidget(new StringWidget(0, 20, width, 9, Component.literal(statusText()), font));

        addRenderableWidget(new StringWidget(leftX, top, COLUMN_WIDTH, 9, Component.literal("Repair Golem"), font));
        addRenderableWidget(new StringWidget(rightX, top, COLUMN_WIDTH, 9, Component.literal("Heal Villager"), font));
        top += 14;

        int y = top;
        addInt(leftX, y, "Start Health %", McModConfigBuilder.MINI_START_PERCENTAGE, McModConfigBuilder.MAXI_START_PERCENTAGE, configBuilder::getRepairGolemStartHealthPercentage, configBuilder::setRepairGolemStartHealthPercentage);
        y += ROW_HEIGHT;
        addInt(leftX, y, "Health Step %", McModConfigBuilder.MINI_STEP_PERCENTAGE, McModConfigBuilder.MAXI_STEP_PERCENTAGE, configBuilder::getRepairGolemHealthStepPercentage, configBuilder::setRepairGolemHealthStepPercentage);
        y += ROW_HEIGHT;
        addInt(leftX, y, "Scan X", McModConfigBuilder.MINI_SCAN, McModConfigBuilder.MAXI_SCAN, configBuilder::getRepairGolemScanX, configBuilder::setRepairGolemScanX);
        y += ROW_HEIGHT;
        addInt(leftX, y, "Scan Y", McModConfigBuilder.MINI_SCAN, McModConfigBuilder.MAXI_SCAN, configBuilder::getRepairGolemScanY, configBuilder::setRepairGolemScanY);
        y += ROW_HEIGHT;
        addInt(leftX, y, "Scan Z", McModConfigBuilder.MINI_SCAN, McModConfigBuilder.MAXI_SCAN, configBuilder::getRepairGolemScanZ, configBuilder::setRepairGolemScanZ);

        y = top;
        addInt(rightX, y, "Start Health %", McModConfigBuilder.MINI_START_PERCENTAGE, McModConfigBuilder.MAXI_START_PERCENTAGE, configBuilder::getHealVillagerStartHealthPercentage, configBuilder::setHealVillagerStartHealthPercentage);
        y += ROW_HEIGHT;
        addInt(rightX, y, "Health Step %", McModConfigBuilder.MINI_STEP_PERCENTAGE, McModConfigBuilder.MAXI_STEP_PERCENTAGE, configBuilder::getHealVillagerHealthStepPercentage, configBuilder::setHealVillagerHealthStepPercentage);
        y += ROW_HEIGHT;
        addInt(rightX, y, "Scan X", McModConfigBuilder.MINI_SCAN, McModConfigBuilder.MAXI_SCAN, configBuilder::getHealVillagerScanX, configBuilder::setHealVillagerScanX);
        y += ROW_HEIGHT;
        addInt(rightX, y, "Scan Y", McModConfigBuilder.MINI_SCAN, McModConfigBuilder.MAXI_SCAN, configBuilder::getHealVillagerScanY, configBuilder::setHealVillagerScanY);
        y += ROW_HEIGHT;
        addInt(rightX, y, "Scan Z", McModConfigBuilder.MINI_SCAN, McModConfigBuilder.MAXI_SCAN, configBuilder::getHealVillagerScanZ, configBuilder::setHealVillagerScanZ);

        int buttonY = top + 5 * ROW_HEIGHT + 14;
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onDone())
                .bounds(width / 2 - COLUMN_WIDTH - COLUMN_GAP / 2, buttonY, COLUMN_WIDTH, 20)
                .build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .bounds(width / 2 + COLUMN_GAP / 2, buttonY, COLUMN_WIDTH, 20)
                .build());
    }

    private String statusText() {
        if (remote) {
            return editable ? "Server config" : "Server config (read-only, operator permission required)";
        }
        return minecraft != null && minecraft.level != null ? "Local config (server does not provide McMod config)" : "Local config";
    }

    private void addInt(int x, int y, String label, int min, int max, Supplier<Integer> getter, Consumer<Integer> setter) {
        addEntry(x, y, label, String.valueOf(getter.get()), text -> {
            try {
                int value = Integer.parseInt(text.trim());
                return value >= min && value <= max ? () -> setter.accept(value) : null;
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }

    private void addEntry(int x, int y, String label, String initialValue, java.util.function.Function<String, Runnable> parser) {
        addRenderableWidget(new StringWidget(x, y + 5, LABEL_WIDTH, 9, Component.literal(label), font));

        EditBox editBox = new EditBox(font, x + LABEL_WIDTH, y, COLUMN_WIDTH - LABEL_WIDTH, FIELD_HEIGHT, Component.literal(label));
        editBox.setMaxLength(16);
        editBox.setEditable(editable);
        editBox.setValue(initialValue);
        Entry entry = new Entry(editBox, parser);
        editBox.setResponder(text -> entry.validate());
        entry.validate();
        entries.add(entry);
        addRenderableWidget(editBox);
    }

    private void onDone() {
        if (!editable) {
            onClose();
            return;
        }
        for (Entry entry : entries) {
            if (entry.applier == null) {
                return; // keep screen open while there are invalid values
            }
        }
        for (Entry entry : entries) {
            entry.applier.run();
        }
        McModConfig config = configBuilder.build();
        if (remote) {
            McModClient.sendConfigUpdate(config);
        } else {
            McMod.updateConfig(config);
        }
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
