package flat.window;

import flat.resources.ResourceStream;
import flat.uxml.UXStringBundle;
import flat.uxml.UXTheme;
import flat.widget.Scene;

public class WindowSettings {

    private ControllerFactory controller;
    private UXTheme theme;
    private ResourceStream themeStream;
    private Scene layout;
    private ResourceStream layoutStream;
    private UXStringBundle stringBundle;
    private ResourceStream stringBundleStream;
    private int width;
    private int height;
    private int multiSamples;
    private boolean transparent;

    public WindowSettings(ControllerFactory controller
            , UXTheme theme, ResourceStream themeStream, Scene layout, ResourceStream layoutStream
            , UXStringBundle stringBundle, ResourceStream stringBundleStream
            , int width, int height, int multiSamples, boolean transparent) {
        this.controller = controller;
        this.theme = theme;
        this.themeStream = themeStream;
        this.layout = layout;
        this.layoutStream = layoutStream;
        this.stringBundle = stringBundle;
        this.stringBundleStream = stringBundleStream;
        this.width = width;
        this.height = height;
        this.multiSamples = multiSamples;
        this.transparent = transparent;
    }

    public ControllerFactory getController() {
        return controller;
    }

    public UXTheme getTheme() {
        return theme;
    }

    public ResourceStream getThemeStream() {
        return themeStream;
    }

    public Scene getLayout() {
        return layout;
    }

    public ResourceStream getLayoutStream() {
        return layoutStream;
    }

    public UXStringBundle getStringBundle() {
        return stringBundle;
    }

    public ResourceStream getStringBundleStream() {
        return stringBundleStream;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMultiSamples() {
        return multiSamples;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public static class Builder {

        private ControllerFactory controller;
        private UXTheme theme;
        private ResourceStream themeStream;
        private Scene layout;
        private ResourceStream layoutStream;
        private UXStringBundle stringBundle;
        private ResourceStream stringBundleStream;
        private int width = 800;
        private int height = 600;
        private int multiSamples = 8;
        private boolean transparent;

        public WindowSettings build() {
            return new WindowSettings(controller,
                    theme, themeStream, layout, layoutStream, stringBundle, stringBundleStream,
                    width, height, multiSamples, transparent);
        }

        public Builder layout(String stream) {
            this.layout = null;
            this.layoutStream = new ResourceStream(stream);
            return this;
        }

        public Builder layout(ResourceStream stream) {
            this.layout = null;
            this.layoutStream = stream;
            return this;
        }

        public Builder layout(Scene scene) {
            this.layout = scene;
            this.layoutStream = null;
            return this;
        }

        public Builder theme(String stream) {
            this.theme = null;
            this.themeStream = new ResourceStream(stream);
            return this;
        }

        public Builder theme(ResourceStream stream) {
            this.theme = null;
            this.themeStream = stream;
            return this;
        }

        public Builder theme(UXTheme theme) {
            this.theme = theme;
            this.themeStream = null;
            return this;
        }

        public Builder stringBundle(String stream) {
            this.stringBundle = null;
            this.stringBundleStream = new ResourceStream(stream);
            return this;
        }

        public Builder stringBundle(ResourceStream stream) {
            this.stringBundle = null;
            this.stringBundleStream = stream;
            return this;
        }

        public Builder stringBundle(UXStringBundle stringBundle) {
            this.stringBundle = stringBundle;
            this.stringBundleStream = null;
            return this;
        }

        public Builder controller(ControllerFactory controller) {
            this.controller = controller;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder multiSamples(int multiSamples) {
            this.multiSamples = multiSamples;
            return this;
        }

        public Builder transparent(boolean transparent) {
            this.transparent = transparent;
            return this;
        }
    }
}
