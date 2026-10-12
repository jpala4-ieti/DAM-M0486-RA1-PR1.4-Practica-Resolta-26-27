package com.project.pr14;

import com.project.objectes.Llibre;
import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Classe principal que gestiona la lectura i el processament de fitxers JSON per obtenir dades de llibres
 * utilitzant Jakarta JSON Processing (JSON-P).
 */
public class PR14GestioLlibreriaJakartaMain {

    private static final String NOM_FITXER_SORTIDA = "llibres_output_jakarta.json";

    private final File dataFile;

    /**
     * Constructor de la classe PR14GestioLlibreriaJakartaMain.
     *
     * @param dataFile Fitxer on es troben els llibres.
     */
    public PR14GestioLlibreriaJakartaMain(File dataFile) {
        this.dataFile = dataFile;
    }

    public static void main(String[] args) {
        File dataFile = new File(System.getProperty("user.dir"), "data/pr14" + File.separator + "llibres_input.json");
        PR14GestioLlibreriaJakartaMain app = new PR14GestioLlibreriaJakartaMain(dataFile);
        app.processarFitxer();
    }

    /**
     * Processa el fitxer JSON per carregar, modificar, afegir, esborrar i guardar les dades dels llibres.
     */
    public void processarFitxer() {
        List<Llibre> llibres = carregarLlibres();
        if (llibres != null) {
            modificarAnyPublicacio(llibres, 1, 1995);
            afegirNouLlibre(llibres, new Llibre(4, "Històries de la ciutat", "Miquel Soler", 2022));
            esborrarLlibre(llibres, 2);
            guardarLlibres(llibres);
        }
    }

    /**
     * Carrega els llibres des del fitxer JSON.
     * Si el fitxer no existeix o no conté un JSON vàlid, mostra un missatge d'error i retorna null
     * (no ha de propagar cap excepció).
     *
     * @return Llista de llibres o null si hi ha hagut un error en la lectura.
     */
    public List<Llibre> carregarLlibres() {
        // try-with-resources: el JsonReader (i el FileReader que conté) es tanquen sempre
        try (JsonReader jsonReader = Json.createReader(new FileReader(dataFile, StandardCharsets.UTF_8))) {
            // Model d'objectes: tot el document es carrega en memòria com un JsonArray
            JsonArray jsonArray = jsonReader.readArray();

            List<Llibre> llibres = new ArrayList<>();
            for (JsonObject jsonObject : jsonArray.getValuesAs(JsonObject.class)) {
                llibres.add(new Llibre(
                        jsonObject.getInt("id"),
                        jsonObject.getString("titol"),
                        jsonObject.getString("autor"),
                        jsonObject.getInt("any")));
            }
            return llibres;
        } catch (IOException | JsonException e) {
            // IOException: el fitxer no existeix o no es pot llegir
            // JsonException (no comprovada): el contingut no és un JSON vàlid o no és un array
            System.err.println("Error en llegir " + dataFile.getName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Modifica l'any de publicació d'un llibre amb un id específic.
     *
     * @param llibres Llista de llibres.
     * @param id Identificador del llibre a modificar.
     * @param nouAny Nou any de publicació.
     */
    public void modificarAnyPublicacio(List<Llibre> llibres, int id, int nouAny) {
        for (Llibre llibre : llibres) {
            if (llibre.getId() == id) {
                llibre.setAny(nouAny);
            }
        }
    }

    /**
     * Afegeix un nou llibre a la llista de llibres.
     *
     * @param llibres Llista de llibres.
     * @param nouLlibre Nou llibre a afegir.
     */
    public void afegirNouLlibre(List<Llibre> llibres, Llibre nouLlibre) {
        llibres.add(nouLlibre);
    }

    /**
     * Esborra un llibre amb un id específic de la llista de llibres.
     *
     * @param llibres Llista de llibres.
     * @param id Identificador del llibre a esborrar.
     */
    public void esborrarLlibre(List<Llibre> llibres, int id) {
        llibres.removeIf(llibre -> llibre.getId() == id);
    }

    /**
     * Guarda la llista de llibres en un fitxer nou anomenat llibres_output_jakarta.json,
     * dins del mateix directori que el fitxer d'entrada.
     *
     * @param llibres Llista de llibres a guardar.
     */
    public void guardarLlibres(List<Llibre> llibres) {
        // Construeix l'array JSON a partir dels objectes Llibre
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (Llibre llibre : llibres) {
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("id", llibre.getId())
                    .add("titol", llibre.getTitol())
                    .add("autor", llibre.getAutor())
                    .add("any", llibre.getAny()));
        }

        // Fàbrica d'escriptors configurada amb sagnat (pretty printing) per fer el fitxer llegible
        JsonWriterFactory writerFactory = Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
        Path outputPath = dataFile.getAbsoluteFile().toPath().resolveSibling(NOM_FITXER_SORTIDA);

        try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
             JsonWriter jsonWriter = writerFactory.createWriter(writer)) {
            jsonWriter.writeArray(arrayBuilder.build());
            System.out.println("Llibres guardats a " + outputPath);
        } catch (IOException | JsonException e) {
            System.err.println("Error en guardar " + outputPath.getFileName() + ": " + e.getMessage());
        }
    }
}
