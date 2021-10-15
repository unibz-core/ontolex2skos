package it.unibz.core.thor;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.*;
import java.util.stream.Stream;

import static it.unibz.core.thor.Vocabulary.THORL;
import static it.unibz.core.thor.Vocabulary.getPrefixDeclarations;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class WorkingGraph extends KnowledgeGraph {

  public WorkingGraph() {
    super();
  }

  @Override
  protected OntModel createGraph() {
    return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
  }

  public String createSkosConcept() {
    String individualUri = THORL.uri + "Concept_" + UUID.randomUUID();
    Statement s1 = new Statement(individualUri, "rdf:type", "skos:Concept");
    Statement s2 = new Statement(individualUri, "rdf:type", "thor:DerivedConcept");
    insertStatements(s1, s2);

    return individualUri;
  }

  public List<String> getLexicalSenseUris() {
    return getInstanceUris("ontolex:LexicalSense");
  }

  public List<Synset> findSynsets() {
    List<String> allSenses = getLexicalSenseUris();

    List<Synset> synsets = new ArrayList<>();
    Map<String, Synset> map = new HashMap<>();

    for (String senseUri : allSenses) {
      Synset synset = map.get(senseUri);

      if (synset == null) {
        synset = new Synset(senseUri);
        map.put(senseUri, synset);
        synsets.add(synset);
      }

      List<String> synonyms = getSynonyms(senseUri);
      synset.addAll(synonyms);

      for (String synonym : synonyms) {
        map.put(synonym, synset);
      }
    }

    return synsets;
  }

  public List<String> getSynonyms(String senseUri) {
    return getPropertiesAsUris(senseUri, "lexinfo:synonym");
  }

  public void resolveConflictsOnPrefLabels() {
    Map<Literal, List<String>> prefLabelConflicts = getPrefLabelConflicts();
    Map<String, Map<String, String>> domainLabels = getDomainLabels();

    List<Statement> statementsToDelete = new ArrayList<>();
    List<Statement> statementsToInsert = new ArrayList<>();

    for (Literal conflictingPrefLabel : prefLabelConflicts.keySet()) {
      String language = conflictingPrefLabel.getLanguage();
      String text = conflictingPrefLabel.getString();

      for (String conceptUri : prefLabelConflicts.get(conflictingPrefLabel)) {

        Map<String, String> domainLabelMap = domainLabels.get(conceptUri);
        String domainString;
        Literal newPrefLabel;

        if (domainLabelMap.containsKey(language)) {
          domainString = domainLabelMap.get(language);
          newPrefLabel = model.createLiteral(text + " (" + domainString + ")", language);
        } else if (domainLabelMap.containsKey(null)) {
          domainString = domainLabelMap.get(null);
          newPrefLabel = model.createLiteral(text + " (" + domainString + ")");
        } else {
          continue;
        }

        statementsToDelete.add(new Statement(conceptUri, "skos:prefLabel", conflictingPrefLabel));
        statementsToInsert.add(new Statement(conceptUri, "skos:prefLabel", newPrefLabel));

        insertStatements(statementsToInsert);
        deleteStatements(statementsToDelete);

      }
    }
  }

  public Map<Literal, List<String>> getPrefLabelConflicts() {
    String sparql = getPrefixDeclarations() +
            "SELECT DISTINCT ?label (GROUP_CONCAT(?concept;SEPARATOR=\"±\") as ?concepts) " +
            "WHERE { " +
            "   ?concept skos:prefLabel ?label . " +
            "   ?concept rdf:type thor:DerivedConcept . " +
            "}" +
            "GROUP BY ?label " +
            "HAVING (COUNT(*) > 1)";

    Query query = QueryFactory.create(sparql);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      Map<Literal, List<String>> conflicts = new HashMap<>();

      ResultSet results = qexec.execSelect();
      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();

        Literal label = solution.getLiteral("label");
        String joinedUris = solution.getLiteral("concepts").getString();
        List<String> conceptUris = asList(joinedUris.split("±"));

        conflicts.put(label, conceptUris);
      }

      return conflicts;
    }
  }

  public Map<String, Map<String, String>> getDomainLabels() {
    String sparql = getPrefixDeclarations() +
            "SELECT ?concept " +
            "       (GROUP_CONCAT( CONCAT(?domainLabel,'@',LANG(?domainLabel));SEPARATOR='±') as ?domainLabels) " +
            "WHERE { " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense lexinfo:domain ?domain . " +
            "   ?domain rdfs:label ?domainLabel . " +
            "}" +
            "GROUP BY ?concept ";

    Query query = QueryFactory.create(sparql);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      Map<String, Map<String, String>> conceptMap = new HashMap<>();

      ResultSet results = qexec.execSelect();
      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();
        RDFNode concept = solution.get("concept");

        if (!concept.isURIResource())
          continue;

        String conceptUri = concept.asResource().getURI();
        Map<String, String> labelMap = new HashMap<>();
        conceptMap.put(conceptUri, labelMap);

        String[] labels = solution.getLiteral("domainLabels")
                .toString()
                .split("±");

        for (String label : labels) {
          int lastAtIndex = label.lastIndexOf("@");
          String text = (lastAtIndex != -1) ? label.substring(0, lastAtIndex) : label;
          String language = (lastAtIndex != -1) ? label.substring(lastAtIndex + 1) : null;

          labelMap.put(language, text);
        }
      }

      return conceptMap;
    }
  }

  public void addIsLexicalizedSenseOf(String conceptUri, Synset synset) {
    List<Statement> statements = synset.stream()
            .map(uri -> new Statement(uri, "ontolex:isLexicalizedSenseOf", conceptUri))
            .collect(toList());

    insertStatements(statements);
  }


  private boolean hasUri(RDFNode entity) {
    return entity.isURIResource() && entity.asResource().getURI() != null;
  }

  public void deriveIsConceptOf() {
    String sparqlQuery = getPrefixDeclarations() +
            "SELECT ?concept ?entity " +
            "WHERE { " +
            "   ?concept rdf:type skos:Concept . " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense ontolex:reference ?entity " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> results = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution solution = resultSet.nextSolution();
        RDFNode concept = solution.get("?concept");
        RDFNode entity = solution.get("?entity");

        if (hasUri(entity)) {
          Statement s = new Statement(concept, "ontolex:isConceptOf", entity);
          results.add(s);
        }
      }

      insertStatements(results);
    }
  }

  public void deriveIsEvokedBy() {
    String sparqlQuery = getPrefixDeclarations() +
            "SELECT ?concept ?entry " +
            "WHERE { " +
            "   ?concept rdf:type skos:Concept . " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense ontolex:isSenseOf ?entry . " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> results = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution solution = resultSet.nextSolution();
        RDFNode concept = solution.get("concept");
        RDFNode entry = solution.get("entry");

        Statement s = new Statement(concept, "ontolex:isEvokedBy", entry);
        results.add(s);
      }

      insertStatements(results);
    }
  }

  public void deriveBroaderNarrower() {
    String sparqlQuery = getPrefixDeclarations() +
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

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> results = new ArrayList<>();
      ResultSet resultSet = qexec.execSelect();

      while (resultSet.hasNext()) {
        QuerySolution soln = resultSet.nextSolution();
        RDFNode narrower = soln.getResource("narrower");
        RDFNode broader = soln.getResource("broader");
        Statement s1 = new Statement(narrower, "skos:broader", broader);
        Statement s2 = new Statement(broader, "skos:narrower", narrower);
        results.add(s1);
        results.add(s2);
      }

      insertStatements(results);
    }
  }

  void deriveAlternativeLabelsFromAcronyms() {
    String sparqlQuery = getPrefixDeclarations() +
            "SELECT ?concept ?label " +
            "WHERE { " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense ontolex:isSenseOf ?entry . " +
            "   ?entry lexinfo:fullFormFor ?acronym . " +
            "   ?acronym ontolex:canonicalForm ?form . " +
            "   ?form ontolex:writtenRep ?label . " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> statements = new ArrayList<>();
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();
        RDFNode concept = solution.getResource("concept");
        RDFNode label = solution.getLiteral("label");

        Statement s = new Statement(concept, "skos:altLabel", label);
        statements.add(s);
      }

      insertStatements(statements);
    }

  }

  void deriveHasContext() {
    String sparqlQuery = getPrefixDeclarations() +
            "SELECT ?concept ?domain " +
            "WHERE { " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense lexinfo:domain ?domain . " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> statements = new ArrayList<>();
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();
        RDFNode concept = solution.get("concept");
        RDFNode label = solution.get("domain");

        Statement s = new Statement(concept, "thor:hasContext", label);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }


  private Stream<Statement> createSchemeStatements(Resource lexicon) {
    String schemeUri = THORL.prefix + ":Scheme_" + UUID.randomUUID();

    Statement schemeStatement = new Statement(schemeUri, "rdf:type", "skos:ConceptScheme");
    Statement setStatement = new Statement(schemeUri, "rdf:type", "ontolex:ConceptSet");
    Statement schemeToLexiconStatement = new Statement(schemeUri, "thor:wasDerivedFrom", lexicon);

    return Stream.of(schemeStatement, setStatement, schemeToLexiconStatement);
  }

  public void deriveScheme() {
    List<Resource> lexicons = getInstances("lime:Lexicon");

    Statement[] statements = lexicons.stream()
            .flatMap(this::createSchemeStatements)
            .toArray(Statement[]::new);

    insertStatements(statements);
  }

  void derivePreferredLabels() {
    String sparqlQuery = getPrefixDeclarations() +
            "SELECT ?concept ?label " +
            "WHERE { " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense rdf:type thor:PreferredSense . " +
            "   ?sense ontolex:isSenseOf ?entry ." +
            "   ?entry ontolex:canonicalForm ?form ." +
            "   ?form  ontolex:writtenRep ?label" +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> statements = new ArrayList<>();
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();
        RDFNode concept = solution.get("concept");
        RDFNode label = solution.get("label");

        Statement s = new Statement(concept, "skos:prefLabel", label);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  void deriveAlternativeLabels() {
    String sparqlQuery = getPrefixDeclarations() +
            "SELECT ?concept ?label " +
            "WHERE { " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense rdf:type ontolex:LexicalSense . " +
            "   ?sense ontolex:isSenseOf ?entry ." +
            "   ?entry ontolex:canonicalForm ?form ." +
            "   ?form  ontolex:writtenRep ?label ." +
            "   FILTER NOT EXISTS { ?sense rdf:type thor:PreferredSense  } . " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> statements = new ArrayList<>();
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();
        RDFNode concept = solution.get("concept");
        RDFNode label = solution.get("label");

        Statement s = new Statement(concept, "skos:altLabel", label);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  void copyPropertiesFromSenses() {
    String[] properties = {
            "skos:definition",
            "skos:scopeNote",
            "skos:historyNote",
            "skos:example",
            "foaf:homepage",
    };

    String sparql = Vocabulary.getPrefixDeclarations() +
            "SELECT ?concept ?property ?value " +
            "WHERE { " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense ?property ?value . " +
            "FILTER(?property IN (" + String.join(", ", properties) + ")) " +
            "}";

    Query query = QueryFactory.create(sparql);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> statements = new ArrayList<>();
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();
        RDFNode concept = solution.get("concept");
        RDFNode property = solution.get("property");
        RDFNode value = solution.get("value");

        Statement s = new Statement(concept, property, value);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  public void deriveConcepts() {
    List<Synset> synsets = findSynsets();

    for (Synset synset : synsets) {
      String conceptUri = createSkosConcept();
      addIsLexicalizedSenseOf(conceptUri, synset);
    }
  }

  public void deriveScopeNote() {
    String sparqlQuery = getPrefixDeclarations() +
            "SELECT ?concept ?usage " +
            "WHERE { " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense ontolex:usage ?usage . " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> statements = new ArrayList<>();
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();
        RDFNode concept = solution.get("concept");
        RDFNode usage = solution.get("usage");

        Statement s = new Statement(concept, "skos:scopeNote", usage);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  public void deriveInScheme() {
    String sparqlQuery = getPrefixDeclarations() +
            "SELECT ?concept ?scheme " +
            "WHERE { " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense ontolex:isSenseOf ?entry . " +
            "   ?lexicon lime:entry ?entry . " +
            "   ?scheme thor:wasDerivedFrom ?lexicon . " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> statements = new ArrayList<>();
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();
        RDFNode concept = solution.get("concept");
        RDFNode scheme = solution.get("scheme");

        Statement s = new Statement(concept, "skos:inScheme", scheme);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  public void deriveTopConceptOf() {
    String sparqlQuery = getPrefixDeclarations() +
            "SELECT ?concept ?scheme " +
            "WHERE { " +
            "   ?concept rdf:type skos:Concept . " +
            "   ?concept skos:inScheme ?scheme . " +
            "   FILTER NOT EXISTS { " +
            "      ?concept skos:broader ?broader . " +
            "      ?broader skos:inScheme ?schemew . " +
            "   } . " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> statements = new ArrayList<>();
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();
        RDFNode concept = solution.get("concept");
        RDFNode scheme = solution.get("scheme");

        Statement s = new Statement(concept, "skos:topConceptOf", scheme);
        statements.add(s);
      }

      insertStatements(statements);
    }
  }

  public void deriveSchemeLabel() {
    String sparqlQuery = getPrefixDeclarations() +
            "SELECT ?scheme ?title " +
            "WHERE { " +
            "   ?scheme thor:wasDerivedFrom ?lexicon . " +
            "   ?lexicon dct:title ?title . " +
            "} ";

    Query query = QueryFactory.create(sparqlQuery);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      List<Statement> statements = new ArrayList<>();
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution solution = results.nextSolution();
        RDFNode schemeNode = solution.get("scheme");
        RDFNode titleNode = solution.get("title");

        if (!titleNode.isLiteral())
          continue;

        Literal title = titleNode.asLiteral();
        String titleString = title.getString();
        String titleLanguage = title.getLanguage();

        if (titleString == null)
          continue;

        String labelValue = titleString.replaceAll("Lexicon", "Thesaurus");
        Literal label = model.createLiteral(labelValue, titleLanguage);

        Statement s1 = new Statement(schemeNode, "rdfs:label", label);
        Statement s2 = new Statement(schemeNode, "skos:prefLabel", label);
        statements.add(s1);
        statements.add(s2);
      }

      insertStatements(statements);
    }
  }

}
