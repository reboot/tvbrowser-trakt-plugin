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

import devplugin.ImportanceValue;
import devplugin.Plugin;
import devplugin.Program;

/**
 * Interface for plugin methods that need to be called on the {@link Plugin}
 * class and which are not accessible by the {@link PluginDelegate}.
 *
 * These methods are either {@code protected} or they are overridden by the
 * plugin implementation and the super method can not be called directly.
 */
public interface PluginSuper {

    Frame super_getParentFrame();

    ImportanceValue super_getImportanceValueForProgram(Program program);

    boolean super_saveMe();

}
