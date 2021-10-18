# ontolex2skos

An automated model-driven transformation that generates thesauri from lexica.

This Java application reads lexical data, structured using the [Ontolex](https://www.w3.org/2016/05/ontolex/) vocabulary, and yields thesauri data structured according to the [SKOS](https://www.w3.org/2004/02/skos/) vocabulary.

`ontolex2thor` has been developed in the context of the **ThOR: Thesaurus and Ontological Representation in Healthcare**, a collaboration between the [Conceptual and Cognitive Modeling Research Group (CORE)](https://www.inf.unibz.it/krdb/core/) of the [Free University of Bozen-Bolzano (UNIBZ)](https://unibz.it) and the [Zorginstituut Nederland (ZIN)](https://www.zorginstituutnederland.nl).

Contributors:

* [Tiago Prince Sales](http://inf.unibz.it/~tpsales) (UNIBZ)
* Pedro Paulo Favato Barcelos (UNIBZ)
* Elly Kampert (ZIN)
* Fabien Reniers (ZIN)
* Roxane Segers (ZIN)

## Compiling and running ontolex2skos

This project uses [Maven](https://maven.apache.org), so you can use its expected commands.

To compile and package it, run:

```shell
mvn package 
```

This will generate a `target/thor-1.0.jar` file, a ''fat'' runnable jar that can run the transformation without additional dependencies.

To run the application, execute the command below, replacing `sourceFile.ttl` with the path of your file containing Ontolex data:

```shell
java -jar target/thor-1.0.jar sourceFile.ttl
```

After the program executs, you should see the following files:

* `thesaurus.ttl`: a file containing the generated thesauri
* `lexicon.ttl`:  a file containing your original lexica, but enriched with some basic inferences
* `kg.ttl`: a file containing your original data, the inferences made by the transformation, and the generated thesauri

You can also specify the directory where the output files are saved.

To do that, execute the command below, replacing `targetDir` with your custom output directory:

```shell
java -jar target/thor-1.0.jar sourceFile.ttl targetDir
```

To only run unit tests, execute:

````shell
mvn test
````