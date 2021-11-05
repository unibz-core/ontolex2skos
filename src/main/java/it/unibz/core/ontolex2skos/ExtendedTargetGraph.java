package it.unibz.core.ontolex2skos;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.List;

public class ExtendedTargetGraph extends KnowledgeGraph {
  WorkingGraph source;
  OntModel model;

  public ExtendedTargetGraph(WorkingGraph source) {
    super();
    this.source = source;
    this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    addDefaultNamespaces();
  }

  @Override
  public Model getModel() {
    return model;
  }

  public void extractTypeAssertions() {
    String[] types = {
            "dct:Agent",
            "lexinfo:Adjective",
            "lexinfo:Noun",
            "lexinfo:ProperNoun",
            "lexinfo:Register",
            "lexinfo:Verb",
            "lime:Lexicon",
            "ontolex:ConceptSet",
            "ontolex:Form",
            "ontolex:LexicalEntry",
            "ontolex:LexicalSense",
            "ontolex:LexicalConcept",
            "ontolex:MultiWordExpression",
            "skos:Concept",
            "skos:ConceptScheme",
            "thor:Community",
            "thor:Context",
            "thor:Definition",
            "thor:iStandard",
            "thor:PreferredSense",
    };

    String sparql = Vocabulary.getPrefixDeclarations() +
            "SELECT ?subject ?type " +
            "WHERE { " +
            "   ?subject rdf:type ?type . " +
            "FILTER(?type IN (" + String.join(", ", types) + ")) " +
            "}";

    Query query = QueryFactory.create(sparql);
    try (QueryExecution qexec = QueryExecutionFactory.create(query, source.getModel())) {

      List<Statement> statements = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution solution = resultSet.nextSolution();
        final RDFNode subject = solution.get("subject");
        final RDFNode type = solution.get("type");

        Statement s = new Statement(subject, "rdf:type", type);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  public void extractPropertyAssertions() {
    String[] properties = {
            "dct:created",
            "dct:creator",
            "dct:source",
            "dct:title",
            "dct:valid",
            "foaf:homepage",
            "lexinfo:contractionFor",
            "lexinfo:domain",
            "lexinfo:fullFormFor",
            "lexinfo:hypernym",
            "lexinfo:hyponym",
            "lexinfo:homograph",
            "lexinfo:normativeAuthorization",
            "lexinfo:preferredTerm",
            "lexinfo:supersededTerm",
            "lexinfo:synonym",
            "lime:entry",
            "lime:language",
            "ontolex:canonicalForm",
            "ontolex:denotes",
            "ontolex:evokes",
            "ontolex:isConceptOf",
            "ontolex:isEvokedBy",
            "ontolex:isLexicalizedSenseOf",
            "ontolex:isSenseOf",
            "ontolex:lexicalizedSense",
            "ontolex:lexicalForm",
            "ontolex:reference",
            "ontolex:representation",
            "ontolex:sense",
            "ontolex:usage",
            "ontolex:writtenRep",
            "rdf:value",
            "rdfs:label",
            "skos:altLabel",
            "skos:broader",
            "skos:broaderTransitive",
            "skos:definition",
            "skos:editorialNote",
            "skos:example",
            "skos:inScheme",
            "skos:narrower",
            "skos:narrowerTransitive",
            "skos:note",
            "skos:prefLabel",
            "skos:semanticRelation",
            "skos:topConceptOf",
            "thor:closeMapping",
            "thor:exactMapping",
            "thor:narrowMapping",
            "thor:hasContext ",
    };

    String sparql = Vocabulary.getPrefixDeclarations() +
            "SELECT * " +
            "WHERE { " +
            "   ?subject ?property ?value . " +
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
        final RDFNode value = solution.get("value");

        if (value.isAnon())
          continue;

        Statement s = new Statement(subject, property, value);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }
}
