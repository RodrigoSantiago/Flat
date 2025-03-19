package flat.widget.stages.dialogs;

import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.stages.Dialog;

public class ChoiceDialogBuilder {

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

    public ChoiceDialogBuilder() {
        var stream = new ResourceStream("/default/dialogs/dialog_choice.uxml");
        if (stream.exists()) {
            layoutStream = stream;
        } else {
            layoutStream = null;
        }
    }

    public ChoiceDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public ChoiceDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public ChoiceDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public ChoiceDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public ChoiceDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public ChoiceDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ChoiceDialogBuilder message(String message) {
        this.message = message;
        return this;
    }

    public ChoiceDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public ChoiceDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public ChoiceDialogBuilder onChooseListener(UXWidgetValueListener<Dialog, Integer> onChooseListener) {
        this.onChooseListener = onChooseListener;
        return this;
    }

    public ChoiceDialogBuilder options(String... options) {
        this.options = options;
        return this;
    }

    public ChoiceDialogBuilder initialOption(int initialOption) {
        this.initialOption = initialOption;
        return this;
    }

    public ChoiceDialogBuilder cancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public ChoiceDialogBuilder block(boolean block) {
        this.block = block;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        dialog.setBlockEvents(block);
        if (theme != null) {
            dialog.setTheme(theme);
        }

        dialog.build(layoutStream, new ChoiceDialogController(dialog, this));
        return dialog;
    }
}
