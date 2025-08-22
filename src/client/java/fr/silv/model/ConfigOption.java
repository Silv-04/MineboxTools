package fr.silv.model;

import net.minecraft.text.Text;

public enum ConfigOption {
    SIMPLE("Simple"),
    ADVANCED("Advanced"),
    OFF("Off");

    private final String name;

    ConfigOption(String name) {
        this.name = name;
    }

    public Text getDisplayName() {
        return Text.literal(name);
    }
}
