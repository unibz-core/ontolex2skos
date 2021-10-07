import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class SkosGenerator {

  OntModel sourceGraph;
  OntModel generatedGraph;

  public SkosGenerator(OntModel sourceGraph, OntModel generatedGraph) {
    this.sourceGraph = sourceGraph;
    this.generatedGraph = generatedGraph;
  }

  private static void loadModule(OntModel graph, String filename) {
    String ontologyPath = SkosGenerator.class.getClassLoader().getResource(filename).getFile();
    RDFDataMgr.read(graph, ontologyPath);
  }

  public void generateSkosData() {
    System.out.println("Adding namespaces");
    Domains.addNamespaces(sourceGraph);
    Domains.addNamespaces(generatedGraph);

    System.out.println("Loading ontolex module..");
    loadModule(sourceGraph, "ontolex.ttl");

    System.out.println("Loading lexinfo module...");
    loadModule(sourceGraph, "lexinfo-short.ttl");

    System.out.println("Loading skos module..");
    loadModule(sourceGraph, "skos.rdf");

    System.out.println("Loading property labels...");
    loadPropertyLabels();

    System.out.println("Finding synsets...");
    final List<Synset> synsets = getSynsets();

    System.out.println("Mapping synsets to concepts...");
    synsets.forEach(this::mapSynsetToConcept);

    System.out.println("Deriving skos:altLabel from acronyms...");
    deriveAltLabelFromAcronyms();
    System.out.println("Deriving ontolex:isConceptof...");
    deriveIsConceptOf();
    System.out.println("Deriving ontolex:isEvokedBy...");
    deriveIsEvokedBy();
    System.out.println("Deriving skos:broader and skos:narrower...");
    deriveBroaderNarrower();

    System.out.println("Attempting to resolve name conflicts...");
    resolveConflictsOnPrefLabels();
  }

  private void loadPropertyLabels() {
    List<String> statements = List.of("ontolex:isConceptOf rdf:type owl:ObjectProperty",
            "ontolex:isConceptOf rdfs:label \"is concept of\"@en ",
            "ontolex:isConceptOf rdfs:label \"is concept van\"@nl ");

    insertData(statements);
  }


  private void resolveConflictsOnPrefLabels() {
    Map<Literal, List<String>> conflicts = findPrefLabelConflicts();


  }

  private Map<String, List<String>> getAltLabels() {

    String sparql = Domains.SPARQL_PREFIXES +
            "SELECT DISTINCT ?concept (GROUP_CONCAT(?label) as ?labels) " +
            "WHERE { " +
            "   ?concept rdf:tyoe skos:Concept . " +
            "   ?concept skos:altLabel ?label . " +
            "}" +
            "GROUP BY ?concept ";

    Query query = QueryFactory.create(sparql);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph)) {
      Map<String, List<String>> labelsPerConcept = new HashMap<>();

      ResultSet results = qexec.execSelect();
      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();

        String conceptUri = solution.getResource("concept").getURI();
        String joinedLabels = solution.getLiteral("labels").getString();
        List<String> labels = asList(joinedLabels.split(" "));

        labelsPerConcept.put(conceptUri, labels);
      }

      return labelsPerConcept;
    }

  }

  private Map<Literal, List<String>> findPrefLabelConflicts() {
    String sparql = Domains.SPARQL_PREFIXES +
            "SELECT DISTINCT ?label (GROUP_CONCAT(?concept) as ?concepts) " +
            "WHERE { " +
            "   ?concept skos:prefLabel ?label . " +
            "}" +
            "GROUP BY ?label " +
            "HAVING (COUNT(*) > 1)";

    Query query = QueryFactory.create(sparql);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph)) {
      Map<Literal, List<String>> conflicts = new HashMap<>();

      ResultSet results = qexec.execSelect();
      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();

        Literal label = solution.getLiteral("label");
        String joinedUris = solution.getLiteral("concepts").getString();
        List<String> conceptUris = asList(joinedUris.split(" "));

        conflicts.put(label, conceptUris);
      }

      return conflicts;
    }
  }


  public List<Synset> getSynsets() {
    List<Resource> allSenses = getLexicalSenses();

    List<Synset> synsets = new ArrayList<>();
    Map<Resource, Synset> map = new HashMap<>();

    for (Resource sense : allSenses) {
      Synset synset = map.get(sense);

      if (synset == null) {
        synset = new Synset(sense);
        map.put(sense, synset);
        synsets.add(synset);
      }

      List<Resource> synonyms = getSynonyms(sense.getURI());
      synset.addAll(synonyms);

      for (Resource synonym : synonyms) {
        map.put(synonym, synset);
      }
    }

    return synsets;
  }

  private void mapSynsetToConcept(Synset synset) {
    String conceptUri = createSkosConcept();
    addIsLexicalizedSenseOf(conceptUri, synset);

    addPrefLabel(conceptUri, synset);
    addAltLabels(conceptUri, synset);

    copySkosTextProperty("skos:definition", conceptUri, synset);
    copySkosTextProperty("skos:scopeNote", conceptUri, synset);
    copySkosTextProperty("skos:historyNote", conceptUri, synset);
    copySkosTextProperty("skos:example", conceptUri, synset);
//    copySkosTextProperty("skos:editorialNote", conceptUri, synset);
//    copySkosTextProperty("skos:changeNote", conceptUri, synset);
  }

  private void addIsLexicalizedSenseOf1(String conceptUri, Synset synset) {
    synset.stream()
            .map(Resource::getURI)
            .forEach(senseUri -> addIsLexicalizedSenseOf(senseUri, conceptUri));
  }

  public void addIsLexicalizedSenseOf(String conceptUri, Synset synset) {
    List<String> statements = synset.stream()
            .map(Resource::getURI)
            .map(uri -> "<" + uri + "> ontolex:isLexicalizedSenseOf <" + conceptUri + "> ")
            .collect(toList());

    insertData(statements);
  }

  private void insertData(List<String> statements) {
    String sparqlInsert = Domains.SPARQL_PREFIXES +
            "INSERT DATA { " +
            String.join(" . ", statements) +
            "}";

    execute(sparqlInsert);
  }

  private void execute(String sparql) {
    UpdateRequest request = UpdateFactory.create(sparql);
    UpdateAction.execute(request, sourceGraph);
    UpdateAction.execute(request, generatedGraph);
  }

  private void copySkosTextProperty(String propertyUri, String conceptUri, Synset synset) {
    synset.stream()
            .forEach(sense -> this.addSkosProperty(propertyUri, sense, conceptUri));
  }

  private void addSkosProperty(String propertyUri, Resource source, String targetUri) {
    String senseUri = source.getURI();
    List<Literal> literals = getTextProperty(propertyUri, senseUri);

    literals.stream()
            .forEach(literal -> addTextProperty(propertyUri, targetUri, literal));
  }

  private void addPrefLabel(String conceptUri, Synset synset) {
    String firstSenseUri = synset.first().getURI();
    Literal label = getLabel(firstSenseUri);

    if (label != null)
      addPrefLabel(conceptUri, label);
  }

  private void addAltLabels(String conceptUri, Synset synset) {
    synset.tailSet()
            .stream()
            .map(Resource::getURI)
            .forEach(senseUri -> addAltLabel(conceptUri, senseUri));
  }

  private void addAltLabel(String conceptUri, String senseUri) {
    Literal label = getLabel(senseUri);

    if (label != null)
      addAltLabel(conceptUri, label);

  }

  private void deriveAltLabelFromAcronyms() {
    String sparqlQuery = Domains.SPARQL_PREFIXES +
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

    try (QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph)) {
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

  public String createInstance(String typeUri, String individualUri) {
    String sparql = Domains.SPARQL_PREFIXES +
            "INSERT DATA { " +
            "   <" + individualUri + "> rdf:type " + typeUri + "  " +
            "} ";

    execute(sparql);

    return individualUri;
  }

  public String createLexicalSense(String individualUri) {
    return createInstance("ontolex:LexicalSense", individualUri);
  }

  public String createSkosConcept(String individualUri) {
    return createInstance("skos:Concept", individualUri);
  }

  public String createSkosConcept() {
    return createSkosConcept(Domains.THOR_URI + "Concept_" + UUID.randomUUID());
  }

  public List<Resource> getLexicalSenses() {
    String queryString = Domains.SPARQL_PREFIXES +
            "SELECT ?sense " +
            "WHERE { " +
            "   ?sense rdf:type ontolex:LexicalSense " +
            "} ";

    Query query = QueryFactory.create(queryString);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph)) {
      ResultSet results = qexec.execSelect();

      List<Resource> senses = new ArrayList<>();

      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();
        Resource sense = soln.getResource("sense");
        senses.add(sense);
      }

      return senses;
    }

  }

  public Literal getLabel(String senseUri) {
    String queryString = Domains.SPARQL_PREFIXES +
            "SELECT ?label " +
            "WHERE { " +
            "   <" + senseUri + "> ontolex:isSenseOf ?entry ." +
            "   ?entry ontolex:canonicalForm ?form ." +
            "   ?form  ontolex:writtenRep ?label" +
            "} ";

    Query query = QueryFactory.create(queryString);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph)) {
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();
        return soln.getLiteral("label");
      }

      return null;
    }
  }


  public void addIsLexicalizedSenseOf(String senseUri, String conceptUri) {
    String sparql = Domains.SPARQL_PREFIXES +
            "INSERT DATA { " +
            "   <" + senseUri + "> ontolex:isLexicalizedSenseOf <" + conceptUri + ">" +
            "} ";

    execute(sparql);
  }

  public void deriveIsConceptOf() {
    String sparqlQuery = Domains.SPARQL_PREFIXES +
            "SELECT ?concept ?entity " +
            "WHERE { " +
            "   ?concept rdf:type skos:Concept . " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense ontolex:reference ?entity " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    List<String> statements = new ArrayList<>();

    try (QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph)) {
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();
        Resource concept = soln.getResource("?concept");
        RDFNode entity = soln.get("?entity");

        if (entity.isURIResource() && entity.asResource().getURI() != null)
          statements.add("<" + concept + ">" + " ontolex:isConceptOf " + "<" + entity + ">");
      }

    }

    insertData(statements);
  }


  public void deriveIsEvokedBy() {
    String sparqlQuery = Domains.SPARQL_PREFIXES +
            "SELECT ?concept ?entry " +
            "WHERE { " +
            "   ?concept rdf:type skos:Concept . " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense ontolex:isSenseOf ?entry . " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    List<String> statements = new ArrayList<>();

    try (QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph)) {
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();
        Resource concept = soln.getResource("concept");
        Resource entry = soln.getResource("entry");
        statements.add("<" + concept + ">" + " ontolex:isEvokedBy " + "<" + entry + ">");
      }
    }

    insertData(statements);
  }


  public void setAsSynonyms(String sense1Uri, String sense2Uri) {
    String sparql = Domains.SPARQL_PREFIXES +
            "INSERT DATA { " +
            "   <" + sense1Uri + "> lexinfo:synonym <" + sense2Uri + "> . " +
            "} ";

    execute(sparql);
  }

  public boolean areSynonyms(String sense1Uri, String sense2Uri) {
    String queryString = Domains.SPARQL_PREFIXES +
            "SELECT * " +
            "WHERE { <" + sense1Uri + "> lexinfo:synonym <" + sense2Uri + "> } ";

    Query query = QueryFactory.create(queryString);
    QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph);
    boolean result = qexec.execAsk();
    qexec.close();

    return result;
  }

  public List<String> getSynonymUris(String senseUri) {
    return getSynonyms(senseUri)
            .stream()
            .map(Resource::getURI)
            .collect(toList());
  }

  public List<Resource> getSynonyms(String senseUri) {
    String queryString = Domains.SPARQL_PREFIXES +
            "SELECT ?synonym " +
            "WHERE { <" + senseUri + "> lexinfo:synonym ?synonym } ";

    Query query = QueryFactory.create(queryString);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph)) {
      List<Resource> synonyms = new ArrayList<>();

      ResultSet results = qexec.execSelect();
      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();
        final RDFNode synonym1 = soln.get("synonym");
        Resource synonym = soln.getResource("synonym");

        if (!senseUri.equals(synonym.getURI()))
          synonyms.add(synonym);
      }

      return synonyms;
    }

  }

  public void addPrefLabel(String uri, Literal label) {
    String sanitizedLabel = getSanitizedText(label);

    String sparql = Domains.SPARQL_PREFIXES +
            "INSERT DATA { " +
            "   <" + uri + "> skos:prefLabel " + sanitizedLabel + " ." +
            "   <" + uri + "> rdfs:label " + sanitizedLabel +
            "} ";

    execute(sparql);
  }

  public void addAltLabel(String uri, Literal label) {
    String sanitizedLabel = getSanitizedText(label);

    String sparql = Domains.SPARQL_PREFIXES +
            "INSERT DATA { " +
            "   <" + uri + "> skos:altLabel " + sanitizedLabel + " . " +
            "   <" + uri + "> rdfs:label " + sanitizedLabel +
            "} ";

    execute(sparql);
  }

  public List<Literal> getTextProperty(String propertyUri, String uri) {
    String queryString = Domains.SPARQL_PREFIXES +
            "SELECT ?literal " +
            "WHERE { " +
            "   <" + uri + "> " + propertyUri + " ?literal ." +
            "} ";

    Query query = QueryFactory.create(queryString);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph)) {
      ResultSet results = qexec.execSelect();
      List<Literal> literals = new ArrayList<Literal>();

      while (results.hasNext()) {
        Literal l = results.nextSolution().getLiteral("literal");
        literals.add(l);
      }

      return literals;
    }
  }

  public void addTextProperty(String propertyUri, String uri, Literal definition) {
    String sanitizedDefinition = getSanitizedText(definition);

    String sparql = Domains.SPARQL_PREFIXES +
            "INSERT DATA { " +
            "   <" + uri + "> " + propertyUri + " " + sanitizedDefinition +
            "} ";

    execute(sparql);
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

  public void deriveBroaderNarrower() {
    String sparqlQuery = Domains.SPARQL_PREFIXES +
            "SELECT DISTINCT ?narrower ?broader " +
            "WHERE { " +
            "   ?narrower rdf:type skos:Concept . " +
            "   ?hyponym ontolex:isLexicalizedSenseOf ?narrower . " +
            "   ?hypernym ontolex:isLexicalizedSenseOf ?broader . " +
            "   ?broader rdf:type skos:Concept  . " +
            "   { ?hyponym lexinfo:hypernym ?hypernym } " +
            "   UNION " +
            "   { ?hypernym lexinfo:hyponym ?hyponym }" +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    List<String> statements = new ArrayList<>();

    try (QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph)) {
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();
        Resource narrower = soln.getResource("narrower");
        Resource broader = soln.getResource("broader");
        statements.add("<" + narrower + ">" + " skos:broader " + "<" + broader + ">");
        statements.add("<" + broader + ">" + " skos:narrower " + "<" + narrower + ">");
      }
    }

    insertData(statements);
  }


}
