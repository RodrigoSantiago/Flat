package flat.widget.stages.dialogs;

import flat.Flat;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.widget.stages.Dialog;
import flat.widget.text.Button;
import flat.widget.text.Label;

public class AlertDialogController extends DefaultDialogController {
    private final String title;
    private final String message;
    private final UXListener<Dialog> onShowListener;
    private final UXListener<Dialog> onHideListener;

    AlertDialogController(Dialog dialog, AlertDialogBuilder builder) {
        super(dialog);
        this.title = builder.title;
        this.message = builder.message;
        this.onShowListener = builder.onShowListener;
        this.onHideListener = builder.onHideListener;
    }

    @Flat
    public Label titleLabel;

    @Flat
    public Label messageLabel;

    @Flat
    public Button cancelButton;

    @Flat
    public void hide() {
        dialog.smoothHide();
    }

    @Flat
    public void onOk(ActionEvent event) {
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
        UXListener.safeHandle(onShowListener, dialog);
    }

    @Override
    public void onHide() {
        UXListener.safeHandle(onHideListener, dialog);
    }
}
