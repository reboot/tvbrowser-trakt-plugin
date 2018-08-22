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

import io.github.reboot.tvbrowser.trakt.buttons.ButtonsFactory;
import io.github.reboot.tvbrowser.trakt.contextmenu.ContextMenuFactory;
import io.github.reboot.tvbrowser.trakt.importance.ImportanceFactory;
import io.github.reboot.tvbrowser.trakt.programinfo.ProgramInfoFactory;
import io.github.reboot.tvbrowser.trakt.settings.SettingsService;
import io.github.reboot.tvbrowser.trakt.settings.SettingsTabFactory;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import traktplugin.PluginDelegate;
import devplugin.ActionMenu;
import devplugin.ImportanceValue;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramInfo;
import devplugin.SettingsTab;

@Component
public class Plugin extends PluginDelegate {

    private final Logger logger = LoggerFactory.getLogger(Plugin.class);

    private final SettingsService settingsService;

    private final ApplicationEventPublisher eventPublisher;

    private final ButtonsFactory buttonsFactory;

    private final ContextMenuFactory contextMenuFactory;

    private final ImportanceFactory importanceFactory;

    private final ProgramInfoFactory programInfoFactory;

    private final SettingsTabFactory settingsTabFactory;

    private boolean active;

    @Autowired
    Plugin(traktplugin.TraktPlugin plugin,
           SettingsService settingsService,
           ApplicationEventPublisher eventPublisher,
           ButtonsFactory buttonsFactory,
           ContextMenuFactory contextMenuFactory,
           ImportanceFactory importanceFactory,
           ProgramInfoFactory programInfoFactory,
           SettingsTabFactory settingsTabFactory) {
        super(plugin);

        this.settingsService = settingsService;
        this.eventPublisher = eventPublisher;
        this.buttonsFactory = buttonsFactory;
        this.contextMenuFactory = contextMenuFactory;
        this.importanceFactory = importanceFactory;
        this.programInfoFactory = programInfoFactory;
        this.settingsTabFactory = settingsTabFactory;

        logger.info("Trakt Plugin loaded");
    }

    public PluginInfo getInfo() {
        return new PluginInfo(traktplugin.TraktPlugin.class, "Trakt");
    }

    public void onActivation() {
        if (active) {
            return;
        }

        eventPublisher.publishEvent(new ActivationEvent());

        active = true;
    }

    public void onDeactivation() {
        if (!active) {
            return;
        }

        eventPublisher.publishEvent(new DeactivationEvent());

        active = false;
    }

    public ActionMenu getButtonAction() {
        if (!active) {
            return null;
        }

        return buttonsFactory.create();
    }

    public ImportanceValue getImportanceValueForProgram(Program program) {
        if (!active) {
            return super.getImportanceValueForProgram(program);
        }

        ImportanceValue importanceValue = importanceFactory.create(program);
        if (importanceValue == null) {
            return super.getImportanceValueForProgram(program);
        }

        return importanceValue;
    }

    public ProgramInfo[] getAddtionalProgramInfoForProgram(Program program, String uniqueId) {
        if (!active) {
            return null;
        }

        return programInfoFactory.create(program, uniqueId);
    }

    public ActionMenu getContextMenuActions(Program program) {
        if (!active) {
            return null;
        }

        return contextMenuFactory.create(program);
    }

    public SettingsTab getSettingsTab() {
        return settingsTabFactory.create();
    }

    public void loadSettings(Properties settings) {
        settingsService.loadSettings(settings);
    }

    public Properties storeSettings() {
        return settingsService.storeSettings();
    }

}
