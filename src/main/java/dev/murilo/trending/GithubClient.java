package dev.murilo.trending;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public class GithubClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Repository> searchTrending(LocalDate cutoff, int limit) throws IOException, InterruptedException {
       String query = "created:>" + cutoff;
       String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

       String url = "https://api.github.com/search/repositories"
               + "?q=" + encodedQuery
               + "&sort=stars"
               + "&order=desc"
               + "&per_page=" + limit;



        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent","application/vnd.github+json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Erro na API do GitHub (status " + response.statusCode() + "):");
            System.err.println(response.body());
            throw new IOException("Erro na API do GitHub (status " + response.statusCode() + ")");
        }

        SearchResponse searchResponse = objectMapper.readValue(response.body(), SearchResponse.class);
        return searchResponse.items();
    }
}
