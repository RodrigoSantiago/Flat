package flat.widget.value;

import flat.animations.Animation;
import flat.animations.Interpolation;
import flat.animations.StateInfo;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.math.stroke.BasicStroke;
import flat.uxml.Controller;
import flat.uxml.TaskList;
import flat.uxml.UXAttrs;
import flat.widget.Widget;
import flat.widget.enums.ProgressLineMode;
import flat.window.Activity;

public class ProgressBar extends Widget {

    private float lineWidth = 4f;
    private int lineColor = Color.white;
    private int lineFilledColor = Color.black;
    private ProgressLineMode lineMode = ProgressLineMode.REGULAR;
    private float value;
    private float smoothTransitionDuration;
    private float animationDuration = 2f;

    protected float visibleValue;

    protected final AutoSlideAnimation slideAnimation = new AutoSlideAnimation();
    protected final IndeterminateAnimation indeterminateAnimation = new IndeterminateAnimation();

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setValue(attrs.getAttributeNumber("value", getValue()));
        setSmoothTransitionDuration(attrs.getAttributeNumber("smooth-transition-duration", getSmoothTransitionDuration()));
        setAnimationDuration(attrs.getAttributeNumber("animation-duration", getAnimationDuration()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setLineColor(attrs.getColor("line-color", info, getLineColor()));
        setLineFilledColor(attrs.getColor("line-filled-color", info, getLineFilledColor()));
        setLineWidth(attrs.getSize("line-width", info, getLineWidth()));
        setLineMode(attrs.getConstant("line-mode", info, getLineMode()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        if (wrapWidth) {
            mWidth = Math.max(extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(extraHeight + getLineWidth(), getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);
        drawRipple(graphics);

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        float lineWidth = Math.min(getLineWidth(), Math.min(width, height));
        graphics.setTransform2D(getTransform());
        graphics.setStroker(new BasicStroke(lineWidth, getLineMode() == ProgressLineMode.REGULAR ? 0 : 1, 0));

        float lineRadius = lineWidth * 0.5f;

        float yPos = y + height * 0.5f;
        if (value < 0 && indeterminateAnimation.isPlaying()) {
            float t1 = indeterminateAnimation.getT1() * width;
            float t2 = indeterminateAnimation.getT2() * width;

            float x0 = x;
            float x1 = x + width - t1;
            float x2 = x + width - t2;
            float x3 = x + width;

            if (getLineMode() == ProgressLineMode.REGULAR) {
                graphics.setColor(getLineColor());
                graphics.drawLine(x0, yPos, x3, yPos);

                graphics.setColor(getLineFilledColor());
                graphics.drawLine(x1, y + height * 0.5f, x2, y + height * 0.5f);
            } else if (getLineMode() == ProgressLineMode.ROUND) {
                float secx1 = x0 + Math.min(lineRadius, width * 0.5f);
                float secx2 = Math.max(x3 - lineRadius, secx1);
                graphics.setColor(getLineColor());
                graphics.drawLine(secx1, yPos, secx2, yPos);

                float mainx1 = x1 + Math.min(lineRadius, width * 0.5f);
                float mainx2 = Math.max(x2 - lineRadius, mainx1);
                graphics.setColor(getLineFilledColor());
                graphics.drawLine(mainx1, yPos, mainx2, yPos);
            } else {
                float mainx1 = x1 + Math.min(lineRadius, width * 0.5f);
                float mainx2 = Math.max(x2 - lineRadius, mainx1);
                graphics.setColor(getLineFilledColor());
                graphics.drawLine(mainx1, yPos, mainx2, yPos);

                float secx1 = Math.min(x + width, x0 + lineRadius);
                float secx2 = Math.max(x, x1 - lineRadius * 2f);
                if (secx1 < secx2) {
                    graphics.setColor(getLineColor());
                    graphics.drawLine(secx1, yPos, secx2, yPos);
                }

                float secx3 = Math.min(x + width, x2 + lineRadius * 2f);
                float secx4 = Math.max(x, x3 - lineRadius);
                if (secx3 < secx4) {
                    graphics.setColor(getLineColor());
                    graphics.drawLine(secx3, yPos, secx4, yPos);
                }

            }
        } else {
            float x1 = x;
            float x2 = x + width * visibleValue;
            float x3 = x + width;

            if (getLineMode() == ProgressLineMode.REGULAR) {
                graphics.setColor(getLineColor());
                graphics.drawLine(x1, yPos, x3, yPos);

                if (getValue() > 0) {
                    graphics.setColor(getLineFilledColor());
                    graphics.drawLine(x1, yPos, x2, yPos);
                }

            } else if (getLineMode() == ProgressLineMode.ROUND) {

                float mainx1 = Math.min(x1 + lineRadius, x + width * 0.5f);
                float secx2 = Math.max(x3 - lineRadius, mainx1);
                graphics.setColor(getLineColor());
                graphics.drawLine(mainx1, yPos, secx2, yPos);

                if (getValue() > 0) {
                    float mainx2 = Math.max(x2 - lineRadius, mainx1);
                    graphics.setColor(getLineFilledColor());
                    if (mainx1 == mainx2) {
                        graphics.drawEllipse(x - lineRadius, yPos - lineRadius, lineRadius * 2, lineRadius * 2, true);
                    } else {
                        graphics.drawLine(mainx1, yPos, mainx2, yPos);
                    }
                }

            } else {
                float mainx1 = Math.min(x1 + lineRadius, x + width * 0.5f);
                float mainx2 = Math.max(x2 - lineRadius, mainx1);
                if (getValue() > 0) {
                    graphics.setColor(getLineFilledColor());
                    if (mainx1 == mainx2) {
                        graphics.drawEllipse(x, yPos - lineRadius, lineRadius * 2, lineRadius * 2, true);
                    } else {
                        graphics.drawLine(mainx1, yPos, mainx2, yPos);
                    }
                }

                float secx1 = x2 + lineRadius * 2f;
                float secx2 = x3 - lineRadius;
                if (secx1 < secx2) {
                    graphics.setColor(getLineColor());
                    graphics.drawLine(secx1, yPos, secx2, yPos);
                }
            }
        }
    }

    @Override
    protected void onActivityChange(Activity prev, Activity current, TaskList tasks) {
        super.onActivityChange(prev, current, tasks);
        if (prev == null && current != null) {
            if (getAnimationDuration() > 0 && getValue() < 0) {
                tasks.add(indeterminateAnimation::play);
            }
        }
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        if (this.lineWidth == lineWidth) {
            this.lineWidth = lineWidth;
            invalidate(isWrapContent());
        }
    }

    public ProgressLineMode getLineMode() {
        return lineMode;
    }

    public void setLineMode(ProgressLineMode progressLineMode) {
        if (progressLineMode == null) progressLineMode = ProgressLineMode.REGULAR;

        if (this.lineMode != progressLineMode) {
            this.lineMode = progressLineMode;
            invalidate(false);
        }
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        if (this.lineColor != lineColor) {
            this.lineColor = lineColor;
            invalidate(false);
        }
    }

    public int getLineFilledColor() {
        return lineFilledColor;
    }

    public void setLineFilledColor(int lineFilledColor) {
        if (this.lineFilledColor != lineFilledColor) {
            this.lineFilledColor = lineFilledColor;
            invalidate(false);
        }
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        value = Math.min(1, value);

        if (this.value != value) {
            this.value = value;
            if (value < 0) {
                if (getAnimationDuration() > 0) {
                    indeterminateAnimation.play();
                }
                visibleValue = 0;
            } else if (getSmoothTransitionDuration() <= 0 || getActivity() == null) {
                visibleValue = value;
            } else {
                slideAnimation.play();
            }
            invalidate(false);
        }
    }

    public float getSmoothTransitionDuration() {
        return smoothTransitionDuration;
    }

    public void setSmoothTransitionDuration(float smoothTransitionDuration) {
        if (this.smoothTransitionDuration != smoothTransitionDuration) {
            this.smoothTransitionDuration = smoothTransitionDuration;
            if (smoothTransitionDuration > 0) {
                slideAnimation.play();
            }
            invalidate(false);
        }
    }

    public float getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(float animationDuration) {
        if (this.animationDuration != animationDuration) {
            this.animationDuration = animationDuration;
            if (animationDuration > 0 && value < 0) {
                indeterminateAnimation.play();
            }
            invalidate(false);
        }
    }

    protected class IndeterminateAnimation implements Animation {

        private boolean playing;
        private float t1;
        private float t2;
        private float t3;

        public void play() {
            playing = true;
            if (getActivity() != null) {
                getActivity().addAnimation(this);
            }
        }

        public float getT1() {
            return Interpolation.exp5Out.apply(t1);
        }

        public float getT2() {
            return Interpolation.exp5In.apply(t2);
        }

        public float getT3() {
            return t3;
        }

        @Override
        public boolean isPlaying() {
            return playing;
        }

        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        public void handle(float seconds) {
            if (animationDuration <= 0 || value >= 0) {
                t3 = 0;
                t2 = 0;
                t1 = 0;
                playing = false;
            } else {
                if (isDisabled()) {
                    if (t1 != 0.5f || t2 != 0.5f || t3 != 0.5f) {
                        t1 = 0.5f;
                        t2 = 0.5f;
                        t3 = 0;
                        invalidate(false);
                    }
                } else {
                    t1 = Math.min(1, t1 + seconds / animationDuration);
                    t2 = Math.min(1, t2 + seconds / animationDuration);
                    t3 += seconds / animationDuration;
                    if (t2 >= 1) {
                        t2 = 0;
                        t1 = 0;
                    }
                    invalidate(false);
                }
            }
        }
    }

    protected class AutoSlideAnimation implements Animation {

        private boolean playing;

        public void play() {
            playing = true;
            if (getActivity() != null) {
                getActivity().addAnimation(this);
            }
        }

        @Override
        public boolean isPlaying() {
            return playing;
        }

        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        public void handle(float seconds) {
            if (value >= 0 && smoothTransitionDuration > 0 && !isDisabled()) {
                if (visibleValue > value) {
                    visibleValue = Math.max(value, visibleValue - seconds / smoothTransitionDuration);
                } else if (visibleValue < value) {
                    visibleValue = Math.min(value, visibleValue + seconds / smoothTransitionDuration);
                }
                if (visibleValue == value) {
                    playing = false;
                }
            } else {
                visibleValue = Math.min(1, Math.max(value, 0));
                playing = false;
            }
            invalidate(false);
        }
    }
}
