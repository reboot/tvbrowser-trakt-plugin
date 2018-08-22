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
package io.github.reboot.tvbrowser.trakt.plugin;

import static devplugin.Plugin.getPluginManager;
import io.github.reboot.tvbrowser.trakt.database.DatabaseChangeEvent;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import devplugin.PluginManager;

@Component
class ProgramListRefresher {

    private final Logger logger = LoggerFactory.getLogger(ProgramListRefresher.class);

    private ScheduledExecutorService executorService;

    private Future<?> pendingRefresh;

    ProgramListRefresher(java.util.concurrent.ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    @EventListener
    private void onDatabaseChangeEvent(DatabaseChangeEvent event) {
        logger.info("Database changed, scheduling repaint of program table");

        scheduleRefresh();
    }

    private synchronized void scheduleRefresh() {
        if (pendingRefresh != null) {
            pendingRefresh.cancel(false);
        }
        pendingRefresh = executorService.schedule(new Runnable() {

            @Override
            public void run() {
                doRefresh();
            }

        }, 1, TimeUnit.SECONDS);
    }

    private synchronized void doRefresh() {
        logger.debug("Triggering repaint of program table");

        PluginManager pluginManager = getPluginManager();
        pluginManager.goToDate(pluginManager.getCurrentDate());
    }

}
