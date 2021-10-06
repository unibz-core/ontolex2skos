import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Lexinfo {

  public final static String IRI = "http://www.lexinfo.net/ontology/3.0/lexinfo#";
  public final static String SYNONYM_URI = IRI + "synonym";

  private OntModel model;

  public Lexinfo(OntModel model) {
    this.model = model;

    String ontologyPath = Ontolex.class.getClassLoader().getResource("lexinfo-short.ttl").getFile();
    RDFDataMgr.read(model, ontologyPath);
  }

  private void execute(String sparql) {
    UpdateRequest request = UpdateFactory.create(sparql);
    UpdateAction.execute(request, model);
  }

  public void setAsSynonyms(String sense1Uri, String sense2Uri) {
    String sparql = "PREFIX lexinfo: <http://www.lexinfo.net/ontology/3.0/lexinfo#> " +
            "INSERT DATA { " +
            "   <" + sense1Uri + "> lexinfo:synonym <" + sense2Uri + "> . " +
            "} ";

    execute(sparql);
  }

  public boolean areSynonyms(String sense1Uri, String sense2Uri) {
    String queryString = "PREFIX lexinfo: <http://www.lexinfo.net/ontology/3.0/lexinfo#> " +
            "SELECT * " +
            "WHERE { <" + sense1Uri + "> lexinfo:synonym <" + sense2Uri + "> } ";

    Query query = QueryFactory.create(queryString);
    QueryExecution qexec = QueryExecutionFactory.create(query, model);
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
    String queryString = "PREFIX lexinfo: <http://www.lexinfo.net/ontology/3.0/lexinfo#> " +
            "SELECT ?synonym " +
            "WHERE { <" + senseUri + "> lexinfo:synonym ?synonym } ";

    Query query = QueryFactory.create(queryString);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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
}
