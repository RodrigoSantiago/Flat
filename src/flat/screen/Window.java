package flat.screen;

import flat.image.Image;
import flat.widget.Scene;

import java.util.ArrayList;

public abstract class Window {

    private final Scene scene;
    private ArrayList<InputListener> inputListeners = new ArrayList<>();

    public Window() {
        scene = new Scene();
    }

    public abstract void start(Scene scene);

    public final Scene getScene() {
        return scene;
    }

    public void addInputListener(InputListener inputListener) {
        inputListeners.add(inputListener);
    }

    public void removeInputListener(InputListener inputListener) {
        inputListeners.remove(inputListener);
    }

    public final void launch() {

    }

    public final void setFullscreen(boolean fullscreen) {

    }

    public final boolean isFullscreen() {
        return false;
    }

    public final boolean isResisable() {
        return false;
    }

    public final boolean isDecorated() {
        return false;
    }

    public final String getTitle() {
        return null;
    }

    public final void setTitle(String title) {

    }

    public final void setIcons(Image... icons) {

    }

    public final int getX() {
        return 0;
    }

    public final void setX(int x) {

    }

    public final int getY() {
        return 0;
    }

    public final void setY(int y) {

    }

    public final void setPosition(int x, int y) {

    }

    public final int getWidth() {
        return 0;
    }

    public final void setWidth(int width) {

    }

    public final int getHeight() {
        return 0;
    }

    public final void setHeight(int height) {

    }

    public final void setSize(int width, int height) {

    }

    public final int getMinWidth() {
        return 0;
    }

    public final void setMinWidth(int minWidth) {

    }

    public final int getMinHeight() {
        return 0;
    }

    public final void setMinHeight(int minHeight) {

    }

    public final int getMaxWidth() {
        return 0;
    }

    public final void setMaxWidth(int maxWidth) {

    }

    public final int getMaxHeight() {
        return 0;
    }

    public final void setMaxHeight(int maxHeight) {

    }

    public final void setSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight) {

    }

    public final void show() {

    }

    public final void hide() {

    }

    public final void maximize() {

    }

    public final void minimize() {

    }

    public final void restore() {

    }

    public final void focus() {

    }

    public final void invalidate() {

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public interface InputListener {
        void mouseEvent();
        void touchEvent();
        void keyEvent();
        void joyEvent();
    }
}
