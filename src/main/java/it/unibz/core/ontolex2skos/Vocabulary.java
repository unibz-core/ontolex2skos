package it.unibz.core.ontolex2skos;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.riot.Lang;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public enum Vocabulary {
  THOR("thor", "http://purl.org/thor/", null, null),
  THORL("thorl", "http://purl.org/ZIN-Thor/", null, null),
  ONTOLEX("ontolex", "http://www.w3.org/ns/lemon/ontolex#", "vocabularies/ontolex.ttl", Lang.TURTLE),
  LEXINFO("lexinfo", "http://www.lexinfo.net/ontology/3.0/lexinfo#", "vocabularies/lexinfo-short.ttl", Lang.TURTLE),
  LIME("lime", "http://www.w3.org/ns/lemon/lime#", "vocabularies/lime.ttl", Lang.TURTLE),
  VARTRANS("vartrans", "http://www.w3.org/ns/lemon/vartrans", "vartrans.ttl", Lang.TURTLE),
  SKOS("skos", "http://www.w3.org/2004/02/skos/core#", "vocabularies/skos.rdf", Lang.RDFXML),
  VPH("vph-g", "http://purl.org/ozo/vph-g#", null, null),
  RDF("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", null, null),
  RDFS("rdfs", "http://www.w3.org/2000/01/rdf-schema#", null, null),
  OWL("owl", "http://www.w3.org/2002/07/owl#", null, null),
  FOAF("foaf", "http://xmlns.com/foaf/0.1/", "vocabularies/foaf.rdf", Lang.RDFXML),
  DCT("dct", "http://purl.org/dc/terms/", null, null),
  XSD("xsd", "http://www.w3.org/2001/XMLSchema#", null, null);

  public final String prefix;
  public final String uri;
  public final String filename;
  public final Lang format;

  Vocabulary(String prefix, String uri, String filename, Lang format) {
    this.prefix = prefix;
    this.uri = uri;
    this.filename = filename;
    this.format = format;
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

  public InputStream getInputStream() {
    return Vocabulary.class.getClassLoader().getResourceAsStream(filename);
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
