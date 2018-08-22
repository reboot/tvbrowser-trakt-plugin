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
package io.github.reboot.tvbrowser.trakt.importance;

import io.github.reboot.trakt.api.json.sync.HistoryItem;
import io.github.reboot.tvbrowser.trakt.database.PluginDatabaseService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import devplugin.ImportanceValue;
import devplugin.Program;

@Service
public class ImportanceFactory {

    private final Logger logger = LoggerFactory.getLogger(ImportanceFactory.class);

    private final PluginDatabaseService pluginDatabaseService;

    @Autowired
    public ImportanceFactory(PluginDatabaseService pluginDatabaseService) {
        this.pluginDatabaseService = pluginDatabaseService;
    }

    public ImportanceValue create(Program program) {
        Iterable<HistoryItem> historyItems = pluginDatabaseService.findHistoryItem(program);
        int count = 0;
        for (HistoryItem historyItem : historyItems) {
            logger.debug("Found item {}", historyItem.getId());
            count++;
        }
        if (count > 0) {
            logger.debug("Settings importance to minimum");
            return new ImportanceValue((byte) 1, (short) 1);
        }

        return null;
    }

}
