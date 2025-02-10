package test;

import flat.uxml.UXBuilder;
import flat.window.Application;

public class Main {
    public static void main(String[] args) {
        UXBuilder.installDefaultWidgets();
        var settings = new Application.Settings(MainActivity::new, 1000, 800, 0, 8, false);
        Application.launch(settings);
    }
}