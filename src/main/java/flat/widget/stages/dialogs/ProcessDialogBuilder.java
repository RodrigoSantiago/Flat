package flat.widget.stages.dialogs;

import flat.resources.ResourceStream;
import flat.uxml.UXListener;
import flat.uxml.UXSheet;
import flat.uxml.UXTheme;
import flat.widget.stages.Dialog;

public class ProcessDialogBuilder {

    private final ResourceStream layoutStream;

    String title;
    String message;
    UXTheme theme;
    boolean block;
    boolean cancelable;
    UXListener<Dialog> onShowListener;
    UXListener<Dialog> onHideListener;
    UXListener<Dialog> onRequestCancelListener;

    public ProcessDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public ProcessDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public ProcessDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ProcessDialogBuilder message(String message) {
        this.message = message;
        return this;
    }

    public ProcessDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public ProcessDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public ProcessDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public ProcessDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public ProcessDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public ProcessDialogBuilder onRequestCancelListener(UXListener<Dialog> onRequestCancelListener) {
        this.onRequestCancelListener = onRequestCancelListener;
        return this;
    }

    public ProcessDialogBuilder cancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public ProcessDialogBuilder block(boolean block) {
        this.block = block;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        dialog.setBlockEvents(block);
        if (theme != null) {
            dialog.setTheme(theme);
        }
        dialog.build(layoutStream, new ProcessDialogController(dialog, this));
        return dialog;
    }
}
