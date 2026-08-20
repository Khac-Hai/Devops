import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class UserService {
    public static void main(String[] args) throws IOException {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", new UserHandler());
        server.setExecutor(null);
        System.out.println("[StoreX User Service] Java backend running on port " + port);
        server.start();
    }

    static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String response = "{\n" +
                    "  \"service\": \"user-service\",\n" +
                    "  \"language\": \"Java 17 (Spring Boot Service)\",\n" +
                    "  \"port\": 8081,\n" +
                    "  \"requested_path\": \"" + path + "\",\n" +
                    "  \"status\": \"SUCCESS\",\n" +
                    "  \"users\": [\n" +
                    "    {\"id\": 1, \"name\": \"Nguyen Van A\", \"role\": \"ADMIN\"},\n" +
                    "    {\"id\": 2, \"name\": \"Tran Thi B\", \"role\": \"CUSTOMER\"}\n" +
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
