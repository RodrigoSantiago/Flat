package flat.graphics.context;

import flat.backend.GL;
import flat.exception.FlatException;
import flat.graphics.context.enums.ShaderType;

public final class Shader extends ContextObject {

    private final int shaderId;
    private final ShaderType type;

    private String source;
    private boolean compiled;
    private String log;

    public Shader(Context context, ShaderType type) {
        this(context, type, null);
    }

    public Shader(Context context, ShaderType type, String source) {
        super(context);
        this.type = type;
        this.source = source;

        final int shaderId = GL.ShaderCreate(type.getInternalEnum());
        if (shaderId == 0) {
            throw new FlatException("Unable to create a new OpenGL Shader");
        }

        this.shaderId = shaderId;
        assignDispose(() -> GL.ShaderDestroy(shaderId));
    }

    int getInternalId() {
        return shaderId;
    }

    public void setSource(String source) {
        if (compiled) {
            throw new FlatException("A compiled shader is immutable");
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

    public ShaderType getType() {
        return type;
    }

    public String getLog() {
        return log;
    }
}
