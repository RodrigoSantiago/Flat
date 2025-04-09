package main;

import flat.window.Application;
import flat.window.WindowSettings;

public class Main {
    public static void main(String[] args) {
        Application.init();
        Application.setup(new WindowSettings.Builder()
                .layout("/default/screen_test/widgets.uxml")
                .theme("/default/themes/light")
                .stringBundle("/default/locale/english.uxml")
                .controller(MainController::new)
                .size(1280, 940)
                .multiSamples(8)
                .transparent(true)
                .build()
        );
        Application.launch();
    }
}