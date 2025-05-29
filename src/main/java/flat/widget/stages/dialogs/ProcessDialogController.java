package flat.widget.stages.dialogs;

import flat.Flat;
import flat.animations.ProgressTaskRefresh;
import flat.concurrent.ProgressTask;
import flat.events.ActionEvent;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.widget.enums.Visibility;
import flat.widget.stages.Dialog;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.widget.value.ProgressBar;

class ProcessDialogController extends DefaultDialogController {

    private final String title;
    private final String message;
    private final boolean cancelable;
    private final UXListener<Dialog> onShowListener;
    private final UXListener<Dialog> onHideListener;
    private final UXListener<Dialog> onRequestCancelListener;
    private final ProgressTask<?> task;
    private ProgressTaskRefresh anim;

    ProcessDialogController(Dialog dialog, ProcessDialogBuilder builder) {
        super(dialog);
        this.title = builder.title;
        this.message = builder.message;
        this.cancelable = builder.cancelable;
        this.onShowListener = builder.onShowListener;
        this.onHideListener = builder.onHideListener;
        this.onRequestCancelListener = builder.onRequestCancelListener;
        this.task = builder.task;
        if (task != null) {
            anim = new ProgressTaskRefresh(this, task, this::onProgress, this::onDone);
        }
    }

    @Flat
    public Label titleLabel;

    @Flat
    public Label messageLabel;

    @Flat
    public Button cancelButton;

    @Flat
    public ProgressBar progressBar;

    @Flat
    public void hide(ActionEvent event) {
        dialog.smoothHide();
    }

    @Flat
    public void onRequestCancel(ActionEvent event) {
        if (task != null) {
            task.requestCancel();
        }
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
        if (anim != null) {
            getActivity().addAnimation(anim);
        }
        UXListener.safeHandle(onShowListener, dialog);
    }

    @Override
    public void onHide() {
        if (task != null) {
            task.requestCancel();
        }
        UXListener.safeHandle(onHideListener, dialog);
    }

    private void onProgress(float val) {
        if (progressBar != null) {
            progressBar.setValue(val);
        }
    }

    private void onDone() {
        dialog.smoothHide();
    }
}
