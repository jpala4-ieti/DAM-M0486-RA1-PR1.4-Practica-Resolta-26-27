package com.project.pr14;

import com.project.objectes.Llibre;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.List;

/**
 * Classe principal que gestiona la lectura i el processament de fitxers JSON per obtenir dades de llibres
 * utilitzant Jackson 3.
 */
public class PR14GestioLlibreriaJacksonMain {

    private static final String NOM_FITXER_SORTIDA = "llibres_output_jackson.json";

    private final File dataFile;

    // El mapper és immutable i segur entre fils: es crea una sola vegada i es reutilitza
    private final JsonMapper mapper = JsonMapper.builder().build();

    /**
     * Constructor de la classe PR14GestioLlibreriaJacksonMain.
     *
     * @param dataFile Fitxer on es troben els llibres.
     */
    public PR14GestioLlibreriaJacksonMain(File dataFile) {
        this.dataFile = dataFile;
    }

    public static void main(String[] args) {
        File dataFile = new File(System.getProperty("user.dir"), "data/pr14" + File.separator + "llibres_input.json");
        PR14GestioLlibreriaJacksonMain app = new PR14GestioLlibreriaJacksonMain(dataFile);
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
        try {
            // Vinculació de dades: l'array JSON es converteix directament en una List<Llibre>.
            // TypeReference conserva el tipus genèric complet (List<Llibre>), que Java esborra
            // en temps d'execució.
            return mapper.readValue(dataFile, new TypeReference<List<Llibre>>() {});
        } catch (JacksonException e) {
            // JacksonException és no comprovada: inclou fitxer inexistent (JacksonIOException),
            // JSON mal format (StreamReadException) i tipus incorrectes (DatabindException)
            System.err.println("Error en llegir " + dataFile.getName()
                    + ": " + e.getOriginalMessage());
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
     * Guarda la llista de llibres en un fitxer nou anomenat llibres_output_jackson.json,
     * dins del mateix directori que el fitxer d'entrada.
     *
     * @param llibres Llista de llibres a guardar.
     */
    public void guardarLlibres(List<Llibre> llibres) {
        File outputFile = new File(dataFile.getAbsoluteFile().getParentFile(), NOM_FITXER_SORTIDA);
        try {
            // Escriptura amb sagnat (pretty printer) per fer el fitxer llegible
            mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, llibres);
            System.out.println("Llibres guardats a " + outputFile.getPath());
        } catch (JacksonException e) {
            System.err.println("Error en guardar " + outputFile.getName()
                    + ": " + e.getOriginalMessage());
        }
    }
}
