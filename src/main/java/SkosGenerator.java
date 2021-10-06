import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

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
    System.out.println("Retrieving synsets...");
    final List<Synset> synsets = getSynsets();

    System.out.println("Mapping synsets to concepts...");
    synsets.forEach(this::mapSynsetToConcept);

    System.out.println("Deriving skos:altLabel from acronyms...");
    deriveAltLabelFromAcronyms();
    System.out.println("Deriving ontolex:isConceptof...");
    ontolex.deriveIsConceptOf();
    System.out.println("Deriving ontolex:isEvokedBy...");
    ontolex.deriveIsEvokedBy();
    System.out.println("Deriving skos:broader and skos:narrower...");
    skos.deriveBroaderNarrower();

    //TODO: DERIVE alt label from acronym (ZIN/CIZ should be alt label of the concept that points to Zoorginstituut Nederland)
    //TODO: name conflict resolution (aambieder and zorgambieder)
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
    String conceptUri = skos.createConcept();
    addIsLexicalizedSenseOf(conceptUri, synset);

    addPrefLabel(conceptUri, synset);
    addAltLabels(conceptUri, synset);

    copySkosTextProperty("skos:definition", conceptUri, synset);
    copySkosTextProperty("skos:editorialNote", conceptUri, synset);
    copySkosTextProperty("skos:scopeNote", conceptUri, synset);
    copySkosTextProperty("skos:historyNote", conceptUri, synset);
    copySkosTextProperty("skos:example", conceptUri, synset);
    copySkosTextProperty("skos:changeNote", conceptUri, synset);
  }

  private void addIsLexicalizedSenseOf1(String conceptUri, Synset synset) {
    synset.stream()
            .map(Resource::getURI)
            .forEach(senseUri -> ontolex.addIsLexicalizedSenseOf(senseUri, conceptUri));
  }

  public void addIsLexicalizedSenseOf(String conceptUri, Synset synset) {
    List<String> statements = synset.stream()
            .map(Resource::getURI)
            .map(uri -> "<" + uri + "> ontolex:isLexicalizedSenseOf <" + conceptUri + "> ")
            .collect(toList());

    insertData(statements);
  }

  private void insertData(List<String> statements) {
    String sparqlInsert = "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX lexinfo: <http://www.lexinfo.net/ontology/3.0/lexinfo#> " +
            "INSERT DATA { " +
            String.join(" . ", statements) +
            "}";

    execute(sparqlInsert);
  }

  private void execute(String sparql) {
    UpdateRequest request = UpdateFactory.create(sparql);
    UpdateAction.execute(request, model);
    UpdateAction.execute(request, target);
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

  private void deriveAltLabelFromAcronyms() {
    String sparqlQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
            "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#> " +
            "PREFIX lexinfo: <http://www.lexinfo.net/ontology/3.0/lexinfo#> " +
            "SELECT ?concept ?label " +
            "WHERE { " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense ontolex:isSenseOf ?entry . " +
            "   ?entry lexinfo:fullFormFor ?acronym . " +
            "   ?acronym ontolex:canonicalForm ?form . " +
            "   ?form ontolex:writtenRep ?label . " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    List<String> statements = new ArrayList<>();

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();
        Resource concept = soln.getResource("concept");
        Literal label = soln.getLiteral("label");
        String sanitizedLabel = getSanitizedText(label);
        statements.add("<" + concept + ">" + " skos:altLabel " + sanitizedLabel);
      }
    }

    insertData(statements);
  }

  private String getSanitizedText(Literal literal) {
    String language = literal.getLanguage();
    String text = literal.getString()
            .replaceAll("\n", " ")
            .replaceAll("\r", "")
            .replaceAll("\"", "\\\\\"")
            .trim();

    return "\"\"\"" + text + "\"\"\"@" + language;
  }


}
