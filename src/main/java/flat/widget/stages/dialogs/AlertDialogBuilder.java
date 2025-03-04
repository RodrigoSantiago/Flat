package flat.widget.stages.dialogs;

import flat.Flat;
import flat.events.ActionEvent;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.uxml.UXSheet;
import flat.uxml.UXTheme;
import flat.widget.Widget;
import flat.widget.stages.Dialog;
import flat.widget.text.Label;

public class AlertDialogBuilder {

    private final ResourceStream layoutStream;

    private String title;
    private String message;
    private UXTheme theme;
    private boolean block;
    private UXListener<Dialog> onShowListener;
    private UXListener<Dialog> onHideListener;

    public AlertDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public AlertDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public AlertDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public AlertDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public AlertDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public AlertDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public AlertDialogBuilder message(String message) {
        this.message = message;
        return this;
    }

    public AlertDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public AlertDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public AlertDialogBuilder block(boolean block) {
        this.block = block;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        dialog.setBlockEvents(block);
        if (theme != null) {
            dialog.setTheme(theme);
        }

        Controller controller = new Controller() {
            @Flat
            public void hide(ActionEvent event) {
                dialog.smoothHide();
            }

            @Override
            public void onShow() {
                UXListener.safeHandle(onShowListener, dialog);
            }

            @Override
            public void onHide() {
                UXListener.safeHandle(onHideListener, dialog);
            }
        };

        dialog.build(layoutStream, controller);

        Widget titleWidget = dialog.findById("title");
        if (titleWidget instanceof Label titleLabel) {
            titleLabel.setText(title);
        } else if (dialog.getChildrenIterable().size() > 0) {
            titleWidget = dialog.getChildrenIterable().get(0).findById("title");
            if (titleWidget instanceof Label titleLabel) {
                titleLabel.setText(title);
            }
        }

        Widget messageWidget = dialog.findById("message");
        if (messageWidget instanceof Label messageLabel) {
            messageLabel.setText(message);
        } else if (dialog.getChildrenIterable().size() > 0) {
            messageWidget = dialog.getChildrenIterable().get(0).findById("message");
            if (messageWidget instanceof Label messageLabel) {
                messageLabel.setText(message);
            }
        }
        return dialog;
    }
}
