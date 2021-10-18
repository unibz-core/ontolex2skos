# ontolex2skos

An automated model-driven transformation that generates thesauri from lexica.

This Java application reads lexical data, structured using the [Ontolex](https://www.w3.org/2016/05/ontolex/) vocabulary, and yields thesauri data structured according to the [SKOS](https://www.w3.org/2004/02/skos/) vocabulary.

`ontolex2thor` has been developed in the context of the **ThOR: Thesaurus and Ontological Representation in Healthcare**, a collaboration between the [Conceptual and Cognitive Modeling Research Group (CORE)](https://www.inf.unibz.it/krdb/core/) of the [Free University of Bozen-Bolzano (UNIBZ)](https://unibz.it) and the [iStandaarden](https://istandaarden.nl) unit of the [Zorginstituut Nederland (ZIN)](https://www.zorginstituutnederland.nl).

Contributors:

* [Tiago Prince Sales](http://inf.unibz.it/~tpsales) (UNIBZ)
* Pedro Paulo Favato Barcelos (UNIBZ)
* Elly Kampert (ZIN)
* Fabien Reniers (ZIN)
* Roxane Segers (ZIN)

## Requirements

* [Maven](https://maven.apache.org)
* Java 15

## Compilation

This project uses Maven, so you can use its expected commands.

To compile and package it, run:

```shell
mvn package
```

If you don't have Maven installed locally, you can run:

```shell
./mvnw package # on Linux or macOS
mvnw.cmd package # on Windows
```

This will generate a `target/thor-1.0.jar` file, a ''fat'' runnable jar that can run the transformation without additional dependencies.

To only run unit tests, execute:

````shell
mvn test
````

## Usage

Go to the directory containing `thor-1.0.jar`

To run the application, execute the command below, replacing `sourceFile.ttl` with the path of your file containing Ontolex data:


```shell
java -jar thor-1.0.jar sourceFile.ttl
```

After the program executes, you should see the following files:

* `thesaurus.ttl`: a file containing the generated thesauri, the main output of the application
* `lexicon.ttl`:  a file containing your original lexica, but enriched with some basic inferences
* `kg.ttl`: a file containing your original data, the inferences made by the transformation, and the generated thesauri

You can also specify the directory where the output files are saved.

To do that, execute the command below, replacing `targetDir` with your custom output directory:

```shell
java -jar target/thor-1.0.jar sourceFile.ttl targetDir
```

