package com.example.arlacontrole.checklist;

public class ChecklistItemState {
    public String key;
    public String label;
    public boolean checked;
    public boolean autoManaged;

    public ChecklistItemState(String key, String label, boolean checked, boolean autoManaged) {
        this.key = key;
        this.label = label;
        this.checked = checked;
        this.autoManaged = autoManaged;
    }
}
