package flat.widget.stages.dialogs;

import flat.Flat;
import flat.events.ActionEvent;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.widget.enums.Visibility;
import flat.widget.stages.Dialog;
import flat.widget.text.Button;
import flat.widget.text.Label;

class ProcessDialogController extends Controller {

    private final Dialog dialog;
    private final String title;
    private final String message;
    private final boolean cancelable;
    private final UXListener<Dialog> onShowListener;
    private final UXListener<Dialog> onHideListener;
    private final UXListener<Dialog> onRequestCancelListener;

    ProcessDialogController(Dialog dialog, ProcessDialogBuilder builder) {
        this.dialog = dialog;
        this.title = builder.title;
        this.message = builder.message;
        this.cancelable = builder.cancelable;
        this.onShowListener = builder.onShowListener;
        this.onHideListener = builder.onHideListener;
        this.onRequestCancelListener = builder.onRequestCancelListener;
    }

    @Flat
    public Label titleLabel;

    @Flat
    public Label messageLabel;

    @Flat
    public Button cancelButton;

    @Flat
    public void hide(ActionEvent event) {
        dialog.smoothHide();
    }

    @Flat
    public void onRequestCancel(ActionEvent event) {
        UXListener.safeHandle(onRequestCancelListener, dialog);
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
        UXListener.safeHandle(onShowListener, dialog);
    }

    @Override
    public void onHide() {
        UXListener.safeHandle(onHideListener, dialog);
    }
}
