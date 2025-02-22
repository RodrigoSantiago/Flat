package test;

import flat.Flat;
import flat.data.ObservableList;
import flat.events.ActionEvent;
import flat.graphics.SmartContext;
import flat.graphics.image.PixelMap;
import flat.uxml.Controller;
import flat.uxml.ValueChange;
import flat.widget.Widget;
import flat.widget.layout.LinearBox;
import flat.widget.structure.*;
import flat.widget.stages.dialogs.ConfirmDialogBuilder;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.window.Activity;
import flat.window.Application;
import flat.window.WindowSettings;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Controller {

    public MainActivity(Activity activity) {
    }

    @Flat public Button button;
    @Flat public Label label;
    @Flat public LinearBox linear;
    @Flat public Button iconButton;
    @Flat public ListView listView;

    private ObservableList<String> items = new ObservableList<>();

    private int num;

    @Flat
    public void linearAction(ActionEvent actionEvent) {
        List<Page> list = new ArrayList<>();
        new Tab().addPage(list);
        getActivity().getWindow().setIcon((PixelMap) iconButton.getIcon());
    }

    @Flat
    public void onDialogClick(ActionEvent actionEvent) {
        var alert = new ConfirmDialogBuilder("/default/screen_test/dialog_confirm.uxml")
                .title("This is THE Title")
                .message("This is THE Message")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onYesListener((dg) -> System.out.println("Yes"))
                .onNoListener((dg) -> System.out.println("No"))
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onWindowClick(ActionEvent event) {
        Application.createWindow(new WindowSettings.Builder()
                .layout("/default/screen_test/screen_test.uxml")
                .theme("/default/themes")
                .controller(MainActivity::new)
                .size(1000, 800)
                .multiSamples(8)
                .transparent(false)
                .build());
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

    @Flat
    public void onAddItem(ActionEvent event) {
        items.add("New " + items.size());
    }

    @Flat
    public void onRemoveItem(ActionEvent event) {
        if (items.size() > 0) items.remove(items.size() / 2);
    }

    @Override
    public void onShow() {
        listView.setAdapter(new ListViewDefaultAdapter<>(items) {
            @Override
            public void buildListItem(int index, Widget item) {
                var label = (ListItem) item;
                label.setText(items.get(index));
                label.setLayers(index % 6);
            }
        });
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
}