package it.unibz.core.thor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Runner {

  public static void main(String[] args) throws IOException {
    if (args.length == 0)
      throw new IllegalArgumentException("Please provide the path to the file containing ontolex data.");

    String sourceFilePath = args[0];
    Path sourceFile = Path.of(sourceFilePath);

    if (!Files.exists(sourceFile))
      throw new IllegalArgumentException("'" + sourceFile.toString() + "' does not exist!");

    if (!Files.isRegularFile(sourceFile))
      throw new IllegalArgumentException("'" + sourceFile.toString() + "' is not a file!");

    String targetDirectoryPath = (args.length >= 2) ? args[1] : "";
    Path targetDirectory = Path.of(targetDirectoryPath);
    Files.createDirectories(targetDirectory);

    generateThesaurus(sourceFile, targetDirectory);

  }

  private static void generateThesaurus(Path sourceFilePath, Path targetDirectoryPath) throws IOException {
    final String absoluteSourceFilePath = sourceFilePath.toAbsolutePath().toString();
    System.out.println("\nGenerating a thesaurus for '" + absoluteSourceFilePath + "'");

    ThorGenerator gen = new ThorGenerator(absoluteSourceFilePath);
    gen.run();

    KnowledgeGraph skosSubgraph = gen.getSkosSubgraph();
    skosSubgraph.writeToFile(targetDirectoryPath.resolve("thesaurus.ttl").toString());

    KnowledgeGraph ontolexSubgraph = gen.getOntolexSubgraph();
    ontolexSubgraph.writeToFile(targetDirectoryPath.resolve("lexicon.ttl").toString());

    KnowledgeGraph completeGraph = gen.getCompleteGraph();
    completeGraph.writeToFile(targetDirectoryPath.resolve("kg.ttl").toString());

  }

}
