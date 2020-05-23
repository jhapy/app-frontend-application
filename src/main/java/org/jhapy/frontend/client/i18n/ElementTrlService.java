package org.jhapy.frontend.client.i18n;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.jhapy.dto.domain.i18n.ElementTrl;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.i18n.FindByIso3Query;
import org.jhapy.dto.serviceQuery.i18n.GetByNameAndIso3Query;
import org.jhapy.dto.serviceQuery.i18n.elementTrl.CountByElementQuery;
import org.jhapy.dto.serviceQuery.i18n.elementTrl.FindByElementQuery;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-21
 */
@FeignClient(name = "${app.remote-services.i18n-server.name:null}", url = "${app.remote-services.i18n-server.url:}", path = "/elementTrlService", fallback = ElementTrlServiceFallback.class)
@Primary
public interface ElementTrlService {

  @PostMapping(value = "/findByElement")
  ServiceResult<List<ElementTrl>> findByElement(@RequestBody FindByElementQuery query);

  @PostMapping(value = "/countByElement")
  ServiceResult<Long> countByElement(@RequestBody CountByElementQuery query);

  @PostMapping(value = "/findByIso3")
  ServiceResult<List<ElementTrl>> findByIso3(@RequestBody FindByIso3Query query);

  @PostMapping(value = "/getByNameAndIso3")
  ServiceResult<ElementTrl> getByNameAndIso3(@RequestBody GetByNameAndIso3Query query);


  @PostMapping(value = "/getById")
  ServiceResult<ElementTrl> getById(@RequestBody GetByIdQuery query);

  @PostMapping(value = "/save")
  ServiceResult<ElementTrl> save(@RequestBody SaveQuery<ElementTrl> query);

  @PostMapping(value = "/delete")
  ServiceResult<Void> delete(@RequestBody DeleteByIdQuery query);
}
