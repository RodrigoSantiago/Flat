package flat.widget.stages.dialogs;

import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.stages.Dialog;

public class ChooseDialogBuilder {

    private final ResourceStream layoutStream;

    String title;
    String message;
    UXTheme theme;
    boolean block;
    boolean cancelable;
    String[] options;
    int initialOption;
    UXListener<Dialog> onShowListener;
    UXListener<Dialog> onHideListener;
    UXWidgetValueListener<Dialog, Integer> onChooseListener;

    public ChooseDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public ChooseDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public ChooseDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public ChooseDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public ChooseDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public ChooseDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ChooseDialogBuilder message(String message) {
        this.message = message;
        return this;
    }

    public ChooseDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public ChooseDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public ChooseDialogBuilder onChooseListener(UXWidgetValueListener<Dialog, Integer> onChooseListener) {
        this.onChooseListener = onChooseListener;
        return this;
    }

    public ChooseDialogBuilder options(String... options) {
        this.options = options;
        return this;
    }

    public ChooseDialogBuilder initialOption(int initialOption) {
        this.initialOption = initialOption;
        return this;
    }

    public ChooseDialogBuilder cancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public ChooseDialogBuilder block(boolean block) {
        this.block = block;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        dialog.setBlockEvents(block);
        if (theme != null) {
            dialog.setTheme(theme);
        }

        dialog.build(layoutStream, new ChooseDialogController(dialog, this));
        return dialog;
    }
}
