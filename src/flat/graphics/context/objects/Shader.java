package flat.graphics.context.objects;

import flat.graphics.context.ContextObject;

public class Shader extends ContextObject {

    public Shader() {

    }

    public Shader(String source) {

    }

    public boolean link() {
        return false;
    }

    public boolean isLinked() {
        return false;
    }

    public String getSource() {
        return null;
    }

    public void setSource(String source) {

    }

    public String getLog() {
        return null;
    }

    @Override
    protected void onDispose() {

    }
}
