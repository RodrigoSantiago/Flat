package main;

import flat.Flat;
import flat.animations.Interpolation;
import flat.backend.GL;
import flat.backend.SVG;
import flat.backend.WL;
import flat.concurrent.ProgressTask;
import flat.data.ObservableList;
import flat.events.*;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.Surface;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.image.ImageData;
import flat.graphics.symbols.Font;
import flat.graphics.context.ShaderProgram;
import flat.graphics.context.enums.AlphaComposite;
import flat.graphics.context.enums.ImageFileFormat;
import flat.graphics.image.PixelMap;
import flat.math.*;
import flat.math.shapes.Circle;
import flat.math.shapes.Ellipse;
import flat.math.shapes.Path;
import flat.math.stroke.BasicStroke;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.image.ImageView;
import flat.widget.layout.Drawer;
import flat.widget.layout.LinearBox;
import flat.widget.stages.dialogs.*;
import flat.widget.structure.*;
import flat.widget.text.*;
import flat.window.Application;
import flat.window.WindowSettings;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

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
    @Flat public Tab tabDefault;
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
    @Flat public Tab tabEffects;

    @Flat public ListView listView1;
    @Flat public ListView listView2;
    @Flat public ListView listView3;
    @Flat public TreeView treeView1;
    @Flat public TreeView treeView2;
    @Flat public TreeView treeView3;

    @Flat public Label statusLabel;

    private ObservableList<String> items = new ObservableList<>();

    private int num;


    @Flat public TextArea simple;
    @Flat
    public void onHidden(KeyEvent keyEvent) {
        if (keyEvent.getType() == KeyEvent.RELEASED && keyEvent.getKeycode() == KeyCode.KEY_LEFT_SHIFT) {
            simple.setFollowStyleProperty("hidden", false);
            simple.setHidden(!simple.isHidden());
        }
    }

    @Flat
    public void toggleDrawer() {
        mainDrawer.toggle();
    }

    @Flat
    public void setTabDefault() {
        mainTab.selectTab(tabDefault);
        mainDrawer.hide();
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
    public void setTabEffects() {
        mainTab.selectTab(tabEffects);
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
    public void onSnackDialog() {
        var alert = new SnackbarDialogBuilder()
                .message("This is an alert message")
                .duration(5f)
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .build();
        alert.show(getActivity());
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
        ProgressTask<Integer> task = new ProgressTask<>(getWindow(), (report) -> {
            for (int i = 0; i < 100; i++) {
                if (report.isRequestCancel()) {
                    break;
                }
                report.setProgress(i / 99f);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
            return 0;
        });
        new Thread(task).start();
        var alert = new ProcessDialogBuilder()
                .title("This is process Dialog")
                .message("Please cancel this process")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onRequestCancelListener((dg) -> {
                    System.out.println("Request the cancel!!!");
                })
                .task(task)
                .cancelable(true)
                .block(false)
                .build();
        alert.show(getActivity());
    }

    @Flat
    public void onBlockProcessDialog() {
        ProgressTask<Integer> task = new ProgressTask<>(getWindow(), (report) -> {
            for (int i = 0; i < 100; i++) {
                if (report.isRequestCancel()) {
                    break;
                }
                report.setProgress(i / 99f);
                System.out.println("Progress " + (i / 99f));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
            return 0;
        });
        new Thread(task).start();
        var alert = new ProcessDialogBuilder()
                .title("This is process Dialog")
                .message("Please cancel this process")
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onRequestCancelListener((dg) -> {
                    System.out.println("Request the cancel!!!");
                })
                .task(task)
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
    public void onColorPickerDialog() {
        var alert = new ColorPickerDialogBuilder()
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onColorPickListener((dg, value) -> System.out.println(Color.toFloat(value)))
                .alpha(true)
                .palette(Color.red, Color.yellow, Color.green, Color.aqua, Color.blue, Color.purple, Color.gray, Color.black)
                .block(false)
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
    public void onEmojiPickerDialog(ActionEvent event) {
        var alert = new EmojiDialogBuilder()
                .onShowListener((dg) -> System.out.println("Show"))
                .onHideListener((dg) -> System.out.println("Hide"))
                .onEmojiPick((dg, val) -> {
                    for (int i = 0; i < val.length(); i++) {
                        var cp = val.codePointAt(i);
                        System.out.print(Integer.toHexString(cp) + " ");
                        i += Character.charCount(cp) - 1;
                    }
                    System.out.println();
                })
                .block(true)
                .build();
        alert.show(getActivity());
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

    private void setupListView(ListView listView) {
        ObservableList<String> list1 = new ObservableList<>();
        for (int i = 0; i < 50; i++) {
            list1.add("List Item " + (i + 1));
        }
        listView.setAdapter(new ListViewDefaultAdapter<>(list1));
    }

    private void setupTreeView(TreeView treeView) {
        treeView.addTreeItem(new AssetData("Item", false), false);
        treeView.addTreeItem(new AssetData("Item2", false), false);
        treeView.addTreeItem(new AssetData("Item3", false), false);
        treeView.addTreeItem(new AssetData("Item4", false), false);
        treeView.addTreeItem(new AssetData("ZFolder", true), false);
        treeView.addTreeItem(new AssetData("ZFolder/Item5", false), false);
        treeView.addTreeItem(new AssetData("ZFolder/Item6", false), false);
        treeView.addTreeItem(new AssetData("ZFolder/AFolder", true), false);
        treeView.addTreeItem(new AssetData("ZFolder/AFolder/Item7", false), false);
        treeView.addTreeItem(new AssetData("ZFolder/AFolder/Item8", false), false);
        treeView.setStylizeListener(this::onTreeViewStylize);
        treeView.setDragListener(this::onTreeViewDrag);
    }

    @Flat
    public void optimize(PointerEvent pointer) {
        /*if (pointer.getPointerID() == 1 && pointer.getType() == PointerEvent.PRESSED) {
            if (pointer.getSource() instanceof ImageView view) {
                LineMap lineMap = (LineMap) view.getImage();
                lineMap.optimize();
            }
        }*/
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

    PixelMap[] maps;
    @Flat public ImageView img0, img1, img2, img3, img4, img5, img6, img7, img8, img9, img10,
            img11, img12, img13, img14, img15, img16;
    private ImageView[] images;

    private boolean mode;
    private ImageData pasteImageData;
    private PixelMap pasteImage;
    @Flat
    public void toggleDefault(ActionEvent event) {
        //mode = !mode;
        //getActivity().setRenderPartialEnabled(mode);
        //System.out.println(mode);
        var old = pasteImageData;
        pasteImageData = WL.GetClipboardImage(0);
        if (pasteImageData != null) {
            pasteImage = new PixelMap(pasteImageData.getData(), pasteImageData.getWidth(), pasteImageData.getHeight(), pasteImageData.getFormat());
        }
        if (old != null) {
            WL.SetClipboardImage(0, old);
        }
    }

    private PixelMap cutTest;
    @Override
    public void onShow() {
        images = new ImageView[]{img0, img1, img2, img3, img4, img5, img6, img7, img8, img9, img10,
                img11, img12, img13, img14, img15, img16};
        getWindow().setIcon(PixelMap.parse(new ResourceStream("/default/icons/window-icon.png")));
        setupListView(listView1);
        setupListView(listView2);
        setupListView(listView3);
        setupTreeView(treeView1);
        setupTreeView(treeView2);
        setupTreeView(treeView3);

        Application.setVsync(1);
        getActivity().setContinuousRendering(false);
        var graphics = getGraphics();

        shader = graphics.createImageRenderShader(
                """
                #version 330 core
                uniform vec4 col;
                uniform vec4 bac;
                vec4 fragment(vec2 pos, vec2 uv) {
                    return pos.x > 8 && bac.r > 0 ? vec4(col.rgb, clamp(col.a * (pos.x - 8) / 16, 0, 1))
                    : pos.x > 40 && bac.r < 0 ? vec4(col.rgb, 1 - clamp(col.a * (pos.x - 40) / 16, 0, 1))
                    : col;
                }
                """);
        Surface surface = new Surface(64, 64, 8);
        graphics.setSurface(surface);

        maps = new PixelMap[AlphaComposite.values().length];
        for (int i = 0; i < maps.length; i++) {
            if (i >= maps.length - 5) {
                graphics.clear(0xFFFFFFFF, 0, 0);
            }
            graphics.setAlphaComposite(AlphaComposite.SRC_OVER);
            shader.set("col", Color.toFloat(0x0040FFFF));
            shader.set("bac", new Vector4(-1, -1, -1, -1));
            graphics.blitCustomShader(shader, 0, 0, 56, 40);
            graphics.setAlphaComposite(AlphaComposite.values()[i]);
            shader.set("col", Color.toFloat(0xFF4000FF));
            shader.set("bac", new Vector4(1, 1, 1, 1));
            graphics.blitCustomShader(shader, 8, 24, 56, 40);
            graphics.setAlphaComposite(AlphaComposite.SRC_OVER);
            maps[i] = graphics.createPixelMap();
            images[i].setImage(maps[i]);
            graphics.clear(0, 0, 0);
        }

        //graphics.clear(0, 0, 0);
        //graphics.setTransform2D(null);
        //graphics.setColor(Color.blue);
        //graphics.drawRect(0, 0, 64, 64, true);
        //graphics.setColor(Color.red);
        //graphics.drawRect(0, 0, 32, 32, true);
        //graphics.setColor(Color.black);
        //graphics.setStroke(new BasicStroke(1));
        //graphics.drawCircle(32, 32, 32, false);
        //graphics.drawCircle(32, 32, 16, false);
        //graphics.drawCircle(32, 32, 8, false);
        //graphics.drawLine(0, 16, 32, 16);
        //cutTest = graphics.createPixelMap(32, 16, 32, 32, PixelFormat.RGBA);
        //graphics.setSurface(null);

        cutTest = new PixelMap(new byte[] {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF}, 1, 1, PixelFormat.RGBA);

        if (tabChips != null) {
            search(tabChips.getFrame());
        }

        if (tabDefault != null) {
            search(tabDefault.getFrame());
        }
    }

    @Flat
    public void onTreeViewDrag(DragEvent event) {
        if (event.getType() == DragEvent.DONE && event.getData() instanceof TreeViewDragData data) {
            var drop = data.getSource().getDropPos(event.getX(), event.getY());
            System.out.println(data.getItems().size() + " >> " + drop.getCell().getData());
        }
    }

    @Flat
    public void onTreeViewStylize(TreeViewStyle event) {
        TreeItemCell cell = event.getCell();
        ListItem item = event.getItem();
        AssetData data = (AssetData) event.getData();
        TreeView treeView = event.getTreeView();

        item.setText(event.isMultiselection() ? "..." : data.getName());
        item.removeStyle("tree-item-dragged");
        item.removeStyle("tree-item-selected");
        item.removeStyle("tree-item-folder");
        item.removeStyle("tree-item-folder-open");
        item.removeStyle("tree-item-floating");
        item.removeStyle("tree-item-multiselection");
        item.addStyle("tree-item");
        if (cell.isFolder()) {
            if (cell.isOpen()) {
                item.addStyle("tree-item-folder-open");
            } else {
                item.addStyle("tree-item-folder");
            }
        }
        if (cell.isSelected()) {
            item.addStyle("tree-item-selected");
        }
        if (cell.isDragged()) {
            item.addStyle("tree-item-dragged");
        }
        if (event.isFloating()) {
            item.setLayers(0);
            item.addStyle("tree-item-floating");
        } else {
            item.setLayers(cell.getLevels());
        }
        if (event.isMultiselection()) {
            item.addStyle("tree-item-multiselection");
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
    int n;
    long time;

    public static Vector2 magicLerp(Vector2 a, Vector2 b, float t, float distortion) {
        float ax = a.x, ay = a.y;
        float bx = b.x, by = b.y;

        float az = (float)Math.sqrt(Math.max(0, 1 - ax * ax - ay * ay));
        float bz = (float)Math.sqrt(Math.max(0, 1 - bx * bx - by * by));

        // Convert to 3D vectors
        float[] A = new float[] { ax, ay, az };
        float[] B = new float[] { bx, by, bz };

        // Dot and angle
        float dot = A[0]*B[0] + A[1]*B[1] + A[2]*B[2];
        dot = Math.max(-1f, Math.min(1f, dot)); // Clamp
        float theta = (float)Math.acos(dot);

        // Avoid divide by zero
        if (theta < 0.0001f) {
            return new Vector2(ax + t * (bx - ax), ay + t * (by - ay));
        }

        // Slerp in 3D
        float sinTheta = (float)Math.sin(theta);
        float w1 = (float)Math.sin((1 - t) * theta) / sinTheta;
        float w2 = (float)Math.sin(t * theta) / sinTheta;

        float sx = w1 * A[0] + w2 * B[0];
        float sy = w1 * A[1] + w2 * B[1];
        float sz = w1 * A[2] + w2 * B[2];

        // Project back to 2D
        Vector2 spherical = new Vector2(sx, sy);
        Vector2 linear = new Vector2(ax + t * (bx - ax), ay + t * (by - ay));

        // Blend between linear and spherical based on distortion
        Vector2 result = new Vector2(
                (1 - distortion) * linear.x + distortion * spherical.x,
                (1 - distortion) * linear.y + distortion * spherical.y
        );

        return result;
    }

    float a = 0;
    @Override
    public void onDraw(Graphics graphics) {
        super.onDraw(graphics);
        a += 1;
        if (a > 500) a = 0;
        graphics.setTransform2D(null);
        if (pasteImage != null) graphics.drawImage(pasteImage, 100, 100);

        //graphics.setColor(Color.black);
        //graphics.setStroke(new BasicStroke(5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{18, 6}, 0));
        //graphics.drawRect(100, 100, 100, 100, false);
        // graphics.drawImage(cutTest, 200, 200, 50, 50);

        n++;
        long now = System.currentTimeMillis();
        if (now - time > 250) {
            time = time == 0 ? now : now - (now - time - 250);
            // statusLabel.setText("FPS : " + (n * 4));
            n = 0;
        }

        if (t > 0) {
            if (t == 1) {
                screen = getActivity().getContext().getGraphics().createPixelMap();
                System.out.println("Print : " + GL.GetError());
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
            getActivity().repaint();
        }
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
        /*ResourceStream unicodes = new ResourceStream("/default/emojis/emojis.txt");
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
        System.out.println();*/
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
        // getActivity().getWindow().showOpenFileDialog((file) -> {
        //     EmojiConverter.convertNotoMetaToDic(file);
        // }, null, "json");
//        getActivity().getWindow().showOpenFolderDialog((file) -> {
//            ArrayList<int[]> emojis = new ArrayList<>();
//            PixelMap map = EmojiConverter.createFromNotoEmoji(file, emojis, 4096);
//            saveImage(map, "C:\\Nova\\emojis.png");
//            saveEmojis(emojis, "C:\\Nova\\emojis.txt");
//        }, null);
    }

    @Flat
    public void toggleDrawer2(ActionEvent event) {
        drawer2.toggle();
    }

    @Flat
    public void onCanvasDraw(DrawEvent event) {
        var graphics = event.getGraphics();
        var box = event.getInBox();
        graphics.setColor(0xFF0000FF);
        graphics.setAlphaComposite(AlphaComposite.DST_OUT);
        graphics.drawEllipse(box.x, box.y, box.width, box.height, true);
        graphics.setAlphaComposite(AlphaComposite.SRC_OVER);
    }

}