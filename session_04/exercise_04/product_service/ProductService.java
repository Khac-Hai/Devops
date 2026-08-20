import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class ProductService {
    public static void main(String[] args) throws IOException {
        int port = 8082;
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", new ProductHandler());
        server.setExecutor(null);
        System.out.println("[StoreX Product Service] Java backend running on port " + port);
        server.start();
    }

    static class ProductHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String response = "{\n" +
                    "  \"service\": \"product-service\",\n" +
                    "  \"language\": \"Java 17 (Spring Boot Service)\",\n" +
                    "  \"port\": 8082,\n" +
                    "  \"requested_path\": \"" + path + "\",\n" +
                    "  \"status\": \"SUCCESS\",\n" +
                    "  \"products\": [\n" +
                    "    {\"id\": 101, \"name\": \"Laptop Dell XPS 15\", \"price\": 1800},\n" +
                    "    {\"id\": 102, \"name\": \"iPhone 15 Pro Max\", \"price\": 1200}\n" +
                    "  ]\n" +
                    "}\n";
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            byte[] bytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
