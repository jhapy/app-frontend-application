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

package org.jhapy.frontend.client;

import com.google.maps.model.AutocompletePrediction;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jhapy.commons.utils.HasLogger;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.AutocompleteSearchRequestQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.GetGooglePhotoQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.GetPlaceDetailsQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.SearchAroundByKeywordQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.SearchPlacesQuery;
import org.jhapy.dto.utils.StoredFile;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-06-02
 */
@Component
public class GooglePlaceSearchServiceFallback implements GooglePlaceSearchService, HasLogger,
    FallbackFactory<GooglePlaceSearchServiceFallback> {

  final Throwable cause;

  public GooglePlaceSearchServiceFallback() {
    this(null);
  }

  GooglePlaceSearchServiceFallback(Throwable cause) {
    this.cause = cause;
  }

  @Override
  public GooglePlaceSearchServiceFallback create(Throwable cause) {
    if (cause != null) {
      String errMessage = StringUtils.isNotBlank(cause.getMessage()) ? cause.getMessage()
          : "Unknown error occurred : " + cause;
      // I don't see this log statement
      logger().debug("Client fallback called for the cause : {}", errMessage);
    }
    return new GooglePlaceSearchServiceFallback(cause);
  }

  @Override
  public ServiceResult<List<PlacesSearchResult>> searchPlaces(SearchPlacesQuery query) {
    logger().error(getLoggerPrefix("searchPlaces") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server",
        Collections.emptyList());
  }

  @Override
  public ServiceResult<List<PlacesSearchResult>> searchAroundByKeyword(
      SearchAroundByKeywordQuery query) {
    logger().error(getLoggerPrefix("searchAroundByKeyword") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server",
        Collections.emptyList());
  }

  @Override
  public ServiceResult<List<AutocompletePrediction>> autocompleteSearchRequest(
      AutocompleteSearchRequestQuery query) {
    logger()
        .error(getLoggerPrefix("autocompleteSearchRequest") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server",
        Collections.emptyList());
  }

  @Override
  public ServiceResult<StoredFile> getGooglePhoto(GetGooglePhotoQuery query) {
    logger().error(getLoggerPrefix("getGooglePhoto") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }

  @Override
  public ServiceResult<PlaceDetails> getPlaceDetails(GetPlaceDetailsQuery query) {
    logger().error(getLoggerPrefix("getPlaceDetails") + "Cannot connect to the server");

    return new ServiceResult<>(false, "Cannot connect to server", null);
  }
}
