package flat.widget.stages.dialogs;

import flat.Flat;
import flat.data.ListChangeListener;
import flat.data.ObservableList;
import flat.events.ActionEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Surface;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.context.fonts.FontDetail;
import flat.graphics.image.ImageTexture;
import flat.graphics.symbols.*;
import flat.uxml.UXListener;
import flat.uxml.UXWidgetValueListener;
import flat.widget.Widget;
import flat.widget.enums.Visibility;
import flat.widget.image.ImageView;
import flat.widget.layout.LinearBox;
import flat.widget.stages.Dialog;
import flat.widget.structure.ListView;
import flat.widget.structure.ListViewAdapter;
import flat.widget.text.Button;
import flat.widget.text.Label;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.util.*;

class FontPickerDialogController extends DefaultDialogController {

    private final String title;
    private final String message;
    private final boolean cancelable;
    private final Font initialFont;
    private final UXListener<Dialog> onShowListener;
    private final UXListener<Dialog> onHideListener;
    private final UXWidgetValueListener<Dialog, Font> onChooseListener;

    FontPickerDialogController(Dialog dialog, FontPickerDialogBuilder builder) {
        super(dialog);
        this.title = builder.title;
        this.message = builder.message;
        this.cancelable = builder.cancelable;
        this.initialFont = builder.initialFont;
        this.onShowListener = builder.onShowListener;
        this.onHideListener = builder.onHideListener;
        this.onChooseListener = builder.onChooseListener;
    }

    @Flat
    public Label titleLabel;

    @Flat
    public Label messageLabel;

    @Flat
    public Button cancelButton;

    @Flat
    public LinearBox optionsArea;
    @Flat
    public ListView fontView;
    @Flat
    public ListView fontStyleView;

    @Flat
    public void hide() {
        dialog.smoothHide();
    }

    @Flat
    public void onChoose(ActionEvent event) {
        Font font = selected.font;
        if (font == null) {
            font = FontManager.createSystemFont(selected.detail);
        }
        UXWidgetValueListener.safeHandle(onChooseListener, dialog, font);
        dialog.smoothHide();
    }

    @Flat
    public void onCancel(ActionEvent event) {
        dialog.smoothHide();
    }

    @Flat
    public void onOpenFont(ActionEvent event) {
        getActivity().getWindow().showOpenFileDialog((file) -> {
            if (file != null) {
                addCustomFont(new File(file));
            }
        }, null, "ttf","ttf");
    }

