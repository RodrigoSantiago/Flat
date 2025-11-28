package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.math.Vector2;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.enums.Direction;
import flat.widget.text.content.Caret;
import flat.window.Activity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DecimalInputField extends TextInputField {
    
    private DecimalFormat df;

    private UXListener<ActionEvent> actionIncreaseListener;
    private UXListener<ActionEvent> actionDecreaseListener;
    private Direction direction = Direction.HORIZONTAL;
    
    private float minValue = -Float.MAX_VALUE;
    private float maxValue = Float.MAX_VALUE;
    private float steps = 0.1f;
    private int decimals = 2;
    private boolean fixedDecimals;
    
    private float dragX;
    private float dragY;
    private double dragValue;
    private boolean pressOnAction;
    private boolean slideIntegerEnabled;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setRangeLimits(attrs.getAttributeNumber("min-value", getMinValue()), attrs.getAttributeNumber("max-value", getMaxValue()));
        setSteps(attrs.getAttributeNumber("steps", getSteps()));
        setDecimals((int) attrs.getAttributeNumber("decimals", getDecimals()));
        setFixedDecimals(attrs.getAttributeBool("fixed-decimals", isFixedDecimals()));
        setActionIncreaseListener(attrs.getAttributeListener("on-increase", ActionEvent.class, controller, getActionIncreaseListener()));
        setActionDecreaseListener(attrs.getAttributeListener("on-decrease", ActionEvent.class, controller, getActionDecreaseListener()));
    }
    
    @Override
    public void applyStyle() {
        super.applyStyle();
        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setDirection(attrs.getConstant("direction", info, getDirection()));
        setSlideIntegerEnabled(attrs.getBool("slide-integer-enabled", info, isSlideIntegerEnabled()));
    }
    
    @Override
    protected boolean isLineWrapReallyEnabled() {
        return false;
    }

    @Override
    public void hover(HoverEvent event) {
        UXListener.safeHandle(getHoverListener(), event);
        if (!event.isConsumed() && event.getType() == HoverEvent.MOVED) {
            setUndefined(pressOnAction || isOverActionButton(screenToLocal(event.getX(), event.getY())));
        }
        if (!event.isConsumed() && event.getType() == HoverEvent.EXITED) {
            setUndefined(pressOnAction);
        }
    }
    
    private float warpCursorX;
    private float warpCursorY;

    @Override
    public void pointer(PointerEvent event) {
        UXListener.safeHandle(getPointerListener(), event);
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.PRESSED) {
            if (isOverActionButton(screenToLocal(event.getX(), event.getY()))) {
                setCaretHidden();
                pressOnAction = true;
                dragValue = getNumber();
                dragX = event.getX();
                dragY = event.getY();
                warpCursorX = 0;
                warpCursorY = 0;
            }
        }
        if (pressOnAction) {
            event.consume();
        }
        if (pressOnAction && event.getPointerID() == 1 && event.getType() == PointerEvent.DRAGGED) {
            setCaretHidden();
            var window = getActivity().getWindow();
            if (direction == Direction.HORIZONTAL || direction == Direction.IHORIZONTAL) {
                float xPos = window.getMousePosition().x;
                double offset = (xPos - (dragX + warpCursorX)) * steps;
                double target = dragValue + offset * (direction == Direction.IHORIZONTAL ? -1 : 1);
                if (slideIntegerEnabled) {
                    target = Math.round(target);
                }
                if (Math.abs(target - getNumber()) > window.getWidth() * steps * 0.85f) {
                    if (xPos >= window.getWidth() - 1) {
                        window.setMousePosition(1, window.getPointerY());
                        return;
                    } else if (xPos <= 0) {
                        window.setMousePosition(window.getWidth() - 2, window.getPointerY());
                        return;
                    }
                }
                setNumber(target);
                
                if (offset > 0) fireActionIncrease();
                if (offset < 0) fireActionDecrease();
                
                if (xPos >= window.getWidth() - 1) {
                    warpCursorX -= window.getWidth();
                    window.setMousePosition(1, window.getPointerY());
                } else if (xPos <= 0) {
                    warpCursorX += window.getWidth();
                    window.setMousePosition(window.getWidth() - 2, window.getPointerY());
                }
                
            } else {
                float yPos = window.getMousePosition().y;
                double offset = (yPos - (dragY + warpCursorY)) * steps;
                double target = dragValue - offset * (direction == Direction.IVERTICAL ? -1 : 1);
                if (slideIntegerEnabled) {
                    target = Math.round(target);
                }
                if (Math.abs(target - getNumber()) > window.getHeight() * steps * 0.85f) {
                    if (yPos >= window.getHeight() - 1) {
                        window.setMousePosition(window.getPointerX(), 1);
                        return;
                    } else if (yPos <= 0) {
                        window.setMousePosition(window.getPointerX(), window.getHeight() - 2);
                        return;
                    }
                }
                setNumber(target);
                
                if (offset > 0) fireActionIncrease();
                if (offset < 0) fireActionDecrease();
                if (yPos >= getActivity().getWindow().getHeight() - 1 || yPos <= 0) {
                    if (yPos >= window.getHeight() - 1) {
                        warpCursorY -= window.getHeight();
                        window.setMousePosition(window.getPointerX(), 1);
                    } else if (yPos <= 0) {
                        warpCursorY += window.getHeight();
                        window.setMousePosition(window.getPointerX(), window.getHeight() - 2);
                    }
                }
            }
            
            fireTextType();
        }
        if (pressOnAction && event.getPointerID() == 1 && event.getType() == PointerEvent.RELEASED) {
            pressOnAction = false;
            warpCursorX = 0;
            warpCursorY = 0;
            action();
            setUndefined(isOverActionButton(screenToLocal(event.getX(), event.getY())));
        }
        if (!pressOnAction) {
            Vector2 point = screenToLocal(event.getX(), event.getY());
            point.x += getViewOffsetX();
            point.y += getViewOffsetY();
            textPointer(event, point);
        }
    }
    
    @Override
    public void requestFocus(boolean focus) {
        if (isFocusable()) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.runLater(() -> {
                    setFocused(focus);
                    if (focus && !isUndefined()) setCaretVisible();
                });
            }
        }
    }
    
    private String fomart(String text) {
        text = text.replaceAll(",", ".");
        text = text.replaceAll("[^0-9\\-.]", "");
        int first = text.indexOf(".");
        int last = text.lastIndexOf(".");
        if (first != last) {
            text = text.substring(0, first + 1) + text.substring(first + 1).replaceAll("\\.", "");
        }
        if (text.contains("-")) {
            text = text.charAt(0) + text.substring(1).replaceAll("-", "");
        }
        return text;
    }

    @Override
    protected void editText(Caret first, Caret second, String input) {
        if (!isEditable()) return;
        super.editText(first, second, input.replaceAll(",", ".").replaceAll("[^0-9\\-.]", ""));
    }
    
    @Override
    protected boolean hasLocalFilter() {
        return true;
    }
    
    @Override
    protected String localFilter(String value) {
        return fomart(value);
    }
    
    @Override
    public void setText(String input) {
        super.setText(input == null ? null : fomart(input));
    }

    public double getNumber() {
        String text = getText();
        if (text == null) {
            return Math.max(minValue, Math.min(maxValue, 0));
        }
        try {
            return Math.max(minValue, Math.min(maxValue, Double.parseDouble(text)));
        } catch (Exception e) {
            return Math.max(minValue, Math.min(maxValue, 0));
        }
    }

    public void setNumber(double number) {
        if (Double.isNaN(number) || Double.isInfinite(number)) {
            number = 0;
        }
        number = Math.max(minValue, Math.min(maxValue, number));
        setText(floatToString(number));
    }

    public void increase() {
        setNumber(getNumber() + steps);
        fireActionIncrease();
        fireTextType();
    }

    public void decrease() {
        setNumber(getNumber() - steps);
        fireActionDecrease();
        fireTextType();
    }
    
    public float getSteps() {
        return steps;
    }
    
    public void setSteps(float steps) {
        if (steps < 0.0001f) steps = 0.0001f;
        
        if (this.steps != steps) {
            this.steps = steps;
        }
    }
    
    public int getDecimals() {
        return decimals;
    }
    
    public void setDecimals(int decimals) {
        if (this.decimals != decimals) {
            this.decimals = decimals;
            df = null;
            setNumber(getNumber());
        }
    }
    
    public boolean isFixedDecimals() {
        return fixedDecimals;
    }
    
    public void setFixedDecimals(boolean fixedDecimals) {
        if (this.fixedDecimals != fixedDecimals) {
            this.fixedDecimals = fixedDecimals;
            setNumber(getNumber());
        }
    }
    
    @Override
    protected void setCaretHidden() {
        super.setCaretHidden();
        setNumber(getNumber());
    }

    private void fireActionIncrease() {
        if (actionIncreaseListener != null) {
            UXListener.safeHandle(actionIncreaseListener, new ActionEvent(this));
        }
    }

    public UXListener<ActionEvent> getActionIncreaseListener() {
        return actionIncreaseListener;
    }

    public void setActionIncreaseListener(UXListener<ActionEvent> actionIncreaseListener) {
        this.actionIncreaseListener = actionIncreaseListener;
    }

    private void fireActionDecrease() {
        if (actionDecreaseListener != null) {
            UXListener.safeHandle(actionDecreaseListener, new ActionEvent(this));
        }
    }

    public UXListener<ActionEvent> getActionDecreaseListener() {
        return actionDecreaseListener;
    }

    public void setActionDecreaseListener(UXListener<ActionEvent> actionDecreaseListener) {
        this.actionDecreaseListener = actionDecreaseListener;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setRangeLimits(float minValue, float maxValue) {
        float cMinValue = Math.min(minValue, maxValue);
        float cMaxValue = Math.max(minValue, maxValue);
        if (cMinValue != this.minValue || cMaxValue != this.maxValue) {
            this.minValue = cMinValue;
            this.maxValue = cMaxValue;
            setNumber(getNumber());
        }
    }
    
    private String floatToString(double value) {
        if (isFixedDecimals()) {
            if (decimals == 0) {
                return ((long)value) + "";
            } else {
                return String.format(Locale.ROOT, "%." + decimals + "f", value);
            }
        } else {
            if (df == null) {
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
                symbols.setDecimalSeparator('.');
                df = new DecimalFormat((decimals > 0 ? "0." : "0") + "#".repeat(Math.max(0, decimals)), symbols);
            }
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return df.format(Math.max(minValue, Math.min(maxValue, 0)));
            }
            return df.format(value);
        }
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public void setDirection(Direction direction) {
        if (direction == null) direction = Direction.HORIZONTAL;
        
        if (this.direction != direction) {
            this.direction = direction;
            invalidate(false);
        }
    }
    
    public boolean isSlideIntegerEnabled() {
        return slideIntegerEnabled;
    }
    
    public void setSlideIntegerEnabled(boolean slideIntegerEnabled) {
        this.slideIntegerEnabled = slideIntegerEnabled;
    }
}
