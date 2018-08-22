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

import static java.util.Collections.unmodifiableMap;
import static java.util.regex.Pattern.compile;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class EpisodeUtils {

    private static final Map<Pattern, String> EPISODE_REPLACEMENTS;
    static {
        Map<Pattern, String> episodeReplacements = new HashMap<>();
        episodeReplacements.put(compile(" - Part ([0-9]+)$"), " ($1)");
        episodeReplacements.put(compile(", Pt\\. ([0-9]+)$"), " ($1)");
        episodeReplacements.put(compile(" \\([0-9]+\\)$"), "");
        episodeReplacements.put(compile("^\\(.*?\\) "), "");
        EPISODE_REPLACEMENTS = unmodifiableMap(episodeReplacements);
    }

    public static final NumberFormat EPISODE_NUMBER_FORMAT;
    static {
        EPISODE_NUMBER_FORMAT = NumberFormat.getIntegerInstance();
        EPISODE_NUMBER_FORMAT.setMinimumIntegerDigits(2);
    }

    public static void addEpisode(List<String> episodes, String value) {
        StringUtils.addReplacements(EPISODE_REPLACEMENTS, episodes, value);
    }

}
