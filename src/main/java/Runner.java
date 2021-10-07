import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Runner {

  public static void main(String[] args) {
    generateThesaurus("data-evaluation.ttl");
    generateThesaurus("thor no concepts.ttl");
  }

  private static void generateThesaurus(String filename) {
    System.out.println("\nGenerating a thesaurus for '" + filename + "'");
    OntModel sourceGraph = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
    OntModel generatedGraph = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

    final String path = SkosGenerator.class
            .getClassLoader()
            .getResource(filename)
            .toString();

    System.out.println("Loading the triples from '" + filename + "'...");
    RDFDataMgr.read(sourceGraph, path);

    SkosGenerator gen = new SkosGenerator(sourceGraph, generatedGraph);
    gen.generateSkosData();

//    writeTurtleFile("complete-graph-" + filename, sourceGraph);
    writeTurtleFile("generated-thesaurus-" + filename, generatedGraph);

//    String sparql = Domains.SPARQL_PREFIXES +
//            "SELECT ?concept ?lexicalEntry ?lexicon " +
//            "WHERE { " +
//            "   ?concept ontolex:isEvokedBy ?lexicalEntry . " +
//            "   ?lexicon lime:entry ?lexicalEntry . " +
//            "}";
//
//    System.out.println(sparql);
//
//    Query query = QueryFactory.create(sparql);
//
//    try (QueryExecution qexec = QueryExecutionFactory.create(query, sourceGraph)) {
//
//      ResultSet results = qexec.execSelect();
//      while (results.hasNext()) {
//        QuerySolution solution = results.nextSolution();
//        System.out.println(solution);
////        String conceptUri = solution.getResource("concept").getURI();
////        String lexicalEntryUri = solution.getResource("lexicalEntry").getURI();
////        String lexiconUri = solution.getResource("lexicon").getURI();
//      }

//    }

  }

  private static void writeTurtleFile(String filename, OntModel model) {
    System.out.println("Writing output to '" + filename + "'");
    try (OutputStream out = new FileOutputStream(filename)) {
      RDFDataMgr.write(out, model, Lang.TURTLE);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
