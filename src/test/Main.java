package test;

import flat.screen.Activity;
import flat.screen.Application;
import flat.screen.Window;

public class Main extends Application {

    @Override
    public void start(Window window) {
        window.setActivity(new Activity());
        window.show();
    }

    public static void main(String[] args) {
        launch(new Main());
    }
}