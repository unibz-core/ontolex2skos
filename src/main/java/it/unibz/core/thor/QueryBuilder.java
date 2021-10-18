package it.unibz.core.thor;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.unibz.core.thor.Vocabulary.getPrefixDeclarations;
import static java.util.stream.Collectors.joining;

public class QueryBuilder {
  List<Statement> statements;

  public QueryBuilder() {
    this.statements = new ArrayList<>();
  }

  public void addStatement(RDFNode subjectNode, RDFNode predicateNode, RDFNode objectNode) {
    Statement s = new Statement(subjectNode, predicateNode, objectNode);
    addStatements(s);
  }

  public void addStatement(RDFNode subjectNode, RDFNode predicateNode, String objectString) {
    Statement s = new Statement(subjectNode, predicateNode, objectString);
    addStatements(s);
  }

  public void addStatement(RDFNode subjectNode, String predicateString, RDFNode objectNode) {
    Statement s = new Statement(subjectNode, predicateString, objectNode);
    addStatements(s);
  }

  public void addStatement(RDFNode subjectNode, String predicateString, String objectString) {
    Statement s = new Statement(subjectNode, predicateString, objectString);
    addStatements(s);
  }

  public void addStatement(String subjectString, RDFNode predicateNode, RDFNode objectNode) {
    Statement s = new Statement(subjectString, predicateNode, objectNode);
    addStatements(s);
  }

  public void addStatement(String subjectString, RDFNode predicateNode, String objectString) {
    Statement s = new Statement(subjectString, predicateNode, objectString);
    addStatements(s);
  }

  public void addStatement(String subjectString, String predicateString, RDFNode objectNode) {
    Statement s = new Statement(subjectString, predicateString, objectNode);
    addStatements(s);
  }

  public void addStatement(String subjectString, String predicateString, String objectString) {
    Statement s = new Statement(subjectString, predicateString, objectString);
    addStatements(s);
  }

  public void addStatements(List<Statement> statements) {
    this.statements.addAll(statements);
  }

  public void addStatements(Statement... statements) {
    addStatements(Arrays.asList(statements));
  }

  public String getWhereClause() {
    return statements.stream()
            .map(Statement::toString)
            .collect(joining(" . "));
  }

  public String[] extractVariables() {
    return statements.stream()
            .flatMap(s -> s.getVariables().stream())
            .distinct()
            .toArray(String[]::new);
  }

  public String getVariableClause() {
    return String.join(" ", extractVariables()) + " ";
  }

  public String getQueryString() {
    return getPrefixDeclarations() + " " +
            "SELECT " + getVariableClause() + " " +
            "WHERE { " + getWhereClause() + " }";
  }

  public Query build() {
    String queryString = getQueryString();
    return QueryFactory.create(queryString);
  }

  public static Query build(RDFNode subjectNode, RDFNode predicateNode, RDFNode objectNode) {
    QueryBuilder b = new QueryBuilder();
    b.addStatement(subjectNode, predicateNode, objectNode);
    return b.build();
  }

  public static Query build(RDFNode subjectNode, RDFNode predicateNode, String objectString) {
    QueryBuilder b = new QueryBuilder();
    b.addStatement(subjectNode, predicateNode, objectString);
    return b.build();
  }

  public static Query build(RDFNode subjectNode, String predicateString, RDFNode objectNode) {
    QueryBuilder b = new QueryBuilder();
    b.addStatement(subjectNode, predicateString, objectNode);
    return b.build();
  }

  public static Query build(RDFNode subjectNode, String predicateString, String objectString) {
    QueryBuilder b = new QueryBuilder();
    b.addStatement(subjectNode, predicateString, objectString);
    return b.build();
  }

  public static Query build(String subjectString, RDFNode predicateNode, RDFNode objectNode) {
    QueryBuilder b = new QueryBuilder();
    b.addStatement(subjectString, predicateNode, objectNode);
    return b.build();
  }

  public static Query build(String subjectString, RDFNode predicateNode, String objectString) {
    QueryBuilder b = new QueryBuilder();
    b.addStatement(subjectString, predicateNode, objectString);
    return b.build();
  }

  public static Query build(String subjectString, String predicateString, RDFNode objectNode) {
    QueryBuilder b = new QueryBuilder();
    b.addStatement(subjectString, predicateString, objectNode);
    return b.build();
  }

  public static Query build(String subjectString, String predicateString, String objectString) {
    QueryBuilder b = new QueryBuilder();
    b.addStatement(subjectString, predicateString, objectString);
    return b.build();
  }

  public static Query build(Statement... statements) {
    QueryBuilder b = new QueryBuilder();
    b.addStatements(statements);
    return b.build();
  }


}
