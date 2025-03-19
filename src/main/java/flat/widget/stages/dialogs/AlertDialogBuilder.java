package flat.widget.stages.dialogs;

import flat.resources.ResourceStream;
import flat.uxml.UXListener;
import flat.uxml.UXSheet;
import flat.uxml.UXTheme;
import flat.widget.stages.Dialog;

public class AlertDialogBuilder {

    private final ResourceStream layoutStream;

    String title;
    String message;
    UXTheme theme;
    boolean block;
    UXListener<Dialog> onShowListener;
    UXListener<Dialog> onHideListener;

    public AlertDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public AlertDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public AlertDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public AlertDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public AlertDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public AlertDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public AlertDialogBuilder message(String message) {
        this.message = message;
        return this;
    }

    public AlertDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public AlertDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public AlertDialogBuilder block(boolean block) {
        this.block = block;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        dialog.setBlockEvents(block);
        if (theme != null) {
            dialog.setTheme(theme);
        }
        dialog.build(layoutStream, new AlertDialogController(dialog, this));
        return dialog;
    }
}
