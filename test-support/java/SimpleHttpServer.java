import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // 创建API端点
        server.createContext("/", new RootHandler());
        server.createContext("/api/v1/users", new UsersHandler());
        
        server.setExecutor(null); // 使用默认线程池
        server.start();
        System.out.println("服务器已启动，监听端口: " + port);
        System.out.println("可用端点:");
        System.out.println("  - http://localhost:" + port + "/");
        System.out.println("  - http://localhost:" + port + "/api/v1/users");
    }

    // 根路径处理器
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "麻将计分系统服务器运行正常！";
            t.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes("UTF-8"));
            os.close();
        }
    }

    // 用户接口处理器
    static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // 返回模拟的用户数据
            String response = "{\"code\": 200, \"message\": \"success\", \"data\": {\"users\": [], \"total\": 0, \"page\": 1, \"limit\": 10}}";
            
            // 添加CORS头信息
            t.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            // 处理OPTIONS请求
            if ("OPTIONS".equals(t.getRequestMethod())) {
                t.sendResponseHeaders(204, -1);
                return;
            }
            
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes("UTF-8"));
            os.close();
        }
    }
}