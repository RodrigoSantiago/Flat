package test;

import flat.Flat;
import flat.animations.Interpolation;
import flat.data.ObservableList;
import flat.events.ActionEvent;
import flat.events.TextEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.context.Font;
import flat.graphics.image.PixelMap;
import flat.uxml.Controller;
import flat.uxml.ValueChange;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.layout.Drawer;
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
import java.util.Arrays;
import java.util.List;

public class MainController extends Controller {

    public MainController(Activity activity) {
    }

    @Flat
    public Button button;
    @Flat
    public Label label;
    @Flat
    public LinearBox linear;
    @Flat
    public Button iconButton;
    @Flat
    public ListView listView;
    @Flat
    public Drawer mainDrawer;
    @Flat
    public Drawer drawer2;
    @Flat public Tab mainTab;
    @Flat public Page pageButtons;
    @Flat public Page pageChips;
    @Flat public Page pageForms;
    @Flat public Page pageProgress;
    @Flat public Page pageText;

    private ObservableList<String> items = new ObservableList<>();

    private int num;

    @Flat
    public void toggleDrawer() {
        mainDrawer.toggle();
    }

    @Flat
    public void setPageButtons() {
        mainTab.selectPage(pageButtons);
        mainDrawer.hide();
    }

    @Flat
    public void setPageChips() {
        mainTab.selectPage(pageChips);
        mainDrawer.hide();
    }

    @Flat
    public void setPageForms() {
        mainTab.selectPage(pageForms);
        mainDrawer.hide();
    }

    @Flat
    public void setPageProgress() {
        mainTab.selectPage(pageProgress);
        mainDrawer.hide();
    }

    @Flat
    public void setPageText() {
        mainTab.selectPage(pageText);
        mainDrawer.hide();
    }

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
    @Override
    public void onShow() {
        search(pageChips.getFrame());
    }

    float t = 0;
    float speed = 0.01f;
    @Override
    public void onDraw(Graphics graphics) {
        super.onDraw(graphics);
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
    @Flat
    public void export(ActionEvent event) {
        save = true;
    }
    @Flat
    public void toggleDrawer2(ActionEvent event) {
        drawer2.toggle();
    }
}