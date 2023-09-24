package test;

import flat.animations.ActivityTransition;
import flat.graphics.SmartContext;

public class TransitionTest extends ActivityTransition {

    float t = 1;
    @Override
    public void handle(long time) {
        super.handle(time);
        t -= time / 2000f;
        t = Math.min(1, t);
    }

    @Override
    public boolean draw(SmartContext context) {
        return super.draw(context);
    }

    @Override
    public boolean isPlaying() {
        return t > 0;
    }
}
