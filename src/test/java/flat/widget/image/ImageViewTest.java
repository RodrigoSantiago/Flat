package flat.widget.image;

import flat.events.ActionEvent;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXListener;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueColor;
import flat.uxml.value.UXValueText;
import flat.widget.Widget;
import flat.widget.enums.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DrawableReader.class})
public class ImageViewTest {

    ResourceStream resImage;
    Drawable image;

    @Before
    public void before() {
        mockStatic(DrawableReader.class);

        image = mock(Drawable.class);
        when(image.getWidth()).thenReturn(16f);
        when(image.getHeight()).thenReturn(20f);

        resImage = mock(ResourceStream.class);

        when(DrawableReader.parse(resImage)).thenReturn(image);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);
        UXListener<ActionEvent> action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        ImageView imageView = new ImageView();
        imageView.setAttributes(createNonDefaultValues(), "imageview");
        imageView.applyAttributes(controller);
        imageView.applyStyle();

        assertEquals(image, imageView.getImage());
        assertEquals(ImageFilter.LINEAR, imageView.getImageFilter());
        assertEquals(ImageScale.FIT, imageView.getImageScale());
        assertEquals(0xFF0000FF, imageView.getColor());
        assertEquals(VerticalAlign.BOTTOM, imageView.getVerticalAlign());
        assertEquals(HorizontalAlign.RIGHT, imageView.getHorizontalAlign());
    }

    @Test
    public void measure() {
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.onMeasure();

        assertEquals(16, imageView.getMeasureWidth(), 0.1f);
        assertEquals(20, imageView.getMeasureHeight(), 0.1f);

        imageView.setMargins(1, 2, 3, 4);
        imageView.setPadding(5, 4, 2, 3);
        imageView.onMeasure();

        assertEquals(16 + 13, imageView.getMeasureWidth(), 0.1f);
        assertEquals(20 + 11, imageView.getMeasureHeight(), 0.1f);

        imageView.setPrefSize(100, 200);
        imageView.onMeasure();

        assertEquals(106, imageView.getMeasureWidth(), 0.1f);
        assertEquals(204, imageView.getMeasureHeight(), 0.1f);

        imageView.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        imageView.onMeasure();

        assertEquals(Widget.MATCH_PARENT, imageView.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, imageView.getMeasureHeight(), 0.1f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxImage = mock(UXValue.class);
        when(uxImage.asResource(any())).thenReturn(resImage);

        hash.put(UXHash.getHash("color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("image"), uxImage);
        hash.put(UXHash.getHash("image-filter"), new UXValueText(ImageFilter.LINEAR.toString()));
        hash.put(UXHash.getHash("image-scale"), new UXValueText(ImageScale.FIT.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(VerticalAlign.BOTTOM.toString()));
        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        return hash;
    }
}