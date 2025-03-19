package flat.widget.stages.dialogs;

import flat.events.ActionEvent;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.Visibility;
import flat.widget.stages.Dialog;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class})
public class ProgressDialogBuilderTest {

    @Test
    public void build() {
        Widget root = mock(Widget.class);
        Label title = mock(Label.class);
        Label message = mock(Label.class);
        Button cancelButton = mock(Button.class);

        ResourceStream stream = mock(ResourceStream.class);
        UXNode node = mock(UXNode.class);
        UXBuilder builder = mock(UXBuilder.class);

        UXTheme theme = mock(UXTheme.class);
        mockStatic(UXNode.class);
        when(UXNode.parse(stream)).thenReturn(node);

        Controller[] controller = new Controller[1];
        when(node.instance(any())).thenAnswer((a) -> {
            controller[0] = a.getArgument(0, Controller.class);
            return builder;
        });
        when(builder.build(theme)).thenAnswer((a) -> {
            controller[0].assign("titleLabel", title);
            controller[0].assign("messageLabel", message);
            controller[0].assign("cancelButton", cancelButton);
            return root;
        });

        UXListener<Dialog> showListener = mock(UXListener.class);
        UXListener<Dialog> hideListener = mock(UXListener.class);
        UXListener<Dialog> onRequestCancelListener = mock(UXListener.class);

        Dialog dialog = new ProcessDialogBuilder(stream)
                .title("Title")
                .message("Message")
                .theme(theme)
                .onShowListener(showListener)
                .onHideListener(hideListener)
                .onRequestCancelListener(onRequestCancelListener)
                .build();

        assertEquals(theme, dialog.getTheme());
        assertEquals(1, dialog.getChildrenIterable().size());
        assertEquals(root, dialog.getChildrenIterable().get(0));

        assertNotNull(dialog.getController().getListenerMethod("hide", ActionEvent.class));
        var requestCancel = dialog.getController().getListenerMethod("onRequestCancel", ActionEvent.class);
        assertNotNull(requestCancel);

        dialog.getController().onShow();
        verify(showListener, times(1)).handle(dialog);

        verify(title, times(1)).setText("Title");
        verify(message, times(1)).setText("Message");
        verify(cancelButton, times(1)).setVisibility(Visibility.GONE);

        dialog.getController().onHide();
        verify(hideListener, times(1)).handle(dialog);

        Activity activity = mock(Activity.class);
        Scene scene = new Scene();
        ActivitySupport.setActivity(scene, activity);
        when(activity.getScene()).thenReturn(scene);

        dialog.show(activity);
        verify(showListener, times(2)).handle(dialog);
        requestCancel.handle(mock(ActionEvent.class));
        verify(hideListener, times(1)).handle(dialog);
        assertTrue(dialog.isShown());
    }
}