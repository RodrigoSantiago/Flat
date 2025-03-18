package main;

import flat.Flat;
import flat.animations.Interpolation;
import flat.backend.GL;
import flat.backend.SVG;
import flat.data.ObservableList;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.Surface;
import flat.graphics.context.Context;
import flat.graphics.context.Font;
import flat.graphics.context.ShaderProgram;
import flat.graphics.context.enums.ImageFileFormat;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.image.DrawableReader;
import flat.graphics.image.LineMap;
import flat.graphics.image.PixelMap;
import flat.math.Affine;
import flat.math.Vector4;
import flat.math.shapes.Circle;
import flat.math.shapes.Ellipse;
import flat.math.shapes.Path;
import flat.math.shapes.Rectangle;
import flat.math.stroke.BasicStroke;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.image.ImageView;
import flat.widget.layout.Drawer;
import flat.widget.layout.LinearBox;
import flat.widget.structure.*;
import flat.widget.stages.dialogs.ConfirmDialogBuilder;
import flat.widget.text.Button;
import flat.widget.text.Chip;
import flat.widget.text.Label;
import flat.window.Activity;
import flat.window.Application;
import flat.window.WindowSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
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
    @Flat public TabView mainTab;
    @Flat public Tab tabButtons;
    @Flat public Tab tabChips;
    @Flat public Tab tabForms;
    @Flat public Tab tabProgress;
    @Flat public Tab tabText;
    @Flat public Tab tabTabs;
    @Flat public Tab tabToolbars;
    @Flat public Tab tabScrolls;
    @Flat public Tab tabImages;
    @Flat public Tab tabMenus;

    private ObservableList<String> items = new ObservableList<>();

    private int num;

    @Flat
    public void toggleDrawer() {
        mainDrawer.toggle();
    }

    @Flat
    public void setTabButtons() {
        mainTab.selectTab(tabButtons);
        mainDrawer.hide();
    }

    @Flat
    public void setTabChips() {
        mainTab.selectTab(tabChips);
        mainDrawer.hide();
    }

    @Flat
    public void setTabForms() {
        mainTab.selectTab(tabForms);
        mainDrawer.hide();
    }

    @Flat
    public void setTabProgress() {
        mainTab.selectTab(tabProgress);
        mainDrawer.hide();
    }

    @Flat
    public void setTabText() {
        mainTab.selectTab(tabText);
        mainDrawer.hide();
    }

    @Flat
    public void setTabTabs() {
        mainTab.selectTab(tabTabs);
        mainDrawer.hide();
    }

    @Flat
    public void setTabToolbars() {
        mainTab.selectTab(tabToolbars);
        mainDrawer.hide();
    }

    @Flat
    public void setTabScrolls() {
        mainTab.selectTab(tabScrolls);
        mainDrawer.hide();
    }

    @Flat
    public void setTabImages() {
        mainTab.selectTab(tabImages);
        mainDrawer.hide();
    }

    @Flat
    public void setTabMenus() {
        mainTab.selectTab(tabMenus);
        mainDrawer.hide();
    }

    @Flat
    public void setThemeLight() {
        t = 1;
        getActivity().setTheme("/default/themes/light");
    }

    @Flat
    public void setThemeDark() {
        t = 1;
        getActivity().setTheme("/default/themes/dark");
    }

    @Flat
    public void linearAction(ActionEvent actionEvent) {
        List<Tab> list = new ArrayList<>();
        new TabView().addTab(list);
        getActivity().getWindow().setIcon((PixelMap) iconButton.getIcon());
    }

    private void search(Widget widget) {
        if (widget instanceof Parent parent) {
            for (var child : parent.getChildrenIterable()) {
                if (child instanceof Chip chip) {
                    chip.setActionListener((event) -> chip.setActivated(!chip.isActivated()));
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

    ShaderProgram shader;
    PixelMap pix;

    @Override
    public void onShow() {
        var pm = (PixelMap) DrawableReader.parse(new ResourceStream("/default/image_transp_test.png"));
        var pm2 = (PixelMap) DrawableReader.parse(new ResourceStream("/default/image.png"));
        var graphics = getActivity().getContext().getGraphics();
        var a = pm.getTexture(graphics.getContext());
        var a2 = pm2.getTexture(graphics.getContext());

        shader = graphics.createImageRenderShader(
                """
                uniform vec2 view;
                uniform float col[4];
                uniform sampler2D texture1;
                uniform sampler2D texture2;
                vec4 fragment(vec2 pos, vec2 uv) {
                    return texture(texture1, uv) * texture(texture2, uv) * vec4(col[0], col[1], col[2], col[3]);
                }
                """);
        System.out.println("Init Shader " + GL.GetError());

        shader.set("col", new float[]{1, 1, 1, 1});
        shader.set("texture1", 0);
        shader.set("texture2", 1);

        graphics.pushClip(new Ellipse(10, 10, 40, 40));
        Surface surface = new Surface(graphics.getContext(), 64, 64, 8, PixelFormat.RGBA);
        graphics.setSurface(surface);
        //graphics.clear(0, 0, 0x00);
        //graphics.clearClip();
        graphics.blitCustomShader(shader, a, a2);
        graphics.setColor(Color.red);
        graphics.setTransform2D(null);
        graphics.setAntialiasEnabled(false);
        graphics.setStroke(new BasicStroke(1));
        graphics.drawEllipse(0, 0, 64, 64, true);
        graphics.setSurface(null);
        graphics.popClip();

        pix = surface.createPixelMap();

        System.out.println("Render to PixelMap " + GL.GetError());


        if (tabChips != null) {
            search(tabChips.getFrame());
        }
    }

    @Flat
    public void optimize(PointerEvent pointer) {
        if (pointer.getPointerID() == 1 && pointer.getType() == PointerEvent.PRESSED) {
            if (pointer.getSource() instanceof ImageView view) {
                LineMap lineMap = (LineMap) view.getImage();
                lineMap.optimize();
            }
        }
    }


    boolean debug = false;
    @Flat
    public void toggleDebugMode() {
        debug = !debug;
        GL.SetDebug(debug);
        SVG.SetDebug(debug);
    }

    float t = 0;
    float av = 0;

    @Override
    public void onDraw(Graphics graphics) {
        super.onDraw(graphics);

        graphics.setTransform2D(null);
        graphics.setColor(Color.white);
        graphics.drawRect(0, getActivity().getHeight() - 16, 52, 16, true);
        graphics.setColor(Color.red);
        if (av == 0) {
            av = Application.getLoopTime();
        } else {
            av = (av * 0.8f + Application.getLoopTime() * 0.2f);
        }
        graphics.drawText(10, getActivity().getHeight() - 16, "FPS : " + (int)(1f / av));

        /*graphics.setTransform2D(null);

        var clips = graphics.getClipState();
        graphics.pushClip(new Ellipse(100, 100, 200, 200));
        var box1 = clips.clipBox.get(clips.clipBox.size() - 1);
        graphics.pushClip(new Ellipse(150, 150, 200, 200));
        var box2 = clips.clipBox.get(clips.clipBox.size() - 1);
        graphics.clearClip();

        graphics.setStroke(new BasicStroke(2));
        graphics.setColor(Color.blue);
        graphics.drawShape(new Ellipse(100, 100, 200, 200), false);
        graphics.drawShape(new Ellipse(150, 150, 200, 200), false);
        graphics.setStroke(new BasicStroke(1));
        graphics.setColor(Color.red);
        graphics.drawRect(box1, false);
        graphics.drawRect(box2, false);
        graphics.drawImage(pix, 100, 100, 640, 640);*/

        if (t > 0) {
            if (t == 1) {
                screen = getActivity().getContext().getGraphics().createPixelMap();
            }
            int w = (int) getActivity().getWidth();
            int h = (int) getActivity().getHeight();
            graphics.setTransform2D(null);

            Path inverse = new Path();
            inverse.moveTo(0, 0);
            inverse.lineTo(w, 0);
            inverse.lineTo(w, h);
            inverse.lineTo(0, h);
            inverse.closePath();
            inverse.append(new Circle(w, 0, (float) Math.sqrt(w * w + h * h) * Interpolation.exp5.apply(1 - t)), false);

            graphics.pushClip(inverse);
            graphics.drawImage(screen, 0, 0, getActivity().getWidth(), getActivity().getHeight());
            graphics.popClip();
            t -= Application.getLoopTime();
            getActivity().invalidateWidget(getActivity().getScene());
        }
        if (save) {
            save = false;
            Font font = Font.getDefault();
            var ctx = graphics.getContext();
            PixelMap pixelMap = font.createImageFromAtlas(graphics.getContext());
            saveImage(pixelMap, "C:\\Nova\\image-3.png");
            System.out.println("SAVED");
        }
    }

    public static void saveImage(PixelMap pixelMap, String filePath) {
        try {
            Files.write(new File(filePath).toPath(), pixelMap.export(ImageFileFormat.PNG));
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

    PixelMap screen;
}