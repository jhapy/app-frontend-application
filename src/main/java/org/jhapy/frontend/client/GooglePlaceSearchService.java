package org.jhapy.frontend.client;

import com.google.maps.model.AutocompletePrediction;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.jhapy.dto.serviceQuery.ServiceResult;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.AutocompleteSearchRequestQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.GetGooglePhotoQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.GetPlaceDetailsQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.SearchAroundByKeywordQuery;
import org.jhapy.dto.serviceQuery.googlePlaceSearch.SearchPlacesQuery;
import org.jhapy.dto.utils.StoredFile;

/**
 * @author jHapy Lead Dev.
 * @version 1.0
 * @since 2019-03-16
 */
@FeignClient(name = "${app.remote-services.backend-server.name:null}", url = "${app.remote-services.backend-server.url:}", path = "/googlePlaceSearchService", fallbackFactory = GooglePlaceSearchServiceFallback.class)
@Primary
public interface GooglePlaceSearchService {

  @PostMapping(value = "/searchPlaces")
  ServiceResult searchPlaces(@RequestBody SearchPlacesQuery query);

  @PostMapping(value = "/searchAroundByKeyword")
  ServiceResult<List<PlacesSearchResult>> searchAroundByKeyword(
      @RequestBody SearchAroundByKeywordQuery query);

  @PostMapping(value = "/autocompleteSearchRequest")
  ServiceResult<List<AutocompletePrediction>> autocompleteSearchRequest(
      @RequestBody AutocompleteSearchRequestQuery query);

  @PostMapping(value = "/getGooglePhoto")
  ServiceResult<StoredFile> getGooglePhoto(@RequestBody GetGooglePhotoQuery query);

  @PostMapping(value = "/getPlaceDetails")
  ServiceResult<PlaceDetails> getPlaceDetails(@RequestBody GetPlaceDetailsQuery query);
}