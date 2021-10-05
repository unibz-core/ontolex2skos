import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RDFDataMgr;

import java.util.UUID;

public class Skos {

  public final static String IRI = "http://www.w3.org/2004/02/skos/core#";
  public final static String CONCEPT_URI = IRI + "Concept";
  public final static String PREF_LABEL_URI = IRI + "prefLabel";
  public final static String ALT_LABEL_URI = IRI + "altLabel";

  private OntModel model;

  public Skos(OntModel model) {
    this.model = model;
    String ontologyPath = Ontolex.class.getClassLoader().getResource("skos.rdf").getFile();
    RDFDataMgr.read(model, ontologyPath);
  }

  public OntClass getConceptClass() {
    return model.getOntClass(CONCEPT_URI);
  }

  public OntProperty getPrefLabelProperty() {
    return model.getOntProperty(PREF_LABEL_URI);
  }

  public OntProperty getAltLabelProperty() {
    return model.getOntProperty(ALT_LABEL_URI);
  }

  public Individual createConceptInstance(String uri) {
    if (uri == null)
      throw new NullPointerException("URI cannot be null!");

    return getConceptClass().createIndividual(uri);
  }

  public Individual createConceptInstance() {
    return createConceptInstance("http://purl.org/ZIN-Thor/Concept" + UUID.randomUUID());
  }

  public void createPrefLabel(Individual concept, RDFNode label) {
    final Property prop = getPrefLabelProperty();
    concept.addProperty(prop, label);
  }

  public void createAltLabel(Individual concept, RDFNode label) {
    final Property prop = getAltLabelProperty();
    concept.addProperty(prop, label);
  }
}
