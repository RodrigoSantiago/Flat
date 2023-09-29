package flat.animations;

import flat.widget.Activity;
import flat.widget.Widget;

public final class StateAnimation implements Animation, StateInfo {

    public final Widget widget;

    float fEnabled, fFocused, fActivated, fHovered, fPressed, fDragged, fError, fDisabled;
    byte tEnabled, tFocused, tActivated, tHovered, tPressed, tDragged, tError, tDisabled;

    float disabledOverlay;
    boolean disabledOverlayed;

    long duration;

    public StateAnimation(Widget widget) {
        this.widget = widget;
    }

    @Override
    public boolean isPlaying() {
        return !(fEnabled == tEnabled && fFocused == tFocused && fActivated == tActivated &&
                fHovered == tHovered && fPressed == tPressed && fDragged == tDragged &&
                fError == tError && fDisabled == tDisabled);
    }

    @Override
    public void handle(long milis) {
        float pass = milis / (float) duration;

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
        tEnabled = (byte) ((bitmask & (1 << ENABLED)) == (1 << ENABLED) ? 1 : 0);
        tFocused = (byte) ((bitmask & (1 << FOCUSED)) == (1 << FOCUSED) ? 1 : 0);
        tActivated = (byte) ((bitmask & (1 << ACTIVATED)) == (1 << ACTIVATED) ? 1 : 0);
        tHovered = (byte) ((bitmask & (1 << HOVERED)) == (1 << HOVERED) ? 1 : 0);
        tPressed = (byte) ((bitmask & (1 << PRESSED)) == (1 << PRESSED) ? 1 : 0);
        tDragged = (byte) ((bitmask & (1 << DRAGGED)) == (1 << DRAGGED) ? 1 : 0);
        tError = (byte) ((bitmask & (1 << ERROR)) == (1 << ERROR) ? 1 : 0);
        tDisabled = (byte) ((bitmask & (1 << DISABLED)) == (1 << DISABLED) ? 1 : 0);
        if (!play && isPlaying()) {
            Activity activity = widget.getActivity();
            if (activity != null) {
                activity.addAnimation(this);
            }
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
            case ENABLED: return fEnabled;
            case FOCUSED: return fFocused;
            case ACTIVATED: return fActivated;
            case HOVERED: return fHovered;
            case PRESSED: return fPressed;
            case DRAGGED: return fDragged;
            case ERROR: return fError;
            default : return disabledOverlayed ? disabledOverlay : fDisabled;
        }
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
