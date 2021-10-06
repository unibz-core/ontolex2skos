import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkosGenerator {
  OntModel model;
  OntModel target;

  final Ontolex ontolex;
  final Lexinfo lexinfo;
  final Skos skos;

  private Map<String, List<String>> referencesPerSense;

  public SkosGenerator(OntModel model, OntModel target) {
    this.model = model;
    this.target = target;

    System.out.println("Loading ontolex...");
    ontolex = new Ontolex(model, target);
    System.out.println("Loading lexinfo...");
    lexinfo = new Lexinfo(model);
    System.out.println("Loading skos...");
    skos = new Skos(model, target);

    System.out.println("Building maps...");
    referencesPerSense = ontolex.getReferenceUriMap();
  }

  public void generateSkosData() {
    System.out.println("Retrieving synsets...");
    final List<Synset> synsets = getSynsets();

    System.out.println("Mapping synsets to concepts...");
    synsets.forEach(this::mapSynsetToConcept);
  }

  public List<Synset> getSynsets() {
    List<Resource> allSenses = ontolex.getLexicalSenses();

    List<Synset> synsets = new ArrayList<>();
    Map<Resource, Synset> map = new HashMap<>();

    for (Resource sense : allSenses) {
      Synset synset = map.get(sense);

      if (synset == null) {
        synset = new Synset(sense);
        map.put(sense, synset);
        synsets.add(synset);
      }

      List<Resource> synonyms = lexinfo.getSynonyms(sense.getURI());
      synset.addAll(synonyms);

      for (Resource synonym : synonyms) {
        map.put(synonym, synset);
      }
    }

    return synsets;
  }

  private void mapSynsetToConcept(Synset synset) {
    System.out.println("Mapping synset: " + synset);
    String conceptUri = skos.createConcept();

    System.out.println("Adding ontolex:isLexicalizedSenseOf...");
    addIsLexicalizedSenseOf(conceptUri, synset);
    System.out.println("Adding ontolex:isConceptof...");
    addIsConceptOf(conceptUri, synset);

    System.out.println("Adding skos:prefLabel...");
    addPrefLabel(conceptUri, synset);
    System.out.println("Adding skos:altLabel...");
    addAltLabels(conceptUri, synset);

    System.out.println("Copying skos textual properties...");
    copySkosTextProperty("skos:definition", conceptUri, synset);
    copySkosTextProperty("skos:editorialNote", conceptUri, synset);
    copySkosTextProperty("skos:scopeNote", conceptUri, synset);
    copySkosTextProperty("skos:historyNote", conceptUri, synset);
    copySkosTextProperty("skos:example", conceptUri, synset);
    copySkosTextProperty("skos:changeNote", conceptUri, synset);
  }


  private void addIsLexicalizedSenseOf(String conceptUri, Synset synset) {
    synset.stream()
            .map(Resource::getURI)
            .forEach(senseUri -> ontolex.addIsLexicalizedSenseOf(senseUri, conceptUri));
  }

  private void addIsConceptOf(String conceptUri, Synset synset) {
    synset.stream()
            .map(Resource::getURI)
            .filter(referencesPerSense::containsKey)
            .map(referencesPerSense::get)
            .forEach(referenceUris -> ontolex.addIsConceptOf(conceptUri, referenceUris));
  }

  private void copySkosTextProperty(String propertyUri, String conceptUri, Synset synset) {
    synset.stream()
            .forEach(sense -> this.addSkosProperty(propertyUri, sense, conceptUri));
  }

  private void addSkosProperty(String propertyUri, Resource source, String targetUri) {
    String senseUri = source.getURI();
    List<Literal> literals = skos.getTextProperty(propertyUri, senseUri);

    literals.stream()
            .forEach(literal -> skos.addTextProperty(propertyUri, targetUri, literal));
  }

  private void addPrefLabel(String conceptUri, Synset synset) {
    String firstSenseUri = synset.first().getURI();
    Literal label = ontolex.getLabel(firstSenseUri);

    if (label != null)
      skos.addPrefLabel(conceptUri, label);
  }

  private void addAltLabels(String conceptUri, Synset synset) {
    synset.tailSet()
            .stream()
            .map(Resource::getURI)
            .forEach(senseUri -> addAltLabel(conceptUri, senseUri));
  }

  private void addAltLabel(String conceptUri, String senseUri) {
    Literal label = ontolex.getLabel(senseUri);

    if (label != null)
      skos.addAltLabel(conceptUri, label);

  }


}
