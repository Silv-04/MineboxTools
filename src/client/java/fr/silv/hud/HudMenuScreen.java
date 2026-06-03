package fr.silv.hud;

import fr.silv.Lang;
import fr.silv.ModConfig;
import fr.silv.availability.AvailabilityEntry;
import fr.silv.availability.AvailabilityRegistry;
import fr.silv.availability.AvailabilitySection;
import fr.silv.hud.widget.HudWidgetManager;
import fr.silv.hud.widget.config.CheckboxListWidget;
import fr.silv.hud.widget.config.ConfigOption;
import fr.silv.items.ItemHighlightHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Main in-game HUD menu screen for MineboxTools.
 */
public class HudMenuScreen extends Screen {
    private static final int BACKGROUND_COLOR = 0x90000000;
    private static final int HELP_TEXT_COLOR = 0xFFB0B0B0;

    private String searchQuery = "";
    private EditBox searchField;
    private CheckboxListWidget insectList;
    private CheckboxListWidget shopList;
    private List<AvailabilityEntry> insectEntries = List.of();
    private List<AvailabilityEntry> worldEntries = List.of();

    /**
     * Creates a new HudMenuScreen.
     */
    public HudMenuScreen() {
        super(Component.literal("MineboxTools Menu"));
    }

    @Override
    public void init() {
        super.init();
        Lang.load(ModConfig.getLanguage());

        Button languageButton = Button.builder(languageLabel(), button -> {
            String nextLanguage = switch (ModConfig.getLanguage()) {
                case "en_us" -> "fr_fr";
                case "fr_fr" -> "en_us";
                default -> "en_us";
            };
            ModConfig.setLanguage(nextLanguage);
            Lang.load(ModConfig.getLanguage());
            ModConfig.save();
            rebuildWidgets();
        }).bounds(20, 20, 160, 20).build();

        CycleButton<Boolean> durabilityToggle = createBooleanToggle(
                20, 60, "mineboxtools.menu.durability",
                ModConfig.isEnabled(ModConfig.FeatureFlag.DURABILITY),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.DURABILITY, value));

