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

import static org.dizitart.no2.IndexOptions.indexOptions;
import io.github.reboot.tvbrowser.trakt.database.ProgramMapping.Type;

import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.event.ChangeListener;
import org.dizitart.no2.objects.ObjectFilter;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import devplugin.Program;
import devplugin.ProgramFieldType;

public class PluginDatabase {

    private final Logger logger = LoggerFactory.getLogger(PluginDatabase.class);

    private final Nitrite database;

    private final ObjectRepository<ProgramMapping> programMappingRepository;

    PluginDatabase(Nitrite database) {
        this.database = database;

        programMappingRepository = database.getRepository(ProgramMapping.class);

        if (!programMappingRepository.hasIndex("title")) {
            programMappingRepository.createIndex("title", indexOptions(IndexType.NonUnique));
        }
        if (!programMappingRepository.hasIndex("episode")) {
            programMappingRepository.createIndex("episode", indexOptions(IndexType.NonUnique));
        }
    }

    public void register(Class<?> itemClass, ChangeListener changeListener) {
        if (ProgramMapping.class.equals(itemClass)) {
            programMappingRepository.register(changeListener);
        }
    }

    public void deregister(Class<?> itemClass, ChangeListener changeListener) {
        if (ProgramMapping.class.equals(itemClass)) {
            programMappingRepository.deregister(changeListener);
        }
    }

    public void commit() {
        database.commit();
    }

    public void close() {
        programMappingRepository.close();
    }

    private String findTraktId(ObjectFilter filter) {
        ProgramMapping programMapping = programMappingRepository.find(filter)
                .firstOrDefault();
        if (programMapping == null) {
            return null;
        }
        return programMapping.getTraktId();
    }

    public String findMovieId(Program program) {
        String title = program.getTextField(ProgramFieldType.TITLE_TYPE);
        if (title == null) {
            return null;
        }
        int year = program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE);

        ObjectFilter filter = ObjectFilters.and(
                ObjectFilters.eq("title", title),
                ObjectFilters.eq("year", year),
                ObjectFilters.eq("type", Type.MOVIE));
        return findTraktId(filter);
    }

    public String findShowId(Program program) {
        String title = program.getTextField(ProgramFieldType.TITLE_TYPE);
        if (title == null) {
            return null;
        }

        ObjectFilter filter = ObjectFilters.and(
                ObjectFilters.eq("title", title),
                ObjectFilters.eq("type", Type.SHOW));
        return findTraktId(filter);
    }

    public String findEpisodeId(Program program) {
        String title = program.getTextField(ProgramFieldType.TITLE_TYPE);
        if (title == null) {
            return null;
        }
        String episode = program.getTextField(ProgramFieldType.EPISODE_TYPE);
        if (episode == null) {
            return null;
        }

        ObjectFilter filter = ObjectFilters.and(
                ObjectFilters.eq("title", title),
                ObjectFilters.eq("episode", episode),
                ObjectFilters.eq("type", Type.EPISODE));
        return findTraktId(filter);
    }

    public void addProgramMapping(Program program, Type type, String traktId) {
        logger.debug("Adding mapping for program {} to {} {}", program.getID(), type, traktId);

        ProgramMapping programMapping = new ProgramMapping();
        programMapping.setTitle(program.getTextField(ProgramFieldType.TITLE_TYPE));
        if (type == Type.MOVIE) {
            programMapping.setYear(program.getIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE));
        } else if (type == Type.SHOW) {
            // Nothing to add
        } else if (type == Type.EPISODE) {
            programMapping.setEpisode(program.getTextField(ProgramFieldType.EPISODE_TYPE));
        } else {
            return;
        }
        programMapping.setType(type);
        programMapping.setTraktId(traktId);

        ObjectFilter filter = ObjectFilters.and(
                ObjectFilters.eq("type", type),
                ObjectFilters.eq("traktId", traktId));
        programMappingRepository.update(filter, programMapping, true);
    }

    public void deleteProgramMapping(Type type, String traktId) {
        ObjectFilter filter = ObjectFilters.and(
                ObjectFilters.eq("type", type),
                ObjectFilters.eq("traktId", traktId));
        programMappingRepository.remove(filter);
    }

}
