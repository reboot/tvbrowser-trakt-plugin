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
package io.github.reboot.tvbrowser.trakt.ui.search;

import util.settings.ProgramPanelSettings;
import util.ui.ProgramPanel;
import devplugin.Plugin;

class SearchProgramPanel extends ProgramPanel {

    private static final long serialVersionUID = 1L;

    public SearchProgramPanel() {
        super(Plugin.getPluginManager().getExampleProgram(),
                new ProgramPanelSettings(
                        ProgramPanelSettings.SHOW_PICTURES_EVER,
                        0,
                        0,
                        false,
                        true,
                        0,
                        new String[0],
                        ProgramPanelSettings.X_AXIS,
                        true,
                        true,
                        false));
    }

}
