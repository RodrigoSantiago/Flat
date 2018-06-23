package flat.widget.selection;

import flat.animations.Animation;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.math.Affine;
import flat.math.operations.Area;
import flat.math.shapes.*;
import flat.uxml.Controller;
import flat.resources.SVGParser;
import flat.uxml.UXAttributes;
import flat.uxml.UXChildren;
import flat.resources.Dimension;

import java.util.Objects;

public class Checkbox extends ToogleWidget {

    private static Shape defIcon = new SVGParser().parse("M10,17L5,12L6.41,10.58L10,14.17L17.59,6.58L19,8", 0);
    private static Rectangle defBound = new Rectangle(0, 0, 24, 24);

    private Shape icon;

    private int onColor, offColor;

    // Checkbox animations
    private AnimShowHide animation = new AnimShowHide();
    private float bgSize, chSize;
    private Path bgPath = new Path(Path.WIND_EVEN_ODD);
    private RoundRectangle outter = new RoundRectangle();
    private Rectangle inner = new Rectangle();
    private Area icPath;
    private int bgColor;

    private Affine affine = new Affine();

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);

        float dp24 = Dimension.dpPx(20);
        setPrefSize(attributes.asNumber("width", dp24), (attributes.asNumber("height", dp24)));
        setOnColor(attributes.asColor("onColor", 0x000000FF));
        setOffColor(attributes.asColor("offColor", 0x808080FF));

        setIcon(attributes.asShape("icon", defIcon), attributes.asBounds("iconBounds", defBound));

        bgColor = getOffColor();
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);
    }

    @Override
    public void onSelected(boolean selected) {
        super.onSelected(selected);
        animation.show = selected;
        float p = 0;
        if (animation.isPlaying()) {
            p = 1 - animation.getPosition();
        }
        animation.stop();
        animation.play(p);
        invalidate(false);
    }


    public Shape getIcon() {
        return icon;
    }

    public void setIcon(Shape icon) {
        setIcon(icon, null);
    }

    public void setIcon(Shape icon, Rectangle bounds) {
        if (!Objects.equals(icon, this.icon)) {
            if (icon == null) {
                this.icon = null;
                icPath = new Area();
            } else {
                this.icon = icon;
                this.icPath = new Area(icon);
                if (bounds == null) {
                    bounds = icPath.bounds();
                }
                if (!bounds.isEmpty()) {
                    float s = Math.min(1/bounds.width, 1/bounds.height);
                    icPath.transform(affine.identity()
                                    .scale(s, s)
                                    .translate(-(bounds.x + bounds.width / 2f), -(bounds.y + bounds.height / 2f))
                            );
                }
            }
            invalidate(false);
        }
    }

    public int getOnColor() {
        return onColor;
    }

    public void setOnColor(int onColor) {
        if (this.onColor != onColor) {
            this.onColor = onColor;
            invalidate(false);
        }
    }

    public int getOffColor() {
        return offColor;
    }

    public void setOffColor(int offColor) {
        if (this.offColor != offColor) {
            this.offColor = offColor;
            invalidate(false);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        float s = 2 * (width / 20);
        float a = 2 * (width / 20);
        context.setTransform2D(getTransformView());
        context.setColor(bgColor);

        outter.set(x, y, width, height, a, a, a, a);

        bgPath.reset();

        bgPath.append(outter, false);
        bgPath.reverse();

        if (chSize <= 0) {
            float iw = (width - s - s) / 2f * bgSize;
            float ih = (height - s - s) / 2f * bgSize;
            inner.set(x + s + iw, y + s + ih, width - (s + iw) * 2, height - (s + ih) * 2);
            bgPath.append(inner, false);
        } else {
            bgPath.append(icPath.pathIterator(affine.identity()
                    .translate((x + width) / 2, (y + height) / 2)
                    .scale(chSize * width, chSize * height)), false);
        }
        bgPath.reverse();
        bgPath.closePath();

        context.drawShapeOptimized(bgPath, true);

        if (isRippleEffectEnabled() && getRipple().isVisible()) {
            context.setTransform2D(getTransformView());
            getRipple().drawRipple(context, null, getRippleColor());
            context.setTransform2D(null);
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isRippleEffectEnabled()) {
            getRipple().fire(getInX() + getInWidth() / 2f, getInY() + getInHeight() / 2f);
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            toogle();
        }
        super.firePointer(pointerEvent);
    }

    private class AnimShowHide extends Animation {
        boolean show, _show;

        AnimShowHide() {
            setDuration(300);
        }

        @Override
        protected void evaluate() {
            super.evaluate();
            if (isStopped()) {
                _show = show;
            }
        }

        @Override
        protected void compute(float t) {
            if (_show) {
                if (t < 0.5f) {
                    chSize = 0f;
                    bgSize = (t * 2);
                } else {
                    bgSize = 1f;
                    chSize = ((t - 0.5f) * 2);
                }
                bgColor = mixColor(getOffColor(), getOnColor(), t);
            } else {
                if (t < 0.5f) {
                    bgSize = 1f;
                    chSize = 1f - (t * 2);
                } else {
                    chSize = 0;
                    bgSize = 1f - ((t - 0.5f) * 2);
                }
                bgColor = mixColor(getOnColor(), getOffColor(), t);
            }
            invalidate(false);
        }
    }
}
