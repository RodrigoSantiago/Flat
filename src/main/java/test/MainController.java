package test;

import flat.Flat;
import flat.backend.GL;
import flat.backend.SVG;
import flat.data.ObservableList;
import flat.events.ActionEvent;
import flat.events.TextEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.context.enums.CycleMethod;
import flat.graphics.context.Font;
import flat.graphics.context.paints.GaussianShadow;
import flat.graphics.context.paints.ImagePattern;
import flat.graphics.context.paints.LinearGradient;
import flat.graphics.context.paints.RadialGradient;
import flat.graphics.image.DrawableReader;
import flat.graphics.image.PixelMap;
import flat.math.Affine;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.ValueChange;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.layout.LinearBox;
import flat.widget.structure.*;
import flat.widget.stages.dialogs.ConfirmDialogBuilder;
import flat.widget.text.Button;
import flat.widget.text.Chip;
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

public class MainController extends Controller {

    public MainController(Activity activity) {
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

    private void search(Widget widget) {
        if (widget instanceof Parent parent) {
            for (var child : parent.getChildrenIterable()) {
                if (child instanceof Chip chip) {
                    chip.setActionListener((event) -> chip.setActive(!chip.isActive()));
                } else {
                    search(child);
                }
            }
        }
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
                .block(true)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onWindowClick(ActionEvent event) {
        Application.createWindow(new WindowSettings.Builder()
                .layout("/default/screen_test/screen_test.uxml")
                .theme("/default/themes")
                .controller(MainController::new)
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
        search(getActivity().getScene());
        /*listView.setAdapter(new ListViewDefaultAdapter<>(items) {
            @Override
            public void buildListItem(int index, Widget item) {
                var label = (ListItem) item;
                label.setText(items.get(index));
                label.setLayers(index % 6);
            }
        });*/
    }

    @Override
    public void onDraw(Graphics graphics) {
        super.onDraw(graphics);
        /*PixelMap map = (PixelMap) DrawableReader.parse(new ResourceStream("/default/img_test.png"));
        ImagePattern pattern = new ImagePattern.Builder(map.readTexture(graphics.getContext()), 50, 50)
                .cycleMethod(CycleMethod.REFLECT)
                .transform(new Affine().scale(5, 5))
                .build();
        graphics.setTransform2D(null);
        graphics.setPaint(pattern);
        graphics.drawRect(0, 0, 200, 200, true);*/

        /*t += 1;
        RadialGradient linear = new RadialGradient.Builder(50, 50, 100)
                .stop(0, Color.red)
                .stop(1, Color.green)
                .transform(new Affine().rotate(t))
                .build();
        graphics.setTransform2D(null);
        graphics.setPaint(linear);
        graphics.drawRect(0, 0, 200, 200, true);*/

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