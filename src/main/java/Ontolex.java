import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.util.ArrayList;
import java.util.List;

public class Ontolex {
  public static final String IRI = "http://www.w3.org/ns/lemon/ontolex#";
  public static final String PREFIX = "ontolex";

  public static final String LEXICAL_ENTRY_URI = IRI + "LexicalEntry";
  public static final String LEXICAL_SENSE_URI = IRI + "LexicalSense";
  public static final String LEXICAL_CONCEPT_URI = IRI + "LexicalConcept";
  public static final String FORM_URI = IRI + "Form";

  public static final String IS_SENSE_OF_URI = IRI + "isSenseOf";
  public static final String CANONICAL_FORM_URI = IRI + "canonicalForm";
  public static final String WRITTEN_REP_URI = IRI + "writtenRep";
  public static final String IS_LEXICALIZED_SENSE_OF_URI = IRI + "isLexicalizedSenseOf";

  private OntModel model;
  private OntModel target;

  public Ontolex(OntModel model, OntModel target) {
    this.model = model;
    this.target = target;

    String ontologyPath = Ontolex.class.getClassLoader().getResource("ontolex.ttl").getFile();
    RDFDataMgr.read(model, ontologyPath);
  }

  public void createLexicalSense(String uri) {
    String sparql = "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#> " +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "INSERT DATA { " +
            "   <" + uri + "> rdf:type ontolex:LexicalSense  " +
            "} ";

    execute(sparql);
  }

  public List<Resource> getLexicalSenses() {
    String queryString = "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#> " +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
            "SELECT ?sense WHERE { ?sense rdf:type ontolex:LexicalSense } ";

    Query query = QueryFactory.create(queryString);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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
    String queryString = "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#> " +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
            "SELECT ?label " +
            "WHERE { " +
            "   <" + senseUri + "> ontolex:isSenseOf ?entry ." +
            "   ?entry ontolex:canonicalForm ?form ." +
            "   ?form  ontolex:writtenRep ?label" +
            "} ";

    Query query = QueryFactory.create(queryString);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();
        return soln.getLiteral("label");
      }

      return null;
    }
  }


  public void setIsLexicalizedSenseOf(String senseUri, String conceptUri) {
    String sparql = "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#> " +
            "INSERT DATA { " +
            "   <" + senseUri + "> ontolex:isLexicalizedSenseOf <" + conceptUri + ">" +
            "} ";

    execute(sparql);
  }

  private void execute(String sparql) {
    UpdateRequest request = UpdateFactory.create(sparql);
    UpdateAction.execute(request, model);
    UpdateAction.execute(request, target);
  }


}
