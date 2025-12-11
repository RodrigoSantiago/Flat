package flat.widget.text;

import flat.animations.StateInfo;
import flat.graphics.Graphics;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.widget.Group;
import flat.widget.Widget;

public class TextStyledEditorLines extends Widget {
    
    private float lineNumberTextSize = 0;
    private float lineNumberWidth = 0;
    private int lineNumberColor = 0x000000ff;
    private int selectedLneNumberColor = 0x000000ff;
    
    private TextStyledEditor target;
    
    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        
        UXAttrs attrs = getAttrs();
        
        String targetId = attrs.getAttributeString("target-id", null);
        if (targetId != null) {
            Group group = getGroup();
            if (group != null) {
                Widget widget = group.findById(targetId);
                if (widget instanceof TextStyledEditor target) {
                    setTarget(target);
                }
            }
        }
    }
    
    public void setTarget(TextStyledEditor target) {
        if (this.target != target) {
            if (this.target != null) {
                if (this.target.getLinePlugin() != null) {
                    this.target.getLinePlugin().target = null;
                    this.target.setLinePlugin(null);
                }
            }
            this.target = target;
            if (this.target != null) {
                this.target.setLinePlugin(this);
            }
            invalidate(isWrapContent());
        }
    }
    
    public TextStyledEditor getTarget() {
        return target;
    }
    
    @Override
    public void applyStyle() {
        super.applyStyle();
        
        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        
        setLineNumberTextSize(attrs.getSize("line-number-text-size", info, getLineNumberTextSize()));
        setLineNumberWidth(attrs.getSize("line-number-width", info, getLineNumberWidth()));
        setLineNumberColor(attrs.getColor("line-number-color", info, getLineNumberColor()));
        setSelectedLneNumberColor(attrs.getColor("selected-line-number-color", info, getSelectedLneNumberColor()));
    }
    
    @Override
    public void onDraw(Graphics graphics) {
        if (discardDraw(graphics)) return;
        if (target == null) return;
        
        drawBackground(graphics);
        drawRipple(graphics);
        
        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();
        
        if (width <= 0 || height <= 0) return;
        
        graphics.setTransform2D(getTransform());
        graphics.setTextSize(getLineNumberTextSize());
        target.drawLines(graphics, this, x, y, width, getOutHeight());
    }
    
    public int getLineNumberColor() {
        return lineNumberColor;
    }
    
    public void setLineNumberColor(int lineNumberColor) {
        if (this.lineNumberColor != lineNumberColor) {
            this.lineNumberColor = lineNumberColor;
            invalidate(false);
        }
    }
    
    public float getLineNumberWidth() {
        return lineNumberWidth;
    }
    
    public void setLineNumberWidth(float lineNumberWidth) {
        if (this.lineNumberWidth != lineNumberWidth) {
            this.lineNumberWidth = lineNumberWidth;
            invalidate(isWrapContent());
        }
    }
    
    public float getLineNumberTextSize() {
        return lineNumberTextSize;
    }
    
    public void setLineNumberTextSize(float lineNumberTextSize) {
        if (this.lineNumberTextSize != lineNumberTextSize) {
            this.lineNumberTextSize = lineNumberTextSize;
            invalidate(isWrapContent());
        }
    }
    
    public int getSelectedLneNumberColor() {
        return selectedLneNumberColor;
    }
    
    public void setSelectedLneNumberColor(int selectedLneNumberColor) {
        if (this.selectedLneNumberColor != selectedLneNumberColor) {
            this.selectedLneNumberColor = selectedLneNumberColor;
            invalidate(false);
        }
    }
}
