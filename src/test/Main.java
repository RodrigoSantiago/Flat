package test;

import flat.backend.GL;
import flat.backend.WL;
import flat.screen.Window;
import flat.widget.Scene;

import static flat.backend.GLEnuns.*;

public class Main extends Window {

    @Override
    public void start(Scene scene) {

    }

    public static String vertexShaderSource =
            "#version 330 core\n" +
            "layout (location = 0) in vec3 aPos;\n" +
            "void main() {\n" +
            "   gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);\n" +
            "}";

    public static String fragmentShaderSource =
            "#version 330 core\n" +
            "out vec4 FragColor;\n" +
            "void main() {\n" +
            "   FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);\n" +
            "}";


    static final float vertices[] = {
            -0.5f, -0.5f, 0.0f, // left
            0.5f, -0.5f, 0.0f, // right
            0.0f, 0.5f, 0.0f  // top
    };

    public static void main(String[] args) {
        WL.load();
        GL.load();

        if (!WL.Init(50, 50, 600, 400, true, true)) {
            System.out.println("Não foi possível iniciar um contexto gráfico");
            System.exit(0);
        }

        int vertexShader = GL.ShaderCreate(ST_VERTEX_SHADER);
        GL.ShaderSetSource(vertexShader, vertexShaderSource);
        GL.ShaderCompile(vertexShader);

        if (!GL.ShaderIsCompiled(vertexShader)) {
            System.out.println("VertexShader compilation failed : " + GL.ShaderGetLog(vertexShader));
        }

        int fragmentShader = GL.ShaderCreate(ST_FRAGMENT_SHADER);
        GL.ShaderSetSource(fragmentShader, fragmentShaderSource);
        GL.ShaderCompile(fragmentShader);

        if (!GL.ShaderIsCompiled(fragmentShader)) {
            System.out.println("FragmentShader compilation failed : " + GL.ShaderGetLog(fragmentShader));
        }

        int shaderProgram = GL.ProgramCreate();
        GL.ProgramAttachShader(shaderProgram, vertexShader);
        GL.ProgramAttachShader(shaderProgram, fragmentShader);
        GL.ProgramLink(shaderProgram);

        if (!GL.ProgramIsLinked(shaderProgram)) {
            System.out.println("Shader program link failed : " + GL.ShaderGetLog(shaderProgram));
        }
        GL.ShaderDestroy(vertexShader);
        GL.ShaderDestroy(fragmentShader);

        int VAO = GL.VertexArrayCreate();
        int VBO = GL.BufferCreate();

        GL.VertexArrayBind(VAO);
        GL.BufferBind(BB_ARRAY_BUFFER, VBO);
        GL.BufferDataF(BB_ARRAY_BUFFER, vertices, 0, vertices.length, UT_STATIC_DRAW);

        GL.VertexArrayAttribEnable(0, true);
        GL.VertexArrayAttribPointer(0, 3, false, 3 * (4), DT_FLOAT, 0);

        GL.BufferBind(BB_ARRAY_BUFFER, 0);
        GL.VertexArrayBind(0);

        WL.SetFramebufferSizeCallback((int width, int height) -> {
            System.out.println("ha !");
            GL.SetViewport(0, 0, width, height);
        });

        WL.Show();

        while (!WL.IsClosed()) {
            GL.SetClearColor(0xFF0000FF);
            GL.Clear(CB_COLOR_BUFFER_BIT);

            GL.ProgramUse(shaderProgram);
            GL.VertexArrayBind(VAO);
            GL.DrawArrays(VM_TRIANGLES, 0, 3, 1);

            WL.SwapBuffers();
            WL.HandleEvents();
        }

        GL.VertexArrayDestroy(VAO);
        GL.BufferDestroy(VBO);

        WL.Terminate();
    }
}