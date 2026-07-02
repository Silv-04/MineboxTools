package fr.silv.hud;

import fr.silv.Lang;
import fr.silv.ModConfig;
import fr.silv.availability.AvailabilityEntry;
import fr.silv.availability.AvailabilityRegistry;
import fr.silv.availability.AvailabilitySection;
import fr.silv.hud.widget.HudWidgetManager;
import fr.silv.hud.widget.config.CheckboxListWidget;
import fr.silv.hud.widget.config.ConfigOption;
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

        CycleButton<Boolean> museumToggle = createBooleanToggle(
                20, 300, "mineboxtools.menu.museum",
                MuseumHighlightHandler.isEnabled(),
                MuseumHighlightHandler::setEnabled);

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
        ).bounds(190, 300, 100, 20).build();

        itemStatsUpdateButton = Button.builder(
                Component.literal(Lang.get("mineboxtools.menu.item_stats_update")),
                button -> MineboxItemStatFetcher.fetchAndApply()
        ).bounds(20, 320, 160, 20).build();

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
        addRenderableWidget(museumToggle);
        addRenderableWidget(museumRefreshButton);
        addRenderableWidget(itemStatsUpdateButton);
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

        drawContext.text(
                this.font,
                Component.literal(itemStatsStatusText()),
                190,
                326,
                HELP_TEXT_COLOR,
                false);

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
