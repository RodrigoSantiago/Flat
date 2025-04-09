package flat.widget.stages.dialogs;

import flat.resources.ResourceStream;
import flat.uxml.UXListener;
import flat.uxml.UXSheet;
import flat.uxml.UXTheme;
import flat.uxml.UXWidgetValueListener;
import flat.widget.stages.Dialog;

import java.util.List;

public class MultipleChoicesDialogBuilder {

    private final ResourceStream layoutStream;

    String title;
    String message;
    UXTheme theme;
    boolean block;
    boolean cancelable;
    String[] options;
    String[] initialOptions;
    UXListener<Dialog> onShowListener;
    UXListener<Dialog> onHideListener;
    UXWidgetValueListener<Dialog, List<String>> onChooseListener;

    public MultipleChoicesDialogBuilder() {
        var stream = new ResourceStream("/default/dialogs/dialog_multiplechoices.uxml");
        if (stream.exists()) {
            layoutStream = stream;
        } else {
            layoutStream = null;
        }
    }

    public MultipleChoicesDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public MultipleChoicesDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public MultipleChoicesDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public MultipleChoicesDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public MultipleChoicesDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public MultipleChoicesDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public MultipleChoicesDialogBuilder message(String message) {
        this.message = message;
        return this;
    }

    public MultipleChoicesDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public MultipleChoicesDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public MultipleChoicesDialogBuilder onChooseListener(UXWidgetValueListener<Dialog, List<String>> onChooseListener) {
        this.onChooseListener = onChooseListener;
        return this;
    }

    public MultipleChoicesDialogBuilder options(String... options) {
        this.options = options;
        return this;
    }

    public MultipleChoicesDialogBuilder initialOptions(String... initialOptions) {
        this.initialOptions = initialOptions;
        return this;
    }

    public MultipleChoicesDialogBuilder cancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public MultipleChoicesDialogBuilder block(boolean block) {
        this.block = block;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        dialog.setBlockEvents(block);
        if (theme != null) {
            dialog.setTheme(theme);
        }

        dialog.build(layoutStream, new MultipleChoicesDialogController(dialog, this));
        return dialog;
    }
}
