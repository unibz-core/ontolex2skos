package it.unibz.core.thor;

public class Runner {

  public static void main(String[] args) {
//    generateThesaurus("test-data/data-evaluation.ttl");
    generateThesaurus("src/main/resources/test-data/thor.ttl");
  }

  private static void generateThesaurus(String filename) {
    System.out.println("\nGenerating a thesaurus for '" + filename + "'");

    ThorGenerator gen = new ThorGenerator(filename);
    gen.run();

    KnowledgeGraph skosSubgraph = gen.getSkosSubgraph();
    skosSubgraph.writeToFile("thesaurus.ttl");

    KnowledgeGraph ontolexSubgraph = gen.getOntolexSubgraph();
    ontolexSubgraph.writeToFile("lexicon.ttl");

    KnowledgeGraph completeGraph = gen.getCompleteGraph();
    completeGraph.writeToFile("kg.ttl");

  }

}
