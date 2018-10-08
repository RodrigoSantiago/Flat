package flat.animations;

import flat.uxml.UXStyle;
import flat.widget.Application;
import flat.widget.Widget;

public class StateAnimation implements Animation, StateInfo {

    public final Widget widget;

    float fEnabled, fFocused, fActivated, fHovered, fPressed, fDragged, fError, fDisabled;
    byte tEnabled, tFocused, tActivated, tHovered, tPressed, tDragged, tError, tDisabled;

    float disabledOverlay;
    boolean disabledOverlayed;

    long lastTime;
    long duration;

    public StateAnimation(Widget widget) {
        this.widget = widget;
    }

    @Override
    public boolean isPlaying() {
        return (fEnabled == tEnabled && fFocused == tFocused && fActivated == tActivated &&
                fHovered == tHovered && fPressed == tPressed && fDragged == tDragged &&
                fError == tError && fDisabled == tDisabled);
    }

    @Override
    public void handle(long milis) {
        float pass = (milis - lastTime) / (float) duration;

        if (tEnabled == 0) fEnabled = Math.max(0, fEnabled - pass);
        else fEnabled = Math.min(1, fEnabled + pass);
        if (tFocused == 0) fFocused = Math.max(0, fFocused - pass);
        else fFocused = Math.min(1, fFocused + pass);
        if (tActivated == 0) fActivated = Math.max(0, fActivated - pass);
        else fActivated = Math.min(1, fActivated + pass);
        if (tHovered == 0) fHovered = Math.max(0, fHovered - pass);
        else fHovered = Math.min(1, fHovered + pass);
        if (tPressed == 0) fPressed = Math.max(0, fPressed - pass);
        else fPressed = Math.min(1, fPressed + pass);
        if (tDragged == 0) fDragged = Math.max(0, fDragged - pass);
        else fDragged = Math.min(1, fDragged + pass);
        if (tError == 0) fError = Math.max(0, fError - pass);
        else fError = Math.min(1, fError + pass);
        if (tDisabled == 0) fDisabled = Math.max(0, fDisabled - pass);
        else fDisabled = Math.min(1, fDisabled + pass);

        lastTime = milis;

        widget.applyStyle();
    }

    public void setDuration(long milis) {
        this.duration = milis;
    }

    public long getDuration() {
        return duration;
    }

    public void play(int bitmask) {
        boolean play = isPlaying();
        tEnabled = (byte) ((bitmask & UXStyle.ENABLED) << UXStyle.ENABLED);
        tFocused = (byte) ((bitmask & UXStyle.FOCUSED) << UXStyle.FOCUSED);
        tActivated = (byte) ((bitmask & UXStyle.ACTIVATED) << UXStyle.ACTIVATED);
        tHovered = (byte) ((bitmask & UXStyle.HOVERED) << UXStyle.HOVERED);
        tPressed = (byte) ((bitmask & UXStyle.PRESSED) << UXStyle.PRESSED);
        tDragged = (byte) ((bitmask & UXStyle.DRAGGED) << UXStyle.DRAGGED);
        tError = (byte) ((bitmask & UXStyle.ERROR) << UXStyle.ERROR);
        tDisabled = (byte) ((bitmask & UXStyle.DISABLED) << UXStyle.DISABLED);
        if (!play && isPlaying()) {
            Application.runAnimation(this);
            lastTime = System.currentTimeMillis();
        }
    }

    public void stop() {
        fEnabled = tEnabled;
        fFocused = tFocused;
        fActivated = tActivated;
        fHovered = tHovered;
        fPressed = tPressed;
        fDragged = tDragged;
        fError = tError;
        fDisabled = tDisabled;
    }

    public void set(int bitmask) {
        play(bitmask);
        stop();
    }

    @Override
    public float get(int stateIndex) {
        switch (stateIndex) {
            case UXStyle.ENABLED : return fEnabled;
            case UXStyle.FOCUSED  : return fFocused;
            case UXStyle.ACTIVATED  : return fActivated;
            case UXStyle.HOVERED  : return fHovered;
            case UXStyle.PRESSED  : return fPressed;
            case UXStyle.DRAGGED  : return fDragged;
            case UXStyle.ERROR  : return fError;
            default : return disabledOverlayed ? disabledOverlay : fDisabled;
        }
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    public void setDisabledOverlay(float disable) {
        disabledOverlayed = true;
        disabledOverlay = disable;
    }
    public void unsetDisabledOverlay() {
        disabledOverlayed = false;
    }

    public boolean isDisabledOverlay() {
        return disabledOverlayed;
    }

    public float getDisabled() {
        return disabledOverlayed ? disabledOverlay : fDisabled;
    }
}
