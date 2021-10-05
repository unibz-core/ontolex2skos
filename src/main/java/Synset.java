import org.apache.jena.ontology.Individual;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class Synset {
  private TreeSet<Individual> senses = new TreeSet<>((o1, o2) -> o1.getURI().compareToIgnoreCase(o2.getURI()));

  public Synset(Individual... senses) {
    this.senses.addAll(Arrays.asList(senses));
  }

  public void add(Individual sense) {
    this.senses.add(sense);
  }

  public void addAll(Collection<? extends Individual> senses) {
    this.senses.addAll(senses);
  }

  public void remove(Individual sense) {
    this.senses.remove(sense);
  }

  public int size() {
    return this.senses.size();
  }

  public boolean isEmpty() {
    return this.size() == 0;
  }

  public SortedSet<Individual> getSenses() {
    return new TreeSet<>(senses);
  }

  public Stream<Individual> stream() {
    return senses.stream();
  }

  public Individual first() {
    return senses.first();
  }

  public SortedSet<Individual> tailSet() {
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
    return senses.stream()
            .map(Individual::getURI)
            .collect(toSet())
            .toString();
  }
}
