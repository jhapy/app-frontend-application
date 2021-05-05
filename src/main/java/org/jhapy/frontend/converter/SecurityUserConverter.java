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

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.jhapy.dto.domain.security.SecurityKeycloakUser;
import org.jhapy.dto.utils.StoredFile;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 12/06/2020
 */
@Component
public class SecurityUserConverter extends
    BidirectionalConverter<SecurityKeycloakUser, UserRepresentation> {

  @Override
  public UserRepresentation convertTo(SecurityKeycloakUser source,
      Type<UserRepresentation> destinationType,
      MappingContext mappingContext) {
    UserRepresentation result = new UserRepresentation();
    result.setId(source.getId());
    result.setEmail(source.getEmail());
    result.setFirstName(source.getFirstName());
    result.setLastName(source.getLastName());
    result.setUsername(source.getUsername());
    result.setEmailVerified(source.getEmailVerified());
    result.setEnabled(source.getIsActivated());
    source.getAttributes().forEach(
        (s, o) -> result.getAttributes().put(s, Collections.singletonList(o.toString())));
    //result.getAttributes().put("picture", Collections.singletonList( source.getPicture() ));
    return result;
  }

  @Override
  public SecurityKeycloakUser convertFrom(UserRepresentation source,
      Type<SecurityKeycloakUser> destinationType,
      MappingContext mappingContext) {
    SecurityKeycloakUser result = new SecurityKeycloakUser();
    result.setId(source.getId());
    result.setEmail(source.getEmail());
    result.setFirstName(source.getFirstName());
    result.setLastName(source.getLastName());
    result.setUsername(source.getUsername());
    if (source.getAttributes() != null && source.getAttributes().get("title") != null) {
      result.setTitle(source.getAttributes().get("title").get(0));
    }
    if (source.getAttributes() != null && source.getAttributes().get("phone") != null) {
      result.setMobileNumber(source.getAttributes().get("phone").get(0));
    }
    if (source.getAttributes() != null && source.getAttributes().get("locale") != null) {
      result.setLocale(source.getAttributes().get("locale").get(0));
    }
    result.setIsLocal(source.getFederationLink() != null || (source.getSocialLinks() != null
        && source.getSocialLinks().size() > 0));
    if (source.getAttributes() != null && source.getAttributes().get("picture") != null) {
      String pictureStr = source.getAttributes().get("picture").get(0);
      if (pictureStr.startsWith("http")) {
        result.getAttributes().put("picture", pictureStr);
      } else {
        byte[] pictureDecoded = java.util.Base64.getDecoder().decode(pictureStr);
        try {
          MimeType mimeType = TikaConfig.getDefaultConfig().getMimeRepository()
              .forName((new Tika()).detect(pictureDecoded));
          String fileExt = mimeType.getExtension();

          StoredFile storedFile = new StoredFile();
          storedFile.setContent(pictureDecoded);
          storedFile.setOrginalContent(pictureDecoded);
          storedFile.setId(UUID.randomUUID().toString());
          storedFile.setFilename(storedFile.getId() + fileExt);
          storedFile.setFilesize((long) pictureDecoded.length);
          storedFile.setMimeType(mimeType.toString());
          result.setPicture(storedFile);
        } catch (MimeTypeException e) {
          e.printStackTrace();
        }
      }
    }
    Objects.requireNonNull(source.getAttributes())
        .keySet().stream()
        .filter(s -> !s.equalsIgnoreCase("title") && !s.equalsIgnoreCase("phone") &&
            !s.equalsIgnoreCase("locale") && !s.equalsIgnoreCase("picture"))
        .forEach(s -> result.getAttributes().put(s, source.getAttributes().get(s).get(0)));

    //result.setPicture(source.getAttributes().get("picture"));
    result.setEmailVerified(source.isEmailVerified());
    result.setIsActivated(source.isEnabled());
    return result;
  }
}
