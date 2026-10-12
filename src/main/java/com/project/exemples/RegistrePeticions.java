package com.project.exemples;

import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Guarda cada petició HTTP rebuda pel servidor com un fitxer JSON independent
 * (ampliació de la pràctica PR1.4).
 *
 * <p>Cada fitxer conté la data i hora, el mètode, la ruta, l'adreça del client i el cos de la petició.
 * Si el cos és un JSON vàlid es guarda com a JSON aniuat; si no, es guarda com a text.</p>
 */
public class RegistrePeticions {

    // Format del nom dels fitxers: peticio_20261012-153210-123_0001.json (ordenables per data)
    private static final DateTimeFormatter FORMAT_NOM = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    // Fàbrica d'escriptors JSON amb sagnat, reutilitzable i segura entre fils
    private static final JsonWriterFactory WRITER_FACTORY =
            Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));

    private final Path directori;

    // El servidor atén peticions en paral·lel: el comptador atòmic evita noms de fitxer repetits
    private final AtomicLong comptador = new AtomicLong();

    /**
     * @param directori Directori on es guardaran les peticions (es crea si no existeix).
     */
    public RegistrePeticions(Path directori) {
        this.directori = directori;
    }

    /**
     * Guarda una petició com a fitxer JSON dins del directori del registre.
     *
     * @param metode Mètode HTTP (GET, POST...).
     * @param ruta   Ruta sol·licitada.
     * @param client Adreça del client.
     * @param cos    Cos de la petició (pot ser buit).
     * @return Ruta del fitxer creat.
     * @throws IOException Si no es pot crear el directori o escriure el fitxer.
     */
    public Path guardarPeticio(String metode, String ruta, String client, String cos) throws IOException {
        Files.createDirectories(directori);

        OffsetDateTime ara = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        JsonObjectBuilder peticio = Json.createObjectBuilder()
                .add("dataHora", ara.toString())
                .add("metode", metode)
                .add("ruta", ruta)
                .add("client", client);
        afegirCos(peticio, cos);

        String nomFitxer = String.format("peticio_%s_%04d.json", ara.format(FORMAT_NOM), comptador.incrementAndGet());
        Path fitxer = directori.resolve(nomFitxer);

        try (Writer writer = Files.newBufferedWriter(fitxer, StandardCharsets.UTF_8);
             JsonWriter jsonWriter = WRITER_FACTORY.createWriter(writer)) {
            jsonWriter.writeObject(peticio.build());
        } catch (JsonException e) {
            throw new IOException("No s'ha pogut escriure el JSON de la petició", e);
        }
        return fitxer;
    }

    /**
     * Afegeix el cos de la petició: com a JSON si és vàlid, com a text si no ho és, o null si és buit.
     */
    private void afegirCos(JsonObjectBuilder peticio, String cos) {
        if (cos == null || cos.isBlank()) {
            peticio.add("jsonValid", false);
            peticio.addNull("cos");
            return;
        }
        try (JsonReader reader = Json.createReader(new StringReader(cos))) {
            peticio.add("jsonValid", true);
            peticio.add("cos", reader.readValue());
        } catch (JsonException e) {
            peticio.add("jsonValid", false);
            peticio.add("cos", cos);
        }
    }
}
