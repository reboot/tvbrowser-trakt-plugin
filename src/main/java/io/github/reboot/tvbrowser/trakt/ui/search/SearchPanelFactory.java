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

import io.github.reboot.tvbrowser.trakt.client.TraktClientService;
import io.github.reboot.tvbrowser.trakt.database.PluginDatabase;
import io.github.reboot.tvbrowser.trakt.i18n.MessageService;

import javax.swing.JDialog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SearchPanelFactory {

    private final TraktClientService traktClientService;

    private final MessageService messageService;

    @Autowired
    SearchPanelFactory(TraktClientService traktClientService, MessageService messageService) {
        this.traktClientService = traktClientService;
        this.messageService = messageService;
    }

    public SearchPanel create(JDialog dialog, PluginDatabase pluginDatabase) {
        return new SearchPanel(traktClientService, messageService);
    }

}
