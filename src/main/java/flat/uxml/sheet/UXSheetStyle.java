package flat.uxml.sheet;

import java.util.HashMap;

public class UXSheetStyle {
    private String name;
    private String parent;
    private HashMap<String, UXSheetAttribute> attributes = new HashMap<>();
    private HashMap<String, UXSheetStyle> states = new HashMap<>();

    public UXSheetStyle(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void addAttribute(UXSheetAttribute attribute) {
        attributes.put(attribute.getName(), attribute);
    }

    public HashMap<String, UXSheetAttribute> getAttributes() {
        return attributes;
    }

    public void addState(UXSheetStyle state) {
        states.put(state.getName(), state);
    }

    public HashMap<String, UXSheetStyle> getStates() {
        return states;
    }
}
