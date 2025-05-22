import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;
import com.sun.net.httpserver.*;
import org.json.JSONObject;

public class Main {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection(
            "jdbc:postgresql://db:5432/personeldb", "postgres", "123456"
        );

        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);

        server.createContext("/api/personel", exchange -> {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                StringBuilder response = new StringBuilder("[");
                try {
                    PreparedStatement stmt = conn.prepareStatement("SELECT * FROM personel");
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        if (response.length() > 1) response.append(",");
                        response.append(String.format("{\"id\":%d,\"ad\":\"%s\",\"soyad\":\"%s\",\"pozisyon\":\"%s\"}",
                            rs.getInt("id"), rs.getString("ad"), rs.getString("soyad"), rs.getString("pozisyon")));
                    }
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1);
                    return;
                }

                response.append("]");
                headers.add("Content-Type", "application/json");
                byte[] responseBytes = response.toString().getBytes();
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }

            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    body.append(line);
                }

                JSONObject json = new JSONObject(body.toString());
                String ad = json.getString("ad");
                String soyad = json.getString("soyad");
                String pozisyon = json.getString("pozisyon");

                try {
                    PreparedStatement stmt = conn.prepareStatement("INSERT INTO personel (ad, soyad, pozisyon) VALUES (?, ?, ?)");
                    stmt.setString(1, ad);
                    stmt.setString(2, soyad);
                    stmt.setString(3, pozisyon);
                    stmt.executeUpdate();
                    stmt.close();

                    exchange.sendResponseHeaders(200, -1);
                } catch (SQLException e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, -1);
                }

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Java HTTP sunucusu 9090 portunda çalışıyor.");
    }
}
