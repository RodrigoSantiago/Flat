package test;

import flat.Flat;
import flat.animations.ActivityTransition;
import flat.events.ActionEvent;
import flat.events.DragEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.resources.ResourcesManager;
import flat.uxml.UXTheme;
import flat.widget.Activity;
import flat.resources.ResourceStream;
import flat.widget.Application;
import flat.widget.layout.Drawer;
import flat.widget.layout.LinearBox;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.widget.text.TextArea;

public class MainActivity extends Activity {

    int n;
    @Flat public Button btn1;
    @Flat public LinearBox box;
    @Flat public Label label;

    public MainActivity(int n) {
        this.n = n;
        setTheme(new UXTheme(ResourcesManager.getInput("themes/material.uxss")));
        setSceneStream(new ResourceStream("screen_test"));
    }

    @Override
    public void onLoad() {
        label.setText(""+n);
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
    }

    @Flat
    public void onAdd(ActionEvent event) {
        System.out.println("ADD");
        Application.showDialog(new MainActivity(n+1), new ActivityTransition());
    }

    @Flat
    public void onRemove(ActionEvent event) {
        System.out.println("SUB");
        Application.hideDialog(new ActivityTransition());
    }

    @Flat
    public void onSet(ActionEvent event) {
        System.out.println("SET");
        Application.setActivity(new MainActivity(0),  new ActivityTransition());
    }

    @Override
    public void onShow() {
        super.onShow();
        System.out.println(n+" : Show");
    }

    @Override
    public void onHide() {
        super.onHide();
        System.out.println(n+" : Hide");
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println(n+" : Pause");
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println(n+" : Start");
    }
}
