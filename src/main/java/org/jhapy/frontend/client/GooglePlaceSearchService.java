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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.generic.SaveQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.AutocompleteSearchRequestQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.GetGooglePhotoQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.GetPlaceDetailsQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.SearchAroundByKeywordQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.SearchPlacesQuery;
import org.jhapy.dto.utils.StoredFile;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-16
 */
@FeignClient(name = "${jhapy.remote-services.backend-server.name:null}", url = "${jhapy.remote-services.backend-server.url:}", path = "/api/googlePlaceSearchService")
@Primary
public interface GooglePlaceSearchService extends RemoteServiceHandler {

  @PostMapping(value = "/searchPlaces")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "searchPlacesFallback")
  ServiceResult searchPlaces(@RequestBody SearchPlacesQuery query);

  default ServiceResult searchPlacesFallback(SearchPlacesQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("searchPlacesFallback"), e, null);
  }

  @PostMapping(value = "/searchAroundByKeyword")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "searchAroundByKeywordFallback")
  ServiceResult<List<PlacesSearchResult>> searchAroundByKeyword(
      @RequestBody SearchAroundByKeywordQuery query);

  default ServiceResult<List<PlacesSearchResult>> searchAroundByKeywordFallback(SearchPlacesQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("searchAroundByKeywordFallback"), e, null);
  }

  @PostMapping(value = "/autocompleteSearchRequest")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "autocompleteSearchRequestFallback")
  ServiceResult<List<AutocompletePrediction>> autocompleteSearchRequest(
      @RequestBody AutocompleteSearchRequestQuery query);

  default ServiceResult<List<AutocompletePrediction>> autocompleteSearchRequestFallback(SearchPlacesQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("autocompleteSearchRequestFallback"), e, null);
  }

  @PostMapping(value = "/getGooglePhoto")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getGooglePhotoFallback")
  ServiceResult<StoredFile> getGooglePhoto(@RequestBody GetGooglePhotoQuery query);

  default ServiceResult<StoredFile> getGooglePhotoFallback(SearchPlacesQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getGooglePhotoFallback"), e, null);
  }

  @PostMapping(value = "/getPlaceDetails")
  @CircuitBreaker(name = "defaultServiceCircuitBreaker", fallbackMethod = "getPlaceDetailsFallback")
  ServiceResult<PlaceDetails> getPlaceDetails(@RequestBody GetPlaceDetailsQuery query);

  default ServiceResult<PlaceDetails> getPlaceDetailsFallback(SearchPlacesQuery query, Exception e) {
    return defaultFallback(getLoggerPrefix("getPlaceDetailsFallback"), e, null);
  }
}
