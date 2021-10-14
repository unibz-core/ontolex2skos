package it.unibz.core.thor;

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

    // TODO: Create one thesaurus per lexicon
    System.out.println("Deriving skos:Scheme from lime:Lexicon...");
    graph.deriveScheme();

    // TODO: Derive concept title
    System.out.println("Deriving dct:title for concept schemes...");
    graph.deriveSchemeLabel();

    System.out.println("Deriving concepts from synsets...");
    graph.deriveConcepts();

    // TODO: Derive skos:inScheme for every concept
    System.out.println("Deriving skos:inScheme from lime:entry...");
    graph.deriveInScheme();

    // TODO: Fix preferential label generation
    System.out.println("Deriving skos:prefLabel from preferred senses...");
    graph.derivePreferredLabels();

    System.out.println("Deriving skos:altLabel from non-preferred senses...");
    graph.deriveAlternativeLabels();

    System.out.println("Deriving SKOS data properties from lexical senses...");
    graph.deriveSkosDataProperties();

    System.out.println("Deriving skos:altLabel from acronyms...");
    graph.deriveAlternativeLabelsFromAcronyms();

    System.out.println("Deriving ontolex:isConceptof from ontolex:reference...");
    graph.deriveIsConceptOf();

    System.out.println("Deriving skos:broader and skos:narrower from lexinfo:hypernym and lexinfo:hyponym...");
    graph.deriveBroaderNarrower();

    // TODO: Derive skos:topConceptOf (concept that has no broader)
    System.out.println("Deriving skos:topConceptOf...");
    graph.deriveTopConceptOf();

    // TODO: Derive thor:hasContext from lexinfo:domain
    System.out.println("Deriving thor:hasContext from lexinfo:domain...");
    graph.deriveHasContext();

    // TODO: Derive skos:scopeNote from ontolex:usage
    System.out.println("Deriving skos:scopeNote from ontolex:usage...");
    graph.deriveScopeNote();

    System.out.println("Deriving ontolex:isEvokedBy...");
    graph.deriveIsEvokedBy();

    // TODO: Add domains as qualifiers when necessary
    System.out.println("Attempting to resolve name conflicts...");
    graph.resolveConflictsOnPrefLabels();
  }

  public KnowledgeGraph getSkosSubgraph() {
    System.out.println("Extracting subgraph with SKOS data...");
    SkosTargetGraph thesaurus = new SkosTargetGraph(graph);

    System.out.println("Loading custom properties...");
    thesaurus.loadCustomProperties();

//    System.out.println("Loading external mappings...");
//    targetGraph.loadExternalMappings();

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
    OntolexTargetGraph lexicon = new OntolexTargetGraph(graph);

    System.out.println("Saving lexicons...");
    lexicon.copyLexicons();

    System.out.println("Saving lexicons' properties...");
    lexicon.copyLexiconData();


    return lexicon;
  }

  public KnowledgeGraph getCompleteGraph() {
    return graph;
  }

}
