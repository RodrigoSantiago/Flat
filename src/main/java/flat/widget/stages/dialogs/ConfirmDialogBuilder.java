package flat.widget.stages.dialogs;

import flat.resources.ResourceStream;
import flat.uxml.UXListener;
import flat.uxml.UXSheet;
import flat.uxml.UXTheme;
import flat.widget.stages.Dialog;

public class ConfirmDialogBuilder {

    private final ResourceStream layoutStream;

    String title;
    String message;
    UXTheme theme;
    boolean block;
    boolean cancelable;
    UXListener<Dialog> onShowListener;
    UXListener<Dialog> onHideListener;
    UXListener<Dialog> onYesListener;
    UXListener<Dialog> onNoListener;

    public ConfirmDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public ConfirmDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public ConfirmDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ConfirmDialogBuilder message(String message) {
        this.message = message;
        return this;
    }

    public ConfirmDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public ConfirmDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public ConfirmDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public ConfirmDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public ConfirmDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public ConfirmDialogBuilder onYesListener(UXListener<Dialog> onYesListener) {
        this.onYesListener = onYesListener;
        return this;
    }

    public ConfirmDialogBuilder onNoListener(UXListener<Dialog> onNoListener) {
        this.onNoListener = onNoListener;
        return this;
    }

    public ConfirmDialogBuilder cancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public ConfirmDialogBuilder block(boolean block) {
        this.block = block;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        dialog.setBlockEvents(block);
        if (theme != null) {
            dialog.setTheme(theme);
        }
        dialog.build(layoutStream, new ConfirmDialogController(dialog, this));
        return dialog;
    }
}
