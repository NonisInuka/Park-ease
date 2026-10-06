package com.se1020.vehicleparking.repository.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JsonDataAccess {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonDataAccess() {}

    /**
     * Reads from ~/.online-vehicleparking/data/&lt;fileName&gt; if present (after saves), otherwise from classpath
     * (e.g. {@code classpathResource} {@code data/users.json}).
     */
    public static <T> List<T> readList(String classpathResource, String fileName, TypeReference<List<T>> type) {
        try {
            Path ext = Path.of(System.getProperty("user.home"), ".online-vehicleparking", "data", fileName);
            if (Files.exists(ext)) {
                List<T> fromDisk = MAPPER.readValue(ext.toFile(), type);
                return fromDisk != null ? fromDisk : new ArrayList<>();
            }
            InputStream in = JsonDataAccess.class.getClassLoader().getResourceAsStream(classpathResource);
            if (in == null) {
                return new ArrayList<>();
            }
            try (in) {
                List<T> fromClasspath = MAPPER.readValue(in, type);
                return fromClasspath != null ? fromClasspath : new ArrayList<>();
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Reads JSON from classpath only (e.g. bundled seed data).
     */
    public static <T> List<T> readClasspathList(String classpathResource, TypeReference<List<T>> type) {
        try {
            InputStream in = JsonDataAccess.class.getClassLoader().getResourceAsStream(classpathResource);
            if (in == null) {
                return new ArrayList<>();
            }
            try (in) {
                List<T> list = MAPPER.readValue(in, type);
                return list != null ? list : new ArrayList<>();
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Reads JSON from ~/.online-vehicleparking/data/ only; empty list if missing or invalid.
     */
    public static <T> List<T> readExternalList(String fileName, TypeReference<List<T>> type) {
        try {
            Path ext = Path.of(System.getProperty("user.home"), ".online-vehicleparking", "data", fileName);
            if (!Files.exists(ext)) {
                return new ArrayList<>();
            }
            List<T> list = MAPPER.readValue(ext.toFile(), type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static <T> void writeList(String fileName, List<T> list) {
        try {
            Path dir = Path.of(System.getProperty("user.home"), ".online-vehicleparking", "data");
            Files.createDirectories(dir);
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(dir.resolve(fileName).toFile(), list);
        } catch (Exception e) {
            System.err.println("Error saving " + fileName + ": " + e.getMessage());
        }
    }

    public static <T> void writeResourceList(String classpathResource, String fileName, List<T> list) {
        boolean written = false;

        Path sourceResource = Path.of(System.getProperty("user.dir"), "src", "main", "resources", classpathResource);
        written = writeIfPossible(sourceResource, list) || written;

        Path compiledResource = Path.of(System.getProperty("user.dir"), "target", "classes", classpathResource);
        written = writeIfPossible(compiledResource, list) || written;

        if (!written) {
            writeList(fileName, list);
        }
    }

    private static <T> boolean writeIfPossible(Path path, List<T> list) {
        try {
            Path parent = path.getParent();
            if (parent == null || !Files.exists(parent)) {
                return false;
            }
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), list);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving " + path + ": " + e.getMessage());
            return false;
        }
    }
}
