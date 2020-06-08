package pk.lab06.sw.emulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ButtonPinPanel extends PinPanel implements ActionListener {

    final ArrayList<ButtonPanelActionListener> listeners;

    public ButtonPinPanel(int axis) {
        super(axis, 8);
        listeners = new ArrayList<>();
        colorDisable = new Color(144, 144, 144);
        colorEnable = Color.WHITE;
        updateColors();
    }

    @Override
    protected JComponent createComponent(int i){
        JButton p = new JButton(""+i);
        p.setSize(50,50);
        p.setBackground(colorEnable);
        p.addActionListener(this);
        return p;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for(int i = 0 ; i < components.length ; i++){
            if(components[i] == e.getSource()){
                processButton(i);
                return;
            }
        }
    }

    public void processButton(int i){
        setStatus(i, !status.get(i));
        processEvent(i, status.get(i));

        this.repaint();
    }

    public void processEvent(int i, boolean status){
        for(ButtonPanelActionListener l : listeners){
            l.actionPerformed(i, status);
        }
    }

    public void addButtonPanelActionListener(ButtonPanelActionListener al){
        listeners.add(al);
    }
    public void removeButtonPanelActionListener(ButtonPanelActionListener al){
        listeners.remove(al);
    }
}
