package flat.model;

import flat.context.FragmentShader;
import flat.context.ShaderProgram;
import flat.context.VertexShader;

import java.util.HashMap;

public class Material {
    ShaderProgram program;
    FragmentShader[] fragmentShaders;
    VertexShader[] vertexShaders;

    String instanceMatrix4;
    int cullFaces;
    int polygonMode;

    HashMap<String, Object> fragmentShaderData;
    HashMap<String, Object> vertexShaderData;
}
