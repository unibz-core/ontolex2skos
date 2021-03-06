package it.unibz.core.ontolex2skos;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class TestGraph extends KnowledgeGraph {
  OntModel model;
  SkosTargetGraph skosGraph;
  WorkingGraph workingGraph;
  private Ontolex2Skos transformer;

  public TestGraph() {
    this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

    addDefaultNamespaces();
    addTestNamespace();

    loadModule(Vocabulary.ONTOLEX);
    loadModule(Vocabulary.LEXINFO);
    loadModule(Vocabulary.LIME);
  }

  private void addTestNamespace() {
    model.setNsPrefix("test", "http://example.com/");
  }

  public void setAsSynonyms(String sense1Uri, String sense2Uri) {
    Statement s = new Statement(sense1Uri, "lexinfo:synonym", sense2Uri);
    insertStatements(s);
  }

  public void addTypeStatement(String uri, String typeUri) {
    Statement s = new Statement(uri, "rdf:type", typeUri);
    insertStatements(s);
  }

  public void createLexicalSense(String uri) {
    addTypeStatement(uri, "ontolex:LexicalSense");
  }

  public void createPreferredSense(String uri) {
    addTypeStatement(uri, "thor:PreferredSense");
    addTypeStatement(uri, "ontolex:LexicalSense");
  }

  public void createLexicon(String uri) {
    addTypeStatement(uri, "ontolex:Lexicon");
  }

  public void insert(String subjectUri, String predicateUri, String objectUri) {
    Statement s = new Statement(subjectUri, predicateUri, objectUri);
    insertStatements(s);
  }

  @Override
  public Model getModel() {
    return model;
  }

  public OntModel getOntModel() {
    return model;
  }

  public void transform() {
    transformer = new Ontolex2Skos(model);
    transformer.run();

    skosGraph = transformer.getSkosSubgraph();
    workingGraph = transformer.getWorkingGraph();
  }

  public boolean askWorkingGraph(String sparql) {
    return askGraph(workingGraph.getModel(), sparql);
  }

  public boolean askSkosGraph(String sparql) {
    return askGraph(skosGraph.getModel(), sparql);
  }

  private static boolean askGraph(Model model, String sparql) {
    Query query = QueryFactory.create(sparql);
    QueryExecution qexec = QueryExecutionFactory.create(query, model);
    boolean result = qexec.execAsk();
    qexec.close();

    return result;
  }

  public SkosTargetGraph getSkosGraph() {
    return skosGraph;
  }

  public WorkingGraph getWorkingGraph() {
    return workingGraph;
  }
}
