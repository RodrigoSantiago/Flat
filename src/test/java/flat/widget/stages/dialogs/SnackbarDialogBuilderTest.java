package flat.widget.stages.dialogs;

import flat.animations.Animation;
import flat.animations.NormalizedAnimation;
import flat.events.ActionEvent;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.stages.Dialog;
import flat.widget.text.Label;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class})
public class SnackbarDialogBuilderTest {

    @Test
    public void build() {
        Widget root = mock(Widget.class);
        Label message = mock(Label.class);

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
        when(builder.build((UXListener<Widget>) any())).thenAnswer((a) -> {
            ((UXListener)a.getArgument(0)).handle(root);
            controller[0].assign("messageLabel", message);
            return root;
        });

        UXListener<Dialog> showListener = mock(UXListener.class);
        UXListener<Dialog> hideListener = mock(UXListener.class);

        Dialog dialog = new SnackbarDialogBuilder(stream)
                .message("Message")
                .theme(theme)
                .onShowListener(showListener)
                .onHideListener(hideListener)
                .duration(100)
                .build();

        assertEquals(theme, dialog.getTheme());
        assertEquals(1, dialog.getChildrenIterable().size());
        assertEquals(root, dialog.getChildrenIterable().get(0));

        assertNotNull(dialog.getController().getListenerMethod("hide", ActionEvent.class));

        var onOk = dialog.getController().getListenerMethod("onOk", ActionEvent.class);
        assertNotNull(onOk);

        dialog.getController().onShow();
        verify(showListener, times(1)).handle(dialog);

        verify(message, times(1)).setText("Message");
        assertFalse(dialog.isBlockEvents());

        dialog.getController().onHide();
        verify(hideListener, times(1)).handle(dialog);

        ArgumentCaptor<Animation> animationCaptor = ArgumentCaptor.forClass(Animation.class);
        Activity activity = mock(Activity.class);
        Scene scene = new Scene();
        ActivitySupport.setActivity(scene, activity);
        when(activity.getScene()).thenReturn(scene);
        doAnswer(a -> null).when(activity).addAnimation(animationCaptor.capture());
        doAnswer(a -> null).when(activity).removeAnimation(any());

        dialog.show(activity);
        assertTrue(dialog.isShown());
        assertTrue(animationCaptor.getValue().isPlaying());
        verify(showListener, times(2)).handle(dialog);
        onOk.handle(mock(ActionEvent.class));
        verify(hideListener, times(2)).handle(dialog);
        assertFalse(dialog.isShown());
        verify(activity, times(1)).addAnimation(any());
        assertFalse(animationCaptor.getValue().isPlaying());
    }
}