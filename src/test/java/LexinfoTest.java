import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LexinfoTest {
  static final String PREFIX = "http://example.com/";
  static OntModel ontology;
  static OntModel target;
  static Ontolex ontolex;
  static Lexinfo lexinfo;

  @BeforeAll
  static void setUp() {
    ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
    target = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
    ontolex = new Ontolex(ontology, target);
    lexinfo = new Lexinfo(ontology);
  }

  @Test
  void shouldReturnNoSynonym() {
    final String senseUri = PREFIX + "clientSense1";
    ontolex.createLexicalSense(senseUri);
    List<String> synonyms = lexinfo.getSynonymUris(senseUri);
    assertThat(synonyms).isEmpty();
  }

  @Test
  void shouldReturnOneSynonym() {
    ontolex.createLexicalSense(PREFIX + "messageSense1");
    ontolex.createLexicalSense(PREFIX + "messageSense2");
    lexinfo.setAsSynonyms(PREFIX + "messageSense1", PREFIX + "messageSense2");

    List<String> synonyms1 = lexinfo.getSynonymUris(PREFIX + "messageSense1");
    assertThat(synonyms1).containsExactly(PREFIX + "messageSense2");

    List<String> synonyms2 = lexinfo.getSynonymUris(PREFIX + "messageSense2");
    assertThat(synonyms2).containsExactly(PREFIX + "messageSense1");
  }

  @Test
  void shouldReturnTwoSynonyms() {
    ontolex.createLexicalSense(PREFIX + "batSense1");
    ontolex.createLexicalSense(PREFIX + "batSense2");
    ontolex.createLexicalSense(PREFIX + "batSense3");
    lexinfo.setAsSynonyms(PREFIX + "batSense1", PREFIX + "batSense2");
    lexinfo.setAsSynonyms(PREFIX + "batSense1", PREFIX + "batSense3");

    List<String> synonyms1 = lexinfo.getSynonymUris(PREFIX + "batSense1");
    assertThat(synonyms1).containsExactlyInAnyOrder(PREFIX + "batSense2", PREFIX + "batSense3");
  }
}