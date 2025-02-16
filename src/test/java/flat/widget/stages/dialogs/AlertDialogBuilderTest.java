package flat.widget.stages.dialogs;

import flat.events.ActionEvent;
import flat.resources.ResourceStream;
import flat.uxml.UXBuilder;
import flat.uxml.UXListener;
import flat.uxml.UXNode;
import flat.uxml.UXTheme;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.stages.Dialog;
import flat.widget.text.Label;
import flat.window.Activity;
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
public class AlertDialogBuilderTest {

    @Test
    public void build() {
        Widget root = mock(Widget.class);
        Label title = mock(Label.class);
        Label message = mock(Label.class);
        when(root.findById("title")).thenReturn(title);
        when(root.findById("message")).thenReturn(message);

        ResourceStream stream = mock(ResourceStream.class);
        UXNode node = mock(UXNode.class);
        UXBuilder builder = mock(UXBuilder.class);

        UXTheme theme = mock(UXTheme.class);
        mockStatic(UXNode.class);
        when(UXNode.parse(stream)).thenReturn(node);
        when(node.instance(any())).thenReturn(builder);
        when(builder.build(theme)).thenReturn(root);

        UXListener<Dialog> showListener = mock(UXListener.class);
        UXListener<Dialog> hideListener = mock(UXListener.class);

        Dialog dialog = new AlertDialogBuilder(stream)
                .title("Title")
                .message("Message")
                .theme(theme)
                .onShowListener(showListener)
                .onHideListener(hideListener)
                .build();

        assertEquals(theme, dialog.getTheme());
        assertEquals(1, dialog.getChildrenIterable().size());
        assertEquals(root, dialog.getChildrenIterable().get(0));

        verify(title, times(1)).setText("Title");
        verify(message, times(1)).setText("Message");

        var hide = dialog.getController().getListenerMethod("hide", ActionEvent.class);
        assertNotNull(hide);

        dialog.getController().onShow();
        verify(showListener, times(1)).handle(dialog);

        dialog.getController().onHide();
        verify(hideListener, times(1)).handle(dialog);

        Activity activity = mock(Activity.class);
        when(activity.getScene()).thenReturn(mock(Scene.class));

        dialog.show(activity);
        verify(showListener, times(2)).handle(dialog);
        hide.handle(mock(ActionEvent.class));
        verify(hideListener, times(2)).handle(dialog);
        assertFalse(dialog.isShown());
    }
}