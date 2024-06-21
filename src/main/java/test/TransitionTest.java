package test;

import flat.graphics.SmartContext;
import flat.window.Activity;

public class TransitionTest extends Activity.Transition {

    float t = 1;

    public TransitionTest(Activity next) {
        super(next);
    }

    @Override
    public void handle(float time) {
        super.handle(time);
        t -= time * 0.5f;
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
