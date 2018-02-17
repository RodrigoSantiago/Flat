package test;

import flat.application.Application;
import flat.application.Settings;

public class Main {
    public static void main(String[] args) {
        Settings settings = new Settings(null, MainActivity.class);
        settings.multsamples = 0;
        Application.init(settings);
    }
}