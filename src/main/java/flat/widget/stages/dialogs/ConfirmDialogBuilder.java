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

public class ConfirmDialogBuilder {

    private final ResourceStream layoutStream;

    private String title;
    private String message;
    private UXTheme theme;
    private boolean block;
    private UXListener<Dialog> onShowListener;
    private UXListener<Dialog> onHideListener;
    private UXListener<Dialog> onYesListener;
    private UXListener<Dialog> onNoListener;

    public ConfirmDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public ConfirmDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public ConfirmDialogBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ConfirmDialogBuilder message(String message) {
        this.message = message;
        return this;
    }

    public ConfirmDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public ConfirmDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public ConfirmDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public ConfirmDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public ConfirmDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public ConfirmDialogBuilder onYesListener(UXListener<Dialog> onYesListener) {
        this.onYesListener = onYesListener;
        return this;
    }

    public ConfirmDialogBuilder onNoListener(UXListener<Dialog> onNoListener) {
        this.onNoListener = onNoListener;
        return this;
    }

    public ConfirmDialogBuilder block(boolean block) {
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

            @Flat
            public void onYes(ActionEvent event) {
                try {
                    UXListener.safeHandle(onYesListener, dialog);
                } finally {
                    dialog.smoothHide();
                }
            }

            @Flat
            public void onNo(ActionEvent event) {
                try {
                    UXListener.safeHandle(onNoListener, dialog);
                } finally {
                    dialog.smoothHide();
                }
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
