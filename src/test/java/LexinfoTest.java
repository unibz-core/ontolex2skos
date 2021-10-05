import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LexinfoTest {

  static OntModel ontology;
  static Ontolex ontolex;
  static Lexinfo lexinfo;

  @BeforeAll
  static void setUp() {
    ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    ontolex = new Ontolex(ontology);
    lexinfo = new Lexinfo(ontology);
  }

  @Test
  void shouldReturnNoSynonym() {
    Individual sense = ontolex.createLexicalSenseInstance("clientSense1");
    List<Individual> synonyms = lexinfo.getSynonyms(sense);
    assertThat(synonyms).isEmpty();
  }

  @Test
  void shouldReturnOneSynonym() {
    Individual sense1 = ontolex.createLexicalSenseInstance("messageSense1");
    Individual sense2 = ontolex.createLexicalSenseInstance("messageSense2");
    lexinfo.setAsSynonyms(sense1, sense2);

    List<Individual> synonyms1 = lexinfo.getSynonyms(sense1);
    assertThat(synonyms1).containsExactly(sense2);

    List<Individual> synonyms2 = lexinfo.getSynonyms(sense2);
    assertThat(synonyms2).containsExactly(sense1);
  }

  @Test
  void shouldReturnTwoSynonyms() {
    Individual sense1 = ontolex.createLexicalSenseInstance("batSense1");
    Individual sense2 = ontolex.createLexicalSenseInstance("batSense2");
    Individual sense3 = ontolex.createLexicalSenseInstance("batSense3");
    lexinfo.setAsSynonyms(sense1, sense2);
    lexinfo.setAsSynonyms(sense1, sense3);

    List<Individual> synonyms1 = lexinfo.getSynonyms(sense1);
    assertThat(synonyms1).containsExactlyInAnyOrder(sense2, sense3);
  }
}