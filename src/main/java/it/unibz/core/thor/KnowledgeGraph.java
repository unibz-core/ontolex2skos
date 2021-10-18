package it.unibz.core.thor;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public abstract class KnowledgeGraph {
  OntModel model;

  public KnowledgeGraph() {
    model = this.createGraph();
    addDefaultNamespaces();
  }

  abstract protected OntModel createGraph();

  public void readFromFile(Path file) throws IOException {
    RDFDataMgr.read(model, Files.newInputStream(file), Lang.TURTLE);
  }

  public void loadModule(Vocabulary v) {
    InputStream inputStream = v.getInputStream();
    RDFDataMgr.read(model, inputStream, v.format);
  }

  public void loadResource(String filename) {
    Objects.requireNonNull(filename, "Cannot load inputStream from null filename.");

    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
    Objects.requireNonNull(inputStream, "Cannot load inputStream from non-existing filename.");

    RDFDataMgr.read(model, inputStream, Lang.TURTLE);
  }

  public void writeToFile(Path file) throws IOException {
    OutputStream out = Files.newOutputStream(file);
    RDFDataMgr.write(out, model, Lang.TURTLE);
  }

  public void addNamespace(Vocabulary vocabulary) {
    model.setNsPrefix(vocabulary.prefix, vocabulary.uri);
  }

  private void addDefaultNamespaces() {
    Vocabulary.getInstanceStream().forEach(this::addNamespace);
  }

  private String join(Collection<? extends Statement> statements) {
    return statements.stream()
            .map(Statement::toString)
            .collect(joining(" . "));
  }

  void insertStatements(Statement... statement) {
    insertStatements(Arrays.asList(statement));
  }

  void insertStatements(List<Statement> statements) {
    String sparql = Vocabulary.getPrefixDeclarations() +
            "INSERT DATA { " +
            join(statements) +
            "}";

    executeUpdateQuery(sparql);
  }

  public void deleteStatements(Statement... statements) {
    deleteStatements(Arrays.asList(statements));
  }

  public void deleteStatements(List<Statement> statements) {
    String sparql = Vocabulary.getPrefixDeclarations() +
            "DELETE DATA { " +
            join(statements) +
            "}";

    executeUpdateQuery(sparql);
  }

  private void executeUpdateQuery(String sparql) {
    UpdateRequest request = UpdateFactory.create(sparql);
    UpdateAction.execute(request, model);
  }

  public List<RDFNode> getPropertyValues(String subjectUri, String propertyUri) {
    Query query = QueryBuilder.build(subjectUri, propertyUri, "?object");

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      ResultSet results = qexec.execSelect();
      List<RDFNode> objects = new ArrayList<>();

      while (results.hasNext()) {
        RDFNode node = results.nextSolution().get("object");
        objects.add(node);
      }

      return objects;
    }
  }

  public List<String> getPropertiesAsUris(String subjectUri, String propertyUri) {
    return getPropertyValues(subjectUri, propertyUri)
            .stream()
            .map(RDFNode::toString)
            .collect(toList());
  }

  public List<Resource> getInstances(String classUri) {
    Query query = QueryBuilder.build("?instance", "rdf:type", classUri);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      ResultSet results = qexec.execSelect();
      List<Resource> instances = new ArrayList<>();

      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();
        Resource sense = solution.getResource("instance");
        instances.add(sense);
      }

      return instances;
    }
  }

  public List<String> getInstanceUris(String classUri) {
    return getInstances(classUri).stream()
            .map(Resource::getURI)
            .collect(toList());
  }


}
