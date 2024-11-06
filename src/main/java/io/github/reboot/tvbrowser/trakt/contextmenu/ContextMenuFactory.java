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

import static io.github.reboot.tvbrowser.trakt.utils.ProgramUtils.getProgramType;
import static org.apache.commons.collections4.CollectionUtils.addIgnoreNull;
import io.github.reboot.tvbrowser.trakt.database.PluginDatabase;
import io.github.reboot.tvbrowser.trakt.database.PluginDatabaseService;
import io.github.reboot.tvbrowser.trakt.i18n.MessageService;
import io.github.reboot.tvbrowser.trakt.plugin.Plugin;
import io.github.reboot.tvbrowser.trakt.ui.search.SearchPanel;
import io.github.reboot.tvbrowser.trakt.ui.search.SearchPanelFactory;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Program;

@Service
public class ContextMenuFactory {

    private Plugin plugin;

    private final PluginDatabaseService pluginDatabaseService;

    private final MessageService messageService;

    private final SearchPanelFactory searchPanelFactory;

    @Autowired
    ContextMenuFactory(@Lazy Plugin plugin, PluginDatabaseService pluginDatabaseService, MessageService messageService, SearchPanelFactory searchPanelFactory) {
        this.plugin = plugin;
        this.pluginDatabaseService = pluginDatabaseService;
        this.messageService = messageService;
        this.searchPanelFactory = searchPanelFactory;
    }

    public ActionMenu create(Program program) {
        List<Object> actions = new ArrayList<>();

        addIgnoreNull(actions, createAssignTraktItem(program));

        return new ActionMenu(messageService.getMessage("trakt"), plugin.createImageIcon("trakt", "trakt-logomark-square-gradient", 16), actions.toArray());
    }

    private Object createAssignTraktItem(Program program) {
        String name;
        switch (getProgramType(program)) {
        case MOVIE:
            name = messageService.getMessage("contextmenu.assignTraktMovie");
            break;
        case SHOW:
            name = messageService.getMessage("contextmenu.assignTraktEpisode");
            break;
        default:
            return null;
        }

        AbstractAction action = new AbstractAction() {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                PluginDatabase pluginDatabase = pluginDatabaseService.getDatabase();

                JDialog dialog = UiUtilities.createDialog(plugin.getParentFrame(), true);
                dialog.setTitle(name);

                SearchPanel searchPanel = searchPanelFactory.create(dialog, pluginDatabase);
                searchPanel.setProgram(program);
                searchPanel.addListener(new AssignTraktItemListener(dialog, messageService, pluginDatabase));
                dialog.add(searchPanel);

                plugin.layoutWindow("assignTraktItem", dialog);
                dialog.setVisible(true);
            }

        };
        action.putValue(Action.NAME, name);
        action.putValue(Action.SMALL_ICON, plugin.createImageIcon("trakt", "trakt-logomark-square-gradient", 16));

        return action;
    }

}
