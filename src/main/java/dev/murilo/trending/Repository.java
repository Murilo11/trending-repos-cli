package dev.murilo.trending;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Repository(@JsonProperty("full_name") String fullName,
                         String description,
                         @JsonProperty("stargazers_count") int stars,
                         String language,
                         @JsonProperty("html_url") String htmlUrl
) {
    public String format() {
        String lang = (language != null && !language.isBlank()) ? language : "N/A";
        String desc = formatDescription();


        String yellowStar = "\u001B[33m★\u001B[0m";

        return String.format(
                "%s %,d | %s (%s)%n   %s%n   %s",
                yellowStar, stars, fullName, lang, desc, htmlUrl
        );
    }

    private String formatDescription() {
        if (description == null || description.isBlank()) {
            return "Sem descrição";
        }
        String cleanDesc = description.replace("\r", " ").replace("\n", " ").strip();

        int maxLength = 100;
        if (cleanDesc.length() > maxLength) {
            return cleanDesc.substring(0, maxLength) + "...";
        }
        return cleanDesc;
    }




}
