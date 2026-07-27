package fr.silv.hud;

import fr.silv.Lang;
import fr.silv.ModConfig;
import fr.silv.availability.AvailabilityEntry;
import fr.silv.availability.AvailabilityRegistry;
import fr.silv.availability.AvailabilitySection;
import fr.silv.hud.widget.HudWidgetManager;
import fr.silv.hud.widget.config.CheckboxListWidget;
import fr.silv.hud.widget.config.ConfigOption;
import fr.silv.hud.widget.config.WidgetRowListWidget;
import fr.silv.api.MineboxItemStatFetcher;
import fr.silv.api.MuseumDonationCache;
import fr.silv.items.ItemHighlightHandler;
import fr.silv.items.MuseumHighlightHandler;
import fr.silv.utils.MineboxItemStatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Main in-game HUD menu screen for MineboxTools.
 */
@SuppressWarnings("null") // Minecraft/Fabric component APIs lack consistent @NonNull annotations
public class HudMenuScreen extends Screen {
    private static final int BACKGROUND_COLOR = 0x90000000;
    private static final int HELP_TEXT_COLOR = 0xFFB0B0B0;
    private static final int SETTINGS_LIST_WIDTH = 380;
    private static final int SETTINGS_ROW_WIDTH = 160;
    private static final int SETTINGS_LABEL_X = SETTINGS_ROW_WIDTH + 10;
    private static final int MIN_SETTINGS_LIST_HEIGHT = 100;
    private static final DateTimeFormatter LAST_UPDATED_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private String searchQuery = "";
    private EditBox searchField;
    private CheckboxListWidget insectList;
    private CheckboxListWidget shopList;
    private List<AvailabilityEntry> insectEntries = List.of();
    private List<AvailabilityEntry> worldEntries = List.of();
    private Button museumRefreshButton;
    private Button itemStatsUpdateButton;

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

