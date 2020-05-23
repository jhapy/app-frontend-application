package org.jhapy.frontend.client.audit;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.jhapy.dto.domain.audit.Session;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.utils.Page;
import org.jhapy.frontend.client.notification.MailServiceFallback;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-16
 */
@FeignClient(name = "${app.remote-services.audit-server.name:null}", url = "${app.remote-services.audit-server.url:}", path = "/sessionService", fallbackFactory = MailServiceFallback.class)
@Primary
public interface SessionService {

  @PostMapping(value = "/findAnyMatching")
  ServiceResult<Page<Session>> findAnyMatching(@RequestBody FindAnyMatchingQuery query);

  @PostMapping(value = "/countAnyMatching")
  ServiceResult<Long> countAnyMatching(@RequestBody CountAnyMatchingQuery query);

  @PostMapping(value = "/getById")
  ServiceResult<Session> getById(@RequestBody GetByStrIdQuery query);
}
