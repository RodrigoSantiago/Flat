package flat.screen;

import flat.image.Image;

public class Window {
    private long _windowPointer;
    private RefreshHandler onDraw;

    public Window(String title, int width, int height) {

    }

    public Window(String title, int width, int height, boolean resisable, boolean decorated) {

    }

    public void setFullscreen(boolean fullscreen) {

    }

    public boolean isFullscreen() {
        return false;
    }

    public boolean isResisable() {
        return false;
    }

    public boolean isDecorated() {
        return false;
    }

    public String getTitle() {
        return null;
    }

    public void setTitle(String title) {

    }

    public void setIcons(Image... icons) {

    }

    public int getX() {
        return 0;
    }

    public void setX(int x) {

    }

    public int getY() {
        return 0;
    }

    public void setY(int y) {

    }

    public void setPosition(int x, int y) {

    }

    public int getWidth() {
        return 0;
    }

    public void setWidth(int width) {

    }

    public int getHeight() {
        return 0;
    }

    public void setHeight(int height) {

    }

    public void setSize(int width, int height) {

    }

    public int getMinWidth() {
        return 0;
    }

    public void setMinWidth(int minWidth) {

    }

    public int getMinHeight() {
        return 0;
    }

    public void setMinHeight(int minHeight) {

    }

    public int getMaxWidth() {
        return 0;
    }

    public void setMaxWidth(int maxWidth) {

    }

    public int getMaxHeight() {
        return 0;
    }

    public void setMaxHeight(int maxHeight) {

    }

    public void setSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight) {

    }

    RefreshHandler getOnDraw() {
        return onDraw;
    }

    void setOnDraw(RefreshHandler onDraw) {
        this.onDraw = onDraw;
    }

    public void show() {

    }

    public void hide() {

    }

    public void maximize() {

    }

    public void minimize() {

    }

    public void restore() {

    }

    public void focus() {

    }

    public void invalidate() {

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        // todo - destroy window
    }

    public interface RefreshHandler {
        void handle(Context frame);
    }
}
