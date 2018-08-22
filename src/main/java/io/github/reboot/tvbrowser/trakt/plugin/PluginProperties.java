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
package io.github.reboot.tvbrowser.trakt.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import devplugin.Version;

public class PluginProperties {

    private static String version;

    private static String traktClientId;

    private static String traktClientSecret;

    static {
        Properties properties = new Properties();
        try (InputStream inputStream = PluginProperties.class.getResourceAsStream("plugin.properties")) {
            properties.load(inputStream);

            version = properties.getProperty("version");
            traktClientId = properties.getProperty("trakt.clientId");
            traktClientSecret = properties.getProperty("trakt.clientSecret");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Version getVersion() {
        String[] split = version.split("[.-]");
        int major = Integer.parseInt(split[0]);
        int minor = Integer.parseInt(split[1]) * 10 + Integer.parseInt(split[2]);
        boolean beta = !version.endsWith("-SNAPSHOT");
        return new Version(major, minor, beta);
    }

    public static String getTraktClientId() {
        return traktClientId;
    }

    public static String getTraktClientSecret() {
        return traktClientSecret;
    }

}
