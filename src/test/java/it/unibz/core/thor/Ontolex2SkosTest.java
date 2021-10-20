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
  void senseShouldGenerateConceptFromSense() {
    testGraph.createPreferredSense("http://e.com/sense1");
    testGraph.transform();

    String sparql = getPrefixDeclarations() +
            "ASK " +
            "WHERE {" +
            "   ?x rdf:type skos:Concept . " +
            "   ?x ontolex:lexicalizedSense <http://e.com/sense1>" +
            "}";

    boolean exists = testGraph.askWorkingGraph(sparql);
    assertThat(exists).isTrue();
  }

  @Test
  void senseShouldGenerateOneConceptFromSynonyms() {
    testGraph.createPreferredSense("http://e.com/sense1");
    testGraph.createLexicalSense("http://e.com/sense2");
    testGraph.insert("http://e.com/sense1", "lexinfo:synonym", "http://e.com/sense2");
    testGraph.transform();

    String sparql = getPrefixDeclarations() +
            "ASK " +
            "WHERE {" +
            "   ?x rdf:type skos:Concept . " +
            "   ?x ontolex:lexicalizedSense <http://e.com/sense1> ." +
            "   ?x ontolex:lexicalizedSense <http://e.com/sense2> " +
            "}";

    boolean exists = testGraph.askWorkingGraph(sparql);
    assertThat(exists).isTrue();
  }

}