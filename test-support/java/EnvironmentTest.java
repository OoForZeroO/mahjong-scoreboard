public class EnvironmentTest {
    public static void main(String[] args) {
        System.out.println("=== 环境测试开始 ===");
        System.out.println("Java版本: " + System.getProperty("java.version"));
        System.out.println("Java家目录: " + System.getProperty("java.home"));
        System.out.println("操作系统: " + System.getProperty("os.name"));
        System.out.println("当前工作目录: " + System.getProperty("user.dir"));
        
        // 检查类路径
        System.out.println("\n类路径: " + System.getProperty("java.class.path"));
        
        // 检查环境变量
        System.out.println("\nMaven路径: " + System.getenv("MAVEN_HOME"));
        System.out.println("JAVA_HOME: " + System.getenv("JAVA_HOME"));
        
        System.out.println("\n=== 环境测试结束 ===");
    }
}