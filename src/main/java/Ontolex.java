import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;

import java.util.List;

public class Ontolex {
  public static final String IRI = "http://www.w3.org/ns/lemon/ontolex#";

  public static final String LEXICAL_ENTRY_URI = IRI + "LexicalEntry";
  public static final String LEXICAL_SENSE_URI = IRI + "LexicalSense";
  public static final String LEXICAL_CONCEPT_URI = IRI + "LexicalConcept";
  public static final String FORM_URI = IRI + "Form";

  public static final String IS_SENSE_OF_URI = IRI + "isSenseOf";
  public static final String CANONICAL_FORM_URI = IRI + "canonicalForm";
  public static final String WRITTEN_REP_URI = IRI + "writtenRep";
  public static final String IS_LEXICALIZED_SENSE_OF_URI = IRI + "isLexicalizedSenseOf";

  private OntModel model;

  public Ontolex(OntModel model) {
    this.model = model;
    String ontologyPath = Ontolex.class.getClassLoader().getResource("ontolex.ttl").getFile();
    RDFDataMgr.read(model, ontologyPath);
  }

  public OntClass getLexicalSenseClass() {
    return model.getOntClass(LEXICAL_SENSE_URI);
  }

  public OntClass getLexicalEntryClass() {
    return model.getOntClass(LEXICAL_ENTRY_URI);
  }

  public OntClass getLexicalConceptClass() {
    return model.getOntClass(LEXICAL_CONCEPT_URI);
  }

  public OntClass getFormClass() {
    return model.getOntClass(FORM_URI);
  }

  public OntProperty getIsSenseOfProperty() {
    return model.getOntProperty(IS_SENSE_OF_URI);
  }

  public OntProperty getCanonicalFormProperty() {
    return model.getOntProperty(CANONICAL_FORM_URI);
  }

  public OntProperty getWrittenRepProperty() {
    return model.getOntProperty(WRITTEN_REP_URI);
  }

  public OntProperty getIsLexicalizedSenseOfProperty() {
    return model.getOntProperty(IS_LEXICALIZED_SENSE_OF_URI);
  }

  public List<Individual> getLexicalSenseInstances() {
    System.out.println("Retrieving instances of ontolex:LexicalSense");

    return getLexicalSenseClass()
            .listInstances()
            .filterKeep(OntResource::isIndividual)
            .mapWith(resource -> (Individual) resource)
            .toList();
  }

  public RDFNode getLabel(Individual sense) {
    Resource lexicalEntry = getLexicalEntry(sense);

    if (lexicalEntry == null)
      return null;

    Resource form = getCanonicalFormProperty(lexicalEntry);

    if (form == null)
      return null;

    Statement textStatement = getWrittenRep(form);

    if(textStatement == null) {
      return null;
    }

    return textStatement.getObject();
  }

  private Statement getWrittenRep(Resource form) {
    return form.getProperty(getWrittenRepProperty());
  }

  private Resource getCanonicalFormProperty(Resource lexicalEntry) {
    return lexicalEntry.getPropertyResourceValue(getCanonicalFormProperty());
  }

  public Resource getLexicalEntry(Individual sense) {
    return sense.getPropertyResourceValue(getIsSenseOfProperty());
  }

  public Individual createLexicalSenseInstance(String uri) {
    return getLexicalSenseClass().createIndividual(uri);
  }

  public void setIsLexicalizedSenseOf(Individual sense, Individual concept) {
    OntProperty isLexicalizedSenseOf = getIsLexicalizedSenseOfProperty();
    sense.addProperty(isLexicalizedSenseOf, concept);
  }

}
