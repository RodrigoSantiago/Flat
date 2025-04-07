package flat.widget.stages.dialogs;

import flat.events.ActionEvent;
import flat.events.FocusEvent;
import flat.graphics.symbols.Font;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.Visibility;
import flat.widget.stages.Dialog;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.widget.text.TextInputField;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDate;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class})
public class DatePickerDialogBuilderTest {

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
        Button cancelButton = mock(Button.class);
        TextInputField textDateIn = mock(TextInputField.class);
        TextInputField textDateOut = mock(TextInputField.class);

        ResourceStream stream = mock(ResourceStream.class);
        UXNode node = mock(UXNode.class);
        UXBuilder builder = mock(UXBuilder.class);

        UXTheme theme = mock(UXTheme.class);
        mockStatic(UXNode.class);
        when(UXNode.parse(stream)).thenReturn(node);

        String[] options = {"Option A", "Option B", "Option C"};

        Controller[] controller = new Controller[1];
        when(node.instance(any())).thenAnswer((a) -> {
            controller[0] = a.getArgument(0, Controller.class);
            return builder;
        });
        when(builder.build(theme)).thenAnswer((a) -> {
            controller[0].assign("titleLabel", title);
            controller[0].assign("cancelButton", cancelButton);
            controller[0].assign("textDateIn", textDateIn);
            controller[0].assign("textDateOut", textDateOut);
            return root;
        });

        UXListener<Dialog> showListener = mock(UXListener.class);
        UXListener<Dialog> hideListener = mock(UXListener.class);
        UXWidgetRangeValueListener<Dialog, LocalDate> rangeListener = mock(UXWidgetRangeValueListener.class);

        LocalDate initialDate = LocalDate.of(2003, 2, 1);

        Dialog dialog = new DatePickerDialogBuilder(stream)
                .title("Title")
                .theme(theme)
                .onShowListener(showListener)
                .onHideListener(hideListener)
                .onDatePickListener(rangeListener)
                .initialDate(initialDate)
                .ranged(true)
                .block(true)
                .cancelable(false)
                .build();

        assertEquals(theme, dialog.getTheme());
        assertEquals(1, dialog.getChildrenIterable().size());
        assertEquals(root, dialog.getChildrenIterable().get(0));
        assertTrue(dialog.isBlockEvents());

        assertNotNull(dialog.getController().getListenerMethod("hide", ActionEvent.class));
        assertNotNull(dialog.getController().getListenerMethod("onCancel", ActionEvent.class));
        var onOk = dialog.getController().getListenerMethod("onOk", ActionEvent.class);
        assertNotNull(onOk);
        var onTextInputFocus = dialog.getController().getListenerMethod("onTextInputFocus", FocusEvent.class);
        assertNotNull(onTextInputFocus);

        dialog.getController().onShow();

        verify(title, times(1)).setText("Title");
        verify(cancelButton, times(1)).setVisibility(Visibility.GONE);
        verify(textDateIn, times(1)).setText("02/01/2003");
        verify(textDateOut, times(1)).setText("02/01/2003");

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
        onOk.handle(mock(ActionEvent.class));
        verify(hideListener, times(2)).handle(dialog);
        assertFalse(dialog.isShown());

        verify(rangeListener, times(1)).handle(dialog, LocalDate.of(2003, 2, 1), LocalDate.of(2003, 2, 1));

        when(textDateIn.getText()).thenReturn("05/04/2006");

        dialog.show(activity);
        assertTrue(dialog.isShown());
        onTextInputFocus.handle(new FocusEvent(textDateIn, null));
        onOk.handle(mock(ActionEvent.class));
        assertFalse(dialog.isShown());

        verify(rangeListener, times(1)).handle(dialog, LocalDate.of(2003, 2, 1), LocalDate.of(2006, 5, 4));
    }
}