package flat.widget.stages.dialogs;

import flat.Flat;
import flat.animations.NormalizedAnimation;
import flat.events.ActionEvent;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.widget.stages.Dialog;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.window.Activity;

public class SnackbarDialogController extends DefaultDialogController {
    private final String message;
    private final float duration;
    private final UXListener<Dialog> onShowListener;
    private final UXListener<Dialog> onHideListener;
    SelfClose anim;

    SnackbarDialogController(Dialog dialog, SnackbarDialogBuilder builder) {
        super(dialog);
        this.message = builder.message;
        this.duration = builder.duration;
        this.onShowListener = builder.onShowListener;
        this.onHideListener = builder.onHideListener;
        anim = new SelfClose();
        anim.setDuration(duration);
    }

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
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
        if (getActivity() != null) {
            dialog.moveTo(getActivity().getWidth() / 2, getActivity().getHeight() - dialog.getHeight());
        }
        anim.play(getActivity());
        UXListener.safeHandle(onShowListener, dialog);
    }

    @Override
    public void onHide() {
        anim.stop(false);
        UXListener.safeHandle(onHideListener, dialog);
    }

    private class SelfClose extends NormalizedAnimation {

        @Override
        protected void compute(float t) {
            if (t >= 1) {
                hide();
            }
        }

        @Override
        public Activity getSource() {
            return getActivity();
        }
    }
}
