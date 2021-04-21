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

package org.jhapy.frontend.client.security.keycloak;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.config.AppProperties;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.commons.utils.OrikaBeanMapper;
import org.jhapy.dto.domain.security.SecurityKeycloakGroup;
import org.jhapy.dto.domain.security.SecurityKeycloakRole;
import org.jhapy.dto.domain.security.SecurityKeycloakUser;
import org.jhapy.dto.keycloak.MemoryInfo;
import org.jhapy.dto.keycloak.SystemInfo;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.CountAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.DeleteByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.FindAnyMatchingQuery;
import org.jhapy.dto.serviceQuery.generic.GetByNameQuery;
import org.jhapy.dto.serviceQuery.generic.GetByStrIdQuery;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.security.securityRole.GetSecurityRoleByNameQuery;
import org.jhapy.dto.serviceQuery.security.securityUser.GetSecurityUserByUsernameQuery;
import org.jhapy.dto.utils.Page;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.info.MemoryInfoRepresentation;
import org.keycloak.representations.info.SystemInfoRepresentation;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 12/06/2020
 */
@Component
public class KeycloakClient implements HasLogger {

  protected final AppProperties appProperties;

  protected final OrikaBeanMapper orikaBeanMapper;

  public KeycloakClient(AppProperties appProperties,
      OrikaBeanMapper orikaBeanMapper) {
    this.appProperties = appProperties;
    this.orikaBeanMapper = orikaBeanMapper;
  }

  public Keycloak getKeycloakInstance() {
    return Keycloak.getInstance(appProperties.getKeycloakAdmin().getServerAuthUrl(),
        appProperties.getKeycloakAdmin().getMasterRealm(),
        appProperties.getKeycloakAdmin().getUsername(),
        appProperties.getKeycloakAdmin().getPassword(),
        appProperties.getKeycloakAdmin().getClientId());
  }

  public RealmResource getKeycloakRealmInstance() {
    return getKeycloakInstance().realm(appProperties.getKeycloakAdmin().getApplicationRealm());
  }

  @CacheEvict(cacheNames = {"allUsers", "userByName", "userById", "findUsers",
      "countUsers"}, allEntries = true)
  public void cleanUserCache() {
  }

  @Cacheable("userByName")
  public ServiceResult<SecurityKeycloakUser> getUserByUsername(
      GetSecurityUserByUsernameQuery query) {
    String loggerPrefix = getLoggerPrefix("getUserByUsername");

    List<UserRepresentation> users = getKeycloakRealmInstance().users().search(query.getUsername());

    if (users.size() == 0) {
      logger().warn(loggerPrefix + "User not found (username=" + query.getUsername() + ")");
      return new ServiceResult<>(false, "User not found", null);
    } else {
      return getUserById(new GetByStrIdQuery(users.get(0).getId()));
    }
  }

  @Cacheable("userById")
  public ServiceResult<SecurityKeycloakUser> getUserById(GetByStrIdQuery query) {
    String loggerPrefix = getLoggerPrefix("getUser");

    UserResource userResource = getKeycloakRealmInstance().users().get(query.getId());
    if (userResource != null) {
      List<GroupRepresentation> groups = userResource.groups();
      List<RoleRepresentation> roles = userResource.roles().realmLevel().listAll().stream()
          .filter(roleRepresentation -> roleRepresentation.getName().startsWith("ROLE")).collect(
              Collectors.toList());
      List<RoleRepresentation> effectiveRoles = userResource.roles().realmLevel().listEffective();

      SecurityKeycloakUser securityUser = orikaBeanMapper
          .map(userResource.toRepresentation(), SecurityKeycloakUser.class);
      securityUser.setGroups(orikaBeanMapper.mapAsList(groups, SecurityKeycloakGroup.class));
      securityUser.setRoles(orikaBeanMapper.mapAsList(roles, SecurityKeycloakRole.class));
      securityUser
          .setEffectiveRoles(orikaBeanMapper.mapAsList(effectiveRoles, SecurityKeycloakRole.class));

      return new ServiceResult(securityUser);
    } else {
      logger().warn(loggerPrefix + "User not found (id=" + query.getId() + ")");
      return new ServiceResult<>(false, "User not found", null);
    }
  }

