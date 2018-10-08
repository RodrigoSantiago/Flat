package test;

import flat.widget.Application;

public class Main {
    public static void main(String[] args) {
        Application.Settings settings = new Application.Settings(null, MainActivity.class);
        settings.multsamples = 0;
        Application.init(settings);
    }
}