package dk.brightworks.autowirer.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ClasspathScannerUtils {
    private static List<String> traverseFileHierarchy(String path) {
        List<String> result = new ArrayList<>();
        result.add(path);
        if (new File(path).isDirectory()) {
            for (String subpath : new File(path).list()) {
                result.addAll(traverseFileHierarchy(new File(path, subpath).getPath()));
            }
        }
        return result;
    }

    private static List<String> traverseJarEntries(String fn, String resourcePath) {
        try {
            List<String> result = new ArrayList<>();
            JarFile jar = new JarFile(fn);
            for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
                JarEntry entry = e.nextElement();
                if (entry.getRealName().startsWith(resourcePath)) {
                    result.add(entry.getRealName());
                }
            }
            return result;
        } catch (IOException e) {
            return List.of();
        }
    }

    private static Class createClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Class> scanClasspath(String packageName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String resourcePath = packageName.replace('.', '/');
        List<Class> result = new ArrayList<>();
        try {
            for (Enumeration<URL> e = classLoader.getResources(resourcePath); e.hasMoreElements(); ) {
                URL resource = e.nextElement();
                if (resource.getProtocol().equals("file")) {
                    String fn = resource.getFile().endsWith("/") ? resource.getFile().substring(0, resource.getFile().length() - 1) : resource.getFile();
                    result.addAll(traverseFileHierarchy(fn).stream().
                            map(s -> s.substring(fn.length() - resourcePath.length())).
                            filter(s -> s.endsWith(".class")).
                            map(s -> s.substring(0, s.length() - 6)).
                            map(s -> s.replace('/', '.')).
                            filter(s -> !s.matches("\\D*\\$\\d+")).
                            map(s -> createClass(s)).
                            filter(s -> s != null).
                            collect(Collectors.toList())
                    );
                } else if (resource.getProtocol().equals("jar")) {
                    String filename = URLDecoder.decode(resource.getFile(), "UTF-8");
                    filename = filename.substring(5, filename.indexOf("!"));
                    result.addAll(traverseJarEntries(filename, resourcePath).stream().
                            filter(s -> s.endsWith(".class")).
                            map(s -> s.substring(0, s.length() - 6)).
                            map(s -> s.replace('/', '.')).
                            filter(s -> !s.matches("\\D*\\$\\d+")).
                            map(s -> createClass(s)).
                            filter(s -> s != null).
                            collect(Collectors.toList())
                    );
                }
            }
        } catch (IOException e) {
            return List.of();
        }
        return result;
    }

}
