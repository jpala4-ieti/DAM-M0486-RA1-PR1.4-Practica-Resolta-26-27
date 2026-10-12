package com.project.pr14;

import com.project.objectes.Llibre;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe principal que gestiona la lectura i el processament de fitxers JSON per obtenir dades de llibres
 * utilitzant la llibreria org.json (solució alternativa: no forma part de l'enunciat).
 */
public class PR14GestioLlibreriaOrgJsonMain {

    private static final String NOM_FITXER_SORTIDA = "llibres_output_json_org.json";

    private final File dataFile;

    /**
     * Constructor de la classe PR14GestioLlibreriaOrgJsonMain.
     *
     * @param dataFile Fitxer on es troben els llibres.
     */
    public PR14GestioLlibreriaOrgJsonMain(File dataFile) {
        this.dataFile = dataFile;
    }

    public static void main(String[] args) {
        File dataFile = new File(System.getProperty("user.dir"), "data/pr14" + File.separator + "llibres_input.json");
        PR14GestioLlibreriaOrgJsonMain app = new PR14GestioLlibreriaOrgJsonMain(dataFile);
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
     * Carrega els llibres des del fitxer JSON utilitzant la llibreria org.json.
     * Si el fitxer no existeix o no conté un JSON vàlid, mostra un missatge d'error i retorna null.
     *
     * @return Llista de llibres o null si hi ha hagut un error en la lectura.
     */
    public List<Llibre> carregarLlibres() {
        try {
            String content = Files.readString(dataFile.toPath(), StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(content);

            List<Llibre> llibres = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                llibres.add(new Llibre(
                        jsonObject.getInt("id"),
                        jsonObject.getString("titol"),
                        jsonObject.getString("autor"),
                        jsonObject.getInt("any")));
            }
            return llibres;
        } catch (IOException | JSONException e) {
            // IOException: el fitxer no existeix o no es pot llegir
            // JSONException (no comprovada): el contingut no és un JSON vàlid o hi falten claus
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
     * Guarda la llista de llibres en un fitxer nou anomenat llibres_output_json_org.json,
     * dins del mateix directori que el fitxer d'entrada.
     *
     * @param llibres Llista de llibres a guardar.
     */
    public void guardarLlibres(List<Llibre> llibres) {
        JSONArray jsonArray = new JSONArray();
        for (Llibre llibre : llibres) {
            JSONObject llibreJson = new JSONObject();
            llibreJson.put("id", llibre.getId());
            llibreJson.put("titol", llibre.getTitol());
            llibreJson.put("autor", llibre.getAutor());
            llibreJson.put("any", llibre.getAny());
            jsonArray.put(llibreJson);
        }

        Path outputPath = dataFile.getAbsoluteFile().toPath().resolveSibling(NOM_FITXER_SORTIDA);
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(jsonArray.toString(4)); // Format JSON amb sagnat de 4 espais
            System.out.println("Llibres guardats a " + outputPath);
        } catch (IOException e) {
            System.err.println("Error en guardar " + outputPath.getFileName() + ": " + e.getMessage());
        }
    }
}
