package fr.silv.hud;

import fr.silv.Lang;
import fr.silv.ModConfig;
import fr.silv.availability.AvailabilityEntry;
import fr.silv.availability.AvailabilityRegistry;
import fr.silv.availability.AvailabilitySection;
import fr.silv.hud.widget.HudWidgetManager;
import fr.silv.hud.widget.config.CheckboxListWidget;
import fr.silv.hud.widget.config.ConfigOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Main in-game HUD menu screen for MineboxTools.
 */
public class HudMenuScreen extends Screen {
    private String searchQuery = "";
    private TextFieldWidget searchField;
    private CheckboxListWidget insectList;
    private CheckboxListWidget shopList;
    private List<AvailabilityEntry> insectEntries = List.of();
    private List<AvailabilityEntry> worldEntries = List.of();

    /**
     * Creates a new HudMenuScreen instance.
     */
    public HudMenuScreen() {
        super(Text.of("MineboxTools Menu"));
    }

    @Override
    /**
     * Executes the init operation.
     */
    public void init() {
        super.init();
        Lang.load(ModConfig.getLanguage());

        ButtonWidget languageButton = ButtonWidget.builder(languageLabel(), button -> {
            String nextLanguage = switch (ModConfig.getLanguage()) {
                case "en_us" -> "fr_fr";
                case "fr_fr" -> "pl_pl";
                case "pl_pl" -> "en_us";
                default -> "en_us";
            };
            ModConfig.setLanguage(nextLanguage);
            Lang.load(ModConfig.getLanguage());
            ModConfig.save();
            clearAndInit();
        }).dimensions(20, 20, 160, 20).build();

        CyclingButtonWidget<Boolean> durabilityToggle = createBooleanToggle(
                20, 60, "mineboxtools.menu.durability",
                ModConfig.isEnabled(ModConfig.FeatureFlag.DURABILITY),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.DURABILITY, value)
        );

