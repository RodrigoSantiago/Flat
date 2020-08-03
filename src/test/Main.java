package test;

import flat.widget.Application;

import java.awt.geom.Area;

public class Main {
    public static void main(String[] args) {
        Application.Settings settings = new Application.Settings(null);
        settings.vsync = 0;
        settings.multsamples = 4;
        Application.init(settings);
        Application.launch(new MainActivity(0));
    }
}