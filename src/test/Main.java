package test;

import flat.widget.Application;

public class Main {
    public static void main(String[] args) {
        Application.Settings settings = new Application.Settings(null);
        settings.vsync = 0;
        settings.multsamples = 4;
        Application.init(settings);
        Application.launch(new MainActivity());
    }
}