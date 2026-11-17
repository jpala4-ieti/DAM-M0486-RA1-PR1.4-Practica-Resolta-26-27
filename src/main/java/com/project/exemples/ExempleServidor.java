package com.project.exemples;

import io.undertow.Undertow;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.util.Headers;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ExempleServidor {

    // Exercici 2 de la PR1.4: directori on es guarda una còpia de cada petició rebuda
    private static final Path DIRECTORI_PETICIONS = Path.of("data", "requests");

    public static void main(String[] args) {
        RegistrePeticions registre = new RegistrePeticions(DIRECTORI_PETICIONS);

        // Configuració del servidor Undertow
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost") // Escoltar al port 8080
                // Ús de BlockingHandler per permetre I/O bloquejant
                .setHandler(new BlockingHandler(exchange -> {
                    String metode = exchange.getRequestMethod().toString();

                    // Llegir tot el cos de la petició (pot ser buit, per exemple en un GET)
                    String requestBody = new String(exchange.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    System.out.println("Rebuda petició " + metode + ": " + requestBody);

                    // Exercici 2 de la PR1.4: cada petició rebuda es guarda com a fitxer JSON a data/requests
                    try {
                        Path fitxer = registre.guardarPeticio(metode, exchange.getRequestPath(),
                                String.valueOf(exchange.getSourceAddress()), requestBody);
                        System.out.println("Petició guardada a " + fitxer);
                    } catch (IOException e) {
                        // Un error en guardar la petició no ha d'impedir respondre al client
                        System.err.println("No s'ha pogut guardar la petició: " + e.getMessage());
                    }

                    // Establir l'encapçalament de la resposta
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json; charset=UTF-8");

                    // Permetre només peticions POST
                    if (metode.equalsIgnoreCase("POST")) {
                        try {
                            // Convertir el JSON rebut en un objecte JsonObject
                            try (JsonReader jsonReader = Json.createReader(new StringReader(requestBody))) {
                                JsonObject personaJson = jsonReader.readObject();

                                // Recuperar les dades del JSON
                                String nom = personaJson.getString("nom");
                                int anyNaixement = personaJson.getInt("anyNaixement");

                                // Calcular l'any en què la persona complirà 120 anys
                                int any120 = anyNaixement + 120;

                                // Crear la resposta JSON
                                JsonObject responseJson = Json.createObjectBuilder()
                                        .add("nom", nom)
                                        .add("anyComplira120", any120)
                                        .build();

                                // Convertir la resposta JsonObject en String
                                StringWriter stringWriter = new StringWriter();
                                try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
                                    jsonWriter.write(responseJson);
                                }
                                String resposta = stringWriter.toString();

                                // Enviar la resposta al client
                                exchange.getResponseSender().send(resposta);
                            }
                        } catch (Exception e) {
                            // En cas d'error, enviar un missatge d'error al client
                            exchange.setStatusCode(500);
                            exchange.getResponseSender().send("{\"error\": \"Error processant la petició.\"}");
                            e.printStackTrace();
                        }
                    } else {
                        // Si no és una petició POST, indicar que el mètode no està permès
                        exchange.setStatusCode(405); // Mètode no permès
                        exchange.getResponseSender().send("{\"error\": \"Només es permeten peticions POST.\"}");
                    }
                }))
                .build();

        // Arrencar el servidor
        server.start();
        System.out.println("Servidor en execució a http://localhost:8080");
        System.out.println("Les peticions es guarden a " + DIRECTORI_PETICIONS.toAbsolutePath());
    }
}
