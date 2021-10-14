package it.unibz.core.thor;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriSanitizer {

  public static String getSafeNodeString(RDFNode node) {
    Objects.requireNonNull(node, "Cannot sanitize a null value.");

    if (node.isAnon())
      throw new IllegalArgumentException("Cannot sanitize anonymous node!");

    if (node.isResource())
      return getSafeString(node.asResource());

    if (isLanguageString(node))
      return getSafeLanguageString(node.asLiteral());

    if (node.isLiteral())
      return getSafeString(node.asLiteral());

    return node.asNode().toString();
  }

  private static String getSafeLanguageString(Literal literal) {
    String value = literal.getLexicalForm();
    String language = literal.getLanguage();

    value = value.replaceAll("\"", "\\\\\"");

    return "\"\"\"" + value + "\"\"\"@" + language;
  }

  private static String getSafeString(Resource resource) {
    return "<" + resource.getURI() + ">";
  }

  private static String getSafeString(Literal literal) {
    String value = literal.getString();
    String datatypeUri = literal.getDatatypeURI();
    return "\"\"\"" + value + "\"\"\"^^" + "<" + datatypeUri + ">";
  }

  public static String getSafeString(String value) {
    Objects.requireNonNull(value, "Cannot sanitize a null value.");

    if (isSparqlVariable(value))
      return value;

    if (startsWithStandardPrefix(value))
      return value;

    if (isValidLiteralString(value))
      return value;

    return "<" + value + ">";
  }

  public static boolean isValidLiteralString(String value) {
    return isLanguageString(value) || isPlainLiteral(value) || isTypedLiteral(value) ||
            isMultilineLanguageString(value) || isMultilineTypedLiteral(value) ||
            isMultilinePlainLiteral(value);
  }

  public static boolean isLanguageString(String value) {
    return value.matches("^\\\".*\\\"@[a-zA-Z]+$");
  }

  public static boolean isMultilineLanguageString(String value) {
    final String regex = "^\\\"{3}.*\\\"{3}@[a-zA-Z]+$";
    Pattern p = Pattern.compile(regex, Pattern.DOTALL);
    Matcher m = p.matcher(value);
    return m.matches();
  }

  public static boolean isPlainLiteral(String value) {
    return value.matches("^\\\".*\\\"$");
  }

  public static boolean isMultilinePlainLiteral(String value) {
    final String regex = "^\\\"{3}.*\\\"{3}$";
    Pattern p = Pattern.compile(regex, Pattern.DOTALL);
    Matcher m = p.matcher(value);
    return m.matches();
  }

  public static boolean isTypedLiteral(String value) {
    return value.matches("^\\\".*\\\"\\^\\^\\w*:\\w+$") || value.matches("^\\\".*\\\"\\^\\^<.*?>$");
  }

  public static boolean isMultilineTypedLiteral(String value) {
    return isMultilinePrefixedTypedLiteral(value) || isMultilineExtendedTypedLiteral(value);
  }

  public static boolean isMultilinePrefixedTypedLiteral(String value) {
    final String regex = "^\\\"{3}.*\\\"{3}\\^\\^\\w*:\\w+$";
    Pattern p = Pattern.compile(regex, Pattern.DOTALL);
    Matcher m = p.matcher(value);
    return m.matches();
  }

  public static boolean isMultilineExtendedTypedLiteral(String value) {
    final String regex = "^\\\".*\\\"\\^\\^<.*?>$";
    Pattern p = Pattern.compile(regex, Pattern.DOTALL);
    Matcher m = p.matcher(value);
    return m.matches();
  }

  private static boolean isLanguageString(RDFNode node) {
    String languageStringUri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString";
    return node.isLiteral() && node.asLiteral().getDatatypeURI().equals(languageStringUri);
  }

  private static boolean isSparqlVariable(String value) {
    return value.startsWith("?");
  }

  private static boolean startsWithStandardPrefix(String uri) {
    return Vocabulary.getPrefixes()
            .stream()
            .anyMatch(uri::startsWith);
  }

  private static String sanitizeUri(Resource resource) {
    String uri = resource.getURI();

    if (startsWithStandardPrefix(uri))
      return uri;

    return "<" + uri + ">";
  }

//  private static String sanitizeTextLiteral(Literal literal) {
//    String value = literal.getString()
//            .replaceAll("\n", " ")
//            .replaceAll("\r", "")
//            .replaceAll("\"", "\\\\\"")
//            .trim();
//
//    String language = literal.getLanguage();
//    System.out.println(language);
//
//    if (language != null)
//      return "\"\"\"" + value + "\"\"\"@" + language;
//
//    final String datatypeURI = literal.getDatatypeURI();
//    if (datatypeURI != null)
//      return "\"" + value + "\"^^" + datatypeURI;
//
//    return "\"" + value + "\"^^";
//  }

}
