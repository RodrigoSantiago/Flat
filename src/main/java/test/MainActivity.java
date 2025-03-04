package test;

import flat.Flat;
import flat.data.ObservableList;
import flat.events.ActionEvent;
import flat.events.TextEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.context.Font;
import flat.graphics.context.Paint;
import flat.graphics.context.paints.GradientStop;
import flat.graphics.context.paints.RadialGradient;
import flat.graphics.image.PixelMap;
import flat.uxml.Controller;
import flat.uxml.ValueChange;
import flat.widget.Widget;
import flat.widget.layout.LinearBox;
import flat.widget.structure.*;
import flat.widget.stages.dialogs.ConfirmDialogBuilder;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.widget.text.TextField;
import flat.widget.value.ProgressBar;
import flat.window.Activity;
import flat.window.Application;
import flat.window.WindowSettings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    public void onDraw(Graphics graphics) {
        super.onDraw(graphics);
        t += 0.01f;
        if (t > 1) t = 1;
        RadialGradient linearGradient = new RadialGradient.Builder(50, 50, 0, 50)
                .focus(25, 25)
                .stop(new GradientStop(0, Color.red))
                .stop(new GradientStop(1, Color.blue))
                .cycleMethod(Paint.CycleMethod.CLAMP)
                .build();
        graphics.setTransform2D(null);
        graphics.setPaint(linearGradient);
        graphics.drawRect(0, 0, 200, 200, true);
        /*graphics.setTransform2D(null);
        graphics.setStroker(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.setColor(0xFF0000FF);
        Path a = new Path();
        a.moveTo(200, 200);
        a.closePath();
        a.moveTo(300, 300);
        a.lineTo(310, 310);
        graphics.drawShape(a, false);*/
        //t += 1 / 120f;
        //if (t > 1) t = 0;
        //graphics.setTransform2D(null);
        //graphics.setTextSize(64);
        //graphics.setTextFont(Font.getDefault());
        //graphics.setColor(Color.black);
        //graphics.setTextBlur(1);
        //graphics.drawText(32, 300, "Ola Mundo");

        /*graphics.setStroker(new BasicStroke(5.5f));
        graphics.setAntialiasEnabled(false);
        graphics.setColor(0x00000080);
        graphics.setAntialiasEnabled(false);
        graphics.drawLine(100, 100, 500, 600);
        graphics.setAntialiasEnabled(true);
        graphics.drawLine(100, 500, 500, 100);*/
        //graphics.setColor(Color.black);
        //graphics.setTextBlur(0);
        //graphics.drawText(32, 200, "Ola Mundo");
        if (save) {
            save = false;
            Font font = Font.getDefault();
            var ctx = graphics.getContext();
            PixelMap pixelMap = font.createImageFromAtlas(graphics.getContext());
            saveImage(pixelMap.getData(), (int) pixelMap.getWidth(), (int) pixelMap.getHeight(), "C:\\Nova\\image-2.png");
        }

    }
    public static void saveImage(byte[] rgbData, int width, int height, String filePath) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int c = Color.rgbaToColor(255, rgbData[index] & 0xFF, rgbData[index] & 0xFF, rgbData[index] & 0xFF);
                image.setRGB(x, y, c);
            }
        }

        try {
            ImageIO.write(image, "png", new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean save = false;

    @Flat ProgressBar progressBar;
    @Flat ProgressBar progressBar2;

    @Flat
    public void export(ActionEvent event) {
        save = true;
    }

    @Flat
    public void setProgress(ActionEvent event) {
        if (progressBar.getValue() >= 0.99f) {
            progressBar.setValue(-0.1f);
        } else {
            progressBar.setValue(progressBar.getValue() + 0.25f);
        }
        if (progressBar2.getValue() >= 0.99f) {
            progressBar2.setValue(-0.1f);
        } else {
            progressBar2.setValue(progressBar2.getValue() + 0.25f);
        }
    }

    @Flat
    public void filter(TextEvent event) {
        event.setText(formatCep(event.getText()));
    }

    @Flat
    public void textCHANGE(ValueChange<String> event) {
        System.out.println("aqui");
    }

    private String formatCep(String text) {
        if (text.matches("^\\d{5}-\\d{3}$")) return text;
        text = text.replaceAll("\\D", "");
        String finalText = text.length() > 0 ? "" + text.charAt(0) : ""; // 64 123 - 555
        if (text.length() > 1) {
            finalText += text.charAt(1);
        }
        if (text.length() > 2) {
            finalText += text.charAt(2);
        }
        if (text.length() > 3) {
            finalText += text.charAt(3);
        }
        if (text.length() > 4) {
            finalText += text.charAt(4);
        }
        if (text.length() > 5) {
            finalText += "-" + text.charAt(5);
        }
        if (text.length() > 6) {
            finalText += text.charAt(6);
        }
        if (text.length() > 7) {
            finalText += text.charAt(7);
        }

        return finalText;
    }

    @Flat public TextField textField;

    @Flat
    public void setmax(ActionEvent event) {
        textField.setMaxCharacters(10);
    }

    @Flat
    public void here(ActionEvent event) {
        System.out.println("Here " + event);
    }
}