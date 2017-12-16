package test;

import flat.screen.Activity;
import flat.screen.Settings;
import flat.screen.Application;

public class Main {

    public static void main(String[] args) {
        Application.init(new Settings(Activity.class, application -> application.show()));
    }
}