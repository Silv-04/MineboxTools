package fr.silv.model;

import fr.silv.utils.MineboxItemUtils;

import java.util.List;

public class MineboxItem {
    private String id;
    private List<String> location;
    private String condition;

    public MineboxItem(String id, List<String> location, String condition) {
        this.id = id;
        this.location = location;
        this.condition = condition;
    }

    public List<String> getLocation() {
        return location;
    }

    public String getCondition() {
        return condition;
    }

    public String getId() {
        return id;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLocation(List<String> location) {
        this.location = location;
    }
}
