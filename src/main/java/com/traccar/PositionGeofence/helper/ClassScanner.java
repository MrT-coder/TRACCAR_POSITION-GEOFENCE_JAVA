package com.traccar.PositionGeofence.helper;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ClassScanner {

    private ClassScanner() {
    }

    public static List<Class<?>> findSubclasses(
            Class<?> baseClass) throws IOException, URISyntaxException, ReflectiveOperationException {
        return findSubclasses(baseClass, baseClass.getPackageName());
    }

    public static List<Class<?>> findSubclasses(Class<?> baseClass, String packageName)
            throws IOException, URISyntaxException, ReflectiveOperationException {

        List<String> names = new LinkedList<>();
        String packagePath = packageName.replace('.', '/');
        URL packageUrl = baseClass.getClassLoader().getResource(packagePath);

        if (packageUrl.getProtocol().equals("jar")) {
            String jarFileName = URLDecoder.decode(packageUrl.getFile(), StandardCharsets.UTF_8);
            try (JarFile jf = new JarFile(jarFileName.substring(5, jarFileName.indexOf("!")))) {
                Enumeration<JarEntry> jarEntries = jf.entries();
                while (jarEntries.hasMoreElements()) {
                    String entryName = jarEntries.nextElement().getName();
                    if (entryName.startsWith(packagePath) && entryName.length() > packagePath.length() + 5) {
                        names.add(entryName.substring(packagePath.length() + 1, entryName.lastIndexOf('.')));
                    }
                }
            }
        } else {
            File folder = new File(new URI(packageUrl.toString()));
            File[] files = folder.listFiles();
            if (files != null) {
                for (File actual: files) {
                    String entryName = actual.getName();
                    names.add(entryName.substring(0, entryName.lastIndexOf('.')));
                }
            }
        }

        var classes = new LinkedList<Class<?>>();
        for (String name : names) {
            var clazz = Class.forName(packageName + '.' + name);
            if (baseClass.isAssignableFrom(clazz)) {
                classes.add(clazz);
            }
        }
        return classes;
    }

}
