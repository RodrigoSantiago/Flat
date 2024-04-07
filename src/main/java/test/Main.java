package test;

import flat.window.Application;

public class Main {
    public static void main(String[] args) {
        var settings = new Application.Settings(Main.class, MainActivity::new, 800, 600);
        Application.launch(settings);
    }
}