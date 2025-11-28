package flat.widget.text;

import flat.animations.Animation;
import flat.animations.StateInfo;
import flat.events.FocusEvent;
import flat.graphics.Graphics;
import flat.graphics.symbols.Font;
import flat.math.Vector2;
import flat.math.shapes.RoundRectangle;
import flat.math.stroke.BasicStroke;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.widget.text.area.TextBox;
import flat.window.Activity;

import java.util.Objects;

public class TextField extends TextArea {

    private String title;
    private int titleColor = 0x000000FF;
    private float titleTransitionDuration;
    private float titleSize = 8f;
    private float titleSpacing;
    private boolean titleLocked;

    private int textDividerColor = 0x000000FF;
    private float textDividerSize = 0f;

    private float titleWidth;
    private boolean invalidTitleSize;
    
    private String textHint;
    private int textHintColor = 0x000000FF;
    
    private float textHintWidth;
    private boolean invalidTextHintSize;

    private final TextBox hintRender = new TextBox();
    private final TextBox titleRender = new TextBox();
    private final TitleToTitleAnimation titleToTitle = new TitleToTitleAnimation();

    public TextField() {
        titleRender.setFont(getTextFont());
        titleRender.setTextSize(getTitleSize());
        hintRender.setFont(getTextFont());
        hintRender.setTextSize(getTextSize());
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setTitle(attrs.getAttributeString("title", getTitle()));
        setTextHint(attrs.getAttributeString("text-hint", getTextHint()));
    }

