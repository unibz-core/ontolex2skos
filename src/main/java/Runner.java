import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.util.Arrays;
import java.util.List;

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
//            .getResource("test.ttl")
            .getResource("thor.ttl")
            .toString();

    System.out.println("Loading the thor graph...");
    RDFDataMgr.read(thorGraph, path);

//    String queryString = "PREFIX ontolex: <http://www.w3.org/ns/lemon/ontolex#> " +
//            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
//            "SELECT DISTINCT ?sense (GROUP_CONCAT(?entity; separator=\",\") AS ?references) " +
//            "WHERE { " +
//            "   ?sense rdf:type ontolex:LexicalSense . " +
//            "   ?sense ontolex:reference ?entity " +
//            "} " +
//            "GROUP BY ?sense";
//
//    Query query = QueryFactory.create(queryString);
//
//    try (QueryExecution qexec = QueryExecutionFactory.create(query, thorGraph)) {
//      ResultSet results = qexec.execSelect();
//
//      while (results.hasNext()) {
//        QuerySolution soln = results.nextSolution();
//        String sense = soln.getResource("sense").getURI();
//        String[] references = soln.getLiteral("references").getString().split(",");
//        Arrays.asList(references);
//
//        System.out.println(sense);
//        System.out.println(Arrays.toString(references));
//      }
//    }

    SkosGenerator gen = new SkosGenerator(thorGraph, generated);
    gen.generateSkosData();
//
//    RDFDataMgr.write(System.out, thorGraph, Lang.TURTLE);
    RDFDataMgr.write(System.out, generated, Lang.TURTLE);
  }

  // some definitions
  //    String thorPrefix = "http://somewhere/";
  //    String personURI    = thorPrefix + "JohnSmith";
  //    String givenName    = "John";
  //    String familyName   = "Smith";
  //    String fullName     = "John Smith";
  //
  //    // create an empty Model
  //    Model model = ModelFactory.createDefaultModel();
  //
  //    // create the resource
  //    Resource johnSmith = model.createResource(personURI);
  //
  //    // add the property
  //    johnSmith.addProperty(VCARD.FN, fullName);
  //
  //    // create the resource
  //    //   and add the properties cascading style
  //    johnSmith.addProperty(VCARD.N,
  //                    model.createResource()
  //                            .addProperty(VCARD.Given, givenName)
  //                            .addProperty(VCARD.Family, familyName));
  //
  //    model.setNsPrefix("vcard", VCARD.getURI());
  //    model.setNsPrefix("thor", thorPrefix);

  //    RDFDataMgr.write(System.out, model, Lang.TURTLE);

  //    OntClass person = thorGraph.createClass(NAMESPACE + "Person");
  //    person.createIndividual(NAMESPACE + "Tiago");
  //
  //    person.listInstances().forEach(System.out::println);
  //
  //


//    System.out.println(path);

  //    Model dataset = RDFDataMgr.loadModel(path) ;
  //    RDFDataMgr.write(System.out, dataset, Lang.TURTLE);

//    String ONTOLEX = "http://www.w3.org/ns/lemon/ontolex#";
//
//    //    Resource lexicalEntry = dataset.getResource(ONTOLEX+"LexicalEntry");
//
//    OntModel thorGraph = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
//    //    thorGraph.read(path);
//
//    RDFDataMgr.read(thorGraph, path);
//    RDFDataMgr.read(thorGraph, "http://www.w3.org/ns/lemon/ontolex");
//
//    RDFDataMgr.write(System.out, thorGraph, Lang.TURTLE);
//
//    //    RDFDataMgr.write(System.out, thorGraph, Lang.TURTLE);
//
//    //    OntClass lexicalEntry = thorGraph.getOntClass(ONTOLEX + "LexicalEntry");
//    //    lexicalEntry.listInstances().forEach(System.out::println);
//    OntProperty isSenseOf = thorGraph.getOntProperty(ONTOLEX + "isSenseOf");
//    OntProperty canonicalForm = thorGraph.getOntProperty(ONTOLEX + "canonicalForm");
//    OntProperty writtenRep = thorGraph.getOntProperty(ONTOLEX + "writtenRep");
//
//    OntClass lexicalSense = thorGraph.getOntClass(ONTOLEX + "LexicalSense");
//    lexicalSense
//            .listInstances()
//            .forEach(
//                    sense -> {
//                      System.out.println("\n====\n");
//                      System.out.println(sense);
//                      Resource entry = sense.getPropertyResourceValue(isSenseOf);
//
//                      if (entry == null) return;
//
//                      System.out.println(entry);
//                      Resource form = entry.getPropertyResourceValue(canonicalForm);
//
//                      if (form == null) return;
//
//                      System.out.println(form);
//                      Statement text = form.getProperty(writtenRep);
//
//                      if (text == null) return;
//
//                      System.out.println("Text: " + text.getString() + " " + text.getLanguage());
//                    });

  //    System.out.println(lexicalEntry);

  //    For each synset, create one skos:Concept
  //    Copy all the properties of the lexical senses to the concepts
  //    -skos:editorialNote
  //    -skos:note
  //    Create pref labels (for synonyms, pick randomly for now) =>
  // LexicalSense.isSenseOf.canonicalForm.writtenRep
  //    Create alt labels
}
