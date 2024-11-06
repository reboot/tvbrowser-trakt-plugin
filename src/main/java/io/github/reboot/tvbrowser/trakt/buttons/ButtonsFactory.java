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
package io.github.reboot.tvbrowser.trakt.buttons;

import static devplugin.Plugin.BIG_ICON;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;
import io.github.reboot.trakt.api.client.TraktClient;
import io.github.reboot.trakt.api.client.TraktClientException;
import io.github.reboot.trakt.database.TraktDatabase;
import io.github.reboot.trakt.database.sync.TraktSync;
import io.github.reboot.tvbrowser.trakt.client.TraktClientAction;
import io.github.reboot.tvbrowser.trakt.client.TraktClientService;
import io.github.reboot.tvbrowser.trakt.database.TraktDatabaseService;
import io.github.reboot.tvbrowser.trakt.i18n.MessageService;
import io.github.reboot.tvbrowser.trakt.plugin.Plugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import devplugin.ActionMenu;

@Service
public class ButtonsFactory {

    private Plugin plugin;

    private final MessageService messageService;

    private final TraktDatabaseService traktDatabaseService;

    private final TraktClientService traktClientService;

    @Autowired
    ButtonsFactory(@Lazy Plugin plugin, MessageService messageService, TraktDatabaseService traktDatabaseService, TraktClientService traktClientService) {
        this.plugin = plugin;
        this.messageService = messageService;
        this.traktDatabaseService = traktDatabaseService;
        this.traktClientService = traktClientService;
    }

    public ActionMenu create() {
        AbstractAction sync = new AbstractAction() {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent evt) {
                TraktClientAction action = new TraktClientAction() {

                    @Override
                    public void execute(TraktClient traktClient) throws TraktClientException {
                        TraktDatabase traktDatabase = traktDatabaseService.getDatabase();

                        TraktSync traktSync = new TraktSync(traktClient, traktDatabase);
                        traktSync.syncHistory();
                    }

                };
                traktClientService.execute(action);
            }

        };
        sync.putValue(NAME, messageService.getMessage("buttons.sync.name"));
        sync.putValue(SHORT_DESCRIPTION, messageService.getMessage("buttons.sync.description"));
        sync.putValue(SMALL_ICON, plugin.createImageIcon("trakt", "trakt-logomark-square-gradient", 16));
        sync.putValue(BIG_ICON, plugin.createImageIcon("trakt", "trakt-logomark-square-gradient", 22));

        return new ActionMenu(messageService.getMessage("trakt"), plugin.createImageIcon("trakt", "trakt-logomark-square-gradient", 16), new Action[] { sync });
    }

}
