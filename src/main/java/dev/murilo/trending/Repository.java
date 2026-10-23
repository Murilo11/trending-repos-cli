package dev.murilo.trending;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Repository(@JsonProperty("full_name") String fullName,String description,@JsonProperty("stargazers_count") int stars, String language,@JsonProperty("html_url") String htmlUrl) {


}
