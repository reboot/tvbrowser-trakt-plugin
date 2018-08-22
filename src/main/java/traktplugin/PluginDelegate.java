/**
 * Copyright (C) 2018 Christoph Hohmann <reboot@gmx.ch>
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
package traktplugin;

import java.awt.Frame;
import java.awt.Window;

import javax.swing.ImageIcon;

import devplugin.ImportanceValue;
import devplugin.Plugin;
import devplugin.Program;

public class PluginDelegate {

    private Plugin plugin;

    private PluginSuper _super;

    public <T extends Plugin & PluginSuper> PluginDelegate(T plugin) {
        this.plugin = plugin;
        this._super = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public ImageIcon createImageIcon(String category, String icon, int size) {
        return plugin.createImageIcon(category, icon, size);
    }

    public Frame getParentFrame() {
        return _super.super_getParentFrame();
    }

    public ImportanceValue getImportanceValueForProgram(Program program) {
        return _super.super_getImportanceValueForProgram(program);
    }

    public void layoutWindow(String windowId, Window window) {
        plugin.layoutWindow(windowId, window);
    }

    public boolean saveMe() {
        return _super.super_saveMe();
    }

}
