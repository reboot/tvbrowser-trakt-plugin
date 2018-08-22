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
package io.github.reboot.tvbrowser.trakt.database;

import io.github.reboot.tvbrowser.trakt.plugin.ActivationEvent;
import io.github.reboot.tvbrowser.trakt.plugin.DeactivationEvent;

import java.io.File;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.fulltext.UniversalTextTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

@Service
public class NitriteService {

    public static final int EVENT_LISTENER_ORDER = 1;

    private final Logger logger = LoggerFactory.getLogger(NitriteService.class);

    private final File pluginDataDir;

    private Nitrite database;

    @Autowired
    NitriteService(@Value("${plugin.dataDir}") String pluginDataDir) {
        this.pluginDataDir = new File(pluginDataDir);
    }

    @EventListener
    @Order(EVENT_LISTENER_ORDER)
    private void onActivation(ActivationEvent event) {
        File file = new File(pluginDataDir, "plugin.db");
        logger.info("Opening Nitrite database from {}", file);
        database = Nitrite.builder()
                .filePath(file)
                .textTokenizer(new UniversalTextTokenizer())
                .disableAutoCommit()
                .openOrCreate();
    }

    @EventListener
    @Order(-EVENT_LISTENER_ORDER)
    private void onDeactivation(DeactivationEvent event) {
        logger.info("Closing Nitrite database");
        database.close();
        database = null;
    }

    public Nitrite getDatabase() {
        Preconditions.checkState(database != null, "Trying to use database while not active.");

        return database;
    }

}
