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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scans a list of directories for plugin jars. All jars found are added to the class path and all jars that
 * are NOT under a directory called "3rdparty" are scanned for plugin classes.
 */
public class SelectivePluginScanner implements PluginProvider {

    private static Logger logger = Logger.getLogger(SelectivePluginScanner.class.getName());

    private String ignoreDirName;
    private String pluginSuffix;
    URLClassLoader classLoader;
    private Map<Class, List<Class>> foundPlugins = new HashMap<Class, List<Class>>();

    public SelectivePluginScanner(String pluginSuffix, String ignoreDirectory) {
        this.pluginSuffix = pluginSuffix;
        this.ignoreDirName = ignoreDirectory;
    }

    public SelectivePluginScanner(){
        this(".jar", "3rdparty");
    }

    public void scanForPlugins(List<File> pluginCandidates) throws IOException {
        List<File> pluginJars = new LinkedList<File>();
        List<URL> urlsForClassPath = new LinkedList<URL>();

        for (File file : pluginCandidates) {
            findJars(file, pluginJars, urlsForClassPath);
        }
        classLoader = new URLClassLoader(urlsForClassPath.toArray(new URL[urlsForClassPath.size()]));

        for (File jarFile : pluginJars) {
            extractPluginsFromJar(jarFile);
        }
    }

    public <T> List<Class<? extends T>> getPluginsForInterface(Class<T> theInterface) {
        if (foundPlugins.containsKey(theInterface)) {
            List<Class<? extends T>> result = new ArrayList<Class<? extends T>>();
            for (Class anInterface : foundPlugins.get(theInterface)) {
                result.add(anInterface);
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public Set<Class> getFoundInterfaces() {
        return Collections.unmodifiableSet(foundPlugins.keySet());
    }

    private void findJars(File file, List<File> pluginJars, List<URL> urlsForPath) {
        if (file.isDirectory()) {
            for (File subfile : file.listFiles()) {
                boolean is3rdPartyDir = file.getName().equalsIgnoreCase(ignoreDirName);
                findJars(subfile, is3rdPartyDir ? null : pluginJars, urlsForPath);
            }
        } else if (file.isFile() && file.getName().endsWith(pluginSuffix)) {
            try {
                urlsForPath.add(file.toURI().toURL());
                if (pluginJars != null) {
                    pluginJars.add(file);
                }
            } catch (MalformedURLException e) {
                // Just ignore
            }
        }
    }

    private void extractPluginsFromJar(File jarFile) throws IOException {
        for (String className : scanJarForClassNames(jarFile)) {
            try {
                Class pluginProspect = classLoader.loadClass(className);
                if (pluginProspect.getAnnotation(Plugin.class) != null) {
                    Class[] interfaces = pluginProspect.getInterfaces();
                    for (Class anInterface : interfaces) {
                        addPluginForInterface(anInterface, pluginProspect);
                    }
                }
            } catch (ClassNotFoundException e) {
                logger.log(Level.WARNING, "Could not find plugin class: " + className, e);
            } catch (NoClassDefFoundError e) {
                logger.log(Level.WARNING, "Could not create plugin class: " + className, e);
            }
        }
    }

    private void addPluginForInterface(Class theInterface, Class plugin) {
        List<Class> classes = foundPlugins.get(theInterface);
        if (null == classes) {
            classes = new LinkedList<Class>();
            foundPlugins.put(theInterface, classes);
        }
        classes.add(plugin);
    }

    private List<String> scanJarForClassNames(File jarFile) throws IOException {
        List<String> classes = new LinkedList<String>();
        JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFile));
        JarEntry jarEntry;
        while (true) {
            jarEntry = jarStream.getNextJarEntry();
            if (jarEntry == null) {
                break;
            }
            if (jarEntry.getName().endsWith(".class") && !jarEntry.getName().contains("$")) {
                String className = jarEntry.getName().replaceAll("/", "\\.");
                classes.add(className.substring(0, className.length() - 6));
            }
        }
        return classes;
    }
}