    @Override
    public void applyLocalization() {
        super.applyLocalization();
        UXAttrs attrs = getAttrs();
        setTitle(attrs.getAttributeLocale("title", getTitle()));
        setTextHint(attrs.getAttributeLocale("text-hint", getTextHint()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setTitleColor(attrs.getColor("title-color", info, getTitleColor()));
        setTitleSpacing(attrs.getSize("title-spacing", info, getTitleSpacing()));
        setTitleSize(attrs.getSize("title-size", info, getTitleSize()));
        setTitleLocked(attrs.getBool("title-locked", info, isTitleLocked()));
        setTitleTransitionDuration(attrs.getNumber("title-transition-duration", info, getTitleTransitionDuration()));
        setTextHintColor(attrs.getColor("text-hint-color", info, getTextHintColor()));

        setTextDividerColor(attrs.getColor("text-divider-color", info, getTextDividerColor()));
        setTextDividerSize(attrs.getSize("text-divider-size", info, getTextDividerSize()));

        if (!titleToTitle.isPlaying()) {
            titleToTitle.setPose(isTitleFloating() ? 1 : 0);
        }
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        if (getHorizontalBar() != null) {
            getHorizontalBar().onMeasure();
        }
        if (getVerticalBar() != null) {
            getVerticalBar().onMeasure();
        }

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        if (wrapWidth) {
            mWidth = Math.max(getNaturalTextWidth() + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            float titleHeight = hasTitle() ? getTitleHeight() + getTitleSpacing() : 0;
            mHeight = Math.max(getTextHeight() + titleHeight + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public Vector2 onLayoutViewDimension(float width, float height) {
        if (isTextEmpty()) return super.onLayoutViewDimension(width, height);

        float titleHeight = hasTitle() ? getTitleHeight() + getTitleSpacing() : 0;
        return new Vector2(getInWidth(), Math.max(0, getInHeight() - titleHeight));
    }

    @Override
    public Vector2 onLayoutTotalDimension(float width, float height) {
        return super.onLayoutTotalDimension(width, height);
    }

    @Override
    public void onDraw(Graphics graphics) {
        if (discardDraw(graphics)) return;

        drawBackground(graphics);
        drawRipple(graphics);

        if (getOutWidth() <= 0 || getOutHeight() <= 0) return;

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        float titleHeight = hasTitle() ? getTitleHeight() + getTitleSpacing() : 0;

        if (isHorizontalDimensionScroll() || isVerticalDimensionScroll()) {
            float off = getPaddingTop() + titleHeight;
            RoundRectangle bg = getBackgroundShape();
            bg.y += off;
            bg.height = bg.height - off;

            if (bg.height > 0) {
                graphics.pushClip(bg);
                onDrawText(graphics, x, y + titleHeight, width, Math.max(0, getInHeight() - titleHeight));
                graphics.popClip();
            }

        } else {
            onDrawText(graphics, x, y + titleHeight, width, Math.max(0, getInHeight() - titleHeight));
        }

        onDrawTitle(graphics, x, y, width, height);
        onDrawTextDivider(graphics, getOutX(), getOutY() + getOutHeight(), getOutWidth(), getTextDividerSize());

        if (getHorizontalBar() != null && isHorizontalVisible()) {
            getHorizontalBar().onDraw(graphics);
        }

        if (getVerticalBar() != null && isVerticalVisible()) {
            getVerticalBar().onDraw(graphics);
        }
    }

    protected void onDrawText(Graphics graphics, float x, float y, float width, float height) {
        if (isTextEmpty() && getTextHint() != null && !getTextHint().isEmpty() && (!hasTitle() || isTitleFloating())) {
            graphics.setTransform2D(getTransform());
            x -= getViewOffsetX();
            y -= getViewOffsetY();
            float xpos = getTextWidth() < width ? xOff(x, x + width, getTextWidth()) : x;
            float ypos = getTextHeight() < height ? yOff(y, y + height, getTextHeight()) : y;

            graphics.setTransform2D(getTransform());
            graphics.setColor(getTextColor());
            graphics.setTextFont(getTextFont());
            graphics.setTextSize(getTextSize());
            graphics.setTextBlur(0);

            float lineH = getLineHeight();
            graphics.setColor(getTextHintColor());
            int start = Math.max(0, (int) Math.floor((getViewOffsetY() - getInY()) / lineH) - 1);
            int end = start + Math.max(0, (int) Math.ceil(getHeight() / lineH) + 2);
            hintRender.drawText(graphics, xpos, ypos, width * 9999, height * 9999, getHorizontalAlign(), start, end);
        }
        super.onDrawText(graphics, x, y, width, height);
    }

    protected void onDrawTitle(Graphics graphics, float x, float y, float width, float height) {
        if (getTitle() != null && width > 0 && height > 0) {
            float anim = titleToTitle.isPlaying() ? titleToTitle.getPose() : isTitleFloating() ? 1 : 0;

            float scale = getTextSize() / getTitleSize();

            float xpos1 = getTitleWidth() < width ? xOff(x, x + width, getTitleWidth()) : x;
            float ypos1 = getInY();

            float xpos2 = getTitleWidth() * scale < width ? xOff(x, x + width, getTitleWidth() * scale) : x;
            float ypos2 = getInY() + (getTitleHeight() + getTitleSpacing()) * 0.5f;

            float xoff = xpos1 * anim + xpos2 * (1 - anim);
            float yoff = ypos1 * anim + ypos2 * (1 - anim);
            float scl = 1 * anim + scale * (1 - anim);

            graphics.setTransform2D(getTransform().preTranslate(xoff, yoff).scale(scl, scl));
            graphics.setColor(getTitleColor());
            graphics.setTextFont(getTextFont());
            graphics.setTextSize(getTitleSize());
            graphics.setTextBlur(0);
            titleRender.drawText(graphics, 0, 0, width / scl, Math.min(getTitleHeight() * scl, height) / scl,
                    getHorizontalAlign(), 0, 1);
        }
    }

    protected void onDrawTextDivider(Graphics graphics, float x, float y, float width, float height) {
        if (width > 0 && height > 0) {
            graphics.setTransform2D(getTransform());
            graphics.setStroke(new BasicStroke(height));
            graphics.setColor(getTextDividerColor());
            graphics.drawLine(x, y, x + width, y);
        }
    }

    @Override
    public void fireFocus(FocusEvent event) {
        super.fireFocus(event);
        invalidateTitleFloating();
    }

    @Override
    protected float getVisibleTextX() {
        return getInX();
    }

    @Override
    protected float getVisibleTextY() {
        float titleHeight = hasTitle() ? getTitleHeight() + getTitleSpacing() : 0;
        return getInY() + titleHeight;
    }

    @Override
    protected float getVisibleTextHeight() {
        float titleHeight = hasTitle() ? getTitleHeight() + getTitleSpacing() : 0;
        return Math.max(0, getInHeight() - titleHeight);
    }

    @Override
    protected float getVisibleTextWidth() {
        return getInWidth();
    }

    public String getTextHint() {
        return textHint;
    }

    public void setTextHint(String textHint) {
        if (!Objects.equals(this.textHint, textHint)) {
            this.textHint = textHint;
            hintRender.setText(textHint);
            invalidateHintSize();
            invalidate(false);
        }
    }

    public int getTextHintColor() {
        return textHintColor;
    }

    public void setTextHintColor(int textHintColor) {
        if (this.textHintColor != textHintColor) {
            this.textHintColor = textHintColor;
            invalidate(false);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (!Objects.equals(this.title, title)) {
            this.title = title;
            titleRender.setText(title);
            invalidateTitleSize();
        }
    }

    public float getTitleSize() {
        return titleSize;
    }

    public void setTitleSize(float titleSize) {
        if (this.titleSize != titleSize) {
            this.titleSize = titleSize;
            titleRender.setTextSize(titleSize);
            invalidateTitleSize();
        }
    }

    public boolean isTitleLocked() {
        return titleLocked;
    }

    public void setTitleLocked(boolean titleLocked) {
        if (this.titleLocked != titleLocked) {
            this.titleLocked = titleLocked;
            invalidateTitleFloating();
            invalidate(false);
        }
    }

    @Override
    public void setTextFont(Font textFont) {
        if (this.getTextFont() != textFont) {
            hintRender.setFont(textFont);
            invalidateHintSize();
            titleRender.setFont(textFont);
            invalidateTitleSize();
            super.setTextFont(textFont);
        }
    }

    @Override
    public void setTextSize(float textSize) {
        if (textSize != getTextSize()) {
            hintRender.setTextSize(textSize);
            invalidateHintSize();
            super.setTextSize(textSize);
        }
    }

    @Override
    protected void invalidateTextSize() {
        super.invalidateTextSize();
        invalidateTitleFloating();
    }

    public int getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(int titleColor) {
        if (this.titleColor != titleColor) {
            this.titleColor = titleColor;
            invalidate(false);
        }
    }

    public float getTitleTransitionDuration() {
        return titleTransitionDuration;
    }

    public void setTitleTransitionDuration(float titleTransitionDuration) {
        if (this.titleTransitionDuration != titleTransitionDuration) {
            this.titleTransitionDuration = titleTransitionDuration;
        }
    }

    public float getTitleSpacing() {
        return titleSpacing;
    }

    public void setTitleSpacing(float titleSpacing) {
        if (this.titleSpacing != titleSpacing) {
            this.titleSpacing = titleSpacing;
            invalidate(false);
        }
    }

    public boolean isTitleFloating() {
        return getTitle() != null && !getTitle().isEmpty() && (isFocused() || !isTextEmpty() || isTitleLocked());
    }

    public int getTextDividerColor() {
        return textDividerColor;
    }

    public void setTextDividerColor(int textDividerColor) {
        if (this.textDividerColor != textDividerColor) {
            this.textDividerColor = textDividerColor;
            invalidate(false);
        }
    }

    public float getTextDividerSize() {
        return textDividerSize;
    }

    public void setTextDividerSize(float textDividerSize) {
        if (this.textDividerSize != textDividerSize) {
            this.textDividerSize = textDividerSize;
            invalidate(false);
        }
    }

    private void invalidateTitleFloating() {
        if (titleTransitionDuration > 0) {
            titleToTitle.play();
        } else {
            titleToTitle.setPose(isTitleFloating() ? 1 : 0);
        }
    }

    private void invalidateTitleSize() {
        invalidTitleSize = true;
        invalidateTitleFloating();
        invalidate(true);
    }

    protected float getTitleWidth() {
        if (invalidTitleSize) {
            invalidTitleSize = false;
            titleWidth = titleRender.getTextWidth();
        }
        return titleWidth;
    }

    private void invalidateHintSize() {
        invalidTextHintSize = true;
        invalidate(true);
    }

    protected float getTextHintWidth() {
        if (invalidTextHintSize) {
            invalidTextHintSize = false;
            textHintWidth = hintRender.getTextWidth();
        }
        return textHintWidth;
    }

    protected boolean hasTitle() {
        return title != null && !title.isEmpty() && titleSize > 0;
    }

    protected float getTitleHeight() {
        return titleRender.getTextHeight();
    }

    protected class TitleToTitleAnimation implements Animation {

        private boolean playing;
        private float pose;

        public void play() {
            if (getActivity() != null) {
                playing = true;
                getActivity().addAnimation(this);
            }
        }

        public void stop() {
            playing = false;
        }

        public void setPose(float pose) {
            this.pose = pose;
        }

        public float getPose() {
            return pose;
        }

        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        public boolean isPlaying() {
            return playing;
        }

        @Override
        public void handle(float seconds) {
            if (isTitleFloating()) {
                if (titleTransitionDuration == 0) {
                    pose = 1;
                } else {
                    pose += seconds / titleTransitionDuration;
                }
                if (pose >= 1) {
                    pose = 1;
                    stop();
                }
            } else {
                if (titleTransitionDuration == 0) {
                    pose = 0;
                } else {
                    pose -= seconds / titleTransitionDuration;
                }
                if (pose <= 0) {
                    pose = 0;
                    stop();
                }
            }
            invalidate(false);
        }
    }
}
