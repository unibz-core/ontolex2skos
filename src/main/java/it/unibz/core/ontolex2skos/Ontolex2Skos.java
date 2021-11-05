package it.unibz.core.ontolex2skos;

import org.apache.jena.ontology.OntModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static it.unibz.core.ontolex2skos.Vocabulary.*;

public class Ontolex2Skos {
  private static final Logger logger = LogManager.getLogger(Ontolex2Skos.class);

  WorkingGraph graph;

  public Ontolex2Skos(Path sourceFile) throws IOException {
    Objects.requireNonNull(sourceFile);

    logger.info("Creating knowledge graph from '" + sourceFile + "'...");
    graph = new WorkingGraph(sourceFile);

    loadRequiredVocabularies();
  }

  public Ontolex2Skos(OntModel sourceOntolexModel) {
    graph = new WorkingGraph(sourceOntolexModel);

    loadRequiredVocabularies();
  }

  public void run() {
    logger.info("Deriving skos:Scheme from lime:Lexicon...");
    graph.deriveScheme();

    logger.info("Deriving dct:title for concept schemes...");
    graph.deriveSchemeLabel();

    logger.info("Deriving concepts from synsets...");
    graph.deriveConcepts();

    logger.info("Deriving skos:inScheme from lime:entry...");
    graph.deriveInScheme();

    logger.info("Deriving skos:prefLabel from preferred senses...");
    graph.derivePreferredLabels();

    logger.info("Deriving skos:altLabel from non-preferred senses...");
    graph.deriveAlternativeLabels();

    logger.info("Copying properties from lexical senses...");
    graph.copyPropertiesFromSenses();

    logger.info("Deriving skos:altLabel from acronyms...");
    graph.deriveAlternativeLabelsFromAcronyms();

    logger.info("Deriving skos:broader and skos:narrower from lexinfo:hypernym and lexinfo:hyponym...");
    graph.deriveBroaderNarrower();

    logger.info("Deriving mapping properties...");
    graph.deriveThorMappingProperties();

    logger.info("Deriving skos:topConceptOf...");
    graph.deriveTopConceptOf();

    logger.info("Deriving thor:hasContext from lexinfo:domain...");
    graph.deriveHasContext();

    logger.info("Deriving skos:scopeNote from ontolex:usage...");
    graph.deriveScopeNote();

    logger.info("Deriving ontolex:isEvokedBy...");
    graph.deriveIsEvokedBy();

    logger.info("Deriving ontolex:isConceptOf from ontolex:reference...");
    graph.deriveIsConceptOf();

    logger.info("Attempting to resolve name conflicts...");
    graph.resolveConflictsOnPrefLabels();
  }

  private void loadRequiredVocabularies() {
    logger.info("Loading ontolex module..");
    graph.loadModule(ONTOLEX);

    logger.info("Loading lexinfo module...");
    graph.loadModule(LEXINFO);

    logger.info("Loading skos module..");
    graph.loadModule(SKOS);
  }

  private void requireGraph() {
    if (graph == null)
      throw new IllegalStateException("Cannot get Skos subgraph before running the generator.");
  }

  public SkosTargetGraph getSkosSubgraph() {
    requireGraph();

    SkosTargetGraph thesaurus = new SkosTargetGraph(graph);

    logger.info("Extracting schemes...");
    thesaurus.copySchemes();

    logger.info("Extracting schemes' properties...");
    thesaurus.copySchemeData();

    logger.info("Extracting concepts...");
    thesaurus.copyConcepts();

    logger.info("Extracting concepts' properties...");
    thesaurus.copyConceptData();

    logger.info("Extracting reified property values...");
    thesaurus.copyReifiedPropertyValues();

    logger.info("Extracting semantic properties...");
    thesaurus.copySemanticProperties();

    logger.info("Extracting mapping properties...");
    thesaurus.copyMappingProperties();

    logger.info("Extracting context labels...");
    thesaurus.copyDomainLabels();

    return thesaurus;
  }

  public ExtendedTargetGraph getGraphWithInferences() {
    requireGraph();

    ExtendedTargetGraph graph = new ExtendedTargetGraph(this.graph);

    logger.info("Extracting type assertions...");
    graph.extractTypeAssertions();

    logger.info("Extracting property assertions...");
    graph.extractPropertyAssertions();

    return graph;
  }

  public WorkingGraph getWorkingGraph() {
    requireGraph();

    return graph;
  }

}
