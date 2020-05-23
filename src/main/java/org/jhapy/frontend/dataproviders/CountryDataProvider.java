package org.jhapy.frontend.dataproviders;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.jhapy.dto.domain.reference.Country;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.utils.AppContext;
import org.jhapy.dto.utils.Page;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.frontend.client.reference.ReferenceServices;
import org.jhapy.frontend.dataproviders.DefaultDataProvider.DefaultFilter;
import org.jhapy.frontend.utils.AppConst;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class CountryDataProvider extends DefaultDataProvider<Country, DefaultFilter> implements
    Serializable {

  @Autowired
  public CountryDataProvider() {
    super(AppConst.DEFAULT_SORT_DIRECTION,
        new String[]{"name." + AppContext.getInstance().getCurrentIso3Language() + ".value"});
  }

  @Override
  protected Page<Country> fetchFromBackEnd(Query<Country, DefaultFilter> query,
      Pageable pageable) {
    DefaultFilter filter = query.getFilter().orElse(DefaultFilter.getEmptyFilter());
    Page<Country> page = ReferenceServices.getCountryService()
        .findAnyMatching(
            new FindAnyMatchingQuery(filter.getFilter(), null,
                pageable)).getData();
    if (getPageObserver() != null) {
      getPageObserver().accept(page);
    }
    return page;
  }


  @Override
  protected int sizeInBackEnd(Query<Country, DefaultFilter> query) {
    DefaultFilter filter = query.getFilter().orElse(DefaultFilter.getEmptyFilter());
    return ReferenceServices.getCountryService()
        .countAnyMatching(
            new CountAnyMatchingQuery(filter.getFilter(), null))
        .getData().intValue();
  }
}
