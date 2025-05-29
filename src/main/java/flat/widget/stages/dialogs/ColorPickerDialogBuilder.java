package flat.widget.stages.dialogs;

import flat.graphics.Color;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.stages.Dialog;

public class ColorPickerDialogBuilder {

    private final ResourceStream layoutStream;

    UXTheme theme;
    boolean block;
    boolean cancelable;
    boolean alpha;
    int initialColor = Color.white;
    int[] palette;
    UXListener<Dialog> onShowListener;
    UXListener<Dialog> onHideListener;
    UXWidgetValueListener<Dialog, Integer> onColorPickListener;
    boolean artistic;

    public ColorPickerDialogBuilder() {
        var stream = new ResourceStream("/default/dialogs/dialog_colorpicker.uxml");
        if (stream.exists()) {
            layoutStream = stream;
        } else {
            layoutStream = null;
        }
    }

    public ColorPickerDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public ColorPickerDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public ColorPickerDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public ColorPickerDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public ColorPickerDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public ColorPickerDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public ColorPickerDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public ColorPickerDialogBuilder onColorPickListener(UXWidgetValueListener<Dialog, Integer> onDatePickListener) {
        this.onColorPickListener = onDatePickListener;
        return this;
    }

    public ColorPickerDialogBuilder initialColor(int initialColor) {
        this.initialColor = initialColor;
        return this;
    }

    public ColorPickerDialogBuilder cancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public ColorPickerDialogBuilder block(boolean block) {
        this.block = block;
        return this;
    }

    public ColorPickerDialogBuilder alpha(boolean alpha) {
        this.alpha = alpha;
        return this;
    }

    public ColorPickerDialogBuilder artistic(boolean artistic) {
        this.artistic = artistic;
        return this;
    }

    public ColorPickerDialogBuilder palette(int col0, int col1, int col2, int col3, int col4, int col5, int col6, int col7) {
        this.palette = new int[]{col0, col1, col2, col3, col4, col5, col6, col7};
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        dialog.setBlockEvents(block);
        if (theme != null) {
            dialog.setTheme(theme);
        }
        dialog.build(layoutStream, new ColorPickerDialogController(dialog, this));
        return dialog;
    }
}
