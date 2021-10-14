package it.unibz.core.thor;

import org.junit.jupiter.api.Test;

import static it.unibz.core.thor.UriSanitizer.*;
import static org.assertj.core.api.Assertions.assertThat;

class UriSanitizerTest {

  @Test
  void normalStringShouldBePlainLiteral() {
    boolean b = isPlainLiteral("\"hello\"");
    assertThat(b).isTrue();
  }

  @Test
  void numberStringShouldBePlainLiteral() {
    boolean b = isPlainLiteral("\"5\"");
    assertThat(b).isTrue();
  }

  @Test
  void emptyStringShouldBePlainLiteral() {
    boolean b = isPlainLiteral("\"\"");
    assertThat(b).isTrue();
  }

  @Test
  void stringWithoutParenthesisShouldNotBePlainLiteral() {
    boolean b = isPlainLiteral("hello");
    assertThat(b).isFalse();
  }

  @Test
  void languageStringShouldNotBePlainLiteral() {
    boolean b = isPlainLiteral("\"hello\"@en");
    assertThat(b).isFalse();
  }

  @Test
  void typedLiteralShouldNotBePlainLiteral() {
    boolean b = isPlainLiteral("\"hello\"@xsd:string");
    assertThat(b).isFalse();
  }

  @Test
  void plainLiteralShouldNotBeLanguageString() {
    boolean b = isLanguageString("\"hello\"");
    assertThat(b).isFalse();
  }

  @Test
  void emptyPlainLiteralShouldNotBeLanguageString() {
    boolean b = isLanguageString("\"\"");
    assertThat(b).isFalse();
  }

  @Test
  void emptyLanguageStringShouldBeValid() {
    boolean b = isLanguageString("\"\"@en");
    assertThat(b).isTrue();
  }

  @Test
  void prototypicalLanguageStringShouldBeValid() {
    boolean b = isLanguageString("\"hello\"@en");
    assertThat(b).isTrue();
  }

  @Test
  void stringWithoutParenthesisShouldNotBeLanguageString() {
    boolean b = isLanguageString("hello");
    assertThat(b).isFalse();
  }


  @Test
  void typedLiteralShouldNotBeLanguageString() {
    boolean b = isLanguageString("\"hello\"^^xsd:string");
    assertThat(b).isFalse();
  }

  @Test
  void prefixedTypedLiteralShouldBeValid() {
    boolean b = isTypedLiteral("\"hello\"^^xsd:string");
    assertThat(b).isTrue();
  }

  @Test
  void fullTypedLiteralShouldBeValid() {
    boolean b = isTypedLiteral("\"hello\"^^<http://www.w3.org/2001/XMLSchema#string>");
    assertThat(b).isTrue();
  }

  @Test
  void brokenPrefixTypedLiteralShouldBeRejected() {
    boolean b = isTypedLiteral("\"hello\"^^xsd:");
    assertThat(b).isFalse();
  }

  @Test
  void hiddenPrefixTypedLiteralShouldBeValid() {
    boolean b = isTypedLiteral("\"hello\"^^:string");
    assertThat(b).isTrue();
  }

  @Test
  void plainLiteralShouldNotBeTypedLiteral() {
    boolean b = isTypedLiteral("\"hello\"");
    assertThat(b).isFalse();
  }

  @Test
  void languageStringShouldNotBeTypedLiteral() {
    boolean b = isTypedLiteral("\"hello\"@en");
    assertThat(b).isFalse();
  }

  @Test
  void multilinePlainLiteralShouldBeValid() {
    boolean b = isMultilinePlainLiteral("\"\"\"hello\"\"\"");
    assertThat(b).isTrue();
  }

  @Test
  void multilinePrefixedTypedLiteralShouldBeValid() {
    boolean b = isMultilineTypedLiteral("\"\"\"hello\"\"\"^^xsd:string");
    assertThat(b).isTrue();
  }

  @Test
  void multilineFullTypedLiteralShouldBeValid() {
    boolean b = isMultilineTypedLiteral("\"\"\"hello\"\"\"^^<http://www.w3.org/2001/XMLSchema#string>");
    assertThat(b).isTrue();
  }


}