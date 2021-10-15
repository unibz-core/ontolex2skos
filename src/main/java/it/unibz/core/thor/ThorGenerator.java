package it.unibz.core.thor;

import java.util.Objects;

import static it.unibz.core.thor.Vocabulary.*;

public class ThorGenerator {

  String sourceDataFilename;
  WorkingGraph graph;

  public ThorGenerator(String sourceDataFilename) {
    this.sourceDataFilename = sourceDataFilename;
  }

  public void run() {
    System.out.println("Creating knowledge graph...");
    graph = new WorkingGraph();

    System.out.println("Loading lexical data from '" + sourceDataFilename + "'...");
    graph.readFromFile(sourceDataFilename);

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
    if(graph==null)
      throw new IllegalStateException("Cannot get Skos subgraph before running the generator.");
  }

  public KnowledgeGraph getSkosSubgraph() {
    requireGraph();

    System.out.println("Extracting subgraph with SKOS data...");
    SkosTargetGraph thesaurus = new SkosTargetGraph(graph);

    System.out.println("Loading custom properties...");
    thesaurus.loadCustomProperties();

    System.out.println("Saving schemes...");
    thesaurus.copySchemes();

    System.out.println("Saving schemes' properties...");
    thesaurus.copySchemeData();

    System.out.println("Saving concepts...");
    thesaurus.copyConcepts();

    System.out.println("Saving concepts' properties...");
    thesaurus.copyConceptData();

    System.out.println("Saving semantic relations...");
    thesaurus.copySemanticRelations();

    System.out.println("Saving context labels...");
    thesaurus.copyDomainLabels();

    return thesaurus;
  }

  public KnowledgeGraph getOntolexSubgraph() {
    requireGraph();

    OntolexTargetGraph lexicon = new OntolexTargetGraph(graph);

    System.out.println("Saving lexicons...");
    lexicon.copyLexicons();

    System.out.println("Saving lexicons' properties...");
    lexicon.copyLexiconData();

    return lexicon;
  }

  public KnowledgeGraph getCompleteGraph() {
    requireGraph();

    return graph;
  }

}
