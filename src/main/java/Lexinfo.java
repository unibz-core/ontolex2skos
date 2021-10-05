import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Lexinfo {

  public final static String IRI = "http://www.lexinfo.net/ontology/3.0/lexinfo#";
  public final static String SYNONYM_URI = IRI + "synonym";

  private OntModel model;

  public Lexinfo(OntModel model) {
    this.model = model;
    String ontologyPath = Ontolex.class.getClassLoader().getResource("lexinfo-short.ttl").getFile();
    RDFDataMgr.read(model, ontologyPath);
  }

  public OntProperty getSynonym() {
    OntProperty synonym = model.getOntProperty(SYNONYM_URI);

    if (synonym == null)
      throw new NullPointerException("Cannot get lexinfo:synonym");

    return synonym;
  }

  public void setAsSynonyms(Individual sense1, Individual sense2) {
    OntProperty synonym = getSynonym();
    sense1.addProperty(synonym, sense2);
    sense2.addProperty(synonym, sense1);
  }

  public boolean areSynonyms(Individual sense1, Individual sense2) {
    return isSynonymOf(sense1, sense2) ||
            isSynonymOf(sense2, sense1);
  }

  public boolean isSynonymOf(Individual sense1, Individual sense2) {
    return getSynonyms(sense1).contains(sense2);
  }

  public List<Individual> getSynonyms(Individual sense) {
    final OntProperty synonym = getSynonym();

    return sense
            .listProperties(synonym)
            .toList()
            .stream()
            .map(Statement::getObject)
            .map(RDFNode::asResource)
            .map(Resource::getURI)
            .map(uri -> model.getIndividual(uri))
            .collect(toList());
  }
}
