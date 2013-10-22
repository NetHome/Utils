/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome.
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.util.plugin;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * User: Stefan
 * Date: 2011-06-06
 */
public class TestPluginScanner {
    public static void mainXXX(String[] args) {
        try {
            RawPluginScanner pluginScanner = new RawPluginScanner();
            pluginScanner.scanForPlugins(new File("d:/temp/sample"));
            for (Class pluginInterface : pluginScanner.getFoundInterfaces()) {
                List<Class> pluginCollection = pluginScanner.getPluginsForInterface(pluginInterface);
                System.out.println("Interface: " + pluginInterface.getName());
                System.out.println("===============================");
                for (Class plugin : pluginCollection) {
                    System.out.println("Found " + plugin.getName());
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    @Test
    public void dummyTest() {
        assertTrue(true);
    }
}