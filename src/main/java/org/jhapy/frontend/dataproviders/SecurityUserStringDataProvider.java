package org.jhapy.frontend.dataproviders;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.jhapy.dto.domain.security.SecurityUser;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.utils.Page;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.frontend.client.security.SecurityServices;
import org.jhapy.frontend.dataproviders.utils.PageableDataProvider;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class SecurityUserStringDataProvider extends
    PageableDataProvider<SecurityUser, String> implements
    Serializable {


  @Override
  protected Page<SecurityUser> fetchFromBackEnd(Query<SecurityUser, String> query,
      Pageable pageable) {

    return SecurityServices.getSecurityUserService().findAnyMatching(
        new FindAnyMatchingQuery(query.getFilter().orElse(null), true, pageable)).getData();
  }

  @Override
  protected List<QuerySortOrder> getDefaultSortOrders() {
    return Collections.singletonList(new QuerySortOrder("username", SortDirection.ASCENDING));
  }


  @Override
  protected int sizeInBackEnd(Query<SecurityUser, String> query) {
    return SecurityServices.getSecurityUserService()
        .countAnyMatching(new CountAnyMatchingQuery(query.getFilter().orElse(null), true))
        .getData().intValue();
  }
}
