package it.unibz.core.thor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static it.unibz.core.thor.Vocabulary.*;

public class Ontolex2Skos {

  Path sourceOntolexFile;
  WorkingGraph graph;

  public Ontolex2Skos(Path sourceOntolexFile) {
    Objects.requireNonNull(sourceOntolexFile);
    this.sourceOntolexFile = sourceOntolexFile;
  }

  public void run() throws IOException {
    System.out.println("Creating knowledge graph...");
    graph = new WorkingGraph();

    System.out.println("Loading lexical data from '" + sourceOntolexFile + "'...");
    graph.readFromFile(sourceOntolexFile);

    System.out.println("Loading ontolex module..");
    graph.loadModule(ONTOLEX);

    System.out.println("Loading lexinfo module...");
    graph.loadModule(LEXINFO);

    System.out.println("Loading skos module..");
    graph.loadModule(SKOS);

    System.out.println("Deriving skos:Scheme from lime:Lexicon...");
    graph.deriveScheme();

    System.out.println("Deriving dct:title for concept schemes...");
    graph.deriveSchemeLabel();

    System.out.println("Deriving concepts from synsets...");
    graph.deriveConcepts();

    System.out.println("Deriving skos:inScheme from lime:entry...");
    graph.deriveInScheme();

    System.out.println("Deriving skos:prefLabel from preferred senses...");
    graph.derivePreferredLabels();

    System.out.println("Deriving skos:altLabel from non-preferred senses...");
    graph.deriveAlternativeLabels();

    System.out.println("Copying properties from lexical senses...");
    graph.copyPropertiesFromSenses();

    System.out.println("Deriving skos:altLabel from acronyms...");
    graph.deriveAlternativeLabelsFromAcronyms();

    System.out.println("Deriving ontolex:isConceptof from ontolex:reference...");
    graph.deriveIsConceptOf();

    // TODO: Gerated skos:related from ontolex:relatedTerm

    System.out.println("Deriving skos:broader and skos:narrower from lexinfo:hypernym and lexinfo:hyponym...");
    graph.deriveBroaderNarrower();

    System.out.println("Deriving skos:topConceptOf...");
    graph.deriveTopConceptOf();

    System.out.println("Deriving thor:hasContext from lexinfo:domain...");
    graph.deriveHasContext();

    System.out.println("Deriving skos:scopeNote from ontolex:usage...");
    graph.deriveScopeNote();

    System.out.println("Deriving ontolex:isEvokedBy...");
    graph.deriveIsEvokedBy();

    System.out.println("Attempting to resolve name conflicts...");
    graph.resolveConflictsOnPrefLabels();
  }

  private void requireGraph() {
    if (graph == null)
      throw new IllegalStateException("Cannot get Skos subgraph before running the generator.");
  }

  public KnowledgeGraph getSkosSubgraph() {
    requireGraph();

    SkosTargetGraph thesaurus = new SkosTargetGraph(graph);

    System.out.println("Extracting schemes...");
    thesaurus.copySchemes();

    System.out.println("Extracting schemes' properties...");
    thesaurus.copySchemeData();

    System.out.println("Extracting concepts...");
    thesaurus.copyConcepts();

    System.out.println("Extracting concepts' properties...");
    thesaurus.copyConceptData();

    System.out.println("Extracting semantic relations...");
    thesaurus.copySemanticRelations();

    System.out.println("Extracting context labels...");
    thesaurus.copyDomainLabels();

    return thesaurus;
  }

  public KnowledgeGraph getOntolexSubgraph() {
    requireGraph();

    OntolexTargetGraph lexicon = new OntolexTargetGraph(graph);

    System.out.println("Extracting lexicons...");
    lexicon.copyLexicons();

    System.out.println("Extracting lexicons' properties...");
    lexicon.copyLexiconData();

    return lexicon;
  }

  public KnowledgeGraph getCompleteGraph() {
    requireGraph();

    return graph;
  }

  public static void generateAndSave(Path sourceFile, Path targetDirectory) throws IOException {
    System.out.println("\nGenerating a thesaurus for '" + sourceFile + "'");

    Ontolex2Skos gen = new Ontolex2Skos(sourceFile);
    gen.run();

    System.out.println("Extracting thesaurus data...");
    KnowledgeGraph skosSubgraph = gen.getSkosSubgraph();

    final Path thesaurusFile = targetDirectory.resolve("thesaurus.ttl");
    System.out.println("Writing thesauri to '" + thesaurusFile + "'");
    skosSubgraph.writeToFile(thesaurusFile);

    System.out.println("Extracting lexicon data...");
    KnowledgeGraph ontolexSubgraph = gen.getOntolexSubgraph();

    final Path lexiconFile = targetDirectory.resolve("lexicon.ttl");
    System.out.println("Writing lexica to '" + lexiconFile + "'");
    ontolexSubgraph.writeToFile(lexiconFile);

    KnowledgeGraph completeGraph = gen.getCompleteGraph();

    final Path kgFile = targetDirectory.resolve("kg.ttl");
    System.out.println("Writing complete dataset to '" + kgFile + "'");
    completeGraph.writeToFile(kgFile);

  }

}
