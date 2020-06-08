package pk.lab06.sw.emulator;

import java.util.EventListener;

public interface ButtonPanelActionListener extends EventListener {
    void actionPerformed(int index, boolean status);
}
