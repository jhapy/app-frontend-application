package org.jhapy.frontend.client.reference;

import org.springframework.stereotype.Component;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.domain.reference.SubRegionTrl;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.reference.subRegionTrl.GetSubRegionTrlQuery;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-02
 */
@Component
public class SubRegionTrlServiceFallback implements SubRegionTrlService, HasLogger {

  @Override
  public ServiceResult<SubRegionTrl> getSubRegionTrl(GetSubRegionTrlQuery query) {
    logger().error(getLoggerPrefix("getSubRegionTrl") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<SubRegionTrl> getById(GetByIdQuery query) {
    logger().error(getLoggerPrefix("getById") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<SubRegionTrl> save(SaveQuery<SubRegionTrl> query) {
    logger().error(getLoggerPrefix("save") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<Void> delete(DeleteByIdQuery query) {
    logger().error(getLoggerPrefix("delete") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }
}
