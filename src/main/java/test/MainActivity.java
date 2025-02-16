package test;

import flat.Flat;
import flat.events.ActionEvent;
import flat.graphics.SmartContext;
import flat.math.Affine;
import flat.uxml.Controller;
import flat.uxml.ValueChange;
import flat.widget.layout.LinearBox;
import flat.widget.stages.Dialog;
import flat.widget.stages.dialogs.AlertDialogBuilder;
import flat.widget.stages.dialogs.ConfirmDialogBuilder;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.window.Activity;
import flat.window.Application;
import flat.window.WindowSettings;

public class MainActivity extends Controller {

    public MainActivity(Activity activity) {
        super(activity);

        activity.setTheme("default/themes");
        activity.setLayoutBuilder("default/screen_test/screen_test.uxml", this);
    }

    @Flat public Button button;
    @Flat public Label label;
    @Flat public LinearBox linear;

    private int num;

    @Flat
    public void linearAction(ActionEvent actionEvent) {
        linear.setPrefWidth(linear.getPrefWidth() + 5);
    }

    @Flat
    public void onButtonClick(ActionEvent actionEvent) {
        Activity activity = getActivity();
        /*var alert = new AlertDialogBuilder("/default/screen_test/dialog_test.uxml")
                .title("This is THE Title")
                .message("This is THE Message")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .build();
        alert.show(getActivity());*/
        var alert = new ConfirmDialogBuilder("/default/screen_test/dialog_confirm.uxml")
                .title("This is THE Title")
                .message("This is THE Message")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onYesListener((dg) -> System.out.println("Yes"))
                .onNoListener((dg) -> System.out.println("No"))
                .build();
        alert.show(getActivity());

        /*var dialog = new Dialog();
        dialog.setId("dialog");
        dialog.build("/default/screen_test/dialog_test.uxml", new Controller(getActivity()) {
            @Flat
            public void resize(ActionEvent actionEvent) {
                dialog.getChildrenIterable().get(0).setPrefWidth(dialog.getChildrenIterable().get(0).getPrefWidth() + 15);
            }
            @Flat
            public void bring(ActionEvent actionEvent) {
                System.out.println("ola");
                var dialog2 = new Dialog();
                dialog2.setId("dialog" + (++num));
                dialog2.build("/default/screen_test/dialog_test.uxml", null);
                dialog2.show(activity);
            }
        });
        dialog.show(activity);*/
    }

    @Flat
    public void doTheWork(ValueChange<Integer> change) {
        System.out.println("From : " + change.getOldValue() + ", To : " + change.getValue());
    }

    @Flat
    public void slideBar(ActionEvent event) {
        System.out.println("Slide");
    }

    @Flat
    public void slideChange(ValueChange<Float> change) {
        System.out.println("From : " + change.getOldValue() + ", To : " + change.getValue());
    }

    @Override
    public void onShow() {
    }

    float t;
    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
        //t += 1 / 120f;
        //if (t > 1) t = 0;
        //context.setTransform2D(null);
        //context.setTextSize(64);
        //context.setTextFont(Font.getDefault());
        //context.setColor(Color.black);
        //context.setTextBlur(1);
        //context.drawText(32, 300, "Ola Mundo");

        /*context.setStroker(new BasicStroke(5.5f));
        context.setAntialiasEnabled(false);
        context.setColor(0x00000080);
        context.setAntialiasEnabled(false);
        context.drawLine(100, 100, 500, 600);
        context.setAntialiasEnabled(true);
        context.drawLine(100, 500, 500, 100);*/
        //context.setColor(Color.black);
        //context.setTextBlur(0);
        //context.drawText(32, 200, "Ola Mundo");
    }

    @Flat
    public void onAction(ActionEvent event) {
        System.out.println("action");
        Application.createWindow(new WindowSettings.Builder()
                .layout("/default/screen_test/screen_test.uxml")
                .theme("/default/themes")
                .controller(MainActivity::new)
                .size(1000, 800)
                .multiSamples(8)
                .transparent(false)
                .build());
    }
}