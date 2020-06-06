package pk.lab06.sw;

import javax.swing.*;
import java.awt.*;
import java.util.BitSet;

public abstract class PinPanel extends Box {

    protected Color colorEnable;
    protected Color colorDisable;
    protected final JComponent[] components;
    protected final BitSet status;

    public PinPanel(int noComponents){
        this(BoxLayout.X_AXIS, noComponents);
    }

    public PinPanel(int axis, int noComponents) {
        super(axis);
        colorDisable = Color.BLACK;
        colorEnable = Color.YELLOW;
        components = new JComponent[noComponents];
        status = new BitSet(components.length);
        for(int i = 0; i < components.length ; i++){
            components[i] = createComponent(i);
            add(components[i]);
        }
    }

    protected abstract JComponent createComponent(int i);

    protected void setStatus(int i, boolean stat){
        status.set(i, stat);
        Color selected = stat?colorEnable:colorDisable;
        components[i].setBackground(selected);
        double luminance = (0.299*selected.getRed() + 0.587 * selected.getGreen() + 0.114 * selected.getBlue())/255;
        components[i].setForeground(luminance > 0.5 ? Color.BLACK : Color.WHITE);
    }

    public void set(BitSet val){
        for(int i = 0; i < val.size() && i < components.length ; i++){
            setStatus(i, val.get(i));
        }
        this.repaint();
    }
    public void set(int i, boolean stat){
        if(i > 0 && i < components.length){
            setStatus(i, stat);
            this.repaint();
        }
    }
    public boolean get(int i){
        if(i > 0 && i < components.length){
            return status.get(i);
        }
        return false;
    }

    protected void updateColors(){
        for(int i = 0; i < components.length ; i++){
            components[i].setBackground( status.get(i) ? colorEnable:colorDisable );
        }
        this.repaint();
    }

    public Color getColorEnable() {
        return colorEnable;
    }

    public void setColorEnable(Color colorEnable) {
        this.colorEnable = colorEnable;
        updateColors();
    }

    public Color getColorDisable() {
        return colorDisable;
    }

    public void setColorDisable(Color colorDisable) {
        this.colorDisable = colorDisable;
        updateColors();
    }
}
