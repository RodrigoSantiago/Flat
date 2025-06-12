package flat.widget.stages.dialogs;

import flat.Flat;
import flat.data.ListChangeListener;
import flat.data.ObservableList;
import flat.events.ActionEvent;
import flat.graphics.Color;
import flat.graphics.Surface;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.context.fonts.FontDetail;
import flat.graphics.image.PixelMap;
import flat.graphics.symbols.*;
import flat.uxml.UXListener;
import flat.uxml.UXValueListener;
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

import java.lang.ref.WeakReference;
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

    ObservableList<DetailInstalled> fonts = new ObservableList<>();
    ObservableList<ArrayList<DetailInstalled>> fontFamily = new ObservableList<>();

    DetailInstalled selected;

    private static HashMap<String, WeakReference<PixelMap>> globalCache = new HashMap<>();
    private HashMap<String, PixelMap> cache = new HashMap<>();

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
                PixelMap image = getFromCache(detail.get(0));
                ImageView fontPreview = (ImageView)item;
                fontPreview.setImage(image);
                fontPreview.setPointerListener(event -> selectFamily(detail));
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
                PixelMap image = getFromCache(detail);
                ImageView fontPreview = (ImageView)item;
                fontPreview.setImage(image);
                fontPreview.setPointerListener(event -> selectFont(detail));
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

        if (initialFont != null) {
            var init = new DetailInstalled(null, initialFont);
            for (var list : systemFamilies) {
                for (var item : list) {
                    if (item.compareTo(init) == 0) {
                        selected = item;
                        break;
                    }
                }
                if (selected != null) {
                    break;
                }
            }
        }
        if (selected == null) {
            selected = systemFamilies.get(0).get(0);
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

        fonts.addAll(fontFamily.get(0));
        fonts.setChangeListener((index, length, operation) -> {
            if (operation == ListChangeListener.Operation.UPDATE) {
                fontStyleView.refreshItems(index, length);
            } else {
                fontStyleView.refreshItems(index);
            }
        });
        fontStyleView.setAdapter(adapterB);
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

    private Surface surface;
    private PixelMap getFromCache(DetailInstalled detail) {
        var ptr = globalCache.get(detail.toString());
        PixelMap image;
        if (ptr == null || (image = ptr.get()) == null) {
            image = buildCache(detail);
            if (image == null) {
                return null;
            }
            globalCache.put(detail.toString(), new WeakReference<>(image));
        }
        cache.put(detail.toString(), image);
        return image;
    }

    private PixelMap buildCache(DetailInstalled fontDetail) {
        var font = fontDetail.font == null ? FontManager.createSystemFont(fontDetail.detail) : fontDetail.font;
        if (font == null) {
            getWindow().runSync(() -> {
                fonts.remove(fontDetail);
                for (int i = 0; i < fontFamily.size(); i++) {
                    var f = fontFamily.get(i);
                    f.remove(fontDetail);
                    if (f.isEmpty()) {
                        fontFamily.remove(i--);
                    }
                }
            });
            return null;
        }
        var graphics = getGraphics();
        graphics.setSurface(surface);
        graphics.clear(0, 0, 0);
        graphics.setTextFont(font);
        graphics.setTextSize(20f);
        graphics.setColor(Color.black);
        graphics.drawTextSlice(8, 6, 240, 0, font.getName());
        PixelMap fontPreview = graphics.createPixelMap();
        graphics.setSurface(null);
        if (fontDetail.font == null) {
            font.dispose();
        }
        return fontPreview;
    }

    private static class DetailInstalled {
        private final FontDetail detail;
        private final Font font;

        private DetailInstalled(FontDetail detail, Font font) {
            this.detail = detail;
            this.font = font;
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

            result = getPosture().compareTo(o2.getPosture());
            if (result != 0) return result;

            return getWeight().compareTo(o2.getWeight());
        }

        @Override
        public String toString() {
            return font != null ? font.toString() : detail.getFile().getAbsolutePath();
        }
    }

    @Override
    public void onHide() {
        UXListener.safeHandle(onHideListener, dialog);
    }
}
