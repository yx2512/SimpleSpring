package com.simplespring.core.utils;

import com.simplespring.core.annotation.Component;
import com.simplespring.core.annotation.Controller;
import com.simplespring.core.annotation.Repository;
import com.simplespring.core.annotation.Service;
import com.simplespring.exception.PackageScanningException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ClassUtil {
    public static final String FILE_PROTOCOL = "file";
    private static final List<Class<? extends Annotation>> BEAN_ANNOTATIONS =
            Arrays.asList(Component.class, Controller.class, Service.class, Repository.class);

    public static Set<Class<?>> extractPackageClass(String packageName) throws URISyntaxException, IOException {
        ClassLoader classLoader = getClassLoader();
        URL resource = classLoader.getResource(packageName.replace('.', '/'));
        if(resource == null) {
            log.warn("Unable to retrieve from this package: " + packageName);
            return null;
        }

        Set<Class<?>> classSet = null;
        if(resource.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)) {
            classSet = new HashSet<>();
            Path packagePath = Paths.get(resource.toURI());
            extractClassFile(classSet,packagePath,packageName);
        }
        return classSet;
    }

    public static Object newInstance(Class<?> clazz, Boolean accessible) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(accessible);
        constructor.setAccessible(accessible);
        return constructor.newInstance();
    }

    private static void extractClassFile(Set<Class<?>> classSet, Path packagePath, String packageName) throws IOException {
        Files.walkFileTree(packagePath,new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                System.out.println(file.toString());
                if(file.getFileName().toString().endsWith(".class")) {
                    file2Class(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                log.warn("File path traversal failed on "+ file.toString());
                return FileVisitResult.SKIP_SUBTREE;
            }
            
            private void file2Class(Path filePath) {
                String classNameStr = filePath.toAbsolutePath().toString().replace(File.separatorChar,'.');
                classNameStr = classNameStr.substring(classNameStr.indexOf(packageName));
                classNameStr = classNameStr.substring(0,classNameStr.lastIndexOf("."));
                loadClass(classNameStr,classSet);
            }
        });
    }

    private static void loadClass(String className, Set<Class<?>> classSet) {
        try{
            Class<?> clazz = Class.forName(className);
            for( Class<? extends Annotation> annotation : BEAN_ANNOTATIONS) {
                if(clazz.isAnnotationPresent(annotation)) {
                    classSet.add(clazz);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new PackageScanningException(e.getMessage());
        }
    }

    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
