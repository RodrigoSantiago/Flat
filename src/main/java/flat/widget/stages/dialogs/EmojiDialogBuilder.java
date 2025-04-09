package flat.widget.stages.dialogs;

import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.stages.Dialog;

public class EmojiDialogBuilder {

    private final ResourceStream layoutStream;

    UXTheme theme;
    boolean block;
    UXListener<Dialog> onShowListener;
    UXListener<Dialog> onHideListener;
    UXWidgetValueListener<Dialog, String> onEmojiPick;

    public EmojiDialogBuilder() {
        var stream = new ResourceStream("/default/dialogs/dialog_emojis.uxml");
        if (stream.exists()) {
            layoutStream = stream;
        } else {
            layoutStream = null;
        }
    }

    public EmojiDialogBuilder(String layout) {
        this.layoutStream = new ResourceStream(layout);
    }

    public EmojiDialogBuilder(ResourceStream layoutStream) {
        this.layoutStream = layoutStream;
    }

    public EmojiDialogBuilder theme(UXTheme theme) {
        this.theme = theme;
        return this;
    }

    public EmojiDialogBuilder theme(ResourceStream themeStream) {
        this.theme = UXSheet.parse(themeStream).instance();
        return this;
    }

    public EmojiDialogBuilder theme(String themeStream) {
        this.theme = UXSheet.parse(new ResourceStream(themeStream)).instance();
        return this;
    }

    public EmojiDialogBuilder onShowListener(UXListener<Dialog> onShowListener) {
        this.onShowListener = onShowListener;
        return this;
    }

    public EmojiDialogBuilder onHideListener(UXListener<Dialog> onHideListener) {
        this.onHideListener = onHideListener;
        return this;
    }

    public EmojiDialogBuilder onEmojiPick(UXWidgetValueListener<Dialog, String> onEmojiPick) {
        this.onEmojiPick = onEmojiPick;
        return this;
    }

    public EmojiDialogBuilder block(boolean block) {
        this.block = block;
        return this;
    }

    public Dialog build() {
        final Dialog dialog = new Dialog();
        dialog.setBlockEvents(block);
        if (theme != null) {
            dialog.setTheme(theme);
        }
        dialog.build(layoutStream, new EmojiDialogController(dialog, this));
        return dialog;
    }
}
