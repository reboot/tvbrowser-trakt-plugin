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

import io.github.reboot.trakt.api.client.TraktClient;
import io.github.reboot.trakt.api.client.TraktClientException;
import io.github.reboot.trakt.api.json.oauth.device.CodeResponse;
import io.github.reboot.trakt.api.json.oauth.device.TokenResponse;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AuthenticationHandler implements AuthenticationContext {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationHandler.class);

    private final TraktClientService traktClientService;

    private final ScheduledExecutorService executorService;

    private final AuthenticationCallback callback;

    private boolean aborted;

    AuthenticationHandler(TraktClientService traktClientService, ScheduledExecutorService executorService, AuthenticationCallback callback) {
        this.traktClientService = traktClientService;
        this.executorService = executorService;
        this.callback = callback;
    }

    @Override
    public void abort() {
        this.aborted = true;
    }

    void start() {
        TraktClientAction action = new TraktClientAction() {

            @Override
            public void execute(TraktClient traktClient) throws TraktClientException {
                if (aborted) {
                    return;
                }

                CodeResponse response = traktClient.oauth().device().code();
                int interval = response.getInterval();
                int expiresIn = response.getExpiresIn();

                callback.code(response.getUserCode(), response.getVerificationURL(), expiresIn);

                logger.debug("Fetching device token in {} seconds", interval);
                pollDeviceToken(response.getDeviceCode(), interval, expiresIn);
            }

        };
        traktClientService.execute(action, false);
    }

    private void pollDeviceToken(String deviceCode, int interval, int expiresIn) {
        if (expiresIn <= 0) {
            callback.timeout();
            return;
        }

        TraktClientAction action = new TraktClientAction() {

            @Override
            public void execute(TraktClient traktClient) throws TraktClientException {
                TokenResponse response = traktClient.oauth().device().token(deviceCode);
                if (response == null) {
                    logger.debug("No device token received, trying in {} seconds", interval);
                    pollDeviceToken(deviceCode, interval, expiresIn - interval);
                    return;
                }
                callback.success(response.getAccessToken(), response.getRefreshToken());
            }

        };
        UncaughtExceptionHandler exceptionHandler = new UncaughtExceptionHandler() {

            @Override
            public void handleException(TraktClientAction action, TraktClientException exception) {
                callback.failed(exception.getMessage());
            }

        };
        executorService.schedule(new Runnable() {

            @Override
            public void run() {
                if (aborted) {
                    return;
                }

                traktClientService.execute(action, false, exceptionHandler);
            }

        }, interval, TimeUnit.SECONDS);
    }

}
