package it.unibz.core.ontolex2skos;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Runner {
  private static final Logger logger = LogManager.getLogger(Runner.class);

  public static void main(String[] args) throws IOException {
    validateArgs(args);

    Path sourceFile = getSourceFile(args);
    Path targetDirectory = getTargetDirectory(args);

    logger.info("Generating a thesaurus for '" + sourceFile + "'");
    Ontolex2Skos transformer = new Ontolex2Skos(sourceFile);
    transformer.run();

    logger.info("Extracting thesaurus data...");
    KnowledgeGraph skosSubgraph = transformer.getSkosSubgraph();

    final Path thesaurusFile = targetDirectory.resolve("thesaurus.ttl");
    logger.info("Writing thesauri to '" + thesaurusFile + "'");
    skosSubgraph.writeToFile(thesaurusFile);

    logger.info("Extracting lexicon data...");
    KnowledgeGraph ontolexSubgraph = transformer.getGraphWithInferences();

    final Path lexiconFile = targetDirectory.resolve("ontolex+skos.ttl");
    logger.info("Writing lexica to '" + lexiconFile + "'");
    ontolexSubgraph.writeToFile(lexiconFile);

    KnowledgeGraph completeGraph = transformer.getWorkingGraph();

    final Path kgFile = targetDirectory.resolve("kg.ttl");
    logger.info("Writing complete dataset to '" + kgFile + "'");
    completeGraph.writeToFile(kgFile);

    logger.info("Transformation successfully completed.");
  }

  private static void validateArgs(String[] args) {
    if (areArgsEmpty(args))
      throw new IllegalArgumentException("Please provide the path to the file containing ontolex data.");
  }

  private static boolean areArgsEmpty(String[] args) {
    return args.length == 0;
  }

  private static Path getSourceFile(String[] args) {
    String sourceFilePath = args[0];
    Path sourceFile = Path.of(sourceFilePath);

    if (!Files.exists(sourceFile))
      throw new IllegalArgumentException("'" + sourceFile.toString() + "' does not exist!");

    if (!Files.isRegularFile(sourceFile))
      throw new IllegalArgumentException("'" + sourceFile.toString() + "' is not a file!");

    return sourceFile.toAbsolutePath();
  }

  private static Path getTargetDirectory(String[] args) throws IOException {
    String targetDirectoryPath = (args.length >= 2) ? args[1] : "";
    Path targetDirectory = Path.of(targetDirectoryPath);
    Files.createDirectories(targetDirectory);
    return targetDirectory;
  }

}
