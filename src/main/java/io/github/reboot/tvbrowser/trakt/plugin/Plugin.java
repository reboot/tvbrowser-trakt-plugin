/**
 * Copyright (C) 2020 Christoph Hohmann <reboot@gmx.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
