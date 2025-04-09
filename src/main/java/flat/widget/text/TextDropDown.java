package flat.widget.text;

import flat.events.ActionEvent;
import flat.events.TextEvent;
import flat.math.Vector2;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.uxml.UXTheme;
import flat.widget.enums.DropdownAlign;
import flat.widget.stages.Menu;
import flat.widget.stages.MenuItem;
import flat.window.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextDropDown extends TextInputField {

    private UXListener<TextEvent> optionSelectedListener;

    private boolean invalidSubmenuItems;

    private Menu subMenu;
    private List<String> options = new ArrayList<>();
    private List<String> unmodifiableOptions;
    private List<MenuItem> menuItems = new ArrayList<>();
    private List<String> attOpts = new ArrayList<>();

    private float x1, y1, x2, y2;

    public TextDropDown() {
        unmodifiableOptions = Collections.unmodifiableList(options);
        updateSubmenu();
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();
        String content = attrs.getAttributeString("content", null);
        if (content != null) {
            attOpts.clear();
            for (var option : content.trim().split("\n")) {
                option = option.trim();
                if (!option.isEmpty()) {
                    attOpts.add(option);
                }
            }
            setOptions(attOpts.stream().map(this::localizeOption).toList());
        }

        setOptionSelectedListener(attrs.getAttributeListener("on-option-selected", TextEvent.class, controller));
    }

    @Override
    public void applyLocalization() {
        super.applyLocalization();
        UXAttrs attrs = getAttrs();
        setOptions(attOpts.stream().map(this::localizeOption).toList());
    }
    
    @Override
    public void applyStyle() {
        super.applyStyle();

    }

    private String localizeOption(String option) {
        if (option.startsWith("@@")) {
            return option.substring(1);

        } else if (option.startsWith("@")) {
            UXTheme theme = getCurrentTheme();
            if (theme != null) {
                return theme.getText(option);
            }
        }
        return option;
    }

    public List<String> getUnmodifiableOptions() {
        return unmodifiableOptions;
    }

    public void setOptions(List<String> options) {
        this.options.clear();
        this.options.addAll(options);
        updateSubmenu();
    }

    public void addOption(String option) {
        options.add(option);
        updateSubmenu();
    }

    public void addOption(List<String> options) {
        this.options.addAll(options);
        updateSubmenu();
    }

    public void addOption(String... options) {
        for (var option : options) {
            addOption(option);
        }
        updateSubmenu();
    }

    public void removeOption(String option) {
        options.remove(option);
        updateSubmenu();
    }

    public void clearOptions() {
        options.clear();
        updateSubmenu();
    }

    private void updateSubmenu() {
        if (subMenu != null && subMenu.isShown()) {
            createMenuItems();
        } else {
            invalidSubmenuItems = true;
        }
    }

    private void createMenuItems() {
        if (subMenu == null) {
            subMenu = new Menu();
        }
        for (var menuItem : menuItems) {
            menuItem.setActionListener(null);
        }
        menuItems.clear();
        subMenu.removeAll();
        for (var option : options) {
            MenuItem menuItem = new MenuItem();
            menuItem.setText(option);
            menuItem.setActionListener(this::onMenuItemAction);
            subMenu.addMenuItem(menuItem);
            menuItems.add(menuItem);
        }
        invalidSubmenuItems = false;
    }

    protected Menu getSubMenu() {
        createMenuItems();
        return subMenu;
    }

    private void onMenuItemAction(ActionEvent actionEvent) {
        int index = menuItems.indexOf(actionEvent.getSource());
        if (index > -1) {
            selectOption(index);
        }
        hideSubMenu();
    }

    public void showSubMenu() {
        if (invalidSubmenuItems) {
            createMenuItems();
        }

        Activity act = getActivity();
        if (act != null && subMenu != null) {
            float x = getOutX();
            float y = getOutY();
            float width = getOutWidth();
            float height = getOutHeight();
            Vector2 screen1 = localToScreen(x, y);
            Vector2 screen2 = localToScreen(x + width, y + height);
            subMenu.addStyle("drop-down-menu");
            subMenu.setFollowStyleProperty("width", false);
            subMenu.setPrefWidth(getWidth());
            subMenu.show(act, screen2.x, screen2.y, DropdownAlign.TOP_RIGHT);
        }
    }

    public void hideSubMenu() {
        if (subMenu != null) {
            subMenu.hide();
        }
    }

    public void selectOption(int index) {
        if (index >= 0 && index < options.size()) {
            setText(options.get(index));
            fireOptionSelected(options.get(index));
        }
    }

    public void action() {
        showSubMenu();
        super.action();
    }

    public UXListener<TextEvent> getOptionSelectedListener() {
        return optionSelectedListener;
    }

    public void setOptionSelectedListener(UXListener<TextEvent> optionSelectedListener) {
        this.optionSelectedListener = optionSelectedListener;
    }

    private void fireOptionSelected(String option) {
        if (optionSelectedListener != null) {
            UXListener.safeHandle(optionSelectedListener,
                    new TextEvent(this, TextEvent.CHANGE, 0, getLastCaretPosition(), option));
        }
    }
}
