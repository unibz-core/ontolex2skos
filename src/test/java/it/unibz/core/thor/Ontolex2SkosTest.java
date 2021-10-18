package it.unibz.core.thor;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static it.unibz.core.thor.Vocabulary.getPrefixDeclarations;
import static org.assertj.core.api.Assertions.assertThat;

class Ontolex2SkosTest {

  private TestGraph testGraph;

  @BeforeEach
  void setUp() {
    testGraph = new TestGraph();
  }

  @Test
  void senseShouldGenerateConcept() {
    testGraph.createLexicalSense("http://example.com/sense1");
    testGraph.transform();

    String sparql = getPrefixDeclarations() +
            "ASK " +
            "WHERE {" +
            "   ?x rdf:type skos:Concept . " +
            "   ?x ontolex:lexicalizedSense <http://example.com/sense1>" +
            "}";

    boolean exists = testGraph.askWorkingGraph(sparql);
    assertThat(exists).isTrue();
  }


  //  @BeforeAll
//  static void setUp() {
//    ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
//    target = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
//    ontolex = new Ontolex(ontology, target);
//    lexinfo = new Lexinfo(ontology);
//  }
//
//  @Test
//  void shouldReturnNoSynonym() {
//    final String senseUri = PREFIX + "clientSense1";
//    ontolex.createLexicalSense(senseUri);
//    List<String> synonyms = lexinfo.getSynonymUris(senseUri);
//    assertThat(synonyms).isEmpty();
//  }
//
//  @Test
//  void shouldReturnOneSynonym() {
//    ontolex.createLexicalSense(PREFIX + "messageSense1");
//    ontolex.createLexicalSense(PREFIX + "messageSense2");
//    lexinfo.setAsSynonyms(PREFIX + "messageSense1", PREFIX + "messageSense2");
//
//    List<String> synonyms1 = lexinfo.getSynonymUris(PREFIX + "messageSense1");
//    assertThat(synonyms1).containsExactly(PREFIX + "messageSense2");
//
//    List<String> synonyms2 = lexinfo.getSynonymUris(PREFIX + "messageSense2");
//    assertThat(synonyms2).containsExactly(PREFIX + "messageSense1");
//  }
//
//  @Test
//  void shouldReturnTwoSynonyms() {
//    ontolex.createLexicalSense(PREFIX + "batSense1");
//    ontolex.createLexicalSense(PREFIX + "batSense2");
//    ontolex.createLexicalSense(PREFIX + "batSense3");
//    lexinfo.setAsSynonyms(PREFIX + "batSense1", PREFIX + "batSense2");
//    lexinfo.setAsSynonyms(PREFIX + "batSense1", PREFIX + "batSense3");
//
//    List<String> synonyms1 = lexinfo.getSynonymUris(PREFIX + "batSense1");
//    assertThat(synonyms1).containsExactlyInAnyOrder(PREFIX + "batSense2", PREFIX + "batSense3");
//  }
}