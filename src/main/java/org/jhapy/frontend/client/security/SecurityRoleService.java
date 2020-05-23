package org.jhapy.frontend.client.security;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.jhapy.dto.domain.security.SecurityRole;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.DeleteByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.security.securityRole.GetSecurityRoleByNameQuery;
import org.jhapy.dto.utils.Page;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-04-16
 */
@FeignClient(name = "${app.remote-services.authorization-server.name:null}", url = "${app.remote-services.authorization-server.url:}", path = "/uaa/securityRole", fallbackFactory = SecurityRoleServiceFallback.class)
@Primary
public interface SecurityRoleService {

  @PostMapping(value = "/getAllowedLoginRoles")
  ServiceResult<List<SecurityRole>> getAllowedLoginRoles();

  @PostMapping(value = "/getSecurityRoleByName")
  ServiceResult<SecurityRole> getSecurityRoleByName(@RequestBody GetSecurityRoleByNameQuery query);

  @PostMapping(value = "/findAllActive")
  ServiceResult<List<SecurityRole>> findAllActive();

  @PostMapping(value = "/findAnyMatching")
  ServiceResult<Page<SecurityRole>> findAnyMatching(@RequestBody FindAnyMatchingQuery query);

  @PostMapping(value = "/countAnyMatching")
  ServiceResult<Long> countAnyMatching(@RequestBody CountAnyMatchingQuery query);

  @PostMapping(value = "/getById")
  ServiceResult<SecurityRole> getById(@RequestBody GetByStrIdQuery query);

  @PostMapping(value = "/save")
  ServiceResult<SecurityRole> save(@RequestBody SaveQuery<SecurityRole> query);

  @PostMapping(value = "/delete")
  ServiceResult<Void> delete(@RequestBody DeleteByStrIdQuery query);
}
