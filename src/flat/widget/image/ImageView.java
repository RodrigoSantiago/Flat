package flat.widget.image;

import flat.graphics.SmartContext;
import flat.graphics.image.Image;
import flat.graphics.image.ImageVector;
import flat.graphics.text.Align;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;
import flat.uxml.Controller;
import flat.uxml.SVGParser;
import flat.uxml.UXAttributes;
import flat.widget.Widget;

import java.util.HashMap;

public class ImageView extends Widget {

    public static final int NOONE = 0;
    public static final int STRETCH = 1;
    public static final int FIT = 2;
    public static final int CROP = 3;

    protected static final HashMap<String, Integer> scaleTypes = UXAttributes.atts(
            "NOONE", NOONE,
            "STRETCH", STRETCH,
            "FIT", FIT,
            "CROP", CROP
    );

    private Image image;
    private int scaleType;

    private float frame;
    private float speed;
    private long lastTime;

    private Align.Vertical verticalAlign;
    private Align.Horizontal horizontalAlign;

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);

        setScaleType(attributes.asConstant("scaleType", scaleTypes, NOONE));
        setVerticalAlign(attributes.asEnum("verticalAlign", Align.Vertical.class, Align.Vertical.TOP));
        setHorizontalAlign(attributes.asEnum("horizontalAlign", Align.Horizontal.class, Align.Horizontal.LEFT));

        setFrame(attributes.asNumber("frame", 0));
        setSpeed(attributes.asNumber("speed", 1));

        String drawable = attributes.get("drawable");
        if (drawable != null) {
            if (drawable.startsWith("img:")) {
                String imgPath = drawable.substring(3);
                // todo - load img path
            } else if (drawable.startsWith("svg:")) {
                Rectangle rect = null;
                Shape path = null;
                int iBox = drawable.indexOf("viewBox=");
                int iPath = drawable.indexOf("path=");
                if (iBox > -1) {
                    int end = drawable.indexOf(";", iBox);
                    if (end == -1) end = drawable.length();
                    String[] data = drawable.substring(iBox + 8, end).split(" ");
                    try {
                        rect = new Rectangle(
                                Float.parseFloat(data[0]),
                                Float.parseFloat(data[1]),
                                Float.parseFloat(data[2]),
                                Float.parseFloat(data[3]));
                    } catch (Exception ignored) {
                    }
                }
                if (iPath > -1) {
                    int end = drawable.indexOf(";", iPath);
                    if (end == -1) end = drawable.length();
                    try {
                        SVGParser parser = new SVGParser();
                        path = parser.parse(drawable.substring(iPath + 5, end), 0);
                    } catch (Exception ignored) {
                        System.out.println(drawable);
                    }
                }
                if (path != null) {
                    //setImage(new ImageVector(path, rect));
                }
            } else {
                // invalid
            }
        }
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        invalidate(false);
    }

    public void setDrawable(Image image, float speed) {
        this.image = image;
        this.speed = speed;
        invalidate(false);
    }

    public float getFrame() {
        return frame;
    }

    public void setFrame(float frame) {
        this.frame = Math.max(Math.min(1, frame), 0);
        lastTime = 0;
        invalidate(false);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        invalidate(false);
    }

    public void redraw() {
        invalidate(false);
    }

    public int getScaleType() {
        return scaleType;
    }

    public void setScaleType(int scaleType) {
        this.scaleType = scaleType;
    }

    public Align.Vertical getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(Align.Vertical verticalAlign) {
        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(true);
        }
    }

    public Align.Horizontal getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(Align.Horizontal horizontalAlign) {
        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(true);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);

        long now = System.currentTimeMillis();
        if (lastTime != 0) {
            frame += ((now - lastTime) / 1000f) * speed;
        }
        lastTime = now;
        frame = frame < 0 ? 1 - Math.abs(frame % 1f) : frame % 1f;

        if (this.image != null) {
            float dW = image.getWidth();
            float dH = image.getHeight();

            if (dW > 0 && dH > 0) {
                final float x = getInX();
                final float y = getInY();
                float width = getInWidth();
                float height = getInHeight();
                if (scaleType == FIT) {
                    if (dW > dH) {
                        dH = (dH / dW) * height;
                        dW = width;
                    } else {
                        dW = (dW / dH) * width;
                        dH = height;
                    }
                } else if (scaleType == CROP) {
                    if (dW > dH) {
                        dW = (dW / dH) * width;
                        dH = height;
                    } else {
                        dH = (dH / dW) * height;
                        dW = width;
                    }
                } else if (scaleType == STRETCH) {
                    dW = width;
                    dH = height;
                }

                context.setTransform2D(getTransformView());
                image.draw(context, xOff(x, x + width, dW), yOff(y, y + height, dH), dW, dH, frame);
                context.setTransform2D(null);
            }

            if (image.isDynamic()) {
                invalidate(false);
            }
        }
    }

    protected float xOff(float start, float end, float width) {
        if (end < start) return (start + end) / 2f;
        if (horizontalAlign == Align.Horizontal.RIGHT) return end - width;
        if (horizontalAlign == Align.Horizontal.CENTER) return (start + end - width) / 2f;
        return start;
    }

    protected float yOff(float start, float end, float height) {
        if (end < start) return (start + end) / 2f;
        if (verticalAlign == Align.Vertical.BOTTOM || verticalAlign == Align.Vertical.BASELINE) return end - height;
        if (verticalAlign == Align.Vertical.MIDDLE) return (start + end - height) / 2f;
        return start;
    }
}
