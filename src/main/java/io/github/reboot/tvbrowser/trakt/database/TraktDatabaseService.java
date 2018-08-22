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

import io.github.reboot.trakt.api.json.sync.HistoryItem;
import io.github.reboot.trakt.database.TraktDatabase;
import io.github.reboot.tvbrowser.trakt.plugin.ActivationEvent;
import io.github.reboot.tvbrowser.trakt.plugin.DeactivationEvent;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.event.ChangeInfo;
import org.dizitart.no2.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;

@Service
public class TraktDatabaseService {

    public static final int EVENT_LISTENER_ORDER = NitriteService.EVENT_LISTENER_ORDER + 1;

    private final Logger logger = LoggerFactory.getLogger(TraktDatabaseService.class);

    private final NitriteService nitriteService;

    private final ApplicationEventPublisher eventPublisher;

    private final ChangeListenerContainer changeListeners;

    private TraktDatabase database;

    @Autowired
    TraktDatabaseService(NitriteService nitriteService, ApplicationEventPublisher eventPublisher) {
        this.nitriteService = nitriteService;
        this.eventPublisher = eventPublisher;

        changeListeners = new ChangeListenerContainer(new ChangeListenerContainer.Callback() {

            @Override
            public void register(Class<?> itemClass, ChangeListener changeListener) {
                Preconditions.checkState(database != null, "Service has not been activated.");

                database.register(itemClass, changeListener);
            }

            @Override
            public void deregister(Class<?> itemClass, ChangeListener changeListener) {
                Preconditions.checkState(database != null, "Service has not been activated.");

                database.deregister(itemClass, changeListener);
            }

        });
    }

    @EventListener
    @Order(EVENT_LISTENER_ORDER)
    private void onActivation(ActivationEvent event) {
        Nitrite database = nitriteService.getDatabase();

        logger.info("Opening Trakt database");
        this.database = new TraktDatabase(database);
        changeListeners.register(HistoryItem.class, new ChangeListener() {

            @Override
            public void onChange(ChangeInfo changeInfo) {
                eventPublisher.publishEvent(new DatabaseChangeEvent());
            }

        });
    }

    @EventListener
    @Order(-EVENT_LISTENER_ORDER)
    private void onDeactivation(DeactivationEvent event) {
        logger.info("Closing Trakt database");
        changeListeners.deregister();
        database.close();
        database = null;
    }

    public TraktDatabase getDatabase() {
        Preconditions.checkState(database != null, "Trying to use database while not active.");

        return database;
    }

}
