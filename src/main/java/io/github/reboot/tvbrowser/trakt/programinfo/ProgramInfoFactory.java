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
package io.github.reboot.tvbrowser.trakt.programinfo;

import static io.github.reboot.tvbrowser.trakt.programinfo.ProgramInfoId.PAGE;
import static io.github.reboot.tvbrowser.trakt.programinfo.ProgramInfoId.WATCHED_AT;
import static io.github.reboot.tvbrowser.trakt.utils.ProgramUtils.isExampleProgram;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import io.github.reboot.trakt.api.json.sync.HistoryItem;
import io.github.reboot.tvbrowser.trakt.database.PluginDatabaseService;
import io.github.reboot.tvbrowser.trakt.plugin.Plugin;
import io.github.reboot.tvbrowser.trakt.utils.EpisodeUtils;

import java.util.ArrayList;
import java.util.List;

import org.dizitart.no2.util.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import devplugin.Program;
import devplugin.ProgramInfo;

@Service
public class ProgramInfoFactory {

    private final Plugin plugin;

    private final PluginDatabaseService pluginDatabaseService;

    @Autowired
    ProgramInfoFactory(@Lazy Plugin plugin, PluginDatabaseService pluginDatabaseService) {
        this.plugin = plugin;
        this.pluginDatabaseService = pluginDatabaseService;
    }

    public ProgramInfo[] create(Program program, String uniqueId) {
        List<ProgramInfo> programInfos = new ArrayList<>();

        if (!isExampleProgram(program)) {
            Iterable<HistoryItem> historyItems = pluginDatabaseService.findHistoryItem(program);

            if (WATCHED_AT.matches(uniqueId)) {
                StringBuilder value = new StringBuilder();
                for (HistoryItem historyItem : historyItems) {
                    if (value.length() > 0) {
                        value.append("<br/>");
                    }
                    value.append(historyItem.getWatchedAt().toString());
                }
                if (value.length() > 0) {
                    programInfos.add(new ProgramInfo(plugin.getPlugin(), WATCHED_AT.getUniqueId(), "Watched At", value.toString()));
                }
            }
            if (PAGE.matches(uniqueId)) {
                HistoryItem historyItem = Iterables.firstOrDefault(historyItems);
                if (historyItem != null) {
                    StringBuilder name = new StringBuilder();
                    StringBuilder path = new StringBuilder();
                    switch (historyItem.getType()) {
                    case MOVIE:
                        name.append(historyItem.getMovie().getTitle());
                        name.append(" (");
                        name.append(historyItem.getMovie().getYear());
                        name.append(")");

                        path.append("/movies/");
                        path.append(historyItem.getMovie().getIds().getSlug());
                        break;
                    case EPISODE:
                        name.append(historyItem.getShow().getTitle());
                        name.append(" ");
                        name.append(historyItem.getEpisode().getSeason());
                        name.append("x");
                        name.append(EpisodeUtils.EPISODE_NUMBER_FORMAT.format(historyItem.getEpisode().getNumber()));
                        name.append(" \"");
                        name.append(historyItem.getEpisode().getTitle());
                        name.append("\"");

                        path.append("/shows/");
                        path.append(historyItem.getShow().getIds().getSlug());
                        path.append("/seasons/");
                        path.append(historyItem.getEpisode().getSeason());
                        path.append("/episodes/");
                        path.append(historyItem.getEpisode().getNumber());
                        break;
                    default:
                        break;
                    }

                    if (path.length() > 0) {
                        StringBuilder value = new StringBuilder();
                        value.append("<a href=\"https://trakt.tv");
                        value.append(path.toString());
                        value.append("\">");
                        value.append(escapeHtml4(name.toString()));
                        value.append("</a>");

                        programInfos.add(new ProgramInfo(plugin.getPlugin(), PAGE.getUniqueId(), "Trakt Page", value.toString()));
                    }
                }
            }
        } else {
            programInfos.add(new ProgramInfo(plugin.getPlugin(), WATCHED_AT.getUniqueId(), "Watched At", ""));
            programInfos.add(new ProgramInfo(plugin.getPlugin(), PAGE.getUniqueId(), "Trakt Page", ""));
        }

        return programInfos.toArray(new ProgramInfo[programInfos.size()]);
    }

}
