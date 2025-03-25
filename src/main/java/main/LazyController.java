package main;

import flat.Flat;
import flat.uxml.Controller;

public class LazyController extends Controller {
    @Flat
    public void onChangeLocale() {
        System.gc();
        System.out.println("GC");
    }
}
