package org.jhapy.frontend.dataproviders.utils;

import com.vaadin.flow.data.provider.Query;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-03
 */
public abstract class FilterablePageableDataProvider<T extends Serializable, F> extends
    PageableDataProvider<T, F> {

  private F filter = null;

  public void setFilter(F filter) {
    if (filter == null) {
      throw new IllegalArgumentException("Filter cannot be null");
    }
    this.filter = filter;
    refreshAll();
  }

  @Override
  public int size(Query<T, F> query) {
    return super.size(getFilterQuery(query));
  }

  @Override
  public Stream<T> fetch(Query<T, F> query) {
    return super.fetch(getFilterQuery(query));
  }

  private Query<T, F> getFilterQuery(Query<T, F> t) {
    return new Query<>(t.getOffset(), t.getLimit(), t.getSortOrders(),
        t.getInMemorySorting(), filter);
  }
}