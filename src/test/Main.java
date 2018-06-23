package test;

import flat.widget.Application;
import flat.widget.Settings;

public class Main {
    public static void main(String[] args) {
        Settings settings = new Settings(null, MainActivity.class);
        settings.multsamples = 0;
        Application.init(settings);
    }
}