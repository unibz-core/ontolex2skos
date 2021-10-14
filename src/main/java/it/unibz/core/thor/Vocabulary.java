package it.unibz.core.thor;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public enum Vocabulary {
  THOR("thor", "http://purl.org/thor/", null),
  THORL("thorl", "http://purl.org/ZIN-Thor/", null),
  ONTOLEX("ontolex", "http://www.w3.org/ns/lemon/ontolex#", "vocabularies/ontolex.ttl"),
  LEXINFO("lexinfo", "http://www.lexinfo.net/ontology/3.0/lexinfo#", "vocabularies/lexinfo-short.ttl"),
  LIME("lime", "http://www.w3.org/ns/lemon/lime#", "vocabularies/lime.ttl"),
  VARTRANS("vartrans", "http://www.w3.org/ns/lemon/vartrans", "vartrans"),
  SKOS("skos", "http://www.w3.org/2004/02/skos/core#", "vocabularies/skos.rdf"),
  VPH("vph-g", "http://purl.org/ozo/vph-g#", null),
  RDF("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", null),
  RDFS("rdfs", "http://www.w3.org/2000/01/rdf-schema#", null),
  OWL("owl", "http://www.w3.org/2002/07/owl#", null),
  DCT("dct", "http://purl.org/dc/terms/", null),
  XSD("xsd", "http://www.w3.org/2001/XMLSchema#", null);

  public final String prefix;
  public final String uri;
  public final String filename;

  Vocabulary(String prefix, String uri, String filename) {
    this.prefix = prefix;
    this.uri = uri;
    this.filename = filename;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getUri() {
    return uri;
  }

  public String getFilename() {
    return filename;
  }

  public String getPrefixDeclaration() {
    return "PREFIX " + prefix + ": <" + uri + ">";
  }

  public String getFilePath() {
    return Vocabulary.class.getClassLoader().getResource(filename).getFile();
  }

  public static Stream<Vocabulary> getInstanceStream() {
    final Vocabulary[] domains = Vocabulary.values();
    return Arrays.stream(domains);
  }

  public static String getPrefixDeclarations() {
    return getInstanceStream()
            .map(Vocabulary::getPrefixDeclaration)
            .collect(joining(" "));
  }

  public static List<String> getPrefixes() {
    return getInstanceStream()
            .map(Vocabulary::getPrefix)
            .collect(toList());
  }

  public static PrefixMapping PREFIX_MAPPING = getPrefixMapping();

  private static PrefixMapping getPrefixMapping() {
    Model model = ModelFactory.createDefaultModel();

    for (Vocabulary voc : values()) {
      model.setNsPrefix(voc.prefix, voc.uri);
    }

    return model;
  }

}
