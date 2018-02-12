package flat.widget.image;

import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Texture2D;
import flat.graphics.context.enuns.MagFilter;
import flat.graphics.context.enuns.MinFilter;
import flat.graphics.context.enuns.PixelFormat;
import flat.graphics.material.image.ImageClipper;
import flat.math.Matrix3;
import flat.math.Vector2;
import flat.math.Vector4;
import flat.widget.Widget;

public class ImageView extends Widget {

    Texture2D texture;
    ImageClipper clipper = new ImageClipper();
    public ImageView() {
        texture = new Texture2D();
        texture.begin(0);
        texture.setSize(2, 2, PixelFormat.RGBA);
        texture.setLevels(0);
        texture.setData(0, new int[] {
                0xFF00FFFF, 0xFF0000FF,
                0xFF00FF00, 0xFFFF00FF
        }, 0, 0, 0, 2, 2);
        texture.setScaleFilters(MagFilter.NEAREST, MinFilter.NEAREST);
        texture.end();

        setPointerListener(event -> {
            if(event.getType() == PointerEvent.PRESSED) {
                 if (event.getPointerID() == 1) p -= 5; else p += 5;
                 invalidate(true);
            }
            return false;
        });
    }
    float p = 25;

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);

        clipper.setPrjClip(new Matrix3().set(
                getTransformView()
                        .translate(getX() + getWidth() / 2f, getY() + getHeight() / 2f)).invert());
        clipper.setRadius(new Vector4(10, 25, 0, 50));
        clipper.setSize(new Vector2(getWidth() / 2f, getHeight() / 2f));

        context.setImageMaterial(clipper);
        context.drawImage(texture, 0, 0, 1, 1,
                getX(), getY(), getX() + getWidth(), getY() + getHeight(), getTransformView());
        context.setImageMaterial(null);
    }
}
