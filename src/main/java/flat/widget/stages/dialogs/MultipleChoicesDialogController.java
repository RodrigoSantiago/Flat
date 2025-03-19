package flat.widget.stages.dialogs;

import flat.Flat;
import flat.events.ActionEvent;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.widget.enums.Visibility;
import flat.widget.layout.LinearBox;
import flat.widget.selection.Checkbox;
import flat.widget.stages.Dialog;
import flat.widget.text.Button;
import flat.widget.text.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class MultipleChoicesDialogController extends Controller {

    private final Dialog dialog;
    private final String title;
    private final String message;
    private final boolean cancelable;
    private final String[] options;
    private final String[] initialOptions;
    private final UXListener<Dialog> onShowListener;
    private final UXListener<Dialog> onHideListener;
    private final UXWidgetValueListener<Dialog, List<String>> onChooseListener;

    private Checkbox[] checkboxes;

    MultipleChoicesDialogController(Dialog dialog, MultipleChoicesDialogBuilder builder) {
        this.dialog = dialog;
        this.title = builder.title;
        this.message = builder.message;
        this.cancelable = builder.cancelable;
        this.options = builder.options == null ? new String[0] : builder.options;
        this.initialOptions = builder.initialOptions == null ? new String[0] : builder.initialOptions;
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
    public LinearBox optionsArea;

    @Flat
    public void hide() {
        dialog.smoothHide();
    }

    @Flat
    public void onChoose(ActionEvent event) {
        if (checkboxes != null) {
            ArrayList<String> choices = new ArrayList<>();
            for (int i = 0; i < checkboxes.length; i++) {
                if (checkboxes[i].isActivated()) {
                    choices.add(options[i]);
                }
            }
            UXWidgetValueListener.safeHandle(onChooseListener, dialog, choices);
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

        if (optionsArea != null && checkboxes == null) {
            checkboxes = new Checkbox[options.length];
            for (int i = 0; i < options.length; i++) {
                var str = options[i];
                Checkbox checkbox = new Checkbox();
                checkbox.addStyle("dialog-choose-option-checkbox");
                checkboxes[i] = checkbox;

                Label label = new Label();
                label.setText(str);
                label.addStyle("dialog-choose-option-label");
                label.setPointerListener(checkbox::firePointer);
                label.setHoverListener(checkbox::fireHover);

                LinearBox box = new LinearBox();
                box.addStyle("dialog-choose-option-box");
                box.add(checkbox, label);

                optionsArea.add(box);
            }
            if (getActivity() != null) {
                dialog.move(getActivity().getWidth() / 2, getActivity().getHeight() / 2);
            }
        }
        if (checkboxes != null) {
            for (int i = 0; i < options.length; i++) {
                boolean checked = false;
                for (String initialOption : initialOptions) {
                    if (Objects.equals(options[i], initialOption)) {
                        checked = true;
                        break;
                    }
                }
                checkboxes[i].setActivated(checked);
            }
        }
        UXListener.safeHandle(onShowListener, dialog);
    }

    @Override
    public void onHide() {
        UXListener.safeHandle(onHideListener, dialog);
    }
}
