package flat.widget.stages.dialogs;

import flat.Flat;
import flat.events.ActionEvent;
import flat.graphics.emojis.EmojiCharacter;
import flat.graphics.emojis.EmojiDictionary;
import flat.graphics.emojis.EmojiGroup;
import flat.uxml.*;
import flat.widget.Widget;
import flat.widget.layout.Grid;
import flat.widget.layout.ScrollBox;
import flat.widget.stages.Dialog;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.widget.text.TextInputField;

import java.util.ArrayList;
import java.util.Arrays;

public class EmojiDialogController extends DefaultDialogController {

    private final UXListener<Dialog> onShowListener;
    private final UXListener<Dialog> onHideListener;
    private final UXWidgetValueListener<Dialog, String> onEmojiPick;

    EmojiDictionary dictionary;
    UXListener<ActionEvent> emojiClick;

    public EmojiDialogController(Dialog dialog, EmojiDialogBuilder builder) {
        super(dialog);
        this.dictionary = EmojiDictionary.getInstance();
        this.onShowListener = builder.onShowListener;
        this.onHideListener = builder.onHideListener;
        this.onEmojiPick = builder.onEmojiPick;

        if (onEmojiPick != null) {
            emojiClick = (ev) -> {
                Button btn = ev.getSource();
                UXWidgetValueListener.safeHandle(onEmojiPick, dialog, btn.getText());
            };
        }
    }

    @Flat
    public Grid calendarGrid;

    public Button[] btnGroups;

    @Flat public TextInputField searchBar;
    @Flat public ScrollBox emojiScroll;
    @Flat public Button btnGroup0;
    @Flat public Button btnGroup1;
    @Flat public Button btnGroup2;
    @Flat public Button btnGroup3;
    @Flat public Button btnGroup4;
    @Flat public Button btnGroup5;
    @Flat public Button btnGroup6;
    @Flat public Button btnGroup7;
    @Flat public Button btnGroup8;

    @Flat
    public void onOk(ActionEvent event) {
        dialog.smoothHide();
    }

    @Flat
    public void hide() {
        dialog.smoothHide();
    }

    @Flat
    public void onGroup0Click(ActionEvent event) {
        slideToGroup(0);
    }

    @Flat
    public void onGroup1Click(ActionEvent event) {
        slideToGroup(1);
    }

    @Flat
    public void onGroup2Click(ActionEvent event) {
        slideToGroup(2);
    }

    @Flat
    public void onGroup3Click(ActionEvent event) {
        slideToGroup(3);
    }

    @Flat
    public void onGroup4Click(ActionEvent event) {
        slideToGroup(4);
    }

    @Flat
    public void onGroup5Click(ActionEvent event) {
        slideToGroup(5);
    }

    @Flat
    public void onGroup6Click(ActionEvent event) {
        slideToGroup(6);
    }

    @Flat
    public void onGroup7Click(ActionEvent event) {
        slideToGroup(7);
    }

    @Flat
    public void onGroup8Click(ActionEvent event) {
        slideToGroup(8);
    }

    private void slideToGroup(int id) {
        searchBar.setText("");
        getActivity().runLater(() -> {
            int i = 0;
            for (var widget : widgets) {
                if (widget.group != null) {
                    if (i++ == id) {
                        emojiScroll.slideTo(0, widget.widget.getLayoutY());
                    }
                }
            }
        });
    }

    @Flat
    public void onTextSearch(ValueChange<String> event) {
        String txt = event.getValue();
        if (txt != null) {
            txt = txt.toLowerCase().trim();
            if (!txt.isEmpty()) {
                setupGrid(txt.split(" "));
                return;
            }
        }
        setupGrid(null);
    }

    ArrayList<WidgetEmoji> widgets = new ArrayList<>();

    @Override
    public void onShow() {
        btnGroups = new Button[]{
                btnGroup0,
                btnGroup1,
                btnGroup2,
                btnGroup3,
                btnGroup4,
                btnGroup5,
                btnGroup6,
                btnGroup7,
                btnGroup8
        };
        int line = 0;
        for (var group : dictionary.getGroups()) {
            Label title = new Label();
            title.addStyle("dialog-emoji-group-title");
            title.setText(group.getGroup());
            widgets.add(new WidgetEmoji(title, group));

            for (EmojiCharacter emoji : group.getEmoji()) {
                Button btn = new Button();
                btn.addStyle("dialog-emojis-character");
                btn.setText(emoji.getText());
                btn.setActionListener(emojiClick);
                widgets.add(new WidgetEmoji(btn, emoji));
            }
        }
        setupGrid(null);
        UXListener.safeHandle(onShowListener, dialog);
    }

    @Override
    public void onHide() {
        UXListener.safeHandle(onHideListener, dialog);
    }

    private void setupGrid(String[] filter) {
        int countLines = 0;
        int n = 0;
        for (var item : widgets) {
            calendarGrid.remove(item.widget);
        }
        for (var item : widgets) {
            if (item.group != null) {
                if (filter != null) continue;
                countLines++;
                n = 0;
                calendarGrid.add(item.widget, 0, countLines - 1, 9, 1);
            } else {
                if (filter != null && !checkFilters(filter, item.emoji)) {
                    continue;
                }
                if (n == 0) {
                    countLines++;
                }
                calendarGrid.add(item.widget, n % 9, countLines - 1);
                n++;
                if (n == 9) {
                    n = 0;
                }
            }
        }
        if (countLines == 0) {
            countLines = 1;
        }

        float h = calendarGrid.getRowHeight(0);
        float[] rows = new float[countLines];
        Arrays.fill(rows, h);
        calendarGrid.setRows(rows);
    }

    private boolean checkFilters(String[] filters, EmojiCharacter emoji) {
        for (var f : filters) {
            if (!checkFilter(f, emoji)) return false;
        }
        return true;
    }

    private boolean checkFilter(String filter, EmojiCharacter emoji) {
        for (String shortcode : emoji.getShortcodes()) {
            if (shortcode.contains(filter)) return true;
        }
        int[] base = emoji.getBase();
        if ((base[0] >= 0x1f1e6 && base[0] <= 0x1f1ff) && (base[1] >= 0x1f1e6 && base[1] <= 0x1f1ff)) {
            String asText = new String(new char[]{
                    (char) ('a' + (base[0] - 0x1f1e6)),
                    (char) ('a' + (base[1] - 0x1f1e6))});
            if ("flag".contains(filter)) return true;
            if (asText.contains(filter)) return true;
            if (filter.contains("flag") && filter.contains(asText)) return true;
        }
        return false;
    }

    private static class WidgetEmoji {
        public Widget widget;
        public EmojiGroup group;
        public EmojiCharacter emoji;

        public WidgetEmoji(Widget widget, EmojiCharacter emoji) {
            this.widget = widget;
            this.emoji = emoji;
        }

        public WidgetEmoji(Widget widget, EmojiGroup group) {
            this.widget = widget;
            this.group = group;
        }
    }
}
