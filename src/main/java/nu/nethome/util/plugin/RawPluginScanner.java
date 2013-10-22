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
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Scans a directory for jar-files, links the found jar-files into the class path
 * and finds all classes in them which are annotated as @Plugin.
 *
 * @author Stefan
 *         Date: 2011-06-05
 */
public class RawPluginScanner implements PluginProvider {

    private static final Class[] parameters = new Class[]{URL.class};
    private Map<Class, List<Class>> foundPlugins = new HashMap<Class, List<Class>>();

    public List<Class> getPluginsForInterface(Class theInterface) {
        if (foundPlugins.containsKey(theInterface)) {
            return foundPlugins.get(theInterface);
        } else {
            return new LinkedList<Class>();
        }
    }

    public Set<Class> getFoundInterfaces() {
        return foundPlugins.keySet();
    }

    public void scanForPlugins(List<File> directorys) throws IOException {
        for (File file : directorys) {
            scanForPlugins(file);
        }
    }

    /**
     * Scans a directory for jar-files, links the found jar-files into the class path
     * and finds all classes in them which are annotated as @Plugin.
     *
     * @param directory directory to scan
     * @throws java.io.IOException for IO errors
     */
    public void scanForPlugins(File directory) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                scanForPlugins(f);
            } else if (f.getName().endsWith(".jar")) {
                List<String> classNames = getClassNames(f.getAbsolutePath());
                for (String className : classNames) {
                    try {
                        // Remove ".class" at the end
                        String name = className.substring(0, className.length() - 6);
                        Class foundClass = getClass(f, name);

                        // Check if the class is annotated as a Plugin
                        Annotation pluginAnnotation = foundClass.getAnnotation(Plugin.class);
                        if (null != pluginAnnotation) {
                            Class[] interfaces = foundClass.getInterfaces();
                            for (Class anInterface : interfaces) {
                                addPluginForInterface(anInterface, foundClass);
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println("Could not open class: " + e.getMessage());
                    } catch (NoClassDefFoundError e) {
                        System.out.println("Could not open class: " + e.getMessage());
                    }
                }
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

    private List<String> getClassNames(String jarName) throws IOException {
        ArrayList<String> classes = new ArrayList<String>(10);
        JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName));
        JarEntry jarEntry;
        while (true) {
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                break;
            }
            if (jarEntry.getName().endsWith(".class") && !jarEntry.getName().contains("$")) {
                classes.add(jarEntry.getName().replaceAll("/", "\\."));
            }
        }

        return classes;
    }

    private Class getClass(File file, String name) throws ClassNotFoundException, IOException {
        addURLToSystemClassLoader(file.toURI().toURL());

        URLClassLoader tempClassLoader;
        Class result;
        String filePath = file.getAbsolutePath();
        filePath = "jar:file://" + filePath + "!/";
        URL url = new File(filePath).toURI().toURL();
        tempClassLoader = new URLClassLoader(new URL[]{url});
        result = tempClassLoader.loadClass(name);
        return result;
    }

    private void addURLToSystemClassLoader(URL u) throws IOException {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL urls[] = sysLoader.getURLs();
        for (URL sysUrl : urls) {
            if (sysUrl.toString().equalsIgnoreCase(u.toString())) {
                return;
            }
        }
        Class sysclass = URLClassLoader.class;
        Method method = null;
        try {
            method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysLoader, u);
        } catch(NoSuchMethodException e) {
            throw new IOException("Error, could not add URL to system classloader");
        } catch (InvocationTargetException e) {
            throw new IOException("Error, could not add URL to system classloader");
        } catch (IllegalAccessException e) {
            throw new IOException("Error, could not add URL to system classloader");
        }
    }
}