    private void addCustomFont(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String name = file.getName().contains(".") ? file.getName().substring(0, file.getName().lastIndexOf(".")) :
                    file.getName();
            Font realFont = new Font(name, null, null, null, bytes);
            FontDetail font = new FontDetail(file,name, realFont.getPosture(), realFont.getWeight(), realFont.getStyle());
            var detail = new DetailInstalled(font, null);
            getFromCache(detail, realFont);
            fontFamily.add(0, new ArrayList<>(List.of(detail)));
            selectFamily(fontFamily.get(0));
            selectFont(detail);
            getWindow().runSync(() -> {
                fontView.slideVerticalTo(0);
            });
            realFont.dispose();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ObservableList<DetailInstalled> fonts = new ObservableList<>();
    ObservableList<ArrayList<DetailInstalled>> fontFamily = new ObservableList<>();

    DetailInstalled selected;

    private static HashMap<String, WeakReference<ImageCache>> globalCache = new HashMap<>();
    private static ArrayList<DetailInstalled> failed = new ArrayList<>();

    private Surface surface;
    private final HashMap<String, ImageCache> localCache = new HashMap<>();
    private final ArrayList<Runnable> tasks = new ArrayList<>();

    private boolean waitStartup = true;

    @Override
    public void onShow() {
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
        if (!cancelable && cancelButton != null) {
            cancelButton.setVisibility(Visibility.GONE);
        }
        surface = new Surface(256, 32);
        var adapterA = new ListViewAdapter<FontDetail>() {
            @Override
            public int size() {
                return fontFamily.size();
            }
            @Override
            public Widget createListItem() {
                ImageView fontPreview = new ImageView();
                fontPreview.addStyle("dialog-fontpicker-image");
                return fontPreview;
            }
            @Override
            public void buildListItem(int index, Widget item) {
                var detail = fontFamily.get(index);
                ImageView fontPreview = (ImageView)item;
                if (!waitStartup) {
                    ImageTexture image = getFromCache(detail.get(0));
                    fontPreview.setImage(image);
                }

                fontPreview.setPointerListener(event -> {
                    if (event.getType() == PointerEvent.PRESSED) selectFamily(detail);
                });
                fontPreview.setHoverListener(event -> {
                    if (event.getType() == HoverEvent.ENTERED) {
                        for (var prev : detail) {
                            getFromCache(prev);
                        }
                    }
                });

                if (detail.contains(selected)) {
                    fontPreview.addStyle("dialog-fontpicker-image-selected");
                } else {
                    fontPreview.removeStyle("dialog-fontpicker-image-selected");
                }
            }
        };
        var adapterB = new ListViewAdapter<FontDetail>() {
            @Override
            public int size() {
                return fonts.size();
            }

            @Override
            public Widget createListItem() {
                ImageView fontPreview = new ImageView();
                fontPreview.addStyle("dialog-fontpicker-image");
                return fontPreview;
            }
            @Override
            public void buildListItem(int index, Widget item) {
                var detail = fonts.get(index);
                ImageView fontPreview = (ImageView)item;
                if (!waitStartup) {
                    ImageTexture image = getFromCache(detail);
                    fontPreview.setImage(image);
                }
                fontPreview.setPointerListener(event -> {
                    if (event.getType() == PointerEvent.PRESSED) selectFont(detail);
                });
                if (detail == selected) {
                    fontPreview.addStyle("dialog-fontpicker-image-selected");
                } else {
                    fontPreview.removeStyle("dialog-fontpicker-image-selected");
                }
            }
        };
        var systemFamilies = new ArrayList<ArrayList<DetailInstalled>>();
        systemFamilies.addAll(FontManager.listSystemFontFamilies().values()
                .stream().map(a -> new ArrayList<>(
                        a.stream().map(b -> new DetailInstalled(b, null)).toList())
                ).toList());
        systemFamilies.addAll(FontManager.getInstalledFontFamilies().values()
                .stream().map(a -> new ArrayList<>(
                        a.stream().map(b -> new DetailInstalled(null, b)).toList()
                )).toList());
        for (var list : systemFamilies) {
            list.sort(DetailInstalled::compareTo);
        }
        systemFamilies.sort((o1, o2) -> o1.get(0).compareTo(o2.get(0)));

        int familyId = 0;
        if (initialFont != null) {
            boolean found = false;
            var init = new DetailInstalled(null, initialFont);
            for (int i = 0; i < systemFamilies.size(); i++) {
                var list = systemFamilies.get(i);
                for (var item : list) {
                    if (item.compareTo(init) == 0) {
                        found = true;
                        selected = item;
                        break;
                    }
                }
                if (selected != null) {
                    familyId = i;
                    break;
                }
            }
            if (!found) {
                var detail = new DetailInstalled(null, initialFont);
                fontFamily.add(0, new ArrayList<>(List.of(detail)));
                selected = detail;
                familyId = 0;
            }
        }
        if (selected == null) {
            selected = systemFamilies.get(0).get(0);
        }
        for (var prev : systemFamilies.get(familyId)) {
            getFromCache(prev);
        }
        for (int i = 2; i < 20; i++) {
            int id = i / 2 * (i % 2 == 0 ? 1 : -1) + familyId;
            if (id != familyId && id >= 0 && id < systemFamilies.size()) {
                getFromCache(systemFamilies.get(id).get(0));
            }
        }

        fontFamily.addAll(systemFamilies);
        fontFamily.setChangeListener((index, length, operation) -> {
            if (operation == ListChangeListener.Operation.UPDATE) {
                fontView.refreshItems(index, length);
            } else {
                fontView.refreshItems(index);
            }
        });
        fontView.setAdapter(adapterA);

        fonts.addAll(fontFamily.get(familyId));
        fonts.setChangeListener((index, length, operation) -> {
            if (operation == ListChangeListener.Operation.UPDATE) {
                fontStyleView.refreshItems(index, length);
            } else {
                fontStyleView.refreshItems(index);
            }
        });
        fontStyleView.setAdapter(adapterB);
        float id = familyId;
        getWindow().runSync(() -> {
            fontView.slideVerticalTo(
                    (fontView.getTotalDimensionY() - fontView.getViewDimensionY()) * (id / fontFamily.size())
            );
            getWindow().runSync(() -> {
                waitStartup = false;
                fontView.refreshItems();
                fontStyleView.refreshItems();
            });
        });
        UXListener.safeHandle(onShowListener, dialog);
    }

    private void selectFamily(List<DetailInstalled> details) {
        fonts.clear();
        fonts.addAll(details);
        if (!details.contains(selected)) {
            selected = details.get(0);
            fontView.refreshItems();
            fontStyleView.refreshItems();
        }
    }

    private void selectFont(DetailInstalled font) {
        selected = font;
        fontStyleView.refreshItems();
    }

    private ImageTexture getFromCache(DetailInstalled detail) {
        return getFromCache(detail, null);
    }
    private ImageTexture getFromCache(DetailInstalled detail, Font preFont) {
        if (failed.contains(detail)) {
            removeFailed(detail);
            return null;
        }

        var ptr = globalCache.get(detail.toString());
        ImageCache imageCache;
        if (ptr == null || (imageCache = ptr.get()) == null) {
            imageCache = buildCache(detail, null, preFont);
            globalCache.put(detail.toString(), new WeakReference<>(imageCache));
        } else if (!imageCache.render) {
            buildCache(detail, imageCache, preFont);
        }
        localCache.put(detail.toString(), imageCache);
        return imageCache.image;
    }

    private void removeFailed(DetailInstalled fontDetail) {
        getWindow().runSync(() -> {
            fonts.remove(fontDetail);
            for (int i = 0; i < fontFamily.size(); i++) {
                var f = fontFamily.get(i);
                f.remove(fontDetail);
                if (f.isEmpty()) {
                    fontFamily.remove(i--);
                }
            }
            fontView.refreshItems();
            fontStyleView.refreshItems();
        });
    }

    private ImageCache buildCache(DetailInstalled fontDetail, ImageCache cache, Font preFont) {
        ImageCache imageCache = cache == null ?
                new ImageCache(new ImageTexture(new byte[256 * 32 * 4], 256, 32, PixelFormat.RGBA)) : cache;

        if (!imageCache.render) {
            var graphics = getGraphics();
            graphics.setSurface(surface);
            graphics.clear(0, 0, 0);
            graphics.setTextFont(fontDetail.font == null ? preFont == null ? Font.getDefault() : preFont : fontDetail.font);
            graphics.setTextSize(20f);
            graphics.setColor(Color.black);
            graphics.drawTextSlice(8, 6, 240, 0, fontDetail.getName());
            graphics.renderToImage(imageCache.image);
            graphics.setSurface(null);
        }

        if (fontDetail.font != null || imageCache.render) {
            return imageCache;
        }

        tasks.add(() -> {
            var font = FontManager.createSystemFont(fontDetail.detail);
            if (font == null) {
                if (!failed.contains(fontDetail)) {
                    failed.add(fontDetail);
                }
                removeFailed(fontDetail);
            } else if (getGraphics() != null) {
                var graphics = getGraphics();
                graphics.setSurface(surface);
                graphics.clear(0, 0, 0);
                graphics.setTextFont(font);
                graphics.setTextSize(20f);
                graphics.setColor(Color.black);
                graphics.drawTextSlice(8, 6, 240, 0, font.getName());
                graphics.renderToImage(imageCache.image);
                graphics.setSurface(null);
                imageCache.render = true;
                dialog.repaint();
            }
            if (font != null) {
                font.dispose();
            }
            tasks.remove(0);
            if (!tasks.isEmpty() && getActivity() != null) {
                getActivity().runLater(tasks.get(0), 0.01f);
            } else {
                tasks.clear();
            }
        });
        if (tasks.size() == 1) {
            getActivity().runLater(tasks.get(0));
        }
        return imageCache;
    }

    private static class ImageCache {
        ImageTexture image;
        boolean render;

        public ImageCache(ImageTexture image) {
            this.image = image;
        }
    }
    private static class DetailInstalled {
        private final FontDetail detail;
        private final Font font;

        private DetailInstalled(FontDetail detail, Font font) {
            this.detail = detail;
            this.font = font;
        }

        public String getName() {
            return detail != null ? detail.getFamily() : font.getName();
        }

        public String getFamily() {
            return detail != null ? detail.getFamily() : font.getFamily();
        }

        public FontPosture getPosture() {
            return detail != null ? detail.getPosture() : font.getPosture();
        }

        public FontWeight getWeight() {
            return detail != null ? detail.getWeight() : font.getWeight();
        }

        public FontStyle getStyle() {
            return detail != null ? detail.getStyle() : font.getStyle();
        }

        public int compareTo(DetailInstalled o2) {
            int result;

            result = getFamily().compareToIgnoreCase(o2.getFamily());
            if (result != 0) return result;

            result = getStyle().compareTo(o2.getStyle());
            if (result != 0) return result;

            result = getWeight().compareTo(o2.getWeight());
            if (result != 0) return result;

            return getPosture().compareTo(o2.getPosture());
        }

        @Override
        public String toString() {
            return font != null ? font.toString() : detail.getFile().getAbsolutePath();
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) return false;
            DetailInstalled o2 = (DetailInstalled) object;
            if (o2.detail != null && detail != null) {
                return o2.detail.getFile().equals(detail.getFile());
            }
            return o2.font == font;
        }

        @Override
        public int hashCode() {
            return Objects.hash(detail != null ? detail.getFile() : null, font);
        }
    }

    @Override
    public void onHide() {
        UXListener.safeHandle(onHideListener, dialog);
    }
}
