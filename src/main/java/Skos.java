import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Skos {

  public final static String IRI = "http://www.w3.org/2004/02/skos/core#";
  public final static String CONCEPT_URI = IRI + "Concept";
  public final static String PREF_LABEL_URI = IRI + "prefLabel";
  public final static String ALT_LABEL_URI = IRI + "altLabel";

  private OntModel model;
  private OntModel target;

  public Skos(OntModel model, OntModel target) {
    this.model = model;
    this.target = target;
    String ontologyPath = Ontolex.class.getClassLoader().getResource("skos.rdf").getFile();
    RDFDataMgr.read(model, ontologyPath);
  }

  private void execute(String sparql) {
    UpdateRequest request = UpdateFactory.create(sparql);
    UpdateAction.execute(request, model);
    UpdateAction.execute(request, target);
  }

  public String createConcept(String uri) {
    String sparql = "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "INSERT DATA { " +
            "   <" + uri + "> rdf:type skos:Concept . " +
            "} ";

    execute(sparql);
    return uri;
  }

  public String createConcept() {
    return createConcept("http://purl.org/ZIN-Thor/Concept_" + UUID.randomUUID());
  }

  public void addPrefLabel(String uri, Literal label) {
    String sanitizedLabel = getSanitizedText(label);

    String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "INSERT DATA { " +
            "   <" + uri + "> skos:prefLabel " + sanitizedLabel +
            "} ";

    execute(sparql);
  }

  public void addAltLabel(String uri, Literal label) {
    String sanitizedLabel = getSanitizedText(label);

    String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "INSERT DATA { " +
            "   <" + uri + "> skos:altLabel " + sanitizedLabel + " . " +
            "} ";

    execute(sparql);
  }

  public List<Literal> getTextProperty(String propertyUri, String uri) {
    String queryString =
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
                    "SELECT ?literal " +
                    "WHERE { " +
                    "   <" + uri + "> " + propertyUri + " ?literal ." +
                    "} ";

    Query query = QueryFactory.create(queryString);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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

    String sparql = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
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
    String sparqlQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
            "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX lexinfo: <http://www.lexinfo.net/ontology/3.0/lexinfo#> " +
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

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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

  private void insertData(List<String> statements) {
    String sparqlInsert = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "INSERT DATA { " +
            String.join(" . ", statements) +
            "}";

    execute(sparqlInsert);
  }
}
