package test;

import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.screen.Activity;
import flat.widget.layout.*;
import flat.widget.text.Label;

public class MainActivity extends Activity {

    VBox vBox = new VBox();
    public MainActivity() {
        vBox.setBackgroundColor(0x000000FF);

        Label a = new Label();
        a.setText("ola mundo");
        a.setMargins(10, 10, 10, 10);
        a.setPadding(10, 10, 10, 10);
        a.setBackgroundColor(0xFF0FFFFF);

        Box b = new Box();
        b.setPrefSize(100, 100);
        b.setBackgroundColor(0xaF00FFAF);
        Box c = new Box();
        c.setPrefSize(100, 100);
        c.setBackgroundColor(0xFa00FFAF);
        vBox.add(a, b, c);
        vBox.setPadding(10, 10, 10, 10);
        vBox.setMaxWidth(300);
        getScene().add(vBox);
        vBox.setPointerListener(event -> {
            if (event.getType() == PointerEvent.PRESSED || event.getType() == PointerEvent.DRAGGED) {
                int d = event.getPointerID() == 1 ? 1 : -1;
                vBox.setMargins(
                        vBox.getMarginTop() + d,
                        vBox.getMarginTop() + d,
                        vBox.getMarginTop() + d,
                        vBox.getMarginTop() + d);
            }
            return false;
        });
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
    }
}
