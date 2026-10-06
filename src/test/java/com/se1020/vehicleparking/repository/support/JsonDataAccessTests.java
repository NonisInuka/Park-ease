package com.se1020.vehicleparking.repository.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JsonDataAccessTests {

    @TempDir
    Path projectDir;

    @Test
    void writeResourceListUpdatesSourceAndCompiledResources() throws Exception {
        Path sourceUsers = projectDir.resolve("src/main/resources/data/users.json");
        Path compiledUsers = projectDir.resolve("target/classes/data/users.json");
        Files.createDirectories(sourceUsers.getParent());
        Files.createDirectories(compiledUsers.getParent());

        String originalUserDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", projectDir.toString());

            JsonDataAccess.writeResourceList(
                    "data/users.json",
                    "users.json",
                    List.of(Map.of("userId", "U999", "name", "Test User"))
            );
        } finally {
            System.setProperty("user.dir", originalUserDir);
        }

        assertThat(Files.readString(sourceUsers)).contains("\"userId\" : \"U999\"");
        assertThat(Files.readString(compiledUsers)).contains("\"userId\" : \"U999\"");
    }
}
