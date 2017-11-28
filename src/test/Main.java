package test;

import flat.acess.GL;
import flat.acess.WL;
import flat.screen.Window;
import flat.widget.Scene;

public class Main extends Window {
    @Override
    public void start(Scene scene) {

    }

    private static boolean firstView;
    public static void main(String[] args) {
        WL.load();
        GL.load();

        if (!WL.Init(50, 50, 600, 400, true, true)) {
            System.out.println("Não foi possível iniciar um contexto gráfico");
            System.exit(0);
        }

        WL.SetFramebufferSizeCallback((int width, int height) -> {
            GL.SetViewport(0, 0, width, height);
        });
        WL.SetWindowAfterEventsCallback(() -> {
            GL.SetClearColor(0xFF0000FF);
            GL.Clear(0x00004000);
        });

        WL.Show();
        WL.Loop();
        WL.Terminate();
    }
}
