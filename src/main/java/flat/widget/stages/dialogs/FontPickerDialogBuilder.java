package flat.widget.stages.dialogs;

import flat.graphics.symbols.Font;
import flat.resources.ResourceStream;
import flat.uxml.UXListener;
import flat.uxml.UXSheet;
import flat.uxml.UXTheme;
import flat.uxml.UXWidgetValueListener;
import flat.widget.stages.Dialog;

import java.util.List;

public class FontPickerDialogBuilder {

    private final ResourceStream layoutStream;

    String title;
    String message;
    UXTheme theme;
    boolean block;
    boolean cancelable;
    Font initialFont;
    UXListener<Dialog> onShowListener;
    UXListener<Dialog> onHideListener;
    UXWidgetValueListener<Dialog, Font> onChooseListener;

    public FontPickerDialogBuilder() {
        var stream = new ResourceStream("/default/dialogs/dialog_fontpicker.uxml");
        if (stream.exists()) {
            layoutStream = stream;
        } else {
            layoutStream = null;
        }
    }

    public FontPickerDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public FontPickerDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public FontPickerDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public FontPickerDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public FontPickerDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public FontPickerDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public FontPickerDialogBuilder message(String message) {
        this.message = message;
        return this;
    }

    public FontPickerDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public FontPickerDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public FontPickerDialogBuilder onChooseListener(UXWidgetValueListener<Dialog, Font> onChooseListener) {
        this.onChooseListener = onChooseListener;
        return this;
    }

    public FontPickerDialogBuilder initialFont(Font initialFont) {
        this.initialFont = initialFont;
        return this;
    }

    public FontPickerDialogBuilder cancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public FontPickerDialogBuilder block(boolean block) {
        this.block = block;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        dialog.setBlockEvents(block);
        if (theme != null) {
            dialog.setTheme(theme);
        }

        dialog.build(layoutStream, new FontPickerDialogController(dialog, this));
        return dialog;
    }
}
