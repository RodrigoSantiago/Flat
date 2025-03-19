package flat.widget.stages.dialogs;

import flat.Flat;
import flat.events.ActionEvent;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.widget.Widget;
import flat.widget.enums.Visibility;
import flat.widget.layout.LinearBox;
import flat.widget.selection.RadioButton;
import flat.widget.selection.RadioGroup;
import flat.widget.stages.Dialog;
import flat.widget.text.Button;
import flat.widget.text.Label;

class ChooseDialogController extends Controller {

    private final Dialog dialog;
    private final String title;
    private final String message;
    private final boolean cancelable;
    private final String[] options;
    private final int initialOption;
    private final UXListener<Dialog> onShowListener;
    private final UXListener<Dialog> onHideListener;
    private final UXWidgetValueListener<Dialog, Integer> onChooseListener;

    ChooseDialogController(Dialog dialog, ChooseDialogBuilder builder) {
        this.dialog = dialog;
        this.title = builder.title;
        this.message = builder.message;
        this.cancelable = builder.cancelable;
        this.options = builder.options;
        this.initialOption = builder.initialOption;
        this.onShowListener = builder.onShowListener;
        this.onHideListener = builder.onHideListener;
        this.onChooseListener = builder.onChooseListener;
    }

    @Flat
    public Label titleLabel;

    @Flat
    public Label messageLabel;

    @Flat
    public Button cancelButton;

    @Flat
    public RadioGroup optionsGroup;

    @Flat
    public LinearBox optionsArea;

    @Flat
    public void hide() {
        dialog.smoothHide();
    }

    @Flat
    public void onChoose(ActionEvent event) {
        if (optionsGroup != null) {
            UXWidgetValueListener.safeHandle(onChooseListener, dialog, optionsGroup.getSelected());
        }
        dialog.smoothHide();
    }

    @Flat
    public void onCancel(ActionEvent event) {
        dialog.smoothHide();
    }

    @Override
    public void onShow() {
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
        if (!cancelable && cancelButton != null) {
            cancelButton.setVisibility(Visibility.GONE);
        }

        if (optionsGroup != null) {
            Widget optionsWidget = dialog.findById("optionsArea");
            if (optionsArea != null) {
                for (var str : options) {
                    RadioButton radio = new RadioButton();
                    radio.addStyle("dialog-choose-option-button");
                    optionsGroup.add(radio);

                    Label label = new Label();
                    label.setText(str);
                    label.addStyle("dialog-choose-option-label");
                    label.setPointerListener(radio::firePointer);
                    label.setHoverListener(radio::fireHover);

                    LinearBox box = new LinearBox();
                    box.addStyle("dialog-choose-option-box");
                    box.add(radio, label);

                    optionsArea.add(box);
                }
            }
            optionsGroup.select(initialOption);
        }

        UXListener.safeHandle(onShowListener, dialog);
    }

    @Override
    public void onHide() {
        UXListener.safeHandle(onHideListener, dialog);
    }
}
