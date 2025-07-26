package flat.widget.text;

import flat.events.ActionEvent;
import flat.events.TextEvent;
import flat.math.Vector2;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.uxml.UXTheme;
import flat.widget.enums.DropdownAlign;
import flat.widget.stages.Divider;
import flat.widget.stages.Menu;
import flat.widget.stages.MenuItem;
import flat.window.Activity;

import java.util.*;

public class TextDropDown extends TextInputField {

    private UXListener<TextEvent> optionSelectedListener;

    private boolean invalidSubmenuItems;

    private Menu subMenu;
    private final List<Option> options = new ArrayList<>();
    private final List<Option> unmodifiableOptions;
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final List<Option> attOpts = new ArrayList<>();
    private final List<Integer> dividers = new ArrayList<>();
    private final List<Integer> unmodifiableDividers;

    private float x1, y1, x2, y2;

    public TextDropDown() {
        unmodifiableOptions = Collections.unmodifiableList(options);
        unmodifiableDividers = Collections.unmodifiableList(dividers);
        updateSubmenu();
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();
        String content = attrs.getAttributeString("content", null);
        if (content != null) {
            List<Option> attOpts = new ArrayList<>();
            for (var option : content.trim().split("\n")) {
                option = option.trim();
                if (!option.isEmpty()) {
                    if (!option.startsWith("@@") && option.startsWith("@")) {
                        attOpts.add(new Option(option.substring(1), option.substring(1)));
                    } else if (option.startsWith("@@")) {
                        attOpts.add(new Option(option.substring(1)));
                    } else {
                        attOpts.add(new Option(option));
                    }
                }
            }
            setOptions(attOpts.toArray(new Option[0]));
        }

        setOptionSelectedListener(attrs.getAttributeListener("on-option-selected", TextEvent.class, controller, getOptionSelectedListener()));
    }
    
    @Override
    public void applyStyle() {
        super.applyStyle();
    }
    
    @Override
    public void applyLocalization() {
        int selectedOption = getSelectedOption();
        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);
            if (option.getLocale() != null) {
                this.options.set(i, new Option(translate(option), option.getLocale()));
            }
        }
        if (selectedOption != -1 && selectedOption <= this.options.size()) {
            setTextSilently(this.options.get(selectedOption).getValue());
        }
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
    
    public List<Integer> getUnmodifiableDividers() {
        return unmodifiableDividers;
    }
    
    public void setDividers(List<Integer> dividers) {
        this.dividers.clear();
        for (var index : dividers) {
            if (!this.dividers.contains(index)) {
                this.dividers.add(index);
            }
        }
        this.dividers.sort(Integer::compare);
        updateSubmenu();
    }
    
    public void addDivider(int index) {
        if (!dividers.contains(index)) {
            dividers.add(index);
            dividers.sort(Integer::compare);
        }
    }
    
    public void removeDivider(int index) {
        dividers.remove((Integer)index);
        updateSubmenu();
    }
    
    public void clearDividers() {
        dividers.clear();
        updateSubmenu();
    }
    
    public List<Option> getUnmodifiableOptions() {
        return unmodifiableOptions;
    }
    
    public void setOptions(Option... options) {
        this.options.clear();
        for (Option option : options) {
            if (option.getLocale() == null) {
                this.options.add(option);
            } else {
                this.options.add(new Option(translate(option), option.getLocale()));
            }
        }
        updateSubmenu();
    }
    
    public void setOptions(String... options) {
        this.options.clear();
        this.options.addAll(Arrays.stream(options).map(Option::new).toList());
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
        for (int i = 0; i < options.size(); i++) {
            var option = options.get(i);
            MenuItem menuItem = new MenuItem();
            menuItem.setText(option.getValue());
            menuItem.setActionListener(this::onMenuItemAction);
            if (dividers.contains(i)) {
                subMenu.addDivider(new Divider());
            }
            subMenu.addMenuItem(menuItem);
            menuItems.add(menuItem);
        }
        invalidSubmenuItems = false;
    }
    
    private String translate(Option option) {
        if (option.getLocale() == null) {
            return option.getValue();
        }
        
        var theme = getCurrentTheme();
        if (theme != null) {
            var bundle = theme.getStringBundle();
            if (bundle != null) {
                return bundle.get(option.getLocale(), option.getValue());
            }
        }
        return option.getValue();
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
    
    public void setSelectedOption(int index) {
        if (index >= 0 && index < options.size()) {
            setText(options.get(index).getValue());
        }
    }

    public void selectOption(int index) {
        if (index >= 0 && index < options.size()) {
            setText(options.get(index).getValue());
            fireOptionSelected(options.get(index).getValue());
            fireTextType();
        }
    }
    
    public int getSelectedOption() {
        String text = getText();
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getValue().equals(text)) {
                return i;
            }
        }
        return -1;
    }
    
    protected boolean isOverActionButton(Vector2 local) {
        return !isEditable() || super.isOverActionButton(local);
    }
    
    @Override
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
