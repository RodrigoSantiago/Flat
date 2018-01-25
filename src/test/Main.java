package test;

import flat.screen.Application;
import flat.screen.Settings;

public class Main {
    public static void main(String[] args) {
        Application.init(new Settings(MainActivity.class));
    }
}