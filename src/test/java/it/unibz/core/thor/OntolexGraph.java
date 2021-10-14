package it.unibz.core.thor;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

public class OntolexGraph extends KnowledgeGraph {

  @Override
  protected OntModel createGraph() {
    return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
  }

  public OntolexGraph() {
    super();
    loadModule(Vocabulary.ONTOLEX);
    loadModule(Vocabulary.LEXINFO);
    loadModule(Vocabulary.LIME);
    loadModule(Vocabulary.VARTRANS);
  }

  public void setAsSynonyms(String sense1Uri, String sense2Uri) {
    Statement s = new Statement(sense1Uri, "lexinfo:synonym", sense2Uri);
    insertStatements(s);
  }
}
