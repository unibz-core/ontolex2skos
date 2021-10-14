package it.unibz.core.thor;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Synset {
  private TreeSet<String> senses = new TreeSet<>();

  public Synset(String... senses) {
    this.senses.addAll(Arrays.asList(senses));
  }

  public void add(String sense) {
    this.senses.add(sense);
  }

  public void addAll(Collection<? extends String> senses) {
    this.senses.addAll(senses);
  }

  public void remove(String sense) {
    this.senses.remove(sense);
  }

  public int size() {
    return this.senses.size();
  }

  public boolean isEmpty() {
    return this.size() == 0;
  }

  public SortedSet<String> getSenses() {
    return new TreeSet<>(senses);
  }

  public Stream<String> stream() {
    return senses.stream();
  }

  public String first() {
    return senses.first();
  }

  public SortedSet<String> tailSet() {
    return senses.tailSet(senses.first(), false);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Synset)) return false;

    Synset synset = (Synset) o;

    return senses.equals(synset.senses);
  }

  @Override
  public int hashCode() {
    return senses != null ? senses.hashCode() : 0;
  }

  @Override
  public String toString() {
    return senses.toString();
  }

}
