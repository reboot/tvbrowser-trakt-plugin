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

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.Icon;
import javax.swing.JPanel;

import com.google.common.base.Preconditions;

import devplugin.CancelableSettingsTab;

class SettingsTab implements CancelableSettingsTab {

    private final Plugin plugin;

    private final SettingsService settingsService;

    private final TraktClientService traktClientService;

    private final ScheduledExecutorService executorService;

    private final MessageService messageService;

    private Properties properties;

    SettingsTab(Plugin plugin,
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

    @Override
    public JPanel createSettingsPanel() {
        properties = settingsService.storeSettings();

        SettingsPanel settingsPanel = new SettingsPanel(settingsService, traktClientService, executorService, messageService);
        settingsPanel.init();
        return settingsPanel;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void saveSettings() {
        plugin.saveMe();
    }

    @Override
    public void cancel() {
        Preconditions.checkState(properties != null, "properties must not be null on cancel");

        settingsService.loadSettings(properties);
    }

}
