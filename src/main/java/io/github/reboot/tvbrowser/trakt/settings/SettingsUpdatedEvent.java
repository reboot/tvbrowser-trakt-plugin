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

import java.util.List;

import com.google.common.collect.ImmutableList;

public class SettingsUpdatedEvent {

    private List<Scope> scopes;

    SettingsUpdatedEvent(Scope... scopes) {
        this.scopes = ImmutableList.copyOf(scopes);
    }

    public boolean isScope(Scope scope) {
        return scopes.contains(scope);
    }

    public enum Scope {

        TRAKT_ACCOUNT,

    }

}
