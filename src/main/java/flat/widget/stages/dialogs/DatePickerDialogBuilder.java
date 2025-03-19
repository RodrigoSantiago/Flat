package flat.widget.stages.dialogs;

import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.stages.Dialog;

import java.time.LocalDate;

public class DatePickerDialogBuilder {

    private final ResourceStream layoutStream;

    String title;
    UXTheme theme;
    boolean block;
    boolean cancelable;
    boolean ranged;
    LocalDate initialDate;
    UXListener<Dialog> onShowListener;
    UXListener<Dialog> onHideListener;
    UXWidgetRangeValueListener<Dialog, LocalDate> onDatePickListener;

    public DatePickerDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public DatePickerDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public DatePickerDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public DatePickerDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public DatePickerDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public DatePickerDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public DatePickerDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public DatePickerDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public DatePickerDialogBuilder onDatePickListener(UXWidgetRangeValueListener<Dialog, LocalDate> onDatePickListener) {
        this.onDatePickListener = onDatePickListener;
        return this;
    }

    public DatePickerDialogBuilder onDatePickListener(UXWidgetValueListener<Dialog, LocalDate> onDatePickListener) {
        this.onDatePickListener = (d, s, e) -> onDatePickListener.handle(d, s);
        return this;
    }

    public DatePickerDialogBuilder initialDate(LocalDate initialDate) {
        this.initialDate = initialDate;
        return this;
    }

    public DatePickerDialogBuilder cancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public DatePickerDialogBuilder ranged(boolean ranged) {
        this.ranged = ranged;
        return this;
    }

    public DatePickerDialogBuilder block(boolean block) {
        this.block = block;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        dialog.setBlockEvents(block);
        if (theme != null) {
            dialog.setTheme(theme);
        }
        dialog.build(layoutStream, new DatePickerDialogController(dialog, this));
        return dialog;
    }
}
