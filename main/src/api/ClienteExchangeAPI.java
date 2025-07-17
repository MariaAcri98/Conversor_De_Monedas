package api;

import com.google.gson.Gson;
import modelos.TasasDeCambio;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class ClienteExchangeAPI {
    private static final String BASE_API_URL ="https://v6.exchangerate-api.com/v6/976c277c76d63444d2b94b54/latest/";
    private final HttpClient hhtpClient;
    private final Gson gson;

    public ClienteExchangeAPI(){
        this.hhtpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public CompletableFuture<TasasDeCambio> obtenerTasas(String monedaBase){
        String url = BASE_API_URL + monedaBase;

        HttpRequest resquest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_API_URL + monedaBase))
                .timeout(Duration.ofSeconds(15))
                .header("Acceot", "application/json")
                .GET()
                .build();

        return hhtpClient.sendAsync(resquest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body ->{
                    //System.out.println("JSON recibido:\n" + body);
                    return  gson.fromJson(body, TasasDeCambio.class);

                });
    }
}
