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
package traktplugin;

import io.github.reboot.tvbrowser.trakt.plugin.PluginProperties;

import java.awt.Frame;
import java.io.File;
import java.util.Properties;

import org.apache.logging.log4j.core.lookup.MainMapLookup;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import devplugin.ActionMenu;
import devplugin.ImportanceValue;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramInfo;
import devplugin.SettingsTab;
import devplugin.Version;

public class TraktPlugin extends Plugin implements PluginSuper {

    private io.github.reboot.tvbrowser.trakt.plugin.PluginImpl impl;

    public TraktPlugin() {
        File tvBrowserUserHome = new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome());
        File pluginDataDir = new File(tvBrowserUserHome, "trakt");
        pluginDataDir.mkdirs();
        MainMapLookup.setMainArguments(pluginDataDir.getAbsolutePath());

        Properties properties = new Properties();
        properties.put("plugin.dataDir", pluginDataDir.getAbsolutePath());

        PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertyPlaceholderConfigurer.setProperties(properties);

        @SuppressWarnings("resource")
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setClassLoader(TraktPlugin.class.getClassLoader());
        context.getBeanFactory().registerSingleton("traktplugin.TraktPlugin", this);
        context.getBeanFactory().registerSingleton("traktplugin.TraktPlugin.properties", propertyPlaceholderConfigurer);
        context.scan("io.github.reboot.tvbrowser.trakt");
        context.refresh();

        impl = context.getBean(io.github.reboot.tvbrowser.trakt.plugin.PluginImpl.class);
    }

    public static Version getVersion() {
        return PluginProperties.getVersion();
    }

    @Override
    public ProgramInfo[] getAddtionalProgramInfoForProgram(Program program, String uniqueId) {
        return impl.getAddtionalProgramInfoForProgram(program, uniqueId);
    }

    @Override
    public ActionMenu getButtonAction() {
        return impl.getButtonAction();
    }

    @Override
    public ActionMenu getContextMenuActions(Program program) {
        return impl.getContextMenuActions(program);
    }

    @Override
    public ImportanceValue getImportanceValueForProgram(Program program) {
        return impl.getImportanceValueForProgram(program);
    }

    @Override
    public PluginInfo getInfo() {
        return impl.getInfo();
    }

    public SettingsTab getSettingsTab() {
        return impl.getSettingsTab();
    }

    @Override
    public void loadSettings(Properties settings) {
        impl.loadSettings(settings);
    }

    @Override
    public void onActivation() {
        impl.onActivation();
    }

    @Override
    public void onDeactivation() {
        impl.onDeactivation();
    }

    @Override
    public Properties storeSettings() {
        return impl.storeSettings();
    }

    @Override
    public ImportanceValue super_getImportanceValueForProgram(Program program) {
        return super.getImportanceValueForProgram(program);
    }

    @Override
    public Frame super_getParentFrame() {
        return super.getParentFrame();
    }

    @Override
    public boolean super_saveMe() {
        return super.saveMe();
    }

}
