package it.unibz.core.thor;

import org.apache.jena.ontology.OntModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static it.unibz.core.thor.Vocabulary.*;

public class Ontolex2Skos {
  private static final Logger logger = LogManager.getLogger(Ontolex2Skos.class);

  WorkingGraph graph;

  public Ontolex2Skos(Path sourceOntolexFile) throws IOException {
    Objects.requireNonNull(sourceOntolexFile);

    logger.info("Creating knowledge graph from '" + sourceOntolexFile + "'...");
    graph = new WorkingGraph(sourceOntolexFile);

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

    // TODO: Gerated skos:related from ontolex:relatedTerm

    logger.info("Deriving skos:broader and skos:narrower from lexinfo:hypernym and lexinfo:hyponym...");
    graph.deriveBroaderNarrower();

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

    logger.info("Extracting semantic relations...");
    thesaurus.copySemanticRelations();

    logger.info("Extracting context labels...");
    thesaurus.copyDomainLabels();

    return thesaurus;
  }

  public OntolexTargetGraph getOntolexSubgraph() {
    requireGraph();

    OntolexTargetGraph lexicon = new OntolexTargetGraph(graph);

    logger.info("Extracting lexicons...");
    lexicon.copyLexicons();

    logger.info("Extracting lexicons' properties...");
    lexicon.copyLexiconData();

    return lexicon;
  }

  public WorkingGraph getWorkingGraph() {
    requireGraph();

    return graph;
  }

  public static void generateAndSave(Path sourceFile, Path targetDirectory) throws IOException {
    logger.info("Generating a thesaurus for '" + sourceFile + "'");

    Ontolex2Skos gen = new Ontolex2Skos(sourceFile);
    gen.run();

    logger.info("Extracting thesaurus data...");
    KnowledgeGraph skosSubgraph = gen.getSkosSubgraph();

    final Path thesaurusFile = targetDirectory.resolve("thesaurus.ttl");
    logger.info("Writing thesauri to '" + thesaurusFile + "'");
    skosSubgraph.writeToFile(thesaurusFile);

    logger.info("Extracting lexicon data...");
    KnowledgeGraph ontolexSubgraph = gen.getOntolexSubgraph();

    final Path lexiconFile = targetDirectory.resolve("lexicon.ttl");
    logger.info("Writing lexica to '" + lexiconFile + "'");
    ontolexSubgraph.writeToFile(lexiconFile);

    KnowledgeGraph completeGraph = gen.getWorkingGraph();

    final Path kgFile = targetDirectory.resolve("kg.ttl");
    logger.info("Writing complete dataset to '" + kgFile + "'");
    completeGraph.writeToFile(kgFile);

    logger.info("Transformation successfully completed.");
  }

}
