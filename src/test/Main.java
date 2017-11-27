package test;

import flat.acess.WL;
import flat.screen.Window;
import flat.widget.Scene;

public class Main extends Window {
    @Override
    public void start(Scene scene) {

    }

    public static void main(String[] args) {
        WL.load();

        if (!WL.Init()) {
            System.out.println("Não foi possível criar uma janela");
        }

        long window = WL.Create(50, 50, 600, 400, true, true);
        WL.Show(window);

        long window2 = WL.Create(50, 50, 600, 400, true, true);
        WL.Show(window2);

        if (!WL.Loop()) {
            System.out.println("Não foi possível iniciar um contexto gráfico");
        }

        WL.Destroy(window);
        WL.Destroy(window2);
        WL.Terminate();
    }
}
