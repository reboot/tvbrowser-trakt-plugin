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
package io.github.reboot.tvbrowser.trakt.utils;

import static devplugin.Plugin.getPluginManager;
import static org.apache.commons.collections4.CollectionUtils.addIgnoreNull;

import java.util.ArrayList;
import java.util.List;

import devplugin.Program;
import devplugin.ProgramFieldType;

public class ProgramUtils {

    public static List<String> getTitles(Program program) {
        List<String> titles = new ArrayList<>();
        addIgnoreNull(titles, program.getTextField(ProgramFieldType.TITLE_TYPE));
        addIgnoreNull(titles, program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE));
        return titles;
    }

    public static boolean isExampleProgram(Program program) {
        return getPluginManager().getExampleProgram().equals(program);
    }

    public static ProgramType getProgramType(Program program) {
        int info = program.getIntField(ProgramFieldType.INFO_TYPE);
        if ((info & Program.INFO_CATEGORIE_MOVIE) > 0) {
            return ProgramType.MOVIE;
        } else if ((info & Program.INFO_CATEGORIE_SERIES) > 0) {
            return ProgramType.SHOW;
        }
        // Asuming that everything that has been produced over multiple years is a show
        int firstProductionYear = program.getIntField(ProgramFieldType.FIRST_PRODUCTION_YEAR);
        if (firstProductionYear != -1) {
            return ProgramType.SHOW;
        }
        return ProgramType.UNKNOWN;
    }

    public enum ProgramType {

        UNKNOWN,

        MOVIE,

        SHOW,

    }

}
