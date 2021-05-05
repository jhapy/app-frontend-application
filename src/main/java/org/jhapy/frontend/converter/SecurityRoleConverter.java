/*
 * Copyright 2020-2020 the original author or authors from the JHapy project.
 *
 * This file is part of the JHapy project, see https://www.jhapy.org/ for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jhapy.frontend.converter;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.jhapy.dto.domain.security.SecurityKeycloakRole;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 12/06/2020
 */
@Component
public class SecurityRoleConverter extends
    BidirectionalConverter<SecurityKeycloakRole, RoleRepresentation> {

  @Override
  public RoleRepresentation convertTo(SecurityKeycloakRole source,
      Type<RoleRepresentation> destinationType,
      MappingContext mappingContext) {
    RoleRepresentation result = new RoleRepresentation();
    result.setId(source.getId());
    result.setName(source.getName());
    result.setDescription(source.getDescription());

    return result;
  }

  @Override
  public SecurityKeycloakRole convertFrom(RoleRepresentation source,
      Type<SecurityKeycloakRole> destinationType,
      MappingContext mappingContext) {
    SecurityKeycloakRole result = new SecurityKeycloakRole();
    result.setId(source.getId());
    result.setName(source.getName());
    result.setDescription(source.getDescription());

    return result;
  }
}
