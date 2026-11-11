package dev.murilo.trending;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

public class GithubClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<Repository> searchTrending(LocalDate cutoff, int limit) {
       String query = "created:>" + cutoff;
       String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

       String url = "https://api.github.com/search/repositories"
               + "?q=" + encodedQuery
               + "&sort=stars"
               + "&order=desc"
               + "&per_page=" + limit;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept","application/vnd.github+json")
                .header("User-Agent", "trending-repos-cli")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            throw new TrendingException("Tempo limite de conexão esgotado. Verifique sua conexão com a internet.", e, 3);
        } catch (IOException e) {
            throw new TrendingException("Falha de comunicação com a rede. Verifique se você está conectado à internet.", e, 3);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TrendingException("A requisição foi interrompida.", e, 3);
        }

        int status = response.statusCode();

        if (status == 403 || status == 429) {
            String resetHeader = response.headers().firstValue("X-RateLimit-Reset").orElse(null);
            String mensagem;
            if (resetHeader != null) {
                long epochSeconds = Long.parseLong(resetHeader);
                LocalTime resetTime = LocalTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
                mensagem = "Limite de requisições da API do GitHub excedido. Tente novamente após " + resetTime + ".";
            } else {
                mensagem = "Limite de requisições da API do GitHub excedido. Aguarde alguns minutos e tente novamente.";
            }
            throw new TrendingException(mensagem, null, 2);
        }

        if (status == 422) {
            throw new TrendingException("Parâmetros de busca inválidos na API do GitHub.", null, 4);
        }

        if (status >= 500) {
            throw new TrendingException("Servidores do GitHub estão indisponíveis no momento. Tente novamente mais tarde.", null, 4);
        }

        if (status != 200) {
            throw new TrendingException("Erro inesperado na API do GitHub (status HTTP " + status + ").", null, 4);
        }


        SearchResponse searchResponse;
        try {
            searchResponse = objectMapper.readValue(response.body(), SearchResponse.class);
        } catch (IOException e) {
            throw new TrendingException("Falha ao processar os dados recebidos do GitHub.", e, 4);
        }

        List<Repository> items = searchResponse.items();
        if (items == null || items.isEmpty()) {
            throw new TrendingException("Nenhum repositório encontrado para os critérios informados.", null, 5);
        }

        return items;
    }
}
