package org.jhapy.frontend.dataproviders;

import java.util.function.Function;
import org.jhapy.dto.domain.audit.AuditLog;
import org.jhapy.dto.serviceQuery.BaseRemoteQuery;
import org.jhapy.dto.serviceQuery.auditLog.CountAuditLogQuery;
import org.jhapy.dto.serviceQuery.auditLog.FindAuditLogQuery;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.utils.Page;
import org.jhapy.dto.utils.Pageable;
import org.jhapy.frontend.dataproviders.AuditLogDataProvider.AuditLogFilter;
import org.jhapy.frontend.utils.AppConst;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-02-14
 */
@SpringComponent
@UIScope
public class AuditLogDataProvider extends
    DefaultDataProvider<AuditLog, AuditLogFilter> implements
    Serializable {
  private Function<FindAuditLogQuery, ServiceResult<Page<AuditLog>>> findHandler;
  private Function<CountAuditLogQuery, ServiceResult<Long>> countHandler;

  public AuditLogDataProvider(Function<FindAuditLogQuery, ServiceResult<Page<AuditLog>>> findHandler,
      Function<CountAuditLogQuery,ServiceResult<Long>> countHandler ) {
    super(AppConst.DEFAULT_SORT_DIRECTION,
        AppConst.DEFAULT_SORT_FIELDS);
    this.findHandler = findHandler;
    this.countHandler = countHandler;
  }

  @Override
  protected Page<AuditLog> fetchFromBackEnd(
      Query<AuditLog, AuditLogFilter> query,
      Pageable pageable) {
    AuditLogFilter filter = query.getFilter().orElse(null);
    Page<AuditLog> page = findHandler.apply(new FindAuditLogQuery(filter.getClassName(), filter.getRecordId(), pageable)).getData();
    if (getPageObserver() != null) {
      getPageObserver().accept(page);
    }
    return page;
  }


  @Override
  protected int sizeInBackEnd(Query<AuditLog, AuditLogFilter> query) {
    AuditLogFilter filter = query.getFilter().orElse(null);

    ServiceResult<Long> _count =  countHandler.apply(new CountAuditLogQuery(filter.getClassName(), filter.getRecordId()));

    if ( _count.getIsSuccess() && _count.getData() != null )
      return _count.getData().intValue();
    else
      return 0;
  }

  @Data
  @AllArgsConstructor
  public static class AuditLogFilter extends DefaultFilter {
    private String className = null;
    private Long recordId = null;
  }
}
