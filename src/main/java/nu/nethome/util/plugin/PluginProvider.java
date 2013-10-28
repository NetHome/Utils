/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome (http://www.nethome.nu).
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

import java.util.List;
import java.util.Set;

/**
 * User: Stefan
 * Date: 2011-10-10
 * Time: 20:23
 */
public interface PluginProvider {
    /**
     * Return all found plugin classes which implement the specified interface
     *
     * @param theInterface the interface
     * @return List of plugin classes. Empty list if no plugins exists for the interface
     */
    public <T> List<Class<? extends T>> getPluginsForInterface(Class<T> theInterface);

    /**
     * Return a list of all interfaces found on discovered Plugin classes
     *
     * @return list of interfaces
     */
    Set<Class> getFoundInterfaces();
}
