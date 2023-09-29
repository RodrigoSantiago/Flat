package test;

import flat.widget.Application;

public class Main {
    public static void main(String[] args) {
        var settings = new Application.Settings(MainActivity::new, null, 800, 600);
        Application.launch(settings);
    }
}