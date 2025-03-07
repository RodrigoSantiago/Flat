package test;

import flat.window.Application;
import flat.window.WindowSettings;

public class Main {
    public static void main(String[] args) {
        Application.init();
        Application.launch(new WindowSettings.Builder()
                .layout("/default/screen_test/screen_test.uxml")
                .theme("/default/themes")
                .controller(MainController::new)
                .size(1000, 800)
                .multiSamples(8)
                .transparent(false)
                .build()
        );
    }
}