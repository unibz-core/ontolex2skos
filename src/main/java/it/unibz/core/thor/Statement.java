package it.unibz.core.thor;

import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.List;

import static it.unibz.core.thor.UriSanitizer.getSafeString;
import static it.unibz.core.thor.UriSanitizer.getSafeNodeString;

public class Statement {

  RDFNode subjectNode, predicateNode, objectNode;
  String subjectString, predicateString, objectString;

  public Statement(RDFNode subjectNode, RDFNode predicateNode, RDFNode objectNode) {
    this.subjectNode = subjectNode;
    this.predicateNode = predicateNode;
    this.objectNode = objectNode;
  }

  public Statement(RDFNode subjectNode, RDFNode predicateNode, String objectString) {
    this.subjectNode = subjectNode;
    this.predicateNode = predicateNode;
    this.objectString = objectString;
  }

  public Statement(RDFNode subjectNode, String predicateString, RDFNode objectNode) {
    this.subjectNode = subjectNode;
    this.predicateString = predicateString;
    this.objectNode = objectNode;
  }

  public Statement(RDFNode subjectNode, String predicateString, String objectString) {
    this.subjectNode = subjectNode;
    this.predicateString = predicateString;
    this.objectString = objectString;
  }

  public Statement(String subjectString, RDFNode predicateNode, RDFNode objectNode) {
    this.subjectString = subjectString;
    this.predicateNode = predicateNode;
    this.objectNode = objectNode;
  }

  public Statement(String subjectString, RDFNode predicateNode, String objectString) {
    this.subjectString = subjectString;
    this.predicateNode = predicateNode;
    this.objectString = objectString;
  }

  public Statement(String subjectString, String predicateString, RDFNode objectNode) {
    this.subjectString = subjectString;
    this.predicateString = predicateString;
    this.objectNode = objectNode;
  }

  public Statement(String subjectString, String predicateString, String objectString) {
    this.subjectString = subjectString;
    this.predicateString = predicateString;
    this.objectString = objectString;
  }

  @Override
  public String toString() {
    return getSubject() + " " + getPredicate() + " " + getObject();
  }

  public String getSubject() {
    return (subjectNode != null) ? getSafeNodeString(subjectNode) : getSafeString(subjectString);
  }

  public String getPredicate() {
    return (predicateNode != null) ? getSafeNodeString(predicateNode) : getSafeString(predicateString);
  }

  public String getObject() {
    return (objectNode != null) ? getSafeNodeString(objectNode) : getSafeString(objectString);
  }

  public List<String> getVariables() {
    List<String> variables = new ArrayList<>();

    if (isVariable(subjectString))
      variables.add(subjectString);

    if (isVariable(predicateString))
      variables.add(predicateString);

    if (isVariable(objectString))
      variables.add(objectString);

    return variables;
  }

  private boolean isVariable(String value) {
    return value instanceof String && value.startsWith("?");
  }

}
