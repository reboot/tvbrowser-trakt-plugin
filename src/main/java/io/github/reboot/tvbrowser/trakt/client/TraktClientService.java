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
package io.github.reboot.tvbrowser.trakt.client;

import static io.github.reboot.tvbrowser.trakt.plugin.PluginProperties.getTraktClientId;
import static io.github.reboot.tvbrowser.trakt.plugin.PluginProperties.getTraktClientSecret;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import io.github.reboot.trakt.api.client.ServerOverloadedException;
import io.github.reboot.trakt.api.client.TraktClient;
import io.github.reboot.trakt.api.client.TraktClientException;
import io.github.reboot.trakt.api.client.UnauthorizedException;
import io.github.reboot.trakt.api.json.oauth.TokenRequest.GrantType;
import io.github.reboot.trakt.api.json.oauth.TokenResponse;
import io.github.reboot.trakt.api.json.users.SettingsResponse;
import io.github.reboot.tvbrowser.trakt.plugin.ActivationEvent;
import io.github.reboot.tvbrowser.trakt.plugin.DeactivationEvent;
import io.github.reboot.tvbrowser.trakt.settings.SettingsService;
import io.github.reboot.tvbrowser.trakt.settings.SettingsUpdatedEvent;
import io.github.reboot.tvbrowser.trakt.settings.SettingsUpdatedEvent.Scope;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class TraktClientService {

    private final Logger logger = LoggerFactory.getLogger(TraktClientService.class);

    private final SettingsService settingsService;

    private final ScheduledExecutorService executorService;

    private TraktClient traktClient;

    private SettingsResponse settings;

    @Autowired
    TraktClientService(SettingsService settingsService, ScheduledExecutorService executorService) {
        this.settingsService = settingsService;
        this.executorService = executorService;
    }

    @EventListener
    private void onActivation(ActivationEvent event) {
        logger.info("Creating Trakt client");
        traktClient = new TraktClient(getTraktClientId(), getTraktClientSecret());
    }

    @EventListener
    private void onDeactivation(DeactivationEvent event) {
        logger.info("Destroying Trakt client");
        traktClient = null;
    }

    @EventListener
    private void onSettingsUpdated(SettingsUpdatedEvent event) {
        if (!event.isScope(Scope.TRAKT_ACCOUNT)) {
            return;
        }

        logger.info("Reconfiguring due to settings update");

        String accessToken = settingsService.getTraktAccessToken();
        if (isNotBlank(accessToken)) {
            traktClient.setAccessToken(accessToken);

            executorService.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        settings = traktClient.users().settings();

                        logger.info("Successfully logged in as {}", getAccount());
                    } catch (ServerOverloadedException e) {
                        executorService.schedule(this, e.getRetryDelay(), TimeUnit.SECONDS);
                    } catch (TraktClientException e) {
                        settings = null;
                    }
                }

            });
        } else {
            traktClient.clearAccessToken();

            settings = null;
        }
    }

    public String getAccount() {
        if (settings != null) {
            return format("{0} ({1})", settings.getUser().getName(), settings.getUser().getUsername());
        } else {
            return "Not logged in";
        }
    }

    public boolean execute(TraktClientAction action) {
        return execute(action, true);
    }

    public boolean execute(TraktClientAction action, boolean requiresLogin) {
        return execute(action, requiresLogin, new DefaultUncaughtExceptionHandler());
    }

    public boolean execute(TraktClientAction action, boolean requiresLogin, UncaughtExceptionHandler exceptionHandler) {
        if (traktClient == null) {
            logger.warn("Trying to execute action {} without client", action);

            return false;
        }
        if (requiresLogin && (settings == null)) {
            logger.warn("Trying to execute action {} without being logged in", action);

            return false;
        }

        executorService.submit(new Runnable() {

            @Override
            public void run() {
                executeAction(action, exceptionHandler);
            }

        });
        return true;
    }

    private void executeAction(TraktClientAction action, UncaughtExceptionHandler exceptionHandler) {
        try {
            action.execute(traktClient);
        } catch (TraktClientException e) {
            exceptionHandler.handleException(action, e);
        }
    }

    public AuthenticationContext authenticate(AuthenticationCallback callback) {
        AuthenticationHandler authenticationHandler = new AuthenticationHandler(this, executorService, callback);
        authenticationHandler.start();
        return authenticationHandler;
    }

    private void refreshToken(TraktClientAction action) {
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    String refreshToken = settingsService.getTraktRefreshToken();
                    if (isNotBlank(refreshToken)) {
                        TokenResponse response = traktClient.oauth().token(
                                GrantType.REFRESH_TOKEN,
                                refreshToken,
                                "urn:ietf:wg:oauth:2.0:oob");

                        logger.info("Successfully refreshed access token");
                        settingsService.setAccessToken(response.getAccessToken(), response.getRefreshToken(), true);

                        executeAction(action, new DefaultUncaughtExceptionHandler());
                    } else {
                        settingsService.clearAccessToken();
                    }
                } catch (TraktClientException e) {
                    settingsService.clearAccessToken();
                }
            }

        });

    }

    private final class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
        @Override
        public void handleException(TraktClientAction action, TraktClientException exception) {
            if (exception instanceof ServerOverloadedException) {
                ServerOverloadedException serverOverloadedException = (ServerOverloadedException) exception;

                executorService.schedule(new Runnable() {

                    @Override
                    public void run() {
                        executeAction(action, DefaultUncaughtExceptionHandler.this);
                    }

                }, serverOverloadedException.getRetryDelay(), TimeUnit.SECONDS);
            } else if (exception instanceof UnauthorizedException) {
                refreshToken(action);
            } else {
                logger.warn("Executing the Trakt action caused an uncaught client exception", exception);
            }
        }
    }

}
