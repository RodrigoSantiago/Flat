package flat.animations;

import flat.widget.State;
import flat.window.Activity;
import flat.widget.Widget;

public final class StateAnimation implements Animation, StateInfo {

    public final Widget widget;

    float fEnabled, fFocused, fActivated, fHovered, fPressed, fDragged, fError, fDisabled;
    byte tEnabled, tFocused, tActivated, tHovered, tPressed, tDragged, tError, tDisabled;

    float disabledOverlay;
    boolean disabledOverlayed;

    float duration;
    boolean firstTimeAfterPlay;

    public StateAnimation(Widget widget) {
        this.widget = widget;
    }

    @Override
    public Activity getSource() {
        return widget.getActivity();
    }

    @Override
    public boolean isPlaying() {
        return !(fEnabled == tEnabled && fFocused == tFocused && fActivated == tActivated &&
                fHovered == tHovered && fPressed == tPressed && fDragged == tDragged &&
                fError == tError && fDisabled == tDisabled);
    }

    @Override
    public void handle(float time) {
        float pass = time / duration;
        if (!firstTimeAfterPlay) {
            pass = 0;
            firstTimeAfterPlay = true;
        }

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

    public void setDuration(float time) {
        this.duration = time;
    }

    public float getDuration() {
        return duration;
    }

    private void setTargetMasks(int bitmask) {
        tEnabled    = (byte) ((bitmask & (State.ENABLED.bitset())) != 0 ? 1 : 0);
        tFocused    = (byte) ((bitmask & (State.FOCUSED.bitset())) != 0 ? 1 : 0);
        tActivated  = (byte) ((bitmask & (State.ACTIVATED.bitset())) != 0 ? 1 : 0);
        tHovered    = (byte) ((bitmask & (State.HOVERED.bitset())) != 0 ? 1 : 0);
        tPressed    = (byte) ((bitmask & (State.PRESSED.bitset())) != 0 ? 1 : 0);
        tDragged    = (byte) ((bitmask & (State.DRAGGED.bitset())) != 0 ? 1 : 0);
        tError      = (byte) ((bitmask & (State.ERROR.bitset())) != 0 ? 1 : 0);
        tDisabled   = (byte) ((bitmask & (State.DISABLED.bitset())) != 0 ? 1 : 0);
    }

    public void setMasks() {
        fEnabled = tEnabled;
        fFocused = tFocused;
        fActivated = tActivated;
        fHovered = tHovered;
        fPressed = tPressed;
        fDragged = tDragged;
        fError = tError;
        fDisabled = tDisabled;
    }

    public void play(int bitmask) {
        boolean play = isPlaying();
        setTargetMasks(bitmask);

        if (!play && isPlaying()) {
            firstTimeAfterPlay = false;
            Activity activity = widget.getActivity();
            if (activity != null) {
                activity.addAnimation(this);
            }
        }
    }

    public void stop() {
        setMasks();
    }

    public void set(int bitmask) {
        setTargetMasks(bitmask);
        setMasks();
    }

    @Override
    public float get(State stateIndex) {
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
