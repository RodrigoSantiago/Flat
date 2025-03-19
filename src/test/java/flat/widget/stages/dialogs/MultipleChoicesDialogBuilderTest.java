package flat.widget.stages.dialogs;

import flat.events.ActionEvent;
import flat.graphics.context.Font;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.Visibility;
import flat.widget.layout.LinearBox;
import flat.widget.selection.Checkbox;
import flat.widget.stages.Dialog;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class})
public class MultipleChoicesDialogBuilderTest {

    Font defaultFont;

    @Before
    public void before() {
        mockStatic(Font.class);

        defaultFont = mock(Font.class);
        when(Font.getDefault()).thenReturn(defaultFont);

        when(Font.findFont(any(), any(), any(), any())).thenReturn(defaultFont);
        when(defaultFont.getHeight(anyFloat())).thenReturn(16f);
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(64f);

        when(defaultFont.getWidth(any(), anyInt(), anyInt(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);
    }

    @Test
    public void build() {
        Widget root = mock(Widget.class);
        Label title = mock(Label.class);
        Label message = mock(Label.class);
        Button cancelButton = mock(Button.class);
        LinearBox optionsArea = mock(LinearBox.class);

        ResourceStream stream = mock(ResourceStream.class);
        UXNode node = mock(UXNode.class);
        UXBuilder builder = mock(UXBuilder.class);

        UXTheme theme = mock(UXTheme.class);
        mockStatic(UXNode.class);
        when(UXNode.parse(stream)).thenReturn(node);

        String[] options = {"Option A", "Option B", "Option C"};
        String[] initialOptions = {"Option A"};

        Controller[] controller = new Controller[1];
        when(node.instance(any())).thenAnswer((a) -> {
            controller[0] = a.getArgument(0, Controller.class);
            return builder;
        });
        when(builder.build(theme)).thenAnswer((a) -> {
            controller[0].assign("titleLabel", title);
            controller[0].assign("messageLabel", message);
            controller[0].assign("cancelButton", cancelButton);
            controller[0].assign("optionsArea", optionsArea);
            return root;
        });

        UXListener<Dialog> showListener = mock(UXListener.class);
        UXListener<Dialog> hideListener = mock(UXListener.class);
        UXWidgetValueListener<Dialog, List<String>> chooseListener = mock(UXWidgetValueListener.class);

        Dialog dialog = new MultipleChoicesDialogBuilder(stream)
                .title("Title")
                .message("Message")
                .theme(theme)
                .onShowListener(showListener)
                .onHideListener(hideListener)
                .onChooseListener(chooseListener)
                .initialOptions(initialOptions)
                .options(options)
                .block(true)
                .cancelable(false)
                .build();

        assertEquals(theme, dialog.getTheme());
        assertEquals(1, dialog.getChildrenIterable().size());
        assertEquals(root, dialog.getChildrenIterable().get(0));
        assertTrue(dialog.isBlockEvents());

        assertNotNull(dialog.getController().getListenerMethod("hide", ActionEvent.class));
        assertNotNull(dialog.getController().getListenerMethod("onCancel", ActionEvent.class));

        var onChoose = dialog.getController().getListenerMethod("onChoose", ActionEvent.class);
        assertNotNull(onChoose);

        dialog.getController().onShow();

        verify(title, times(1)).setText("Title");
        verify(message, times(1)).setText("Message");
        verify(cancelButton, times(1)).setVisibility(Visibility.GONE);

        ArgumentCaptor<Widget> linearBoxCap = ArgumentCaptor.forClass(Checkbox.class);
        verify(optionsArea, times(3)).add(linearBoxCap.capture());

        Checkbox lastCheckbox = (Checkbox) linearBoxCap.getValue().getChildrenIterable().get(0);

        dialog.getController().onHide();

        verify(showListener, times(1)).handle(dialog);
        verify(hideListener, times(1)).handle(dialog);

        Activity activity = mock(Activity.class);
        Scene scene = new Scene();
        ActivitySupport.setActivity(scene, activity);
        when(activity.getScene()).thenReturn(scene);

        dialog.show(activity);
        assertTrue(dialog.isShown());
        verify(showListener, times(2)).handle(dialog);
        onChoose.handle(mock(ActionEvent.class));
        verify(hideListener, times(2)).handle(dialog);
        assertFalse(dialog.isShown());

        ArgumentCaptor<List<String>> paramCaptor = ArgumentCaptor.forClass(List.class);
        verify(chooseListener, times(1)).handle(any(), paramCaptor.capture());

        List<String> expected1 = paramCaptor.getValue();
        assertEquals(1, expected1.size());
        assertEquals("Option A", expected1.get(0));

        dialog.show(activity);
        lastCheckbox.setActivated(true);
        onChoose.handle(mock(ActionEvent.class));

        ArgumentCaptor<List<String>> paramCaptor2 = ArgumentCaptor.forClass(List.class);
        verify(chooseListener, times(2)).handle(any(), paramCaptor2.capture());

        List<String> expected2 = paramCaptor2.getValue();
        assertEquals(2, expected2.size());
        assertEquals("Option A", expected2.get(0));
        assertEquals("Option C", expected2.get(1));
    }
}