package com.example.rdf_sparql.controller;

import com.example.rdf_sparql.services.RdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.Map;

@RestController
@RequestMapping("/api/rdf")
public class RdfController {

    @Autowired
    private RdfService rdfService;

    // Endpoint para carregar o arquivo RDF (TTL)
    @PostMapping("/upload")
    public String uploadRdfFile(@RequestParam("file") MultipartFile file) {
        try {
            rdfService.loadRdfFile(file.getInputStream()); //o arquivo é lido diretamente na memória usando o método file.getInputStream() e passado para o serviço rdfService para ser processado.
            return "RDF file loaded successfully!";
        } catch (IOException e) {
            return "Failed to load RDF file: " + e.getMessage();
        }
    }

    // Endpoint para executar consultas SPARQL
    @PostMapping(value = "/sparql", produces = MediaType.APPLICATION_JSON_VALUE)
    public String executeSparql(@RequestParam("query") String query) {
        return rdfService.executeSparqlQuery(query);
    }

    // Endpoint para executar updates SPARQL
    @PostMapping(value = "/update", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String executeSparqlUpdate(@RequestBody String update) {
        return rdfService.executeSparqlUpdate(update);
    }

    @PostMapping(value = "/createInstancesjson", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createInstances(@RequestBody Map<String, String> instances) {
        rdfService.createInstances(instances);
        return "Instances created successfully!";
    }

    @PostMapping("/export")
    public ResponseEntity<String> exportRdfToFile(@RequestParam String filePath) {
        String result = rdfService.exportRdfToFile(filePath);
        if (result.contains("successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("/createscenariopopulation")
    public ResponseEntity<String> createInstancesbyQuantity(@RequestBody Map<String, Integer> instanceRequest) {
        rdfService.createInstancesbyQuantity(instanceRequest);
        return ResponseEntity.ok("Instances created successfully!");
    }
}
