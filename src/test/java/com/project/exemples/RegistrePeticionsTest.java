package com.project.exemples;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de l'exercici 2: emmagatzematge de les peticions rebudes pel servidor.
 */
public class RegistrePeticionsTest {

    @TempDir
    Path tempDir;

    private JsonObject llegir(Path fitxer) throws IOException {
        try (JsonReader reader = Json.createReader(Files.newBufferedReader(fitxer, StandardCharsets.UTF_8))) {
            return reader.readObject();
        }
    }

    @Test
    void testGuardaPeticioJsonValida() throws IOException {
        Path directori = tempDir.resolve("data").resolve("requests");
        RegistrePeticions registre = new RegistrePeticions(directori);

        Path fitxer = registre.guardarPeticio("POST", "/", "/127.0.0.1:50000",
                "{\"nom\":\"Núria Puig\",\"anyNaixement\":1985}");

        assertTrue(Files.isDirectory(directori), "El directori de peticions s'hauria de crear si no existeix.");
        assertTrue(Files.exists(fitxer), "Hauria d'existir el fitxer de la petició.");
        assertTrue(fitxer.getFileName().toString().endsWith(".json"), "El fitxer hauria de tenir extensió .json.");

        JsonObject peticio = llegir(fitxer);
        assertEquals("POST", peticio.getString("metode"));
        assertTrue(peticio.getBoolean("jsonValid"));
        assertEquals("Núria Puig", peticio.getJsonObject("cos").getString("nom"));
        assertEquals(1985, peticio.getJsonObject("cos").getInt("anyNaixement"));
    }

    @Test
    void testGuardaPeticioAmbCosNoJson() throws IOException {
        RegistrePeticions registre = new RegistrePeticions(tempDir);
        Path fitxer = registre.guardarPeticio("POST", "/", "client", "això no és JSON");

        JsonObject peticio = llegir(fitxer);
        assertFalse(peticio.getBoolean("jsonValid"));
        assertEquals("això no és JSON", peticio.getString("cos"));
    }

    @Test
    void testCosAmbTextDarrereDelJsonEsGuardaSencer() throws IOException {
        // Un JSON seguit de més text no és un JSON vàlid: s'ha de guardar tot el cos com a text
        RegistrePeticions registre = new RegistrePeticions(tempDir);
        for (String cos : new String[] {"{\"a\":1} xyz", "{\"a\":1}{\"b\":2}"}) {
            JsonObject peticio = llegir(registre.guardarPeticio("POST", "/", "client", cos));
            assertFalse(peticio.getBoolean("jsonValid"));
            assertEquals(cos, peticio.getString("cos"), "No s'ha de perdre cap part del cos.");
        }
    }

    @Test
    void testCadaPeticioGeneraUnFitxerDiferent() throws IOException {
        RegistrePeticions registre = new RegistrePeticions(tempDir);
        for (int i = 0; i < 5; i++) {
            registre.guardarPeticio("GET", "/", "client", "");
        }
        try (Stream<Path> fitxers = Files.list(tempDir)) {
            assertEquals(5, fitxers.count(), "Cada petició hauria de generar un fitxer diferent.");
        }
    }
}
