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
package io.github.reboot.tvbrowser.trakt.contextmenu;

import static io.github.reboot.tvbrowser.trakt.utils.ProgramUtils.getTitles;
import io.github.reboot.trakt.api.json.SearchResult;
import io.github.reboot.tvbrowser.trakt.database.PluginDatabase;
import io.github.reboot.tvbrowser.trakt.database.ProgramMapping;
import io.github.reboot.tvbrowser.trakt.database.ProgramMapping.Type;
import io.github.reboot.tvbrowser.trakt.i18n.MessageService;
import io.github.reboot.tvbrowser.trakt.ui.search.SearchPanelListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import devplugin.Program;

class AssignTraktItemListener implements SearchPanelListener {

    private final JDialog dialog;

    private final MessageService messageService;

    private final PluginDatabase pluginDatabase;

    AssignTraktItemListener(JDialog dialog, MessageService messageService, PluginDatabase pluginDatabase) {
        this.dialog = dialog;
        this.messageService = messageService;
        this.pluginDatabase = pluginDatabase;
    }

    @Override
    public void select(Program program, SearchResult searchResult) {
        if (searchResult == null) {
            return;
        }

        Type type;
        String traktId;
        switch (searchResult.getType()) {
        case MOVIE:
            type = ProgramMapping.Type.MOVIE;
            traktId = searchResult.getMovie().getIds().getTrakt();
            break;
        case EPISODE:
            if (!getTitles(program).contains(searchResult.getShow().getTitle()) && pluginDatabase.findShowId(program) == null) {
                Object[] options = {
                        messageService.getMessage("contextmenu.assignTraktItem.abort"),
                        messageService.getMessage("contextmenu.assignTraktItem.assignShowAndEpisode"),
                        messageService.getMessage("contextmenu.assignTraktItem.yes"),
                };
                int option = JOptionPane.showOptionDialog(dialog,
                        messageService.getMessage("contextmenu.assignTraktItem.message"),
                        messageService.getMessage("contextmenu.assignTraktItem.title"),
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                if (option == 0) {
                    return;
                }

                type = ProgramMapping.Type.SHOW;
                traktId = searchResult.getShow().getIds().getTrakt();

                pluginDatabase.addProgramMapping(program, type, traktId);

                if (option == 2) {
                    dialog.dispose();

                    return;
                }
            }

            type = ProgramMapping.Type.EPISODE;
            traktId = searchResult.getEpisode().getIds().getTrakt();
            break;
        default:
            throw new AssertionError(searchResult.getType());
        }

        pluginDatabase.addProgramMapping(program, type, traktId);
        pluginDatabase.commit();

        dialog.dispose();
    }

    @Override
    public void close() {
        dialog.dispose();
    }

}
