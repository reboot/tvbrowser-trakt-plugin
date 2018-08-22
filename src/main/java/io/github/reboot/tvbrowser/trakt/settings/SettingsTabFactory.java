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
package io.github.reboot.tvbrowser.trakt.settings;

import io.github.reboot.tvbrowser.trakt.client.TraktClientService;
import io.github.reboot.tvbrowser.trakt.i18n.MessageService;
import io.github.reboot.tvbrowser.trakt.plugin.Plugin;

import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class SettingsTabFactory {

    private final Plugin plugin;

    private final SettingsService settingsService;

    private final TraktClientService traktClientService;

    private final ScheduledExecutorService executorService;

    private final MessageService messageService;

    @Autowired
    SettingsTabFactory(@Lazy Plugin plugin,
                       SettingsService settingsService,
                       TraktClientService traktClientService,
                       ScheduledExecutorService executorService,
                       MessageService messageService) {
        this.plugin = plugin;
        this.settingsService = settingsService;
        this.traktClientService = traktClientService;
        this.executorService = executorService;
        this.messageService = messageService;
    }

    public devplugin.SettingsTab create() {
        return new SettingsTab(plugin, settingsService, traktClientService, executorService, messageService);
    }

}
