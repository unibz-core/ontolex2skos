import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Runner {

  public static void main(String[] args) {

    OntModel thorGraph = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
    OntModel generated = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

    final String NAMESPACE = "http://purl.org/ZIN-Thor/";
    final String PREFIX = "thor";
    thorGraph.setNsPrefix(PREFIX, NAMESPACE);

    generated.setNsPrefix(PREFIX, NAMESPACE);
    generated.setNsPrefix("ontolex", "http://www.w3.org/ns/lemon/ontolex#");
    generated.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
    generated.setNsPrefix("vph-g", "http://purl.org/ozo/vph-g#");

    final String path = SkosGenerator.class
            .getClassLoader()
            .getResource("thor no concepts.ttl")
            .toString();

    System.out.println("Loading the thor graph...");
    RDFDataMgr.read(thorGraph, path);

    SkosGenerator gen = new SkosGenerator(thorGraph, generated);
    gen.generateSkosData();

//    System.out.println("Writing output to 'thor-all.ttl'");
//    try(OutputStream out = new FileOutputStream("thor-all.ttl")) {
//      RDFDataMgr.write(out, thorGraph, Lang.TURTLE);
//    } catch (FileNotFoundException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }

    System.out.println("Writing output to 'thor-generated-thesaurus.ttl'");
    try(OutputStream out = new FileOutputStream("thor-generated-thesaurus.ttl")) {
      RDFDataMgr.write(out, generated, Lang.TURTLE);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
            "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX lexinfo: <http://www.lexinfo.net/ontology/3.0/lexinfo#> " +
            "SELECT ?concept ?label " +
            "WHERE { " +
            "   ?concept ontolex:lexicalizedSense ?sense . " +
            "   ?sense ontolex:isSenseOf ?entry . " +
            "   ?entry lexinfo:fullFormFor ?acronym . " +
            "   ?acronym ontolex:canonicalForm ?form . " +
            "   ?form ontolex:writtenRep ?label . " +
            "} ";

    Query query = QueryFactory.create(queryString);

    try (QueryExecution qexec = QueryExecutionFactory.create(query, thorGraph)) {
      ResultSet results = qexec.execSelect();

      while (results.hasNext()) {
        QuerySolution soln = results.nextSolution();

        System.out.println(soln);
        System.out.println();
//        System.out.println(soln.getResource("narrower"));
//        System.out.println(soln.getLiteral("label1"));
//        System.out.println(soln.getResource("broader"));
//        System.out.println(soln.getLiteral("label2"));
//        System.out.println("\n\n");
      }
    }

//
//    RDFDataMgr.write(System.out, thorGraph, Lang.TURTLE);
  }

}
