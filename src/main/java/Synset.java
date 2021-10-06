import org.apache.jena.rdf.model.Resource;

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class Synset {
  private TreeSet<Resource> senses = new TreeSet<>((o1, o2) -> o1.getURI().compareToIgnoreCase(o2.getURI()));

  public Synset(Resource... senses) {
    this.senses.addAll(Arrays.asList(senses));
  }

  public void add(Resource sense) {
    this.senses.add(sense);
  }

  public void addAll(Collection<? extends Resource> senses) {
    this.senses.addAll(senses);
  }

  public void remove(Resource sense) {
    this.senses.remove(sense);
  }

  public int size() {
    return this.senses.size();
  }

  public boolean isEmpty() {
    return this.size() == 0;
  }

  public SortedSet<Resource> getSenses() {
    return new TreeSet<>(senses);
  }

  public Stream<Resource> stream() {
    return senses.stream();
  }

  public Resource first() {
    return senses.first();
  }

  public SortedSet<Resource> tailSet() {
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
            .map(Resource::getURI)
            .collect(toSet())
            .toString();
  }
}
