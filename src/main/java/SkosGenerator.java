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

  public SkosGenerator(OntModel model, OntModel target) {
    this.model = model;
    this.target = target;

    System.out.println("Loading ontolex...");
    ontolex = new Ontolex(model, target);
    System.out.println("Loading lexinfo...");
    lexinfo = new Lexinfo(model);
    System.out.println("Loading skos...");
    skos = new Skos(model, target);
  }

  public void generateSkosData() {
    final List<Synset> synsets = getSynsets();
    synsets.forEach(System.out::println);
    synsets.forEach(this::mapSynsetToConcept);
  }

  public List<Synset> getSynsets() {
    System.out.println("Retrieving synsets...");
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
    String conceptUri = skos.createConcept();
    addIsLexicalizedSenseOf(conceptUri, synset);
    addPrefLabel(conceptUri, synset);
    addAltLabels(conceptUri, synset);
    copySkosProperty("skos:definition", conceptUri, synset);
    copySkosProperty("skos:editorialNote", conceptUri, synset);
    copySkosProperty("skos:scopeNote", conceptUri, synset);
    copySkosProperty("skos:historyNote", conceptUri, synset);
    copySkosProperty("skos:example", conceptUri, synset);
    copySkosProperty("skos:changeNote", conceptUri, synset);
  }

  private void copySkosProperty(String propertyUri, String conceptUri, Synset synset) {
    synset.stream()
            .forEach(sense -> this.addSkosProperty(propertyUri, sense, conceptUri));
  }

  private void addSkosProperty(String propertyUri, Resource source, String targetUri) {
    String senseUri = source.getURI();
    List<Literal> literals = skos.getTextProperty(propertyUri, senseUri);

    literals.stream()
            .forEach(literal -> skos.addTextProperty(propertyUri, targetUri, literal));
  }

  private void addIsLexicalizedSenseOf(String conceptUri, Synset synset) {
    if (synset.isEmpty())
      throw new IllegalArgumentException("Empty synset!");

    synset.stream()
            .map(Resource::getURI)
            .forEach(senseUri -> ontolex.setIsLexicalizedSenseOf(senseUri, conceptUri));
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
