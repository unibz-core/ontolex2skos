import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkosGenerator {
  OntModel model;
  final Ontolex ontolex;
  final Lexinfo lexinfo;
  final Skos skos;

  public SkosGenerator(OntModel model) {
    this.model = model;
    System.out.println("Loading ontolex...");
    ontolex = new Ontolex(model);
    System.out.println("Loading lexinfo...");
    lexinfo = new Lexinfo(model);
    System.out.println("Loading skos...");
    skos = new Skos(model);
  }

  public void generateSkosData() {
    final List<Synset> synsets = getSynsets();
    System.out.println("Hello!");
    synsets.forEach(System.out::println);

//    synsets.stream()
//            .map(this::mapToSkosConcept)
//            .map(Individual::getURI)
//            .forEach(System.out::println);
  }

  public List<Synset> getSynsets() {

    List<Individual> allSenses = ontolex.getLexicalSenseInstances();

    List<Synset> synsets = new ArrayList<>();
    Map<Individual, Synset> map = new HashMap<>();

    for (Individual sense : allSenses) {
      System.out.println("Processing " + sense.getURI());
      Synset synset = map.get(sense);

      if (synset == null) {
        synset = new Synset(sense);
        map.put(sense, synset);
        synsets.add(synset);
      }

      List<Individual> synonyms = lexinfo.getSynonyms(sense);
      synset.addAll(synonyms);

      for (Individual synonym : synonyms) {
        map.put(synonym, synset);
      }
    }

    return synsets;
  }

  private Individual mapToSkosConcept(Synset synset) {
    System.out.println("Creating concept for: " + synset);
    Individual concept = skos.createConceptInstance();
    System.out.println("Creating isLexicalizedSenseOf " + concept.getURI());
    createIsLexicalizedSenseOfAssertions(concept, synset);
    createLabels(concept, synset);
    return concept;
  }

  private void createIsLexicalizedSenseOfAssertions(Individual concept, Synset synset) {
    if (synset.isEmpty())
      throw new IllegalArgumentException("Empty synset!");

    synset.stream()
            .forEach(sense -> ontolex.setIsLexicalizedSenseOf(sense, concept));
  }

  private void createLabels(Individual concept, Synset synset) {
    if (synset.isEmpty())
      throw new IllegalArgumentException("Empty synset!");

    this.createPrefLabel(concept, synset.first());

    synset.tailSet()
            .stream()
            .forEach(sense -> this.createAltLabel(concept, sense));
  }

  private void createPrefLabel(Individual concept, Individual sense) {
    RDFNode label = ontolex.getLabel(sense);

    if (label == null)
      return;

    System.out.println("Creating prefLabel: " + label.toString());
    skos.createPrefLabel(concept, label);
  }

  private void createAltLabel(Individual concept, Individual sense) {
    RDFNode label = ontolex.getLabel(sense);
    System.out.println("Creating altLabel: " + label.toString());
    skos.createAltLabel(concept, label);
  }


}
