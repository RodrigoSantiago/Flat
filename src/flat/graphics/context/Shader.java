package flat.graphics.context;

import flat.backend.GL;
import flat.graphics.context.enuns.ShaderType;

public class Shader extends ContextObject {

    private int shaderId;
    private ShaderType type;
    private String source;
    private boolean compiled;
    private String log;

    public Shader(ShaderType type) {
        super();
        this.type = type;
    }

    @Override
    protected void onInitialize() {
        Context.getContext();

        final int shaderId = GL.ShaderCreate(type.getInternalEnum());

        setDispose(() -> GL.ShaderDestroy(shaderId));

        this.shaderId = shaderId;
    }

    int getInternalID() {
        init();
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
            init();
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