        int settingsListHeight = Math.max(MIN_SETTINGS_LIST_HEIGHT, this.height - 60);
        WidgetRowListWidget settingsList = new WidgetRowListWidget(
                Minecraft.getInstance(), 20, 20, SETTINGS_LIST_WIDTH, settingsListHeight, 20);

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
        }).bounds(0, 0, SETTINGS_ROW_WIDTH, 20).build();
        settingsList.addRow(languageButton);
        settingsList.addSpacer(20);

        CycleButton<Boolean> durabilityToggle = createBooleanToggle(
                "mineboxtools.menu.durability",
                ModConfig.isEnabled(ModConfig.FeatureFlag.DURABILITY),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.DURABILITY, value));
        settingsList.addRow(durabilityToggle);

        CycleButton<Boolean> tooltipToggle = createBooleanToggle(
                "mineboxtools.menu.tooltip",
                ModConfig.isEnabled(ModConfig.FeatureFlag.TOOLTIP),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.TOOLTIP, value));
        settingsList.addRow(tooltipToggle);

        CycleButton<ConfigOption> statDisplayToggle = createEnumToggle(
                "mineboxtools.menu.stats",
                ConfigOption.values(), ConfigOption::getDisplayName,
                ModConfig.getStatDisplay(), ModConfig::setStatDisplay);
        settingsList.addRow(statDisplayToggle);

        CycleButton<Boolean> handToggle = createBooleanToggle(
                "mineboxtools.menu.hand",
                ModConfig.isEnabled(ModConfig.FeatureFlag.HAND),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.HAND, value));
        settingsList.addRow(handToggle);

        CycleButton<Boolean> locationToggle = createBooleanToggle(
                "mineboxtools.menu.location",
                ModConfig.isEnabled(ModConfig.FeatureFlag.LOCATION),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.LOCATION, value));
        settingsList.addRow(locationToggle);

        CycleButton<Boolean> effectsToggle = createBooleanToggle(
                "mineboxtools.menu.effects",
                ModConfig.isEnabled(ModConfig.FeatureFlag.EFFECTS),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.EFFECTS, value));
        settingsList.addRow(effectsToggle);

        CycleButton<ModConfig.IconSize> iconSizeToggle = createEnumToggle(
                "mineboxtools.menu.icon_size",
                ModConfig.IconSize.values(), value -> Component.literal(Lang.get(value.getLabelKey())),
                ModConfig.getHudIconSize(), ModConfig::setHudIconSize);
        settingsList.addRow(iconSizeToggle);

        CycleButton<ModConfig.IconOrientation> iconOrientationToggle = createEnumToggle(
                "mineboxtools.menu.icon_orientation",
                ModConfig.IconOrientation.values(), value -> Component.literal(Lang.get(value.getLabelKey())),
                ModConfig.getHudIconOrientation(), ModConfig::setHudIconOrientation);
        settingsList.addRow(iconOrientationToggle);

        CycleButton<ModConfig.IconDirection> iconDirectionToggle = CycleButton.<ModConfig.IconDirection>builder(
                        value -> Component.literal(Lang.get(value.getLabelKey())),
                        ModConfig.getHudIconDirection())
                .withValues(ModConfig.IconDirection.values())
                .create(0, 0, SETTINGS_ROW_WIDTH, 20, Component.literal(Lang.get("mineboxtools.menu.icon_direction")), (b, value) -> {
                    ModConfig.setHudIconDirection(value);
                    updateIconOrientationToggleState(iconOrientationToggle, value);
                    ModConfig.save();
                });
        updateIconOrientationToggleState(iconOrientationToggle, ModConfig.getHudIconDirection());
        settingsList.addRow(
                () -> ModConfig.getHudIconDirection() != ModConfig.IconDirection.AUTO
                        ? Component.literal(Lang.get("mineboxtools.menu.icon_orientation.auto_only"))
                        : null,
                SETTINGS_LABEL_X, 6, HELP_TEXT_COLOR,
                iconDirectionToggle);
        settingsList.addSpacer(20);

        Button customHudButton = Button.builder(Component.literal(Lang.get("mineboxtools.menu.hud")), button ->
                Minecraft.getInstance().gui.setScreen(new HudConfigScreen(HudWidgetManager.getWidgets()))
        ).bounds(0, 0, SETTINGS_ROW_WIDTH, 20).build();
        settingsList.addRow(customHudButton);
        settingsList.addSpacer(20);

        EditBox[] highlightFieldRef = new EditBox[1];
        highlightFieldRef[0] = new EditBox(
                this.font, 0, 0, 118, 20,
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
        ).bounds(122, 0, 38, 20).build();
        settingsList.addRow(
                () -> Component.literal(Lang.get("mineboxtools.menu.highlight")),
                SETTINGS_LABEL_X, 6, HELP_TEXT_COLOR,
                highlightFieldRef[0], highlightToggle[0]);

        CycleButton<Boolean> museumToggle = createBooleanToggle(
                "mineboxtools.menu.museum",
                MuseumHighlightHandler.isEnabled(),
                MuseumHighlightHandler::setEnabled);
        settingsList.addRow(museumToggle);

        museumRefreshButton = Button.builder(
                Component.literal(Lang.get("mineboxtools.menu.museum_refresh")),
                button -> {
                    var player = Minecraft.getInstance().player;
                    if (player == null) {
                        return;
                    }
                    var outcome = MuseumDonationCache.refreshManual();
                    String feedback = switch (outcome) {
                        case TRIGGERED -> Lang.get("mineboxtools.menu.museum_refresh.triggered");
                        case ON_COOLDOWN -> {
                            long seconds = (MuseumDonationCache.remainingManualCooldownMs() + 999) / 1000;
                            yield Lang.get("mineboxtools.menu.museum_refresh.cooldown").replace("{0}", String.valueOf(seconds));
                        }
                        case NO_PLAYER -> Lang.get("mineboxtools.menu.museum_refresh.cooldown");
                    };
                    player.sendSystemMessage(Component.literal(feedback));
                }
        ).bounds(0, 0, SETTINGS_ROW_WIDTH, 20).build();
        settingsList.addRow(museumRefreshButton);

        itemStatsUpdateButton = Button.builder(
                Component.literal(Lang.get("mineboxtools.menu.item_stats_update")),
                button -> MineboxItemStatFetcher.fetchAndApply()
        ).bounds(0, 0, SETTINGS_ROW_WIDTH, 20).build();
        settingsList.addRow(
                () -> Component.literal(itemStatsStatusText()),
                SETTINGS_LABEL_X, 6, HELP_TEXT_COLOR,
                itemStatsUpdateButton);

        int listTop = 44;
        int listWidth = 150;
        int shopListHeight = Math.max(20, this.height - 96);
        int insectSearchHeight = 20;
        int insectListTop = listTop + insectSearchHeight;
        int insectListHeight = Math.max(20, shopListHeight - insectSearchHeight);

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

        addRenderableWidget(settingsList);
        addRenderableWidget(searchField);
        addRenderableWidget(insectList);
        addRenderableWidget(shopList);
        addRenderableWidget(Button.builder(Component.literal(Lang.get("mineboxtools.menu.close")), button -> onClose())
                .bounds(this.width - 100, this.height - 40, 80, 20)
                .build());

        setFocused(searchField);
        searchField.setFocused(true);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float delta) {
        drawContext.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);
        updateMuseumRefreshButtonState();
        updateItemStatsUpdateButtonState();
        super.extractRenderState(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Disables the museum refresh button while its cooldown is active and shows the
     * remaining time on its label, so it's clear the click was throttled rather than broken.
     */
    private void updateMuseumRefreshButtonState() {
        if (museumRefreshButton == null) {
            return;
        }

        long remainingMs = MuseumDonationCache.remainingManualCooldownMs();
        if (remainingMs <= 0) {
            museumRefreshButton.active = true;
            museumRefreshButton.setMessage(Component.literal(Lang.get("mineboxtools.menu.museum_refresh")));
        } else {
            long seconds = (remainingMs + 999) / 1000;
            museumRefreshButton.active = false;
            museumRefreshButton.setMessage(Component.literal(
                    Lang.get("mineboxtools.menu.museum_refresh.wait").replace("{0}", String.valueOf(seconds))));
        }
    }

    /**
     * Disables the item stats update button while a fetch is running or its 1-hour
     * cooldown hasn't elapsed, so the menu can't be used to spam the API.
     */
    private void updateItemStatsUpdateButtonState() {
        if (itemStatsUpdateButton == null) {
            return;
        }

        if (MineboxItemStatFetcher.getState() == MineboxItemStatFetcher.State.FETCHING) {
            itemStatsUpdateButton.active = false;
            itemStatsUpdateButton.setMessage(Component.literal(Lang.get("mineboxtools.menu.item_stats_update")));
            return;
        }

        long remainingMs = MineboxItemStatFetcher.remainingCooldownMs();
        if (remainingMs <= 0) {
            itemStatsUpdateButton.active = true;
            itemStatsUpdateButton.setMessage(Component.literal(Lang.get("mineboxtools.menu.item_stats_update")));
        } else {
            long minutes = (remainingMs + 59_999) / 60_000;
            itemStatsUpdateButton.active = false;
            itemStatsUpdateButton.setMessage(Component.literal(
                    Lang.get("mineboxtools.menu.item_stats_update.wait").replace("{0}", String.valueOf(minutes))));
        }
    }

    private String itemStatsStatusText() {
        MineboxItemStatFetcher.State state = MineboxItemStatFetcher.getState();
        return switch (state) {
            case FETCHING -> {
                long secondsLeft = (MineboxItemStatFetcher.estimatedRemainingMs() + 999) / 1000;
                yield Lang.get("mineboxtools.menu.item_stats_update.fetching")
                        .replace("{0}", String.valueOf(MineboxItemStatFetcher.getCurrentPage()))
                        .replace("{1}", String.valueOf(MineboxItemStatFetcher.getTotalPages()))
                        .replace("{2}", String.valueOf(secondsLeft));
            }
            case SUCCESS -> Lang.get("mineboxtools.menu.item_stats_update.success") + " " + lastUpdatedText();
            case ERROR -> {
                String errorKey = "mineboxtools.menu.item_stats_update.error."
                        + MineboxItemStatFetcher.getLastError().name().toLowerCase(Locale.ROOT);
                yield Lang.get("mineboxtools.menu.item_stats_update.error").replace("{0}", Lang.get(errorKey));
            }
            case IDLE -> lastUpdatedText();
        };
    }

    private String lastUpdatedText() {
        String timestamp = MineboxItemStatUtils.getLastUpdated()
                .map(instant -> LAST_UPDATED_FORMAT.format(instant))
                .orElse(Lang.get("mineboxtools.menu.item_stats_update.never"));
        return Lang.get("mineboxtools.menu.item_stats_update.last_updated").replace("{0}", timestamp);
    }

    private Component languageLabel() {
        return switch (ModConfig.getLanguage()) {
            case "fr_fr" -> Component.literal("Langue : FR");
            default -> Component.literal("Language: EN");
        };
    }

    private CycleButton<Boolean> createBooleanToggle(String langKey,
                                                    boolean initialValue, Consumer<Boolean> setter) {
        return CycleButton.onOffBuilder(initialValue)
                .create(0, 0, SETTINGS_ROW_WIDTH, 20, Component.literal(Lang.get(langKey)), (button, value) -> {
                    setter.accept(value);
                    ModConfig.save();
                });
    }

    private <T> CycleButton<T> createEnumToggle(String langKey,
                                                T[] values, Function<T, Component> display,
                                                T initialValue, Consumer<T> setter) {
        return CycleButton.<T>builder(display, initialValue)
                .withValues(values)
                .create(0, 0, SETTINGS_ROW_WIDTH, 20, Component.literal(Lang.get(langKey)), (button, value) -> {
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