        CyclingButtonWidget<Boolean> tooltipToggle = createBooleanToggle(
                20, 80, "mineboxtools.menu.tooltip",
                ModConfig.isEnabled(ModConfig.FeatureFlag.TOOLTIP),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.TOOLTIP, value)
        );

        CyclingButtonWidget<ConfigOption> statDisplayToggle = createConfigOptionToggle(
                20, 100, "mineboxtools.menu.stats",
                ModConfig.getStatDisplay(),
                ModConfig::setStatDisplay
        );

        CyclingButtonWidget<Boolean> handToggle = createBooleanToggle(
                20, 120, "mineboxtools.menu.hand",
                ModConfig.isEnabled(ModConfig.FeatureFlag.HAND),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.HAND, value)
        );

        CyclingButtonWidget<Boolean> locationToggle = createBooleanToggle(
                20, 140, "mineboxtools.menu.location",
                ModConfig.isEnabled(ModConfig.FeatureFlag.LOCATION),
                value -> ModConfig.setEnabled(ModConfig.FeatureFlag.LOCATION, value)
        );

        CyclingButtonWidget<ModConfig.IconSize> iconSizeToggle = createIconSizeToggle(
            20, 160, "mineboxtools.menu.icon_size",
            ModConfig.getHudIconSize(),
            ModConfig::setHudIconSize
        );

        CyclingButtonWidget<ModConfig.IconOrientation> iconOrientationToggle = createIconOrientationToggle(
            20, 180, "mineboxtools.menu.icon_orientation",
            ModConfig.getHudIconOrientation(),
            ModConfig::setHudIconOrientation
        );

        CyclingButtonWidget<ModConfig.IconDirection> iconDirectionToggle = createIconDirectionToggle(
            20, 200, "mineboxtools.menu.icon_direction",
            ModConfig.getHudIconDirection(),
            ModConfig::setHudIconDirection,
            iconOrientationToggle
        );

        updateIconOrientationToggleState(iconOrientationToggle, ModConfig.getHudIconDirection());

        int listTop = 44;
        int listWidth = 150;
        int shopListHeight = this.height - 96;
        int insectSearchHeight = 20;
        int insectListTop = listTop + insectSearchHeight;
        int insectListHeight = shopListHeight - insectSearchHeight;

        ButtonWidget customHudButton = ButtonWidget.builder(Text.literal(Lang.get("mineboxtools.menu.hud")), button ->
                MinecraftClient.getInstance().setScreen(new HudConfigScreen(HudWidgetManager.getWidgets()))
        ).dimensions(20, 240, 160, 20).build();

        searchField = new TextFieldWidget(
            this.textRenderer,
                this.width - 170,
                listTop,
                listWidth,
                insectSearchHeight,
            Text.literal("Search")
        );
        searchField.setMaxLength(64);
        searchField.setPlaceholder(Text.literal("Search insects..."));
        searchField.setText(searchQuery);
        searchField.setChangedListener(value -> {
            searchQuery = value;
            refreshInsectList();
        });

        insectList = new CheckboxListWidget(
                MinecraftClient.getInstance(),
            this.width - 170,
                insectListTop,
                listWidth,
                insectListHeight,
                20
        );
        insectEntries = AvailabilityRegistry.entriesForSection(AvailabilitySection.INSECTS);

        shopList = new CheckboxListWidget(
                MinecraftClient.getInstance(),
            this.width - 330,
                listTop,
                listWidth,
                shopListHeight,
                20
        );
        worldEntries = AvailabilityRegistry.entriesForSection(AvailabilitySection.WORLD);

        refreshInsectList();
        addCheckboxOptions(shopList, worldEntries);

        addDrawableChild(languageButton);
        addDrawableChild(durabilityToggle);
        addDrawableChild(tooltipToggle);
        addDrawableChild(statDisplayToggle);
        addDrawableChild(handToggle);
        addDrawableChild(locationToggle);
        addDrawableChild(iconSizeToggle);
        addDrawableChild(iconOrientationToggle);
        addDrawableChild(iconDirectionToggle);
        addDrawableChild(searchField);
        addDrawableChild(insectList);
        addDrawableChild(shopList);
        addDrawableChild(customHudButton);
        addDrawableChild(ButtonWidget.builder(Text.literal(Lang.get("mineboxtools.menu.close")), button -> close())
                .dimensions(this.width - 100, this.height - 40, 80, 20)
                .build());

        setFocused(searchField);
        searchField.setFocused(true);
    }

    @Override
    /**
     * Executes the render operation.
     * @param drawContext value for drawContext
     * @param mouseX value for mouseX
     * @param mouseY value for mouseY
     * @param delta value for delta
     */
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.fill(0, 0, this.width, this.height, 0x90000000);
        super.render(drawContext, mouseX, mouseY, delta);

        if (ModConfig.getHudIconDirection() != ModConfig.IconDirection.AUTO) {
            drawContext.drawText(
                    this.textRenderer,
                    Text.literal(Lang.get("mineboxtools.menu.icon_orientation.auto_only")),
                    20,
                    224,
                    0xFFB0B0B0,
                    false
            );
        }
    }

    @Override
    /**
     * Executes the should pause operation.
     * @return true if the condition is met; otherwise false
     */
    public boolean shouldPause() {
        return false;
    }

    private Text languageLabel() {
        return switch (ModConfig.getLanguage()) {
            case "fr_fr" -> Text.of("Langue : FR");
            case "pl_pl" -> Text.of("JÃ„â„¢zyk: PL");
            default -> Text.of("Language: EN");
        };
    }

    private CyclingButtonWidget<Boolean> createBooleanToggle(int x, int y, String langKey,
                                                             boolean initialValue, Consumer<Boolean> setter) {
        return CyclingButtonWidget.onOffBuilder(initialValue)
                .build(x, y, 160, 20, Text.literal(Lang.get(langKey)), (button, value) -> {
                    setter.accept(value);
                    ModConfig.save();
                });
    }

    private CyclingButtonWidget<ConfigOption> createConfigOptionToggle(int x, int y, String langKey,
                                                                       ConfigOption initialValue, Consumer<ConfigOption> setter) {
        return CyclingButtonWidget.builder(ConfigOption::getDisplayName)
                .values(ConfigOption.values())
                .initially(initialValue)
                .build(x, y, 160, 20, Text.literal(Lang.get(langKey)), (button, value) -> {
                    setter.accept(value);
                    ModConfig.save();
                });
    }

    private CyclingButtonWidget<ModConfig.IconSize> createIconSizeToggle(int x, int y, String langKey,
                                                                          ModConfig.IconSize initialValue,
                                                                          Consumer<ModConfig.IconSize> setter) {
        return CyclingButtonWidget.<ModConfig.IconSize>builder(value -> Text.literal(Lang.get(value.getLabelKey())))
            .values(List.of(ModConfig.IconSize.values()))
                .initially(initialValue)
                .build(x, y, 160, 20, Text.literal(Lang.get(langKey)), (button, value) -> {
                    setter.accept(value);
                    ModConfig.save();
                });
    }

    private CyclingButtonWidget<ModConfig.IconOrientation> createIconOrientationToggle(int x, int y, String langKey,
                                                                                         ModConfig.IconOrientation initialValue,
                                                                                         Consumer<ModConfig.IconOrientation> setter) {
        return CyclingButtonWidget.<ModConfig.IconOrientation>builder(value -> Text.literal(Lang.get(value.getLabelKey())))
            .values(List.of(ModConfig.IconOrientation.values()))
                .initially(initialValue)
                .build(x, y, 160, 20, Text.literal(Lang.get(langKey)), (button, value) -> {
                    setter.accept(value);
                    ModConfig.save();
                });
    }

    private CyclingButtonWidget<ModConfig.IconDirection> createIconDirectionToggle(int x, int y, String langKey,
                                                                                     ModConfig.IconDirection initialValue,
                                                                                     Consumer<ModConfig.IconDirection> setter,
                                                                                     CyclingButtonWidget<ModConfig.IconOrientation> orientationToggle) {
        return CyclingButtonWidget.<ModConfig.IconDirection>builder(value -> Text.literal(Lang.get(value.getLabelKey())))
            .values(List.of(ModConfig.IconDirection.values()))
                .initially(initialValue)
                .build(x, y, 160, 20, Text.literal(Lang.get(langKey)), (button, value) -> {
                    setter.accept(value);
                    updateIconOrientationToggleState(orientationToggle, value);
                    ModConfig.save();
                });
    }

    private static void updateIconOrientationToggleState(CyclingButtonWidget<ModConfig.IconOrientation> orientationToggle,
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

            insectList.addOption(Text.of(localizedLabel), entry.isEnabled(), checked -> {
                entry.setEnabled(checked);
                ModConfig.save();
            }, entry.icon());
        }
    }

    private void addCheckboxOptions(CheckboxListWidget list, List<AvailabilityEntry> entries) {
        for (AvailabilityEntry entry : entries) {
            list.addOption(Text.of(Lang.get(entry.langKey())), entry.isEnabled(), checked -> {
                entry.setEnabled(checked);
                ModConfig.save();
            }, entry.icon());
        }
    }
}