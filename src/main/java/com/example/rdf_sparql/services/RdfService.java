package com.example.rdf_sparql.services;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.util.Map;
import java.util.UUID;

@Service
public class RdfService {

    private Model model; // Interface em Apache Jena que representa um grafo RDF
    private static final String BASE_URI = "http://myontology.com#";

    // Método para carregar o arquivo TTL
    public void loadRdfFile(InputStream inputStream) {
        model = ModelFactory.createDefaultModel(); // A classe ModelFactory simplifica a criação de modelos RDF de várias formas
        model.read(inputStream, null, "TTL");
    }

    // Método para executar consultas SPARQL
    public String executeSparqlQuery(String queryString) {
        if (model == null) {
            return "RDF model is not loaded.";
        }

        Query query = QueryFactory.create(queryString);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            ResultSetFormatter.outputAsJSON(outputStream, results);
        }

        return outputStream.toString();
    }

    // Método para executar updates SPARQL
    public String executeSparqlUpdate(String updateString) {
        if (model == null) {
            return "RDF model is not loaded.";
        }

        try {
            UpdateRequest updateRequest = UpdateFactory.create(updateString);
            UpdateProcessor updateProcessor = UpdateExecutionFactory.create(updateRequest, DatasetFactory.create(model));
            updateProcessor.execute();
            return "SPARQL update executed successfully!";
        } catch (Exception e) {
            return "Failed to execute SPARQL update: " + e.getMessage();
        }
    }

    // Criar instâncias usando JSON
    public void createInstances(Map<String, String> instances) {
        for (Map.Entry<String, String> entry : instances.entrySet()) {
            String classUri = entry.getKey();
            String instanceId = entry.getValue();

            // Construct the instance URI without repeating the class name
            String instanceUri = classUri.substring(0, classUri.indexOf('#') + 1) + instanceId;

            Resource instance = model.createResource(instanceUri);
            model.add(instance, RDF.type, model.createResource(classUri));
        }
    }

    // Export to RDF
    public String exportRdfToFile(String filePath) {
        if (model == null) {
            return "RDF model is not loaded. Please load the RDF data first.";
        }

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            model.write(outputStream, "TTL"); // Escreve o modelo no formato TTL
            return "RDF model exported successfully to: " + filePath;
        } catch (IOException e) {
            return "Error while exporting RDF model: " + e.getMessage();
        }
    }

    // Criar determinada quantidade de instâncias
    public String createInstancesbyQuantity(Map<String, Integer> instanceRequest) {
        StringBuilder sparqlInsertBuilder = new StringBuilder();
        sparqlInsertBuilder.append("PREFIX : <").append(BASE_URI).append(">\n")
                .append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n")
                .append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n")
                .append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n")
                .append("INSERT DATA {\n");

        instanceRequest.forEach((className, quantity) -> {
            for (int i = 0; i < quantity; i++) {
                String instanceId = generateRandomHash();
                String instanceUri = BASE_URI + instanceId;

                // Adiciona ao modelo RDF
                Resource instance = model.createResource(instanceUri);
                instance.addProperty(RDF.type, model.createResource(BASE_URI + className));
                instance.addProperty(RDFS.label, instanceId, "en");

                // Adiciona ao comando SPARQL
                sparqlInsertBuilder.append("    :").append(instanceId)
                        .append(" rdf:type :").append(className).append(" ;\n")
                        .append("        rdfs:label \"").append(instanceId).append("\"@en .\n");
            }
        });

        sparqlInsertBuilder.append("}\n");
        return sparqlInsertBuilder.toString();
    }

    private String generateRandomHash() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
