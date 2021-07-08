package io.javabrains.moviecatalogservice.resources.io.javabrains.moviecatalogservice.resources;

import io.javabrains.moviecatalogservice.models.CatalogItem;
import io.javabrains.moviecatalogservice.models.Movie;
import io.javabrains.moviecatalogservice.models.Rating;
import io.javabrains.moviecatalogservice.models.UserRating;
import io.javabrains.moviecatalogservice.resources.io.javabrains.moviecatalogservice.services.MovieInfo;
import io.javabrains.moviecatalogservice.resources.io.javabrains.moviecatalogservice.services.UserRatingInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class CatalogResource {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    MovieInfo movieInfo;
    
    @Autowired 
    UserRatingInfo userRatingInfo;
    
    @Autowired
    WebClient.Builder webClientBuilder;

    @RequestMapping("/{userId}")
    //@HystrixCommand(fallbackMethod = "getFallbackCatalog")..comment out  since the 2 api calls have separate 
   // but after the refactor at method/service level hystrix fallback since the control for fallback is not under  
    //the control of proxy wrapper class around our java class which is provided by the framework
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {

        UserRating userRating = userRatingInfo.getUserRating(userId);

        return userRating.getRatings().stream()
                .map(rating -> { return movieInfo.getCatalogItem(rating);  })
                .collect(Collectors.toList());

    }


    public List<CatalogItem> getFallbackCatalog(@PathVariable("userId") String userId) {
    	return Arrays.asList(new CatalogItem("No Movie","",0));// or pick something from the cache
    	//used for fallback mechanism suppose some of the microservices goes down , to uske chakkae main baaki na down hojaaye 
    }

}

/*
Alternative WebClient way
Movie movie = webClientBuilder.build().get().uri("http://localhost:8082/movies/"+ rating.getMovieId())
.retrieve().bodyToMono(Movie.class).block();
*/