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
package io.github.reboot.tvbrowser.trakt.settings;

import io.github.reboot.tvbrowser.trakt.client.TraktClientService;
import io.github.reboot.tvbrowser.trakt.plugin.Plugin;
import io.github.reboot.tvbrowser.trakt.settings.SettingsUpdatedEvent.Scope;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private static final String TRAKT_ACCESS_TOKEN = "trakt.accessToken";

    private static final String TRAKT_REFRESH_TOKEN = "trakt.refreshToken";

    private final Logger logger = LoggerFactory.getLogger(TraktClientService.class);

    private final Plugin plugin;

    private final ApplicationEventPublisher eventPublisher;

    private String traktAccessToken;

    private String traktRefreshToken;

    @Autowired
    SettingsService(@Lazy Plugin plugin, ApplicationEventPublisher eventPublisher) {
        this.plugin = plugin;
        this.eventPublisher = eventPublisher;
    }

    public void loadSettings(Properties settings) {
        logger.debug("Loading settings");

        traktAccessToken = settings.getProperty(TRAKT_ACCESS_TOKEN);
        traktRefreshToken = settings.getProperty(TRAKT_REFRESH_TOKEN);

        eventPublisher.publishEvent(new SettingsUpdatedEvent(Scope.values()));
    }

    public Properties storeSettings() {
        logger.debug("Storing settings");

        Properties properties = new Properties();
        if (traktAccessToken != null) {
            properties.setProperty(TRAKT_ACCESS_TOKEN, traktAccessToken);
        }
        if (traktRefreshToken != null) {
            properties.setProperty(TRAKT_REFRESH_TOKEN, traktRefreshToken);
        }
        return properties;
    }

    public String getTraktAccessToken() {
        return traktAccessToken;
    }

    public String getTraktRefreshToken() {
        return traktRefreshToken;
    }

    public void setAccessToken(String accessToken, String refreshToken, boolean save) {
        traktAccessToken = accessToken;
        traktRefreshToken = refreshToken;

        eventPublisher.publishEvent(new SettingsUpdatedEvent(Scope.TRAKT_ACCOUNT));

        if (save) {
            plugin.saveMe();
        }
    }

    public void clearAccessToken() {
        traktAccessToken = null;
        traktRefreshToken = null;

        eventPublisher.publishEvent(new SettingsUpdatedEvent(Scope.TRAKT_ACCOUNT));
    }

}
