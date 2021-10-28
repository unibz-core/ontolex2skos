package it.unibz.core.ontolex2skos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Runner {

  public static void main(String[] args) throws IOException {
    if (args.length == 0)
      throw new IllegalArgumentException("Please provide the path to the file containing ontolex data.");

    Path sourceFile = parseSourceFileParameter(args);
    Path targetDirectory = parseTargetDirectoryParameter(args);

    Ontolex2Skos.generateAndSave(sourceFile, targetDirectory);
  }

  private static Path parseSourceFileParameter(String[] args) {
    String sourceFilePath = args[0];
    Path sourceFile = Path.of(sourceFilePath);

    if (!Files.exists(sourceFile))
      throw new IllegalArgumentException("'" + sourceFile.toString() + "' does not exist!");

    if (!Files.isRegularFile(sourceFile))
      throw new IllegalArgumentException("'" + sourceFile.toString() + "' is not a file!");

    return sourceFile.toAbsolutePath();
  }

  private static Path parseTargetDirectoryParameter(String[] args) throws IOException {
    String targetDirectoryPath = (args.length >= 2) ? args[1] : "";
    Path targetDirectory = Path.of(targetDirectoryPath);
    Files.createDirectories(targetDirectory);
    return targetDirectory;
  }


}
