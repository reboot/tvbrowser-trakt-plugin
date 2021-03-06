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

import static io.github.reboot.tvbrowser.trakt.utils.StringUtils.addReplacements;
import static java.util.Collections.unmodifiableMap;
import static java.util.regex.Pattern.compile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TitleUtils {

    private static final Map<Pattern, String> TITLE_REPLACEMENTS;
    static {
        Map<Pattern, String> titleReplacements = new HashMap<>();
        titleReplacements.put(compile(" - "), ": ");
        TITLE_REPLACEMENTS = unmodifiableMap(titleReplacements);
    }

    public static void addTitle(List<String> titles, String value) {
        addReplacements(TITLE_REPLACEMENTS, titles, value);
    }

}
