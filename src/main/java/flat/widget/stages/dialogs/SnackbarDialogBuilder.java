package flat.widget.stages.dialogs;

import flat.resources.ResourceStream;
import flat.uxml.UXListener;
import flat.uxml.UXSheet;
import flat.uxml.UXTheme;
import flat.widget.stages.Dialog;

public class SnackbarDialogBuilder {

    private final ResourceStream layoutStream;

    String message;
    UXTheme theme;
    float duration = 10f;
    UXListener<Dialog> onShowListener;
    UXListener<Dialog> onHideListener;

    public SnackbarDialogBuilder() {
        var stream = new ResourceStream("/default/dialogs/dialog_snackbar.uxml");
        if (stream.exists()) {
            layoutStream = stream;
        } else {
            layoutStream = null;
        }
    }

    public SnackbarDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public SnackbarDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public SnackbarDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public SnackbarDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public SnackbarDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public SnackbarDialogBuilder message(String message) {
        this.message = message;
        return this;
    }

    public SnackbarDialogBuilder duration(float duration) {
        this.duration = duration;
        return this;
    }

    public SnackbarDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public SnackbarDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        if (theme != null) {
            dialog.setTheme(theme);
        }
        dialog.build(layoutStream, new SnackbarDialogController(dialog, this));
        return dialog;
    }
}
