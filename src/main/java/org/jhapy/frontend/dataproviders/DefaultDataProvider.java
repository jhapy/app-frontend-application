package org.jhapy.frontend.dataproviders;

import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.QuerySortOrderBuilder;
import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;
import org.jhapy.dto.domain.BaseEntity;
import org.jhapy.dto.utils.DirectionEnum;
import org.jhapy.dto.utils.Page;
import org.jhapy.frontend.dataproviders.DefaultDataProvider.DefaultFilter;
import org.jhapy.frontend.dataproviders.utils.FilterablePageableDataProvider;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
public abstract class DefaultDataProvider<T extends BaseEntity, F extends DefaultFilter> extends
    FilterablePageableDataProvider<T, F> implements Serializable {

  private List<QuerySortOrder> defaultSortOrder;
  private Consumer<Page<T>> pageObserver;

  public DefaultDataProvider(DirectionEnum defaultSortDirection, String[] defaultSortFields) {
    setSortOrder(defaultSortDirection, defaultSortFields);
  }

  private void setSortOrder(DirectionEnum direction, String[] properties) {
    QuerySortOrderBuilder builder = new QuerySortOrderBuilder();
    for (String property : properties) {
      if (direction.equals(DirectionEnum.ASC)) {
        builder.thenAsc(property);
      } else {
        builder.thenDesc(property);
      }
    }
    defaultSortOrder = builder.build();
  }

  @Override
  protected List<QuerySortOrder> getDefaultSortOrders() {
    return defaultSortOrder;
  }

  public Consumer<Page<T>> getPageObserver() {
    return pageObserver;
  }

  public void setPageObserver(Consumer<Page<T>> pageObserver) {
    this.pageObserver = pageObserver;
  }

  @Override
  public Object getId(T item) {
    return item.getId();
  }

  public static class DefaultFilter implements Serializable {

    private String filter;
    private Boolean showInactive;

    public DefaultFilter() {
      this.showInactive = Boolean.FALSE;
    }

    public DefaultFilter(String filter) {
      this(filter, Boolean.FALSE);
    }

    public DefaultFilter(String filter, Boolean showInactive) {
      this.filter = filter;
      this.showInactive = showInactive;
    }

    public static DefaultFilter getEmptyFilter() {
      return new DefaultFilter(null, false);
    }

    public String getFilter() {
      return filter;
    }

    public Boolean isShowInactive() {
      return showInactive;
    }
  }
}
