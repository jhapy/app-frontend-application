package org.jhapy.frontend.dataproviders;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.jhapy.dto.domain.notification.SmsTemplate;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.utils.Page;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.frontend.client.notification.NotificationServices;
import org.jhapy.frontend.dataproviders.utils.PageableDataProvider;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class SmsTemplateStringDataProvider extends
    PageableDataProvider<SmsTemplate, String> implements
    Serializable {


  @Override
  protected Page<SmsTemplate> fetchFromBackEnd(Query<SmsTemplate, String> query,
      Pageable pageable) {

    return NotificationServices.getSmsTemplateService().findAnyMatching(
        new FindAnyMatchingQuery(query.getFilter().orElse(null), true, pageable)).getData();
  }

  @Override
  protected List<QuerySortOrder> getDefaultSortOrders() {
    return Collections.singletonList(new QuerySortOrder("name", SortDirection.ASCENDING));
  }


  @Override
  protected int sizeInBackEnd(Query<SmsTemplate, String> query) {
    return NotificationServices.getSmsTemplateService()
        .countAnyMatching(new CountAnyMatchingQuery(query.getFilter().orElse(null), true))
        .getData().intValue();
  }
}