  public ServiceResult<Boolean> userExists(String username) {
    return new ServiceResult<>(true, null,
        !getKeycloakRealmInstance().users().search(username, true).isEmpty());
  }

  public ServiceResult<Boolean> isValidated(String username) {
    List<UserRepresentation> users = getKeycloakRealmInstance().users().search(username, true);
    if (users.isEmpty()) {
      return new ServiceResult<>(true, null, Boolean.FALSE);
    } else {
      return new ServiceResult<>(true, null, users.get(0).isEmailVerified());
    }
  }

  public ServiceResult<String> registerUser(String username) {
    String loggerPrefix = getLoggerPrefix("registerUser", username);
    List<UserRepresentation> existingUser = getKeycloakRealmInstance().users()
        .search(username, true);
    if (!existingUser.isEmpty()) {
      return new ServiceResult<>(true, null, existingUser.get(0).getUsername());
    } else {
      UserRepresentation userRepresentation = new UserRepresentation();
      userRepresentation.setUsername(username);

      userRepresentation.setEmail(username);
      userRepresentation.setEmailVerified(false);
      Response response = getKeycloakRealmInstance().users().create(userRepresentation);

      if (response.getStatus() == 201) {
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        response.close();
        return new ServiceResult<>(true, null, username);
      } else {
        ErrorRepresentation error = response.readEntity(ErrorRepresentation.class);
        response.close();
        logger().warn(loggerPrefix + "User '" + username + "' not created : "
            + error.getErrorMessage());
        return new ServiceResult<>(false, "User not created : " + error.getErrorMessage(), null);
      }
    }
  }

