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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static void addReplacements(Map<Pattern, String> replacements, List<String> values, String value) {
        if (isBlank(value)) {
            return;
        }
        values.add(value);
        for (Map.Entry<Pattern, String> entry : replacements.entrySet()) {
            Pattern pattern = entry.getKey();
            String replacement = entry.getValue();

            Matcher matcher = pattern.matcher(value);
            StringBuffer stringBuilder = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(stringBuilder, replacement);
            }
            matcher.appendTail(stringBuilder);
            String newValue = stringBuilder.toString();
            if (value.equals(newValue)) {
                continue;
            }
            values.add(newValue);
        }
    }

}
