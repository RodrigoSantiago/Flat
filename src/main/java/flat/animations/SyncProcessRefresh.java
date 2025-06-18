package flat.animations;

import flat.concurrent.SyncProcess;
import flat.uxml.Controller;
import flat.window.Activity;

public class SyncProcessRefresh implements Animation {
    
    private final SyncProcess iterator;
    private final float maxStepDuration;
    private Controller ctrl;
    private Activity activity;
    private Runnable onDone;
    
    public SyncProcessRefresh(Activity activity, SyncProcess syncProcess, float maxStepDuration, Runnable onDone) {
        this.activity = activity;
        this.iterator = syncProcess;
        this.maxStepDuration = maxStepDuration;
        this.onDone = onDone;
    }
    
    public SyncProcessRefresh(Controller ctrl, SyncProcess syncProcess, float maxStepDuration, Runnable onDone) {
        this.ctrl = ctrl;
        this.iterator = syncProcess;
        this.maxStepDuration = maxStepDuration;
        this.onDone = onDone;
    }
    
    @Override
    public Activity getSource() {
        return activity != null ? activity : ctrl != null ? ctrl.getActivity() : null;
    }
    
    @Override
    public boolean isPlaying() {
        return iterator.hasNext();
    }
    
    @Override
    public void handle(float seconds) {
        long now = System.nanoTime();
        while (iterator.hasNext()) {
            if (!iterator.execute()) {
                break;
            }
            if ((System.nanoTime() - now) / 1_000_000_000.0f + seconds > maxStepDuration) {
                break;
            }
        }
    }
    
    @Override
    public void onRemoved() {
        if (onDone != null) {
            onDone.run();
        }
    }
}
