import org.apache.jena.ontology.OntModel;

public class Domains {
  static final String THOR_PREFIX = "thor";
  static final String THOR_URI = "http://purl.org/ZIN-Thor/";

  static final String ONTOLEX_PREFIX = "ontolex";
  static final String ONTOLEX_URI = "http://www.w3.org/ns/lemon/ontolex#";

  static final String SKOS_PREFIX = "skos";
  static final String SKOS_URI = "http://www.w3.org/2004/02/skos/core#";

  static final String LEXINFO_PREFIX = "lexinfo";
  static final String LEXINFO_URI = "http://www.lexinfo.net/ontology/3.0/lexinfo#";

  static final String VPH_PREFIX = "vph-g";
  static final String VPH_URI = "http://purl.org/ozo/vph-g#";

  static final String LIME_PREFIX = "lime";
  static final String LIME_URI = "http://www.w3.org/ns/lemon/lime#";

  static final String RDF_PREFIX = "rdf";
  static final String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  static final String RDFS_PREFIX = "rdfs";
  static final String RDFS_URI = "http://www.w3.org/2000/01/rdf-schema#";

  static final String OWL_PREFIX = "owl";
  static final String OWL_URI = "http://www.w3.org/2002/07/owl#";

  static final String SPARQL_PREFIXES = "PREFIX " + RDF_PREFIX + ": <" + RDF_URI + ">" +
          "PREFIX " + RDFS_PREFIX + ": <" + RDFS_URI + ">" +
          "PREFIX " + OWL_PREFIX + ": <" + OWL_URI + ">" +
          "PREFIX " + THOR_PREFIX + ": <" + THOR_URI + ">" +
          "PREFIX " + ONTOLEX_PREFIX + ": <" + ONTOLEX_URI + ">" +
          "PREFIX " + LIME_PREFIX + ": <" + LIME_URI + ">" +
          "PREFIX " + SKOS_PREFIX + ": <" + SKOS_URI + ">" +
          "PREFIX " + LEXINFO_PREFIX + ": <" + LEXINFO_URI + ">" +
          "PREFIX " + VPH_PREFIX + ": <" + VPH_URI + ">";

  public static void addNamespaces(OntModel model) {
    model.setNsPrefix(THOR_PREFIX, THOR_URI);
    model.setNsPrefix(ONTOLEX_PREFIX, ONTOLEX_URI);
    model.setNsPrefix(SKOS_PREFIX, SKOS_URI);
    model.setNsPrefix(VPH_PREFIX, VPH_URI);
    model.setNsPrefix(LEXINFO_PREFIX, LEXINFO_URI);
    model.setNsPrefix(LIME_PREFIX, LIME_URI);
    model.setNsPrefix(RDF_PREFIX, RDF_URI);
    model.setNsPrefix(RDFS_PREFIX, RDFS_URI);
    model.setNsPrefix(OWL_PREFIX, OWL_URI);
  }

}
