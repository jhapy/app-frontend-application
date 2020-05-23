package org.jhapy.frontend.dataproviders;

import com.vaadin.flow.component.crud.CrudFilter;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jhapy.dto.domain.BaseEntity;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
public abstract class DefaultBackend<C extends BaseEntity> extends
    AbstractBackEndDataProvider<C, CrudFilter> implements
    Serializable {

  protected List<C> fieldsMap = new ArrayList<>();
  protected AtomicLong uniqueLong = new AtomicLong();
  private Comparator<C> comparator;
  private SerializablePredicate<C> filter;

  public DefaultBackend() {
  }

  public DefaultBackend(Comparator<C> comparator) {
    this.comparator = comparator;
  }

  @Override
  public abstract Object getId(C value);

  public Collection<C> getValues() {
    if (comparator != null) {
      return fieldsMap.stream().sorted(comparator).collect(Collectors.toList());
    } else {
      return fieldsMap;
    }
  }

  public abstract void setValues(Collection<C> values);

  public abstract void persist(C value);

  public abstract void delete(C value);

  public SerializablePredicate<C> getFilter() {
    return this.filter;
  }

  public void setFilter(SerializablePredicate<C> filter) {
    this.filter = filter;
    this.refreshAll();
  }

  public void addFilter(SerializablePredicate<C> filter) {
    Objects.requireNonNull(filter, "Filter cannot be null");
    if (this.getFilter() == null) {
      this.setFilter(filter);
    } else {
      SerializablePredicate<C> oldFilter = this.getFilter();
      this.setFilter((item) -> oldFilter.test(item) && filter.test(item));
    }

  }

  @Override
  protected int sizeInBackEnd(Query<C, CrudFilter> query) {
    return fieldsMap.size();
  }

  @Override
  protected Stream<C> fetchFromBackEnd(Query<C, CrudFilter> query) {
    Stream<C> stream = fieldsMap.stream();

    Optional<Comparator<C>> comparing = Stream.of(query.getInMemorySorting(), comparator)
        .filter(Objects::nonNull).reduce(Comparator::thenComparing);
    if (comparing.isPresent()) {
      stream = stream.sorted();
    }
    long maxId = 0;
    List<C> result = stream.collect(Collectors.toList());
    for (C c : result) {
      if ((long) c.getId() > maxId) {
        maxId = (long) c.getId();
      }
    }
    uniqueLong.set(maxId + 1);
    return result.stream().skip(query.getOffset()).limit(query.getLimit());
  }
}