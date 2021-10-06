import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkosGeneratorTest {

//  static OntModel model;
//  static Ontolex ontolex;
//  static Lexinfo lexinfo;
//  static SkosGenerator skosGenerator;
//
//  @BeforeAll
//  static void setUp() {
//    model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
//    skosGenerator = new SkosGenerator(model);
//    ontolex = skosGenerator.ontolex;
//    lexinfo = skosGenerator.lexinfo;
//  }
//
//  @Test
//  @DisplayName("two synsets are the same if they contain the same senses")
//  void qualitativeEquality() {
//    Individual sense1 = ontolex.createLexicalSenseInstance("lexicalSense#Message1");
//    Individual sense2 = ontolex.createLexicalSenseInstance("lexicalSense#Message2");
//
//    Synset ss1 = new Synset(sense1, sense2);
//    Synset ss2 = new Synset(sense1, sense2);
//
//    assertThat(ss1).isEqualTo(ss2);
//  }
//
//  @Test
//  @DisplayName("synset equality should work regardless of the order in which senses are added")
//  void qualitativeEqualityOrderless() {
//    Individual sense1 = ontolex.createLexicalSenseInstance("lexicalSense#Message1");
//    Individual sense2 = ontolex.createLexicalSenseInstance("lexicalSense#Message2");
//
//    Synset ss1 = new Synset(sense1, sense2);
//    Synset ss2 = new Synset(sense2, sense1);
//
//    assertThat(ss1).isEqualTo(ss2);
//  }
//
//  @Test
//  @DisplayName("two synsets should be different if they do not contain the exact same senses")
//  void synsetShouldBeDifferent() {
//    Individual sense1 = ontolex.createLexicalSenseInstance("lexicalSense#Message1");
//    Individual sense2 = ontolex.createLexicalSenseInstance("lexicalSense#Message2");
//
//    Synset ss1 = new Synset(sense1);
//    Synset ss2 = new Synset(sense2);
//
//    assertThat(ss1).isNotEqualTo(ss2);
//  }
//
//  @Test
//  @DisplayName("if a sense has no synonym, it should compose a synset on its own")
//  void noSynonyms() {
//    Individual sense = ontolex.createLexicalSenseInstance("lexicalSense#Client");
//
//    List<Synset> synsets = skosGenerator.getSynsets();
//    Synset synset = new Synset(sense);
//
//    assertThat(synsets).contains(synset);
//  }
//
//  @Test
//  @DisplayName("if two senses are synonyms, they should compose a synset")
//  void shouldFindSynsetTwoSynonyms() {
//    Individual sense1 = ontolex.createLexicalSenseInstance("lexicalSense#Message1");
//    Individual sense2 = ontolex.createLexicalSenseInstance("lexicalSense#Message2");
//    lexinfo.setAsSynonyms(sense1, sense2);
//
//    List<Synset> synsets = skosGenerator.getSynsets();
//    Synset synset = new Synset(sense1, sense2);
//
//    assertThat(synsets).contains(synset);
//  }
//
//  @Test
//  @DisplayName("if three senses are synonyms, they should compose a synset")
//  void shouldFindSynsetThreeSynonyms() {
//    Individual sense1 = ontolex.createLexicalSenseInstance("lexicalSense#Patient1");
//    Individual sense2 = ontolex.createLexicalSenseInstance("lexicalSense#Patient2");
//    Individual sense3 = ontolex.createLexicalSenseInstance("lexicalSense#Patient3");
//
//    lexinfo.setAsSynonyms(sense1, sense2);
//    lexinfo.setAsSynonyms(sense1, sense3);
//    lexinfo.setAsSynonyms(sense2, sense3);
//
//    List<Synset> synSets = skosGenerator.getSynsets();
//    Synset synset = new Synset(sense1, sense2, sense3);
//
//    assertThat(synSets).contains(synset);
//  }

}
