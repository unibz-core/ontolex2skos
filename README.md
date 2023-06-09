# Ontolex2SKOS

## Table of Contents

  - [Overview](#overview)
  - [Contributors](#contributors)
  - [Software requirements](#software-requirements)
  - [Code compilation](#code-compilation)
  - [Code execution](#code-execution)
  - [Transformation rules](#transformation-rules)
  - [Publication](#publication)

## Overview

**Ontolex2SKOS** is an automated model-driven transformation for generating SKOS thesauri from Ontolex-Lemon lexicons. 

Ontolex2SKOS allows organizations to have both lexicons and thesauri while building only the former. The domain-independent rules encoded in the transformation comply with strict requirements to guarantee that the output thesaurus is consistent with the input lexicon while allowing any change in the input data to be easily reflected in the output. Ontolex2SKOS was created to encompass domain-specific lexicons and is domain-independent.

This Java application reads lexical data, structured using the [Ontolex](https://www.w3.org/2016/05/ontolex/) vocabulary, and yields thesauri data structured according to the [SKOS](https://www.w3.org/2004/02/skos/) vocabulary. For making this possible, it uses a small vocabulary extension called [ThOR Ontology](https://github.com/unibz-core/thor/).

ThOR stands for “Thesaurus and Ontology Representation”. The ThOR Project was a collaboration project that happened in 2021 between the [*Zorginstituut Nederland* (ZIN)](https://english.zorginstituutnederland.nl/) – the Dutch National Health Care Institute – and the [Conceptual and Cognitive Modeling Research Group (CORE)](https://www.inf.unibz.it/krdb/core/) from the [Free University of Bozen-Bolzano (Unibz)](https://unibz.it/).

## Contributors

* [Tiago Prince Sales (UNIBZ)](https://www.linkedin.com/in/tiago-sales/)
* [Pedro Paulo Favato Barcelos (UNIBZ)](https://www.linkedin.com/in/pedro-paulo-favato-barcelos/)
* [Elly Kampert (ZIN)](https://www.linkedin.com/in/elly-kampert-van-galen/)
* [Fabien Reniers (ZIN)](https://www.linkedin.com/in/fabienreniers/)
* [Roxane Segers (ZIN)](https://www.linkedin.com/in/roxanesegers/)

## Software requirements

* [Maven](https://maven.apache.org)
* Java 15

## Code compilation

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

```shell
mvn test
```

## Code execution

Go to the directory containing `thor-1.0.jar`

To run the application, execute the command below, replacing `sourceFile.ttl` with the path of your file containing Ontolex data:


```shell
java -jar ontolex2skos-1.0.jar sourceFile.ttl
```

After the program executes, you should see the following files:

* `thesaurus.ttl`: a file containing the generated thesauri, the main output of the application
* `lexicon.ttl`:  a file containing your original lexica, but enriched with some basic inferences
* `kg.ttl`: a file containing your original data, the inferences made by the transformation, and the generated thesauri

You can also specify the directory where the output files are saved.

To do that, execute the command below, replacing `targetDir` with your custom output directory:

```shell
java -jar target/ontolex2skos-1.0.jar sourceFile.ttl targetDir
```

## Transformation rules

The automated transformation comprises eight rules. Some rules use the [**thor vocabulary**](https://github.com/unibz-core/thor/), prefixed as http://purl.org/net/thor-ontology/.

### Rule 1. Derive concept schemes from lexicons

Each lime:Lexicon in the input data is mapped to a *skos:Concept*Scheme (i.e., a SKOS thesaurus) in the output data. By default, the title of the source lexicon, defined using Dublin Core’s *dct:title* property, is replicated in the target thesaurus.

```
### Input
:myLexicon a lime:Lexicon ; 
    dct:title "My lexicon" .

### Output
:myThesaurus a skos:ConceptScheme ; 
    dct:title "My thesaurus" .
```

### Rule 2. Derive concepts from synsets

Each synset expresses a single unique concept and corresponds to an *ontolex:LexicalConcept*. Two senses are declared synonyms via the property *lexinfo:synonym*, which is symmetric and transitive. Alternatively, senses can be declared synonyms by associating them to the same *ontolex:LexicalConcept* through the property *ontolex:isLexicalizedSenseOf* (or via its inverse property). Therefore, each synset in the input data gives rise to a *skos:Concept* if it has no associated *ontolex:LexicalConcept*. If that is the case, the newly created concept will be linked to the senses that compose the synset through ontolex:lexicalizedSense, and their respective entries through *ontolex:evokes*.

```
### Input
:batEntry a ontolex:LexicalEntry ;
    ontolex:sense :batSense .
:batSense a ontolex:LexicalSense .

:clubEntry a ontolex:LexicalEntry ;
    ontolex:sense :clubSense .
:clubSense a ontolex:LexicalSense ;
    lexinfo:synonym :batSense .

### Output
:batConcept a skos:Concept ;
    ontolex:lexicalizedSense :batSense, :clubSense ;
    ontolex:isEvokedBy :clubEntry, :batEntry .
```

### Rule 3. Derive concept labels

Each generated *skos:Concept* receives one preferred label (via *skos:prefLabel*) and potentially multiple alternative labels (via *skos:altLabel*). Every label is derived from an ontolex:Form of a lexical entry associated with a sense that composes the synset from which the concept was derived.

For a given concept, if a single form is indirectly associated with its source synset, the representation of that form (identified via the ontolex:writtenRep property) becomes the concept’s preferred label. In this situation, the concept does not receive alternative labels. Alternatively, if multiple forms are indirectly associated with the synset from which a concept was derived, the preferred label comes from the form associated with a thor:PreferredSense—a subclass of LexicalSense defined in the thor vocabulary. All the other forms are mapped into alternative labels.

An exception to the former conditional is the case of lexical entries that are contractions of others (e.g., “EU” is a contraction of “European Union”), which are identified via the *lexinfo:contractionFor*. In such cases, acronyms are always mapped into alternative labels.

```
### Input
:batEntry a ontolex:LexicalEntry ;
    ontolex:sense :batSense ;
    ontolex:canonicalForm :batForm .
:batSense a thor:PreferredSense ;
    lexinfo:synonym :clubSense .
:batForm a ontolex:Form ;
    ontolex:writtenRep "bat"@en .
:clubEntry a ontolex:LexicalEntry ;
    ontolex:sense :clubSense ;
    ontolex:canonicalForm :batForm .
:clubSense a ontolex:LexicalSense .
:clubForm a ontolex:Form ;
    ontolex:writtenRep "club"@en .

### Output
:batConcept ontolex:lexicalizedSense :batSense, :clubSense ;
    skos:prefLabel "bat"@en ;
    skos:altLabel "club"@en ;
```

### Rule 4. Derive documentation properties from lexical senses

The data input may have SKOS documentation properties, such as *skos:definition* or *skos:example*, for describing lexical senses. As these properties apply to lexical senses and concepts, this rule simply maps them to the generated concepts. For the case of synsets, the documentation properties of all senses are mapped to the same resulting concept.

Some documentation properties apply only to lexical senses and thus need to be translated by the transformation. This is the case of *ontolex:usage*. For each *ontolex:usage*, the generated concept will have a property *skos:scopeNote* with the same content.

```
### Input
:batSense skos:definition "a specially shaped piece of wood used for hitting the ball in some games"@en ;
    skos:example "She hit the ball with her bat"@en ;
    ontolex:usage "Bat is most often used for sports equipment"@en .

### Output
:batConcept ontolex:lexicalizedSense :batSense ;
    skos:definition "a specially shaped piece of wood used for hitting the ball in some games"@en ;
    skos:example "She hit the ball with her bat"@en ;
    skos:scopeNote "Bat is most often used for sports equipment"@en .
```

### Rule 5. Derive semantic relations

We rely on the *lexinfo:hypernym* and *lexinfo:hyponym* properties to define hierarchical relations between lexical senses, and on *lexinfo:relatedTerm* for associative relations between them. We map these properties to *skos:broader*, *skos:narrower*, and *skos:related*, respectively.

The mapping of *lexinfo:hypernym* into *skos:broader* works as follows. For every **ontolex:LexicalSense** `LS1` and `LS2`, if `LS1` is the hypernym of `LS2`, then `C1` (the **skos:Concept** into which `LS1` was mapped via **Rule 2**) is broader than `C2` (the *skos:Concept* into which `L2` was mapped via the same rule). The mappings of *lexinfo:hyponym* into *skos:narrower* and that of *lexinfo:relatedTerm* into *skos:related* work analogously.

```
### Input
:equipmentSense a ontolex:LexicalSense .
:batSense a ontolex:LexicalSense ;
    lexinfo:hypernym :equipmentSense .

# Added via the application of Rule 2
:equipmentConcept a skos:Concept ;
    ontolex:lexicalizedSense equipmentSense .
:batConcept a skos:Concept ;
    ontolex:lexicalizedSense batSense . 

### Output
:batConcept skos:broader :equipmentConcept .
```

### Rule 6. Derive mapping properties

The vocabularies that we use here provide a range of properties to map concepts, senses, and ontology entities from external sources. Ontolex-Lemon offers the *ontolex:reference* property to relate instances of *ontolex:LexicalSense* to ontological entities, which are naturally external to the lexical dataset. Both internal and external mappings can set through Lexinfo’s sense relations, such as *lexinfo:synonym* and *lexinfo:hypernym*. However, such relations can only occur between instances of *ontolex:LexicalSense*. SKOS provides specific properties for one to map instances of *skos:Concept* from different schemes, such as *skos:narrowMatch* and *skos:broadMatch*.

However, this set of properties cannot describe some external mappings that are desired in a scenario like the one that motivated the development of Ontolex2SKOS, e.g., to express external mappings from lexical senses to concepts and ontology entities (in a looser way than *ontolex:reference*). To overcome this limitation, we use the mapping properties of the thor vocabulary, which can externally map concepts, senses, or ontology entities with any of these types, as shown in. The thor’s mapping properties are analogous to the SKOS ones, but are named narrowMapping, broadMapping, relatedMapping, exactMapping, and closeMapping. With them, we avoid undesired type inferences, keep the input and output consistent, and simplify the transformation.
Vocabulary extension for mapping properties.

In this rule we simply migrate thor’s mapping properties from lexical senses to the generated concepts. If a sense was mapped originally into an external *skos:Concept*, we translate the mapping properties from the thor vocabulary back to those from SKOS that inspired them.

```
### Input
:batSense a ontolex:LexicalSense ;
    thor:closeMapping ex1:BaseballBat ;
    thor:broadMapping ex2:Artifact .
ex1:BaseballBat a owl:Class .
ex2:Artifact a skos:Concept .

### Output
:batConcept a skos:Concept ;
    ontolex:lexicalizedSense :batSense ;
    thor:closeMapping ex1:BaseballBat ;
    skos:broadMatch ex2:Artifact .
```

### Rule 7. Derive thesaurus components from lexicon components. 

A *skos:Concept* `CO` should belong to a *skos:ConceptScheme* `CS` if there is an *ontolex:LexicalSense* `LS`, an *ontolex:LexicalEntry* `LE`, and a *lime:Lexicon* `LX` such that `CO` has `LS` as its lexicalized sense, `LS` is the sense of `LE`, `LE` is part of `LX`, and `LX` has been mapped to `CS` by **Rule 1**.

```
### Input
:myLexicon a lime:Lexicon ;
    lime:entry :batEntry .
:batEntry ontolex:sense :batSense .

# Added via the application of Rule 1
:myThesaurus a skos:ConceptScheme .

# Added via the application of Rule 2
:batConcept a skos:Concept ;
    ontolex:lexicalizedSense :batSense .

### Output
:batConcept skos:inScheme :myThesaurus .
```

### Rule 8. Resolve label conflicts for homographs. 

An homographs is [“a word that is spelled the same as another word but has a different meaning”](https://dictionary.cambridge.org/us/dictionary/english/homograph). Ontolex-Lemon represents homographs as (i) a lexical entry related to multiple lexical senses, if they have the same grammatical category (e.g., the nouns “bat”, meaning the animal; and “bat”, meaning the wood stick); or as (ii) multiple lexical entries (e.g., the noun “bear”, meaning the animal; and the verb “(to) bear”, which means “to support or carry”), with different lexical senses.

Following **Rule 3**, homographs would yield concepts with the same preferred label, thus conflicting with the SKOS recommendation that no two concepts in the same KOS be given the same preferred lexical label for any given language tag. To address this issue, we adopt a homograph management strategy from [ANSI/NISO Z39.19-2005 (R2010)](https://www.niso.org/publications/ansiniso-z3919-2005-r2010), which recommends their disambiguation using a qualifier. With this strategy, the preferred labels of the “bat as an animal” concept and of the “bat as a wooden club” concept could be “bat (animal)” and “bat (club)”. More precisely, our transformation leverages the domain in which a sense is used to make this distinction, which is expressed by the property *lexinfo:domain*. The domains attributed to senses are preserved in the derived concepts using the property *thor:hasContext*, which is a vocabulary extension created by this work. In addition, the original label is kept as a *skos:altLabel* for the generated concepts, facilitating data retrieval. Homographs labeling is the only case that requires a qualifier.

```
### Input
:batAnimalSense a ontolex:LexicalSense ;
    lexinfo:domain :zoologyDomain .
:zoologyDomain rdfs:label "zoology"@en .

:batArtifactSense a ontolex:LexicalSense ;
    lexinfo:domain :sportsDomain .
:sportsDomain rdfs:label "sports"@en .

:batEntry ontolex:sense :batAnimalSense, :batArtifactSense ;
    canonicalForm :batForm .
:batForm ontolex:canonicalForm "bat"@en .

### Output
:batAnimalConcept ontolex:lexicalizedSense :batAnimalSense ;
    skos:prefLabel "bat (zoology)"@en ;
    skos:altLabel "bat"@en ;
    thor:hasContext :zoologyDomain .
:batArtifactConcept ontolex:lexicalizedSense :batArtifactSense ;
    skos:prefLabel "bat (sports)"@en ;
    skos:altLabel "bat"@en ;
    thor:hasContext :sportsDomain .
```

## Publication

Barcelos, P. P. F., Sales, T. P., Kampert, E., Reniers, F., Segers, R., Guizzardi, G., Franke, W. (2023).  17th Terminology & Ontology: Theories and applications (TOTh 2023) International Conference. Chambéry, France.
