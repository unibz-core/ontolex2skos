package it.unibz.core.thor;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.List;

public class SkosTargetGraph extends KnowledgeGraph {
  WorkingGraph source;
  OntModel model;

  public SkosTargetGraph(WorkingGraph source) {
    this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    this.source = source;
    this.loadCustomProperties();
    addDefaultNamespaces();
  }

  @Override
  public Model getModel() {
    return model;
  }

  public void loadCustomProperties() {
    loadResource("custom-properties.ttl");
  }

  public void copySchemes() {
    String sparql = Vocabulary.getPrefixDeclarations() +
            "SELECT ?scheme " +
            "WHERE { " +
            "   ?scheme rdf:type skos:ConceptScheme . " +
            "   ?scheme thor:wasDerivedFrom ?lexicon . " +
            "}";

    Query query = QueryFactory.create(sparql);
    try (QueryExecution qexec = QueryExecutionFactory.create(query, source.getModel())) {
      List<Statement> statements = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution solution = resultSet.nextSolution();
        final RDFNode scheme = solution.get("scheme");

        Statement s = new Statement(scheme, "rdf:type", "skos:ConceptScheme");
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  public void copySchemeData() {
    String[] properties = {
            "skos:prefLabel",
            "rdfs:label"
    };

    String sparql = Vocabulary.getPrefixDeclarations() +
            "SELECT * " +
            "WHERE { " +
            "   ?scheme ?property ?object . " +
            "   ?scheme rdf:type skos:ConceptScheme . " +
            "   ?scheme thor:wasDerivedFrom ?lexicon . " +
            "FILTER(?property IN (" + String.join(", ", properties) + ")) " +
            "}";

    Query query = QueryFactory.create(sparql);
    try (QueryExecution qexec = QueryExecutionFactory.create(query, source.getModel())) {
      List<Statement> statements = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution solution = resultSet.nextSolution();
        final RDFNode scheme = solution.get("scheme");
        final RDFNode property = solution.get("property");
        final RDFNode object = solution.get("object");

        if (object.isAnon())
          continue;

        Statement s = new Statement(scheme, property, object);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }


  public void copyConcepts() {
    String sparql = Vocabulary.getPrefixDeclarations() +
            "SELECT ?concept " +
            "WHERE { " +
            "   ?concept rdf:type thor:DerivedConcept . " +
            "}";

    Query query = QueryFactory.create(sparql);
    try (QueryExecution qexec = QueryExecutionFactory.create(query, source.getModel())) {
      List<Statement> statements = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution solution = resultSet.nextSolution();
        final RDFNode concept = solution.get("concept");

        Statement s = new Statement(concept, "rdf:type", "skos:Concept");
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  public void copyConceptData() {
    String[] properties = {
            "skos:inScheme",
            "skos:topConceptOf",
            "skos:prefLabel",
            "skos:altLabel",
            "skos:definition",
            "skos:scopeNote",
            "skos:historyNote",
            "skos:example",
            "ontolex:isConceptOf",
            "thor:hasContext",
            "foaf:homepage",
            "rdfs:label"
    };

    String sparql = Vocabulary.getPrefixDeclarations() +
            "SELECT * " +
            "WHERE { " +
            "   ?subject ?property ?object . " +
            "   ?subject rdf:type thor:DerivedConcept . " +
            "FILTER(?property IN (" + String.join(", ", properties) + ")) " +
            "}";

    Query query = QueryFactory.create(sparql);
    try (QueryExecution qexec = QueryExecutionFactory.create(query, source.getModel())) {
      List<Statement> statements = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution solution = resultSet.nextSolution();
        final RDFNode subject = solution.get("subject");
        final RDFNode property = solution.get("property");
        final RDFNode object = solution.get("object");

        if (object.isAnon())
          continue;

        Statement s = new Statement(subject, property, object);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  public void copySemanticRelations() {
    String[] properties = {
            "skos:broader",
            "skos:narrower",
            "skos:related",
    };

    String sparql = Vocabulary.getPrefixDeclarations() +
            "SELECT * " +
            "WHERE { " +
            "   ?subject ?property ?object . " +
            "   ?subject rdf:type thor:DerivedConcept . " +
            "   ?object rdf:type thor:DerivedConcept . " +
            "FILTER(?property IN (" + String.join(", ", properties) + ")) " +
            "}";

    Query query = QueryFactory.create(sparql);
    try (QueryExecution qexec = QueryExecutionFactory.create(query, source.getModel())) {
      List<Statement> statements = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution solution = resultSet.nextSolution();
        final RDFNode subject = solution.get("subject");
        final RDFNode property = solution.get("property");
        final RDFNode object = solution.get("object");

        Statement s = new Statement(subject, property, object);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  public void copyDomainLabels() {
    String sparql = Vocabulary.getPrefixDeclarations() +
            "SELECT * " +
            "WHERE { " +
            "   ?domain rdfs:label ?label . " +
            "   ?concept thor:hasContext ?domain . " +
            "}";

    Query query = QueryFactory.create(sparql);
    try (QueryExecution qexec = QueryExecutionFactory.create(query, source.getModel())) {
      List<Statement> statements = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution solution = resultSet.nextSolution();
        final RDFNode domain = solution.get("domain");
        final RDFNode label = solution.get("label");

        Statement s = new Statement(domain, "rdfs:label", label);
        statements.add(s);
      }

      insertStatements(statements);
    }

  }
}
