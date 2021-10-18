package it.unibz.core.thor;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.List;

public class OntolexTargetGraph extends KnowledgeGraph {
  WorkingGraph source;
  OntModel model;

  public OntolexTargetGraph(WorkingGraph source) {
    super();
    this.source = source;
    this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    addDefaultNamespaces();
  }

  @Override
  public Model getModel() {
    return model;
  }

  public void copyLexicons() {
    String sparql = Vocabulary.getPrefixDeclarations() +
            "SELECT ?lexicon " +
            "WHERE { " +
            "   ?lexicon rdf:type lime:Lexicon . " +
            "}";

    Query query = QueryFactory.create(sparql);
    try (QueryExecution qexec = QueryExecutionFactory.create(query, source.getModel())) {

      List<Statement> statements = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution solution = resultSet.nextSolution();
        final RDFNode lexicon = solution.get("lexicon");

        Statement s = new Statement(lexicon, "rdf:type", "lime:Lexicon");
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  public void copyLexiconData() {
    String[] properties = {
            "lime:entry",
            "lime:language",
            "dct:title"
    };

    String sparql = Vocabulary.getPrefixDeclarations() +
            "SELECT * " +
            "WHERE { " +
            "   ?lexicon ?property ?value . " +
            "   ?lexicon rdf:type lime:Lexicon . " +
            "FILTER(?property IN (" + String.join(", ", properties) + ")) " +
            "}";

    Query query = QueryFactory.create(sparql);
    try (QueryExecution qexec = QueryExecutionFactory.create(query, source.getModel())) {
      List<Statement> statements = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution solution = resultSet.nextSolution();
        final RDFNode lexicon = solution.get("lexicon");
        final RDFNode property = solution.get("property");
        final RDFNode value = solution.get("value");

        if (value.isAnon())
          continue;

        Statement s = new Statement(lexicon, property, value);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }
}
