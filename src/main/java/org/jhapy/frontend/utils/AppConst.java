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

package org.jhapy.frontend.utils;

import java.util.Locale;
import org.jhapy.dto.utils.DirectionEnum;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-26
 */
public class AppConst {

  // Todo: Change to a method
  public static final Locale APP_LOCALE = Locale.ENGLISH;

  public static final String PAGE_ROOT = "";
  public static final String PAGE_LOGIN = "login";
  public static final String PAGE_LOGIN_2 = "login2";
  public static final String PAGE_POST = "posts";
  public static final String PAGE_PLACES = "places";
  public static final String PAGE_PLACES2 = "places2";
  public static final String PAGE_FRIENDS = "friends";
  public static final String PAGE_EVENTS = "events";
  public static final String PAGE_NOTIFICATIONS = "notifications";
  public static final String PAGE_USERS_ADMIN = "usersAdmin";
  public static final String PAGE_USER_ADMIN = "userAdmin";
  public static final String PAGE_MAIL_TEMPLATES_ADMIN = "notifications/mailTemplatesAdmin";
  public static final String PAGE_SMS_TEMPLATES_ADMIN = "notifications/smsTemplatesAdmin";
  public static final String PAGE_MAIL_TEMPLATE_ADMIN = "notifications/mailTemplateAdmin";
  public static final String PAGE_SMS_TEMPLATE_ADMIN = "notifications/smsTemplateAdmin";
  public static final String PAGE_MONITORING = "monitoring";
  public static final String PAGE_SESSIONS_ADMIN = "monitoring/sessionsAdmin";
  public static final String PAGE_ACTUAL_SESSIONS_ADMIN = "monitoring/actualSessionsAdmin";
  public static final String PAGE_EUREKA_ADMIN = "monitoring/eurekaAdmin";
  public static final String PAGE_CLOUD_CONFIG_ADMIN = "cloudConfigAdmin";
  public static final String PAGE_SMS_ADMIN = "notifications/smsAdmin";
  public static final String PAGE_MAILS_ADMIN = "notifications/mailsAdmin";
  public static final String PAGE_USER_SETTINGS = "userSettings";
  public static final String PAGE_SETTINGS = "settings";
  public static final String PAGE_SECURITY = "security";
  public static final String PAGE_USERS = "security/users";
  public static final String PAGE_ROLES = "security/roles";
  public static final String PAGE_GROUPS = "security/groups";
  public static final String PAGE_SESSIONS = "security/sessions";
  public static final String PAGE_ELEMENTS = "settings/i18n/elements";
  public static final String PAGE_REFERENCES = "settings/reference";
  public static final String PAGE_LANGUAGES = "settings/reference/languages";
  public static final String PAGE_I18N = "settings/i18n";
  public static final String PAGE_ACTIONS = "settings/i18n/actions";
  public static final String PAGE_MESSAGES = "settings/i18n/messages";
  public static final String PAGE_SOCIAL_MEDIAS = "settings/places/socialMedia";
  public static final String PAGE_ENTRANCE_TYPE = "settings/places/entranceType";
  public static final String PAGE_RATING_TYPE = "settings/places/ratingType";
  public static final String PAGE_PLACE_TYPE = "settings/places/placeType";
  public static final String PAGE_SECURITY_USERS = "settings/security/securityUsers";
  public static final String PAGE_SECURITY_ROLES = "settings/security/securityRoles";
  public static final String PAGE_SECURITY_GROUPS = "settings/security/securityGroups";
  public static final String PAGE_COUNTRIES = "settings/reference/countries";
  public static final String PAGE_ACTIVITIES_ADMIN = "settings/activities/activitiesAdmin";
  public static final String PAGE_ACTIVITY_ADMIN = "settings/activities/activitiyAdmin";

  public static final String PAGE_PLACE_ADMIN = "placeAdmin";
  public static final String PAGE_PLACES_ADMIN = "placesAdmin";

