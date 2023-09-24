package flat.graphics.context;

import flat.backend.GL;
import flat.graphics.context.enuns.ShaderType;
import flat.widget.Application;

public final class Shader extends ContextObject {

    private int shaderId;
    private ShaderType type;
    private String source;
    private boolean compiled;
    private String log;

    public Shader(ShaderType type) {
        this(type, null);
    }

    public Shader(ShaderType type, String source) {
        this.source = source;
        this.type = type;
        init();
    }

    @Override
    protected void onInitialize() {
        this.shaderId = GL.ShaderCreate(type.getInternalEnum());
    }

    @Override
    protected void onDispose() {
        final int shaderId = this.shaderId;
        Application.runSync(() -> GL.ShaderDestroy(shaderId));
    }

    int getInternalID() {
        return shaderId;
    }

    public void setSource(String source) {
        if (compiled) {
            throw new RuntimeException("A compiled shader is immutable");
        }
        this.source = source;
    }

    public String getSource()  {
        return source;
    }

    public boolean compile() {
        if (!compiled) {
            GL.ShaderSetSource(shaderId, source);
            GL.ShaderCompile(shaderId);
            compiled = GL.ShaderIsCompiled(shaderId);
            if (!compiled) {
                log = GL.ShaderGetLog(shaderId);
            } else {
                log = null;
            }
        }
        return compiled;
    }

    public boolean isCompiled() {
        return compiled;
    }

    public String getLog() {
        return log;
    }

}
