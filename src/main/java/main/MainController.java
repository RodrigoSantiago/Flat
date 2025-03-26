package main;

import flat.Flat;
import flat.animations.Interpolation;
import flat.backend.GL;
import flat.backend.SVG;
import flat.data.ObservableList;
import flat.events.ActionEvent;
import flat.events.KeyEvent;
import flat.events.PointerEvent;
import flat.graphics.Graphics;
import flat.graphics.context.EmojiManager;
import flat.graphics.context.Font;
import flat.graphics.context.ShaderProgram;
import flat.graphics.context.enums.ImageFileFormat;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.graphics.image.LineMap;
import flat.graphics.image.PixelMap;
import flat.math.shapes.Circle;
import flat.math.shapes.Path;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.image.ImageView;
import flat.widget.layout.Drawer;
import flat.widget.layout.LinearBox;
import flat.widget.stages.dialogs.*;
import flat.widget.structure.*;
import flat.widget.text.Button;
import flat.widget.text.Chip;
import flat.widget.text.Label;
import flat.window.Application;
import flat.window.WindowSettings;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainController extends Controller {

    public MainController() {
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
    @Flat public Tab tabTextFields;
    @Flat public Tab tabEmojis;
    @Flat public Tab tabTabs;
    @Flat public Tab tabToolbars;
    @Flat public Tab tabScrolls;
    @Flat public Tab tabImages;
    @Flat public Tab tabLists;
    @Flat public Tab tabMenus;
    @Flat public Tab tabDialogs;

    @Flat public ListView listView1;
    @Flat public ListView listView2;
    @Flat public ListView treeView1;
    @Flat public ListView treeView2;

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
    public void setTabTextFields() {
        mainTab.selectTab(tabTextFields);
        mainDrawer.hide();
    }

    @Flat
    public void setTabEmojis() {
        mainTab.selectTab(tabEmojis);
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
    public void setTabDialogs() {
        mainTab.selectTab(tabDialogs);
        mainDrawer.hide();
    }

    @Flat
    public void setTabLists() {
        mainTab.selectTab(tabLists);
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

    @Flat
    public void onAlertDialog() {
        var alert = new AlertDialogBuilder()
                .title("Alert Dialog")
                .message("This is an alert message")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .block(false)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onBlockAlertDialog() {
        var alert = new AlertDialogBuilder()
                .title("Alert Dialog")
                .message("This is an alert message")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .block(true)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onConfirmDialog() {
        var alert = new ConfirmDialogBuilder()
                .title("This is a confirm Dialog")
                .message("Is this a question?")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onYesListener((dg) -> System.out.println("Yes"))
                .onNoListener((dg) -> System.out.println("No"))
                .block(false)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onBlockConfirmDialog() {
        var alert = new ConfirmDialogBuilder()
                .title("This is a confirm Dialog")
                .message("Is this a question?")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onYesListener((dg) -> System.out.println("Yes"))
                .onNoListener((dg) -> System.out.println("No"))
                .block(true)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onProcessDialog() {
        var alert = new ProcessDialogBuilder()
                .title("This is process Dialog")
                .message("Please cancel this process")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onRequestCancelListener((dg) -> {
                    System.out.println("Cancel");
                    dg.smoothHide();
                })
                .cancelable(true)
                .block(false)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onBlockProcessDialog() {
        var alert = new ProcessDialogBuilder()
                .title("This is process Dialog")
                .message("Please cancel this process")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onRequestCancelListener((dg) -> {
                    System.out.println("Cancel");
                    dg.smoothHide();
                })
                .cancelable(true)
                .block(true)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onChoiceDialog() {
        var alert = new ChoiceDialogBuilder()
                .title("This is a Choice Dialog")
                .message("You can pick a single choice")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onChooseListener((dg, val) -> System.out.println("Choice : " + val))
                .options("Option A", "Option B", "Option C", "Option D", "Option E", "Option F", "Option G", "Option H")
                .initialOption(0)
                .block(false)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onBlockChoiceDialog() {
        var alert = new ChoiceDialogBuilder()
                .title("This is a Choice Dialog")
                .message("You can pick a single choice")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onChooseListener((dg, val) -> System.out.println("Choice : " + val))
                .options("Option A", "Option B", "Option C", "Option D")
                .initialOption(0)
                .block(true)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onMultipleChoicesDialog() {
        var alert = new MultipleChoicesDialogBuilder()
                .title("This is a Multiple Choice Dialog")
                .message("You can pick multiple choices")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onChooseListener((dg, val) -> System.out.println("Choices : " + Arrays.toString(val.toArray())))
                .options("Option A", "Option B", "Option C", "Option D", "Option E", "Option F", "Option G", "Option H")
                .initialOptions("Option A", "Option C")
                .block(false)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onBlockMultipleChoicesDialog() {
        var alert = new MultipleChoicesDialogBuilder()
                .title("This is a Multiple Choice Dialog")
                .message("You can pick multiple choices")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onChooseListener((dg, val) -> System.out.println("Choices : " + Arrays.toString(val.toArray())))
                .options("Option A", "Option B", "Option C", "Option D", "Option E")
                .initialOptions()
                .block(true)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onDatePickerDialog() {
        var alert = new DatePickerDialogBuilder()
                .title("Select a date")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onDatePickListener((dg, value) -> System.out.println(value))
                .block(false)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onBlockDatePickerDialog() {
        var alert = new DatePickerDialogBuilder()
                .title("Select a date")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onDatePickListener((dg, valueStart, valueEnd) -> System.out.println(valueStart + " - " + valueEnd))
                .block(true)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onBlockRangedDatePickerDialog() {
        var alert = new DatePickerDialogBuilder()
                .title("Select a date range")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onDatePickListener((dg, value) -> System.out.println(value))
                .ranged(true)
                .block(true)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onRangedDatePickerDialog() {
        var alert = new DatePickerDialogBuilder()
                .title("Select a date range")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onDatePickListener((dg, valueStart, valueEnd) -> System.out.println(valueStart + " - " + valueEnd))
                .ranged(true)
                .block(false)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onFileSaveDialog() {
        getActivity().getWindow().showSaveFileDialog((file) -> {
            System.out.println(file);
        }, null, "png","jpg,jpeg");
    }

    @Flat
    public void onFileOpenDialog() {
        getActivity().getWindow().showOpenFileDialog((file) -> {
            System.out.println(file);
        }, null, "png","jpg,jpeg");
    }

    @Flat
    public void onFileOpenMultipleDialog() {
        getActivity().getWindow().showOpenMultipleFilesDialog((file) -> {
            System.out.println(file == null ? "null" : Arrays.toString(file));
        }, null, "png","jpg,jpeg");
    }

    @Flat
    public void onFileOpenDirDialog() {
        getActivity().getWindow().showOpenFolderDialog((file) -> {
            System.out.println(file);
        }, null);
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
    public void onCreateWindow(ActionEvent event) {
        Application.createWindow(new WindowSettings.Builder()
                .layout("/default/screen_test/buttons.uxml")
                .theme("/default/themes")
                .controller(LazyController::new)
                .size(1000, 800)
                .multiSamples(8)
                .transparent(false)
                .build());
    }

    @Flat
    public void onKeypress(KeyEvent event) {
        if (event.getType() == KeyEvent.PRESSED) {
            event.getSource().invalidate(true);
            System.out.println("Rquest refresh");
        }
    }

    ShaderProgram shader;
    PixelMap pix;
    Font arial;

    private void setupListView() {
        ObservableList<String> list1 = new ObservableList<>();
        ObservableList<String> list2 = new ObservableList<>();
        for (int i = 0; i < 50; i++) {
            list1.add("List Item " + (i + 1));
            list2.add("List Item " + (i + 1));
        }
        listView1.setAdapter(new ListViewDefaultAdapter<>(list1));
        listView2.setAdapter(new ListViewDefaultAdapter<>(list2));
        ObservableList<TreeCell> list3 = new ObservableList<>();
        ObservableList<TreeCell> list4 = new ObservableList<>();

        Drawable icon = DrawableReader.parse(new ResourceStream("/default/icons/file-outline.svg"));

        TreeCell rootA = new TreeCell(list3, "Tree Item Root", true, icon);
        TreeCell rootB = new TreeCell(list4, "Tree Item Root", true, icon);
        list3.add(rootA);
        list4.add(rootB);

        int aIndex = 1;
        TreeCell child = new TreeCell(list3, "Child", true, icon);
        rootA.add(child);
        for (int i = 0; i < 5; i++) {
            TreeCell child2 = new TreeCell(list3, "Child " + (aIndex++), false, icon);
            child.add(child2);
        }
        TreeCell child3 = new TreeCell(list3, "Child " + (aIndex++), true, icon);
        child.add(child3);
        for (int i = 0; i < 3; i++) {
            TreeCell child2 = new TreeCell(list3, "Other Child With very long Name " + (aIndex++), false, icon);
            child3.add(child2);
        }
        for (int i = 0; i < 3; i++) {
            TreeCell child2 = new TreeCell(list3, "Siblings " + (aIndex++), false, icon);
            rootA.add(child2);
        }
        for (int i = 0; i < 3; i++) {
            TreeCell child2 = new TreeCell(list3, "Root Siblings " + (aIndex++), false, icon);
            list3.add(child2);
        }
        TreeCell child4 = new TreeCell(list3, "Child " + (aIndex++), true, icon);
        list3.add(child4);
        for (int i = 0; i < 3; i++) {
            TreeCell child2 = new TreeCell(list3, "Other Child " + (aIndex++), false, icon);
            child4.add(child2);
        }
        treeView1.setAdapter(new TreeViewAdapter(list3));
        treeView2.setAdapter(new TreeViewAdapter(list4));
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
    public void onShow() {
        setupListView();
        /*Font.installSystemFontFamily("Times New Roman");
        arial = Font.findFont("Times New Roman");
        System.out.println(arial);

        var pm = (PixelMap) DrawableReader.parse(new ResourceStream("/default/image_transp_test.png"));
        var pm2 = (PixelMap) DrawableReader.parse(new ResourceStream("/default/image.png"));
        var graphics = getActivity().getContext().getGraphics();
        var a = pm.getTexture(graphics.getContext());
        var a2 = pm2.getTexture(graphics.getContext());

        shader = graphics.createImageRenderShader(
                """
                #version 330 core
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

        System.out.println("Render to PixelMap " + GL.GetError());*/

        if (tabChips != null) {
            search(tabChips.getFrame());
        }
    }

    @Flat
    public void onChangeLocale() {
        getActivity().setStringBundle("/default/locale/portuguese.uxml");
    }

    @Override
    public void onKeyFilter(KeyEvent keyEvent) {

    }


    PixelMap screen;
    @Override
    public void onDraw(Graphics graphics) {
        super.onDraw(graphics);

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

        /*Font font = Font.getDefault();
        graphics.setTransform2D(null);

        int[] data = new int[4];
        int imageId = (int) SVG.FontPaintGetAtlas(font.getInternalPaintId(), data);
        int w = data[0];
        int h = data[1];
        Texture2D tex = new Texture2D(imageId, w, h, 0, PixelFormat.RED);
        graphics.drawImage(tex, 200, 200, w / 2f, h / 2f);*/
    }

    public static void saveImage(PixelMap pixelMap, String filePath) {
        try {
            Files.write(new File(filePath).toPath(), pixelMap.export(ImageFileFormat.PNG));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveEmojis(ArrayList<int[]> emojis, String filePath) {
        try {
            try (PrintWriter pw = new PrintWriter(filePath)) {
                for (int[] emoji : emojis) {
                    for (int i : emoji) {
                        pw.println(i);
                    }
                }
                pw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Flat
    public void onEmojiLoad(ActionEvent event) {
        ResourceStream unicodes = new ResourceStream("/default/emojis/emojis.txt");
        String[] lines = new String(unicodes.readData(), StandardCharsets.UTF_8).split("\n");
        int[] emojis = new int[lines.length];
        for (int i = 0; i < lines.length; i++) {
            emojis[i] = Integer.parseInt(lines[i].trim());
        }
        for (int i = 0; i < emojis.length; i++) {
            if (emojis[i] != 0) {
                if (i % 6 != 0 && !(emojis[i] >= 0x1F1E6 && emojis[i] <= 0x1F1FF)) {
                    System.out.print("&#x200D;");
                }
                if (i % 6 == 0 && emojis[i] >= 0x1F1E6 && emojis[i] <= 0x1F1FF) {
                    System.out.print(" ");
                }
                System.out.print("&#x" + Integer.toHexString(emojis[i]) + ";");
            }
        }
        System.out.println();
    }

    @Flat
    public void onEmojiExport(ActionEvent event) {
        /*Font font = Font.getDefault();
        PixelMap pixelMap = font.createImageFromAtlas(getActivity().getContext());
        saveImage(pixelMap, "C:\\Nova\\image-3.png");
        System.out.println("SAVED");*/
//        getActivity().getWindow().showOpenFolderDialog((file) -> {
//            try {
//                EmojiManager.renameFlags(file);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }, null);
        getActivity().getWindow().showOpenFolderDialog((file) -> {
            ArrayList<int[]> emojis = new ArrayList<>();
            PixelMap map = EmojiConverter.createFromNotoEmoji(file, emojis);
            saveImage(map, "C:\\Nova\\emojis.png");
            saveEmojis(emojis, "C:\\Nova\\emojis.txt");
        }, null);
    }

    @Flat
    public void toggleDrawer2(ActionEvent event) {
        drawer2.toggle();
    }
}