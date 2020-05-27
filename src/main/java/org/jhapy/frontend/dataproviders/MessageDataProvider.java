package org.jhapy.frontend.dataproviders;

import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.jhapy.dto.domain.i18n.Message;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.utils.Page;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.frontend.client.i18n.I18NServices;
import org.jhapy.frontend.dataproviders.DefaultDataProvider.DefaultFilter;
import org.jhapy.frontend.utils.AppConst;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class MessageDataProvider extends DefaultDataProvider<Message, DefaultFilter> implements
    Serializable {

  @Autowired
  public MessageDataProvider() {
    super(AppConst.DEFAULT_SORT_DIRECTION,
        AppConst.DEFAULT_SORT_FIELDS);
  }

  @Override
  protected Page<Message> fetchFromBackEnd(Query<Message, DefaultFilter> query,
      Pageable pageable) {
    DefaultFilter filter = query.getFilter().orElse(DefaultFilter.getEmptyFilter());
    Page<Message> page = I18NServices.getMessageService()
        .findAnyMatching(new FindAnyMatchingQuery(filter.getFilter(),
            filter.isShowInactive(), pageable)).getData();
    if (getPageObserver() != null) {
      getPageObserver().accept(page);
    }
    return page;
  }


  @Override
  protected int sizeInBackEnd(Query<Message, DefaultFilter> query) {
    DefaultFilter filter = query.getFilter().orElse(DefaultFilter.getEmptyFilter());
    return I18NServices.getMessageService()
        .countAnyMatching(new CountAnyMatchingQuery(filter.getFilter(), filter.isShowInactive()))
        .getData().intValue();
  }
}