  // Todo: I18N titles
  public static final String TITLE_LOGIN = "element.page.title.login";
  public static final String TITLE_POST = "element.page.title.posts";
  public static final String TITLE_PLACES = "element.page.title.places";
  public static final String TITLE_FRIENDS = "element.page.title.friends";
  public static final String TITLE_EVENTS = "element.page.title.events";
  public static final String TITLE_NOTIFICATIONS = "element.page.title.notifications";
  public static final String TITLE_SETTINGS = "element.page.title.settings";
  public static final String TITLE_USERS_ADMIN = "element.page.title.usersAdmin";
  public static final String TITLE_MAILS_ADMIN = "element.page.title.mailsAdmin";
  public static final String TITLE_ACTUAL_SESSIONS_ADMIN = "element.page.title.actualSessionsAdmin";
  public static final String TITLE_SESSIONS_ADMIN = "element.page.title.sessionsAdmin";
  public static final String TITLE_EUREKA_ADMIN = "element.page.title.eurekaAdmin";
  public static final String TITLE_CLOUD_CONFIG_ADMIN = "element.page.title.cloudConfigAdmin";
  public static final String TITLE_SMS_ADMIN = "element.page.title.smsAdmin";
  public static final String TITLE_NOTIFICATION_ADMIN = "element.page.title.notificationAdmin";
  public static final String TITLE_MAIL_TEMPLATES_ADMIN = "element.page.title.mailTemplatesAdmin";
  public static final String TITLE_SMS_TEMPLATES_ADMIN = "element.page.title.smsTemplatesAdmin";
  public static final String TITLE_MAIL_TEMPLATE_ADMIN = "element.page.title.mailTemplateAdmin";
  public static final String TITLE_SMS_TEMPLATE_ADMIN = "element.page.title.smsTemplateAdmin";
  public static final String TITLE_USER_ADMIN = "element.page.title.userAdmin";
  public static final String TITLE_USER_SETTINGS = "element.page.title.userSettings";
  public static final String TITLE_SECURITY = "element.page.title.security";
  public static final String TITLE_MONITORING = "element.page.title.monitoring";
  public static final String TITLE_ACTIVITIES_ADMIN = "element.page.title.activitiesAdmin";
  public static final String TITLE_ACTIVITY_ADMIN = "element.page.title.activityAdmin";
  public static final String TITLE_PLACES_ADMIN = "element.page.title.placesAdmin";
  public static final String TITLE_PLACE_ADMIN = "element.page.title.placeAdmin";
  public static final String TITLE_SECURITY_USERS = "element.page.title.securityUsers";
  public static final String TITLE_SECURITY_ROLES = "element.page.title.securityRoles";
  public static final String TITLE_SECURITY_GROUPS = "element.page.title.securityGroups";
  public static final String TITLE_I18N = "element.page.title.i18n";
  public static final String TITLE_ACTIVITIES = "element.page.title.activities";
  public static final String TITLE_REFERENCES = "element.page.title.references";
  public static final String TITLE_ELEMENTS = "element.page.title.elements";
  public static final String TITLE_LANGUAGES = "element.page.title.languages";
  public static final String TITLE_COUNTRIES = "element.page.title.countries";
  public static final String TITLE_ACTIONS = "element.page.title.actions";
  public static final String TITLE_SOCIAL_MEDIAS = "element.page.title.socialMedia";
  public static final String TITLE_ENTRANCE_TYPE = "element.page.title.entranceType";
  public static final String TITLE_PLACE_TYPE = "element.page.title.placeType";
  public static final String TITLE_RATING_TYPE = "element.page.title.ratingType";
  public static final String TITLE_MESSAGES = "element.page.title.messages";
  public static final String TITLE_MAILS = "element.page.title.mail";
  public static final String TITLE_SMS = "element.page.title.sms";
  public static final String TITLE_LOGOUT = "element.page.title.logout";
  public static final String TITLE_NOT_FOUND = "element.page.title.notFound";
  public static final String TITLE_ACCESS_DENIED = "element.page.accessDenied";

  public static final String[] SECURITY_USER_SORT_FIELDS = {"username", "id"};
  public static final String[] DEFAULT_SORT_FIELDS = {"name", "id"};
  public static final String[] DEFAULT_INSURANCE_POLICIES_SORT_FIELDS = {"consultationDate"};
  public static final String[] DEFAULT_ASSET_TYPE_FIELDS = {"displayName"};
  public static final String[] DEFAULT_USER_SORT_FIELDS = {"u.lastName"};
  public static final String[] DEFAULT_GROUP_SORT_FIELDS = {"n.name"};
  public static final String[] DEFAULT_RECORD_STATUS_SORT_FIELDS = {"seq"};
  public static final String[] DEFAULT_SESSION_SORT_FIELDS = {"sessionStart"};
  public static final String[] DEFAULT_ASSET_SORT_FIELDS = {"a.name", "a.id"};
  public static final String[] DEFAULT_ID_SORT_FIELDS = {"id"};
  public static final String[] COUNTRY_SORT_FIELDS = {"m.name"};
  public static final String[] PLACE_TYPE_SORT_FIELDS = {"t.name"};
  public static final String[] POST_SORT_FIELDS = {"created"};

  public static final DirectionEnum DEFAULT_SORT_DIRECTION = DirectionEnum.ASC;
  public static final DirectionEnum DEFAULT_INSURANCE_POLICIES_SORT_DIRECTION = DirectionEnum.DESC;
  public static final DirectionEnum POST_SORT_DIRECTION = DirectionEnum.DESC;
  public static final DirectionEnum DEFAULT_SESSION_SORT_DIRECTION = DirectionEnum.DESC;

  public static final String ICON_OK = "/images/iconfinder_ok.png";
  public static final String ICON_KO = "/images/iconfinder_ko.png";
  public static final String ICON_BLANK = "/images/blank.gif";
  public static final String ICON_GREEN = "/images/iconfinder_green.png";
  public static final String ICON_RED = "/images/iconfinder_red.png";
  public static final String NO_PICTURE = "/images/NoPicture.jpg";
  public static final String VIEWPORT = "width=device-width, minimum-scale=1, initial-scale=1, user-scalable=yes";

  // Mutable for testing.
  public static final int NOTIFICATION_DURATION = 4000;

}
