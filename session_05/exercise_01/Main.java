import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws Exception {
        String serviceName = System.getenv().getOrDefault("SERVICE_NAME", "QuickBite Service");
        int port = 8080;
        String portEnv = System.getenv("SERVER_PORT");
        if (portEnv != null) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (Exception ignored) {}
        }

        System.out.println("[" + serviceName + "] Spring Boot Simulation started successfully on port " + port);
        System.out.println("[" + serviceName + "] Connected to Database at " + System.getenv().getOrDefault("SPRING_DATASOURCE_URL", "default_db"));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "{\"status\": \"UP\", \"service\": \"" + serviceName + "\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });
        server.setExecutor(null);
        server.start();

        while (true) {
            Thread.sleep(60000);
        }
    }
}