  @CacheEvict(cacheNames = {"allUsers", "userByName", "userById", "findUsers",
      "countUsers"}, allEntries = true)
  public ServiceResult<SecurityKeycloakUser> saveUser(SaveQuery<SecurityKeycloakUser> query) {
    String loggerPrefix = getLoggerPrefix("saveUser");

    if (query.getEntity().getId() != null) {
      UserResource userResource = getKeycloakRealmInstance().users().get(query.getEntity().getId());
      SecurityKeycloakUser user = query.getEntity();
      UserRepresentation userRepresentation = userResource.toRepresentation();
      userRepresentation.setEmail(user.getEmail());
      userRepresentation.setFirstName(user.getFirstName());
      userRepresentation.setLastName(user.getLastName());
      userRepresentation.setUsername(user.getUsername());
      userRepresentation.setEmailVerified(user.getEmailVerified());
      userRepresentation.setEnabled(user.getIsActivated());
      if (userRepresentation.getAttributes() == null) {
        userRepresentation.setAttributes(new HashMap<>());
      }
      user.getAttributes().forEach((s, o) -> {
        userRepresentation.getAttributes().put(s, Collections.singletonList(o.toString()));
      });

      userRepresentation.getAttributes().put("title", Collections.singletonList(user.getTitle()));
      if (user.getPicture() != null) {
        //    logger().debug(loggerPrefix+"Initial Picture : " + userRepresentation.getAttributes().get("picture").get(0) );
        userRepresentation.getAttributes().put("picture", Collections.singletonList(
            new String(java.util.Base64.getEncoder().encode(user.getPicture().getContent()))));
        //    logger().debug(loggerPrefix+"New Picture : " + userRepresentation.getAttributes().get("picture").get(0) );
      }
      if (user.getMobileNumber() != null) {
        userRepresentation.getAttributes()
            .put("phone", Collections.singletonList(user.getMobileNumber()));
      }
      userResource.update(userRepresentation);

      if (StringUtils.isNoneBlank(query.getEntity().getPassword())) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(query.getEntity().getPassword());
        userResource.resetPassword(passwordCred);
      }

      if (userResource.roles() != null && userResource.roles().realmLevel() != null) {
        List<RoleRepresentation> userRoles = userResource.roles().realmLevel().listAll();
        if (userRoles != null && userRoles.size() > 0) {
          userResource.roles().realmLevel().remove(userRoles.stream()
              .filter(roleRepresentation -> roleRepresentation.getName().startsWith("ROLE"))
              .collect(
                  Collectors.toList()));
        }
      }
      if (query.getEntity().getRoles() != null && query.getEntity().getRoles().size() > 0) {
        userResource.roles().realmLevel()
            .add(orikaBeanMapper.mapAsList(query.getEntity().getRoles(), RoleRepresentation.class));
      }

      userResource.groups()
          .forEach(groupRepresentation -> userResource.leaveGroup(groupRepresentation.getId()));
      query.getEntity().getGroups().forEach(group -> userResource.joinGroup(group.getId()));

      return getUserById(new GetByStrIdQuery(query.getEntity().getId()));
    } else {
      Response response = getKeycloakRealmInstance().users()
          .create(orikaBeanMapper.map(query.getEntity(), UserRepresentation.class));
      if (response.getStatus() == 201) {
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        response.close();
        if (StringUtils.isNoneBlank(query.getEntity().getPassword())) {
          CredentialRepresentation passwordCred = new CredentialRepresentation();
          passwordCred.setTemporary(false);
          passwordCred.setType(CredentialRepresentation.PASSWORD);
          passwordCred.setValue(query.getEntity().getPassword());
          getKeycloakRealmInstance().users().get(userId).resetPassword(passwordCred);
        }
        return getUserById(new GetByStrIdQuery(userId));
      } else {
        ErrorRepresentation error = response.readEntity(ErrorRepresentation.class);
        response.close();
        logger().warn(loggerPrefix + "User '" + query.getEntity().getUsername() + "' not created : "
            + error.getErrorMessage());
        return new ServiceResult<>(false, "User not created : " + error.getErrorMessage(), null);
      }
    }
  }

  @CacheEvict(cacheNames = {"allUsers", "userByName", "userById", "findUsers",
      "countUsers"}, allEntries = true)
  public ServiceResult<Void> deleteUser(DeleteByStrIdQuery query) {
    String loggerPrefix = getLoggerPrefix("deleteUser");

    UserResource existing = getKeycloakRealmInstance().users().get(query.getId());

    if (existing == null) {
      logger().warn(loggerPrefix + "User not found (id=" + query.getId() + ")");
      return new ServiceResult<>(false, "User does not exists", null);
    }

    Response response = getKeycloakRealmInstance().users().delete(query.getId());

    if (response.getStatus() != 201) {
      response.close();
      ErrorRepresentation error = response.readEntity(ErrorRepresentation.class);
      logger().warn(loggerPrefix + "User not deleted (id=" + query.getId() + ") : " + error
          .getErrorMessage());
      return new ServiceResult(false, "User not deleted: " + error.getErrorMessage(), null);
    } else {
      response.close();
      return new ServiceResult<>();
    }
  }

  @Cacheable("findUsers")
  public ServiceResult<Page<SecurityKeycloakUser>> findUsers(FindAnyMatchingQuery query) {
    int totalElements = getKeycloakRealmInstance().users().count(query.getFilter());
    int start =
        (query.getPageable().getPage() * query.getPageable().getSize()) + query.getPageable()
            .getOffset();
    int end = Math.min(start + query.getPageable().getSize(), totalElements);

    Page<SecurityKeycloakUser> result = new Page<>();

    List<UserRepresentation> users = getKeycloakRealmInstance().users()
        .search(query.getFilter(), start, end);
    result.setContent(users.stream().map(userRepresentation -> {
      UserResource userResource = getKeycloakRealmInstance().users()
          .get(userRepresentation.getId());
      List<GroupRepresentation> groups = userResource.groups();
      List<RoleRepresentation> roles = userResource.roles().realmLevel().listAll().stream()
          .filter(roleRepresentation -> roleRepresentation.getName().startsWith("ROLE")).collect(
              Collectors.toList());
      List<RoleRepresentation> effectiveRoles = userResource.roles().realmLevel().listEffective();
      SecurityKeycloakUser securityUser = orikaBeanMapper
          .map(userResource.toRepresentation(), SecurityKeycloakUser.class);
      securityUser.setGroups(orikaBeanMapper.mapAsList(groups, SecurityKeycloakGroup.class));
      securityUser.setRoles(orikaBeanMapper.mapAsList(roles, SecurityKeycloakRole.class));
      securityUser
          .setEffectiveRoles(orikaBeanMapper.mapAsList(effectiveRoles, SecurityKeycloakRole.class));

      return securityUser;
    }).collect(Collectors.toList()));

    result.setSize(result.getContent().size());
    result.setTotalElements((long) totalElements);
    result.setTotalPages((totalElements / query.getPageable().getSize()) + 1);
    result.setNumber(query.getPageable().getPage());
    result.setNumberOfElements(result.getContent().size());

    return new ServiceResult<>(result);
  }

  @Cacheable("countUsers")
  public ServiceResult<Long> countUsers(CountAnyMatchingQuery query) {
    return new ServiceResult<>(
        getKeycloakRealmInstance().users().count(query.getFilter()).longValue());
  }

  public void impressionate(String userId) {
    final HttpHeaders httpHeaders = new HttpHeaders() {{
      set("Authorization", getKeycloakInstance().tokenManager().getAccessToken().getToken());
      setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }};

    ResponseEntity<String> configprops = (new RestTemplate()).exchange(URI.create(
        appProperties.getKeycloakAdmin().getServerUrl() + "/admin/realms/" + appProperties
            .getKeycloakAdmin().getApplicationRealm() + "/users/" + userId + "/impersonation"),
        HttpMethod.POST,
        new HttpEntity<>(httpHeaders), String.class);
    String result = configprops.getBody();

  }

  @CacheEvict(cacheNames = {"allRoles", "roleByName", "roleById", "findRoles",
      "countRoles"}, allEntries = true)
  public void cleanRoleCache() {
  }

  @Cacheable("allRoles")
  public ServiceResult<List<SecurityKeycloakRole>> getRoles() {
    return new ServiceResult(orikaBeanMapper.mapAsList(
        getKeycloakRealmInstance().roles().list().stream()
            .filter(roleRepresentation -> roleRepresentation.getName().startsWith("ROLE")).collect(
            Collectors.toList()), SecurityKeycloakRole.class));
  }

  @Cacheable("roleByName")
  public ServiceResult<SecurityKeycloakRole> getRoleByName(GetSecurityRoleByNameQuery query) {
    String loggerPrefix = getLoggerPrefix("getRoleByName");

    Optional<RoleRepresentation> _roleRepresentation = getKeycloakRealmInstance().roles().list()
        .stream().filter(roleRepresentation -> roleRepresentation.getName().equals(query.getName()))
        .findFirst();
    if (_roleRepresentation.isEmpty()) {
      logger().warn(loggerPrefix + "Role not found (name=" + query.getName() + ")");
      return new ServiceResult<>(false, "Role not found", null);
    } else {
      return new ServiceResult<>(
          orikaBeanMapper.map(_roleRepresentation.get(), SecurityKeycloakRole.class));
    }
  }

  @Cacheable("roleById")
  public ServiceResult<SecurityKeycloakRole> getRoleById(GetByStrIdQuery query) {
    String loggerPrefix = getLoggerPrefix("getRole");

    RoleRepresentation roleRepresentation = getKeycloakRealmInstance().rolesById()
        .getRole(query.getId());
    if (roleRepresentation == null) {
      logger().warn(loggerPrefix + "Role not found (id=" + query.getId() + ")");
      return new ServiceResult<>(false, "Role not found", null);
    } else {
      return new ServiceResult<>(
          orikaBeanMapper.map(roleRepresentation, SecurityKeycloakRole.class));
    }
  }

  @Cacheable("findRoles")
  public ServiceResult<Page<SecurityKeycloakRole>> findRoles(FindAnyMatchingQuery query) {
    int totalElements = getKeycloakRealmInstance().roles().list(query.getFilter(), true).size();
    int start = query.getPageable().getOffset();
    int end = Math.min(start + query.getPageable().getSize(), totalElements);

    Page<SecurityKeycloakRole> result = new Page<>();

    List<RoleRepresentation> roles = getKeycloakRealmInstance().roles()
        .list(query.getFilter(), start, end, true);

    result.setContent(orikaBeanMapper.mapAsList(roles, SecurityKeycloakRole.class));

    result.setSize(result.getContent().size());
    result.setTotalElements((long) totalElements);
    result.setTotalPages((totalElements / query.getPageable().getSize()) + 1);
    result.setNumber(query.getPageable().getPage());
    result.setNumberOfElements(result.getContent().size());
    result.setFirst(start == 0);
    result.setLast(end >= totalElements);
    result.setPageable(query.getPageable());
    return new ServiceResult<>(result);
  }

  @Cacheable("countRoles")
  public ServiceResult<Long> countRoles(CountAnyMatchingQuery query) {
    return new ServiceResult<>(
        (long) getKeycloakRealmInstance().roles().list(query.getFilter(), true).size());
  }

  @CacheEvict(cacheNames = {"allRoles", "roleByName", "roleById", "findRoles",
      "countRoles"}, allEntries = true)
  public ServiceResult<SecurityKeycloakRole> saveRole(SaveQuery<SecurityKeycloakRole> query) {
    if (query.getEntity().getId() != null) {
      RoleResource roleResource = getKeycloakRealmInstance().roles()
          .get(query.getEntity().getName());
      roleResource.toRepresentation();
      RoleRepresentation roleRepresentation = orikaBeanMapper
          .map(query.getEntity(), RoleRepresentation.class);
      roleResource.update(roleRepresentation);

      return getRoleById(new GetByStrIdQuery(query.getEntity().getId()));
    } else {
      RoleRepresentation roleRepresentation = orikaBeanMapper
          .map(query.getEntity(), RoleRepresentation.class);
      getKeycloakRealmInstance().roles().create(roleRepresentation);

      return getRoleByName(new GetSecurityRoleByNameQuery(query.getEntity().getName()));
    }
  }

  @CacheEvict(cacheNames = {"allRoles", "roleByName", "roleById", "findRoles",
      "countRoles"}, allEntries = true)
  public ServiceResult<Void> deleteRole(DeleteByStrIdQuery query) {
    String loggerPrefix = getLoggerPrefix("deleteRole");

    List<RoleRepresentation> existing = getKeycloakRealmInstance().roles().list().stream()
        .filter(roleRepresentation -> roleRepresentation.getId().equals(query.getId())).collect(
            Collectors.toList());

    if (existing.size() == 0) {
      logger().warn(loggerPrefix + "Role not found (id=" + query.getId() + ")");
      return new ServiceResult<>(false, "Role does not exists", null);
    }
    existing.forEach(roleRepresentation -> getKeycloakRealmInstance().roles()
        .deleteRole(roleRepresentation.getName()));

    if (getKeycloakRealmInstance().roles().list().stream()
        .anyMatch(roleRepresentation -> roleRepresentation.getId().equals(query.getId()))) {
      logger().warn(loggerPrefix + "Role not deleted (id=" + query.getId() + ")");
      return new ServiceResult(false, "Role not deleted", null);
    } else {
      return new ServiceResult<>();
    }
  }

  @CacheEvict(cacheNames = {"allGroups", "groupByName", "groupById", "findGroups",
      "countGroups"}, allEntries = true)
  public void cleanGroupCache() {
  }

  @Cacheable("allGroups")
  public ServiceResult<List<SecurityKeycloakGroup>> getGroups() {
    return new ServiceResult(orikaBeanMapper
        .mapAsList(getKeycloakRealmInstance().groups().groups(), SecurityKeycloakGroup.class));
  }

  @Cacheable("groupByName")
  public ServiceResult<SecurityKeycloakGroup> getGroupByName(GetByNameQuery query) {
    String loggerPrefix = getLoggerPrefix("getGroupByName");

    Optional<GroupRepresentation> _groupRepresentations = getKeycloakRealmInstance().groups()
        .groups()
        .stream()
        .filter(groupRepresentation -> groupRepresentation.getName().equals(query.getName()))
        .findFirst();
    if (_groupRepresentations.isEmpty()) {
      logger().warn(loggerPrefix + "Group not found (name=" + query.getName() + ")");
      return new ServiceResult<>(false, "Group not found", null);
    } else {
      return new ServiceResult<>(
          orikaBeanMapper.map(_groupRepresentations.get(), SecurityKeycloakGroup.class));
    }
  }

  @Cacheable("groupById")
  public ServiceResult<SecurityKeycloakGroup> getGroupById(GetByStrIdQuery query) {
    String loggerPrefix = getLoggerPrefix("getGroupById");

    GroupResource groupResource = getKeycloakRealmInstance().groups().group(query.getId());
    if (groupResource != null) {
      List<RoleRepresentation> roles = groupResource.roles().realmLevel().listAll().stream()
          .filter(roleRepresentation -> roleRepresentation.getName().startsWith("ROLE")).collect(
              Collectors.toList());
      List<RoleRepresentation> effectiveRoles = groupResource.roles().realmLevel().listEffective();
      List<UserRepresentation> members = groupResource.members();
      SecurityKeycloakGroup securityGroup = orikaBeanMapper
          .map(groupResource.toRepresentation(), SecurityKeycloakGroup.class);
      securityGroup.setRoles(orikaBeanMapper.mapAsList(roles, SecurityKeycloakRole.class));
      securityGroup
          .setEffectiveRoles(orikaBeanMapper.mapAsList(effectiveRoles, SecurityKeycloakRole.class));
      securityGroup.setMembers(orikaBeanMapper.mapAsList(members, SecurityKeycloakUser.class));
      return new ServiceResult(securityGroup);
    } else {
      logger().warn(loggerPrefix + "Group not found (id=" + query.getId() + ")");
      return new ServiceResult<>(false, "Group not found", null);
    }
  }

  @Cacheable("findGroups")
  public ServiceResult<Page<SecurityKeycloakGroup>> findGroups(FindAnyMatchingQuery query) {
    int totalElements = getKeycloakRealmInstance().groups().count(query.getFilter()).get("count")
        .intValue();
    int start =
        (query.getPageable().getPage() * query.getPageable().getSize()) + query.getPageable()
            .getOffset();
    int end = Math.min(start + query.getPageable().getSize(), totalElements);

    Page<SecurityKeycloakGroup> result = new Page<>();

    List<GroupRepresentation> groups = getKeycloakRealmInstance().groups()
        .groups(query.getFilter(), start, end, true);

    result.setContent(groups.stream().map(groupRepresentation -> {
      GroupResource groupResource = getKeycloakRealmInstance().groups()
          .group(groupRepresentation.getId());
      List<RoleRepresentation> roles = groupResource.roles().realmLevel().listAll().stream()
          .filter(roleRepresentation -> roleRepresentation.getName().startsWith("ROLE")).collect(
              Collectors.toList());
      List<RoleRepresentation> effectiveRoles = groupResource.roles().realmLevel().listAll();
      List<UserRepresentation> members = groupResource.members();

      SecurityKeycloakGroup securityGroup = orikaBeanMapper
          .map(groupRepresentation, SecurityKeycloakGroup.class);
      securityGroup.setRoles(orikaBeanMapper.mapAsList(roles, SecurityKeycloakRole.class));
      securityGroup
          .setEffectiveRoles(orikaBeanMapper.mapAsList(effectiveRoles, SecurityKeycloakRole.class));
      securityGroup.setMembers(orikaBeanMapper.mapAsList(members, SecurityKeycloakUser.class));

      return securityGroup;
    }).collect(Collectors.toList()));

    result.setSize(result.getContent().size());
    result.setTotalElements((long) totalElements);
    result.setTotalPages((totalElements / query.getPageable().getSize()) + 1);
    result.setNumber(query.getPageable().getPage());
    result.setNumberOfElements(result.getContent().size());

    return new ServiceResult<>(result);
  }

  @Cacheable("countGroups")
  public ServiceResult<Long> countGroups(CountAnyMatchingQuery query) {
    return new ServiceResult<>(
        getKeycloakRealmInstance().groups().count(query.getFilter()).get("count"));
  }

  @CacheEvict(cacheNames = {"allGroups", "groupByName", "groupById", "findGroups",
      "countGroups"}, allEntries = true)
  public ServiceResult<SecurityKeycloakGroup> saveGroup(SaveQuery<SecurityKeycloakGroup> query) {
    String loggerPrefix = getLoggerPrefix("saveGroup");
    if (query.getEntity().getId() != null) {
      GroupResource groupResource = getKeycloakRealmInstance().groups()
          .group(query.getEntity().getId());
      GroupRepresentation groupRepresentation = orikaBeanMapper
          .map(query.getEntity(), GroupRepresentation.class);
      groupResource.update(groupRepresentation);

      if (groupResource.roles() != null && groupResource.roles().realmLevel() != null) {
        List<RoleRepresentation> groupRoles = groupResource.roles().realmLevel().listAll();
        if (groupRoles != null && groupRoles.size() > 0) {
          groupResource.roles().realmLevel().remove(groupRoles.stream()
              .filter(roleRepresentation -> roleRepresentation.getName().startsWith("ROLE"))
              .collect(
                  Collectors.toList()));
        }
      }
      if (query.getEntity().getRoles() != null && query.getEntity().getRoles().size() > 0) {
        groupResource.roles().realmLevel()
            .add(orikaBeanMapper.mapAsList(query.getEntity().getRoles(), RoleRepresentation.class));
      }

      return getGroupById(new GetByStrIdQuery(query.getEntity().getId()));
    } else {
      GroupRepresentation groupRepresentation = orikaBeanMapper
          .map(query.getEntity(), GroupRepresentation.class);
      Response response = getKeycloakRealmInstance().groups().add(groupRepresentation);

      if (response.getStatus() == 201) {
        String groupId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        response.close();
        GroupResource groupResource = getKeycloakRealmInstance().groups().group(groupId);
        if (groupResource.roles() != null && groupResource.roles().realmLevel() != null) {
          List<RoleRepresentation> groupRoles = groupResource.roles().realmLevel().listAll();
          if (groupRoles != null && groupRoles.size() > 0) {
            groupResource.roles().realmLevel().remove(groupRoles.stream()
                .filter(roleRepresentation -> roleRepresentation.getName().startsWith("ROLE"))
                .collect(
                    Collectors.toList()));
          }
        }
        if (query.getEntity().getRoles() != null && query.getEntity().getRoles().size() > 0) {
          groupResource.roles().realmLevel()
              .add(orikaBeanMapper
                  .mapAsList(query.getEntity().getRoles(), RoleRepresentation.class));
        }

        return getGroupByName(new GetByNameQuery(query.getEntity().getName()));
      } else {
        ErrorRepresentation error = response.readEntity(ErrorRepresentation.class);
        response.close();
        logger().warn(loggerPrefix + "Group '" + query.getEntity().getName() + "' not created : "
            + error.getErrorMessage());
        return new ServiceResult<>(false, "Group not created : " + error.getErrorMessage(), null);
      }
    }
  }

  @CacheEvict(cacheNames = {"allGroups", "groupByName", "groupById", "findGroups",
      "countGroups"}, allEntries = true)
  public ServiceResult<Void> deleteGroup(DeleteByStrIdQuery query) {
    String loggerPrefix = getLoggerPrefix("deleteGroup");

    if (getKeycloakRealmInstance().groups().group(query.getId()) == null) {
      logger().warn(loggerPrefix + "Group not found (id=" + query.getId() + ")");
      return new ServiceResult<>(false, "Group does not exists", null);
    }
    getKeycloakRealmInstance().groups().group(query.getId()).remove();

    if (getKeycloakRealmInstance().groups().groups().stream()
        .anyMatch(group -> group.getId().equals(query.getId()))) {
      logger().warn(loggerPrefix + "Group not deleted (id=" + query.getId() + ")");
      return new ServiceResult(false, "Group not deleted", null);
    } else {
      return new ServiceResult<>();
    }
  }

  public ServiceResult<MemoryInfo> getServerMemoryInfo() {
    MemoryInfoRepresentation memoryInfoRepresentation = getKeycloakInstance().serverInfo().getInfo()
        .getMemoryInfo();
    MemoryInfo memoryInfo = new MemoryInfo();
    memoryInfo.setFree(memoryInfoRepresentation.getFreeFormated());
    memoryInfo.setTotal(memoryInfoRepresentation.getTotalFormated());
    memoryInfo.setUsed(memoryInfoRepresentation.getUsedFormated());

    return new ServiceResult<>(memoryInfo);
  }

  public ServiceResult<SystemInfo> getServerSystemInfo() {
    SystemInfoRepresentation systemInfoRepresentation = getKeycloakInstance().serverInfo().getInfo()
        .getSystemInfo();
    SystemInfo systemInfo = new SystemInfo();
    systemInfo.setServerTime(systemInfoRepresentation.getServerTime());
    systemInfo.setUptime(systemInfoRepresentation.getUptime());
    systemInfo.setVersion(systemInfoRepresentation.getVersion());

    return new ServiceResult<>(systemInfo);
  }

  public void getActiveSessions() {
    List<Map<String, String>> clientSessionStatus = getKeycloakRealmInstance()
        .getClientSessionStats();
  }
}
