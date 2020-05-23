package org.jhapy.frontend.client.reference;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.jhapy.dto.domain.reference.SubRegion;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.DeleteByIdQuery;
import org.jhapy.dto.serviceQuery.generic.GetByIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.reference.subRegion.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.reference.subRegion.FindAnyMatchingQuery;
import org.jhapy.dto.utils.Page;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-27
 */
@FeignClient(name = "${app.remote-services.backend-server.name:null}", url = "${app.remote-services.backend-server.url:}", path = "/subRegionService", fallback = SubRegionServiceFallback.class)
@Primary
public interface SubRegionService {

  @PostMapping(value = "/findAll")
  ServiceResult<List<SubRegion>> findAll();

  @PostMapping(value = "/findAnyMatching")
  ServiceResult<Page<SubRegion>> findAnyMatching(@RequestBody FindAnyMatchingQuery query);

  @PostMapping(value = "/countAnyMatching")
  ServiceResult<Long> countAnyMatching(@RequestBody CountAnyMatchingQuery query);

  @PostMapping(value = "/getById")
  ServiceResult<SubRegion> getById(@RequestBody GetByIdQuery query);

  @PostMapping(value = "/save")
  ServiceResult<SubRegion> save(@RequestBody SaveQuery<SubRegion> query);

  @PostMapping(value = "/delete")
  ServiceResult<Void> delete(@RequestBody DeleteByIdQuery query);
}
