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

import static io.github.reboot.trakt.database.objects.filters.ObjectFilters.eqIgnoreCase;
import static io.github.reboot.tvbrowser.trakt.utils.EpisodeUtils.addEpisode;
import static io.github.reboot.tvbrowser.trakt.utils.ProgramUtils.getProgramType;
import static io.github.reboot.tvbrowser.trakt.utils.TitleUtils.addTitle;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.dizitart.no2.objects.filters.ObjectFilters.and;
import static org.dizitart.no2.objects.filters.ObjectFilters.eq;
import static org.dizitart.no2.objects.filters.ObjectFilters.or;
import static org.dizitart.no2.util.Iterables.firstOrDefault;
import static org.dizitart.no2.util.Iterables.toArray;
import io.github.reboot.trakt.api.json.sync.HistoryItem;
import io.github.reboot.trakt.database.TraktDatabase;
import io.github.reboot.tvbrowser.trakt.plugin.ActivationEvent;
import io.github.reboot.tvbrowser.trakt.plugin.DeactivationEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.event.ChangeInfo;
import org.dizitart.no2.event.ChangeListener;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import devplugin.Program;
import devplugin.ProgramFieldType;

@Service
public class PluginDatabaseService {

    public static final int EVENT_LISTENER_ORDER = NitriteService.EVENT_LISTENER_ORDER + 1;

    private final Logger logger = LoggerFactory.getLogger(PluginDatabaseService.class);

    private final NitriteService nitriteService;

    private final TraktDatabaseService traktDatabaseService;

    private final ApplicationEventPublisher eventPublisher;

    private final ChangeListenerContainer changeListeners;

    private final LoadingCache<Program, Iterable<HistoryItem>> findHistoryItemCache;

    private PluginDatabase database;

    @Autowired
    PluginDatabaseService(NitriteService nitriteService, TraktDatabaseService traktDatabaseService, ApplicationEventPublisher eventPublisher) {
        this.nitriteService = nitriteService;
        this.traktDatabaseService = traktDatabaseService;
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
        findHistoryItemCache = CacheBuilder.newBuilder()
                .expireAfterWrite(300, TimeUnit.SECONDS)
                .softValues()
                .build(new CacheLoader<Program, Iterable<HistoryItem>>() {

                    @Override
                    public Iterable<HistoryItem> load(Program program) throws Exception {
                        return findHistoryItemInteral(program);
                    }

                });
    }

    @EventListener
    @Order(EVENT_LISTENER_ORDER)
    private void onActivation(ActivationEvent event) {
        Nitrite database = nitriteService.getDatabase();

        logger.info("Creating plugin database");
        this.database = new PluginDatabase(database);
        changeListeners.register(ProgramMapping.class, new ChangeListener() {

            @Override
            public void onChange(ChangeInfo changeInfo) {
                eventPublisher.publishEvent(new DatabaseChangeEvent());
            }

        });
    }

    @EventListener
    @Order(-EVENT_LISTENER_ORDER)
    private void onDeactivation(DeactivationEvent event) {
        logger.info("Closing plugin database");
        changeListeners.deregister();
        database.close();
        database = null;
    }

    @EventListener
    private void onDatabaseChange(DatabaseChangeEvent event) {
        logger.info("Database has changed, flushing caches");

        findHistoryItemCache.invalidateAll();
    }

    public PluginDatabase getDatabase() {
        Preconditions.checkState(database != null, "Trying to use database while not active.");

        return database;
    }

    public Iterable<HistoryItem> findHistoryItem(Program program) {
        return findHistoryItemCache.getUnchecked(program);
    }

    private Iterable<HistoryItem> findHistoryItemInteral(Program program) {
        TraktDatabase traktDatabase = traktDatabaseService.getDatabase();

        try {
            List<ObjectFilter> filters = new ArrayList<>();
            switch (getProgramType(program)) {
            case MOVIE:
                String movieTraktId = database.findMovieId(program);
                if (movieTraktId != null) {
                    filters.add(ObjectFilters.eq("movie.ids.trakt", movieTraktId));
                } else {
                    List<String> titles = new ArrayList<String>();
                    addTitle(titles, program.getTextField(ProgramFieldType.TITLE_TYPE));
                    addTitle(titles, program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));
                    filters.add(toOrFilter("movie.title", titles, false));
                    int year = program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE);
                    filters.add(ObjectFilters.eq("movie.year", year));
                }
                break;
            case SHOW:
                String episodeTraktId = database.findEpisodeId(program);
                if (episodeTraktId != null) {
                    filters.add(ObjectFilters.eq("episode.ids.trakt", episodeTraktId));
                } else {
                    String showTraktId = database.findShowId(program);
                    if (showTraktId != null) {
                        filters.add(ObjectFilters.eq("show.ids.trakt", showTraktId));
                    } else {
                        List<String> titles = new ArrayList<String>();
                        addTitle(titles, program.getTextField(ProgramFieldType.TITLE_TYPE));
                        addTitle(titles, program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));
                        filters.add(toOrFilter("show.title", titles, false));
                    }
                    List<String> episodes = new ArrayList<>();
                    addEpisode(episodes, program.getTextField(ProgramFieldType.EPISODE_TYPE));
                    addEpisode(episodes, program.getTextField(ProgramFieldType.ORIGINAL_EPISODE_TYPE));
                    filters.add(toOrFilter("episode.title", episodes, true));
                }
                break;
            default:
                return Collections.emptyList();
            }

            ObjectFilter filter = and(toArray(filters, ObjectFilter.class));
            logger.debug("Searching for history item with filter {}", filter);
            return traktDatabase.findHistoryItems(filter);
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unused")
    private ObjectFilter toOrFilter(String field, String[] values, boolean ignoreCase) {
        return toOrFilter(field, asList(values), ignoreCase);
    }

    private ObjectFilter toOrFilter(String field, List<String> values, boolean ignoreCase) {
        List<ObjectFilter> filters = new ArrayList<>();
        for (String value : values) {
            if (isBlank(value)) {
                continue;
            }
            value = value.replaceAll("\\*", "");
            ObjectFilter filter;
            if (ignoreCase) {
                filter = eqIgnoreCase(field, value);
            } else {
                filter = eq(field, value);
            }
            filters.add(filter);
        }
        if (filters.isEmpty()) {
            throw new IllegalArgumentException("No filter created for values");
        }
        if (filters.size() == 1) {
            return firstOrDefault(filters);
        }
        return or(toArray(filters, ObjectFilter.class));
    }

}
