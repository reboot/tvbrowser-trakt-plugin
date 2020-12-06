package io.github.reboot.tvbrowser.trakt.plugin;

import java.awt.Frame;
import java.awt.Window;

import javax.swing.ImageIcon;

public interface Plugin {

    devplugin.Plugin getPlugin();

    ImageIcon createImageIcon(String category, String icon, int size);

    Frame getParentFrame();

    void layoutWindow(String windowId, Window window);

    boolean saveMe();

}
