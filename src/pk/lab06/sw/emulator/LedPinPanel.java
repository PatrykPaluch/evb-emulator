package pk.lab06.sw.emulator;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;

public class LedPinPanel extends PinPanel {

    public LedPinPanel(int axis){
        super(axis, 8);
    }

    @Override
    protected JComponent createComponent(int i){
        JLabel p = new JLabel(""+i);
        p.setOpaque(true);
        p.setHorizontalAlignment(JLabel.CENTER);
        p.setSize(50,50);
        p.setMaximumSize(new Dimension(50, 50));
        p.setBackground(colorDisable);
        p.setForeground(Color.DARK_GRAY);
        p.setBorder(new BorderUIResource.LineBorderUIResource(Color.GRAY));
        return p;
    }

}