        CycleButton<Boolean> tooltipToggle = createBooleanToggle(
                20, 80, "mineboxtools.menu.tooltip",
                ModConfig.isEnabled(ModConfig.FeatureFlag.TOOLTIP),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.TOOLTIP, value));

        CycleButton<ConfigOption> statDisplayToggle = createEnumToggle(
                20, 100, "mineboxtools.menu.stats",
                ConfigOption.values(), ConfigOption::getDisplayName,
                ModConfig.getStatDisplay(), ModConfig::setStatDisplay);

        CycleButton<Boolean> handToggle = createBooleanToggle(
                20, 120, "mineboxtools.menu.hand",
                ModConfig.isEnabled(ModConfig.FeatureFlag.HAND),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.HAND, value));

        CycleButton<Boolean> locationToggle = createBooleanToggle(
                20, 140, "mineboxtools.menu.location",
                ModConfig.isEnabled(ModConfig.FeatureFlag.LOCATION),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.LOCATION, value));

        CycleButton<ModConfig.IconSize> iconSizeToggle = createEnumToggle(
                20, 160, "mineboxtools.menu.icon_size",
                ModConfig.IconSize.values(), value -> Component.literal(Lang.get(value.getLabelKey())),
                ModConfig.getHudIconSize(), ModConfig::setHudIconSize);

        CycleButton<ModConfig.IconOrientation> iconOrientationToggle = createEnumToggle(
                20, 180, "mineboxtools.menu.icon_orientation",
                ModConfig.IconOrientation.values(), value -> Component.literal(Lang.get(value.getLabelKey())),
                ModConfig.getHudIconOrientation(), ModConfig::setHudIconOrientation);

        CycleButton<ModConfig.IconDirection> iconDirectionToggle = CycleButton.<ModConfig.IconDirection>builder(
                        value -> Component.literal(Lang.get(value.getLabelKey())),
                        ModConfig.getHudIconDirection())
                .withValues(ModConfig.IconDirection.values())
                .create(20, 200, 160, 20, Component.literal(Lang.get("mineboxtools.menu.icon_direction")), (b, value) -> {
                    ModConfig.setHudIconDirection(value);
                    updateIconOrientationToggleState(iconOrientationToggle, value);
                    ModConfig.save();
                });

        updateIconOrientationToggleState(iconOrientationToggle, ModConfig.getHudIconDirection());

        int listTop = 44;
        int listWidth = 150;
        int shopListHeight = this.height - 96;
        int insectSearchHeight = 20;
        int insectListTop = listTop + insectSearchHeight;
        int insectListHeight = shopListHeight - insectSearchHeight;

        Button customHudButton = Button.builder(Component.literal(Lang.get("mineboxtools.menu.hud")), button ->
                Minecraft.getInstance().setScreen(new HudConfigScreen(HudWidgetManager.getWidgets()))
        ).bounds(20, 240, 160, 20).build();

        EditBox[] highlightFieldRef = new EditBox[1];
        highlightFieldRef[0] = new EditBox(
                this.font, 20, 280, 118, 20,
                Component.literal("Highlight"));
        highlightFieldRef[0].setMaxLength(3);
        highlightFieldRef[0].setHint(Component.literal("0-100"));
        highlightFieldRef[0].setValue(String.valueOf(ItemHighlightHandler.getThreshold()));
        highlightFieldRef[0].setEditable(ItemHighlightHandler.isEnabled());
        highlightFieldRef[0].setResponder(value -> {
            try {
                int parsed = Integer.parseInt(value);
                if (parsed >= 0 && parsed <= 100) {
                    ItemHighlightHandler.setThreshold(parsed);
                }
            } catch (NumberFormatException ignored) {
            }
        });

        Button[] highlightToggle = new Button[1];
        highlightToggle[0] = Button.builder(
                Component.literal(ItemHighlightHandler.isEnabled() ? "ON" : "OFF"),
                button -> {
                    boolean next = !ItemHighlightHandler.isEnabled();
                    ItemHighlightHandler.setEnabled(next);
                    button.setMessage(Component.literal(next ? "ON" : "OFF"));
                    highlightFieldRef[0].setEditable(next);
                }
        ).bounds(142, 280, 38, 20).build();

        searchField = new EditBox(
                this.font,
                this.width - 170,
                listTop,
                listWidth,
                insectSearchHeight,
                Component.literal("Search"));
        searchField.setMaxLength(64);
        searchField.setHint(Component.literal("Search insects..."));
        searchField.setValue(searchQuery);
        searchField.setResponder(value -> {
            searchQuery = value;
            refreshInsectList();
        });

        insectList = new CheckboxListWidget(
                Minecraft.getInstance(),
                this.width - 170,
                insectListTop,
                listWidth,
                insectListHeight,
                20);
        insectEntries = AvailabilityRegistry.entriesForSection(AvailabilitySection.INSECTS);

        shopList = new CheckboxListWidget(
                Minecraft.getInstance(),
                this.width - 330,
                listTop,
                listWidth,
                shopListHeight,
                20);
        worldEntries = AvailabilityRegistry.entriesForSection(AvailabilitySection.WORLD);

        refreshInsectList();
        addCheckboxOptions(shopList, worldEntries);

        addRenderableWidget(languageButton);
        addRenderableWidget(durabilityToggle);
        addRenderableWidget(tooltipToggle);
        addRenderableWidget(statDisplayToggle);
        addRenderableWidget(handToggle);
        addRenderableWidget(locationToggle);
        addRenderableWidget(iconSizeToggle);
        addRenderableWidget(iconOrientationToggle);
        addRenderableWidget(iconDirectionToggle);
        addRenderableWidget(searchField);
        addRenderableWidget(insectList);
        addRenderableWidget(shopList);
        addRenderableWidget(customHudButton);
        addRenderableWidget(highlightFieldRef[0]);
        addRenderableWidget(highlightToggle[0]);
        addRenderableWidget(Button.builder(Component.literal(Lang.get("mineboxtools.menu.close")), button -> onClose())
                .bounds(this.width - 100, this.height - 40, 80, 20)
                .build());

        setFocused(searchField);
        searchField.setFocused(true);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
        drawContext.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);
        super.extractRenderState(drawContext, mouseX, mouseY, delta);

        if (ModConfig.getHudIconDirection() != ModConfig.IconDirection.AUTO) {
            drawContext.text(
                    this.font,
                    Component.literal(Lang.get("mineboxtools.menu.icon_orientation.auto_only")),
                    20,
                    224,
                    HELP_TEXT_COLOR,
                    false);
        }

        drawContext.text(
                this.font,
                Component.literal(Lang.get("mineboxtools.menu.highlight")),
                20,
                270,
                HELP_TEXT_COLOR,
                false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component languageLabel() {
        return switch (ModConfig.getLanguage()) {
            case "fr_fr" -> Component.literal("Langue : FR");
            default -> Component.literal("Language: EN");
        };
    }

    private CycleButton<Boolean> createBooleanToggle(int x, int y, String langKey,
                                                    boolean initialValue, Consumer<Boolean> setter) {
        return CycleButton.onOffBuilder(initialValue)
                .create(x, y, 160, 20, Component.literal(Lang.get(langKey)), (button, value) -> {
                    setter.accept(value);
                    ModConfig.save();
                });
    }

    private <T> CycleButton<T> createEnumToggle(int x, int y, String langKey,
                                                T[] values, Function<T, Component> display,
                                                T initialValue, Consumer<T> setter) {
        return CycleButton.<T>builder(display, initialValue)
                .withValues(values)
                .create(x, y, 160, 20, Component.literal(Lang.get(langKey)), (button, value) -> {
                    setter.accept(value);
                    ModConfig.save();
                });
    }

    private static void updateIconOrientationToggleState(CycleButton<ModConfig.IconOrientation> orientationToggle,
                                                         ModConfig.IconDirection direction) {
        orientationToggle.active = direction == ModConfig.IconDirection.AUTO;
    }

    private void refreshInsectList() {
        if (insectList == null) {
            return;
        }

        insectList.clearOptions();
        String normalizedQuery = searchQuery == null ? "" : searchQuery.toLowerCase(Locale.ROOT).trim();

        for (AvailabilityEntry entry : insectEntries) {
            if (!entry.langKey().startsWith("mineboxtools.insect.")) {
                continue;
            }

            String localizedLabel = Lang.get(entry.langKey());
            String normalizedLabel = localizedLabel.toLowerCase(Locale.ROOT);
            boolean matches = normalizedQuery.isEmpty()
                    || normalizedLabel.contains(normalizedQuery)
                    || entry.langKey().toLowerCase(Locale.ROOT).contains(normalizedQuery);
            if (!matches) {
                continue;
            }

            insectList.addOption(Component.literal(localizedLabel), entry.isEnabled(), checked -> {
                entry.setEnabled(checked);
                ModConfig.save();
            }, entry.icon());
        }
    }

    private void addCheckboxOptions(CheckboxListWidget list, List<AvailabilityEntry> entries) {
        for (AvailabilityEntry entry : entries) {
            list.addOption(Component.literal(Lang.get(entry.langKey())), entry.isEnabled(), checked -> {
                entry.setEnabled(checked);
                ModConfig.save();
            }, entry.icon());
        }
    }
}
