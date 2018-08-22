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
package io.github.reboot.tvbrowser.trakt.ui.search;

import static io.github.reboot.tvbrowser.trakt.utils.EpisodeUtils.EPISODE_NUMBER_FORMAT;
import io.github.reboot.trakt.api.client.FiltersParameter;
import io.github.reboot.trakt.api.client.Paginated;
import io.github.reboot.trakt.api.client.PaginationParameter;
import io.github.reboot.trakt.api.client.TraktClient;
import io.github.reboot.trakt.api.client.TraktClientException;
import io.github.reboot.trakt.api.json.SearchResponse;
import io.github.reboot.trakt.api.json.SearchResult;
import io.github.reboot.trakt.api.json.SearchResult.Type;
import io.github.reboot.trakt.api.json.common.Episode;
import io.github.reboot.trakt.api.json.common.Movie;
import io.github.reboot.tvbrowser.trakt.client.TraktClientAction;
import io.github.reboot.tvbrowser.trakt.client.TraktClientService;
import io.github.reboot.tvbrowser.trakt.i18n.MessageService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
class SearchTableModel extends AbstractTableModel {

    private final static int PAGE_SIZE = 10;

    private Logger logger = LoggerFactory.getLogger(SearchTableModel.class);

    private final TraktClientService traktClientService;

    private final MessageService messageService;

    private final String query;

    private final Collection<Type> types;

    private Set<Integer> pendingPageLoads;

    private SearchResult[] searchResults = null;

    public SearchTableModel(TraktClientService traktClientService, MessageService messageService, String query, Collection<Type> types) {
        this.traktClientService = traktClientService;
        this.messageService = messageService;
        this.query = query;
        this.types = types;

        this.pendingPageLoads = new HashSet<Integer>();
    }

    public String getQuery() {
        return query;
    }

    @Override
    public int getRowCount() {
        if (searchResults == null) {
            loadPage(1);

            return 0;
        }
        return searchResults.length;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0:
            return messageService.getMessage("ui.search.title");
        case 1:
            return messageService.getMessage("ui.search.episode");
        case 2:
            return messageService.getMessage("ui.search.type");
        case 3:
            return messageService.getMessage("ui.search.traktId");
        default:
            throw new AssertionError(column);
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SearchResult searchResult = getRow(rowIndex);
        if (searchResult == null) {
            return null;
        }

        switch (columnIndex) {
        case 0:
            switch (searchResult.getType()) {
            case MOVIE:
                Movie movie = searchResult.getMovie();
                return movie.getTitle() + " (" + movie.getYear() + ")";
            case SHOW:
            case EPISODE:
                return searchResult.getShow().getTitle();
            default:
                return null;
            }
        case 1:
            switch (searchResult.getType()) {
            case EPISODE:
                Episode episode = searchResult.getEpisode();
                return episode.getTitle() + " (" + episode.getSeason() + "x" + EPISODE_NUMBER_FORMAT.format(episode.getNumber()) + ")";
            default:
                return null;
            }
        case 2:
            return searchResult.getType();
        case 3:
            switch (searchResult.getType()) {
            case MOVIE:
                return searchResult.getMovie().getIds().getTrakt();
            case SHOW:
                return searchResult.getShow().getIds().getTrakt();
            case EPISODE:
                return searchResult.getEpisode().getIds().getTrakt();
            default:
                return null;
            }
        default:
            throw new AssertionError(columnIndex);
        }
    }

    SearchResult getRow(int rowIndex) {
        if (rowIndex < 0) {
            return null;
        }
        if (rowIndex >= searchResults.length) {
            return null;
        }
        if (!isRowLoaded(rowIndex)) {
            int page = (rowIndex / PAGE_SIZE) + 1;
            loadPage(page);
            return null;
        }
        return searchResults[rowIndex];
    }

    private boolean isRowLoaded(int rowIndex) {
        // No data has been loaded yet
        if (searchResults == null) {
            return false;
        }
        // Data has been loaded but row does not exist
        if (rowIndex >= searchResults.length) {
            return true;
        }
        return searchResults[rowIndex] != null;
    }

    private void loadPage(int page) {
        synchronized (pendingPageLoads) {
            if (!pendingPageLoads.add(page)) {
                return;
            }

            PaginationParameter paginationParameter = new PaginationParameter(page, PAGE_SIZE);
            FiltersParameter filtersParameter = new FiltersParameter(query);
            TraktClientAction action = new TraktClientAction() {

                @Override
                public void execute(TraktClient traktClient) throws TraktClientException {
                    Paginated<SearchResponse> search = traktClient.search(types, paginationParameter, filtersParameter);

                    processSearch(search);

                    synchronized (pendingPageLoads) {
                        pendingPageLoads.remove(page);
                    }
                }

            };
            boolean queued = traktClientService.execute(action);
            if (!queued) {
                pendingPageLoads.remove(page);
            }
        }
    }

    private void processSearch(Paginated<SearchResponse> search) {
        if (search == null) {
            logger.warn("Search did not return a result");

            searchResults = new SearchResult[0];
            fireTableDataChanged();
            return;
        }
        logger.debug("{} / {} ({} total items with {} per page)", search.getPage(), search.getPageCount(), search.getItemCount(), search.getLimit());
        if (searchResults == null) {
            // || search.getItemCount() != searchResults.length
            searchResults = new SearchResult[search.getItemCount()];
        }
        int index = ((search.getPage() - 1) * PAGE_SIZE);
        for (SearchResult searchResult : search.getResponse()) {
            searchResults[index] = searchResult;
            index++;
            if (index >= searchResults.length) {
                break;
            }
        }
        fireTableDataChanged();
    }

}
