package wtf.alexhanwow;

import java.sql.*;
import java.util.Scanner;


public class DaftDatabase {
    private static Connection connection;
    private static String currentDatabase = "";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.printf("    __ _       ______  __  _________\n" +
                "   / /| |     / / __ \\/  |/  /_  __/\n" +
                "  / / | | /| / / / / / /|_/ / / /   \n" +
                " / /__| |/ |/ / /_/ / /  / / / /    \n" +
                "/_____/__/|__/_____/_/  /_/ /_/     \n" +
                "                           by AlexhanWOW   ");

        System.out.println("\n\n========LWDMT MySQL管理工具 =========");
        boolean running = true;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        while (running) {
            clearConsole(); // 每次显示菜单前清空控制台
            System.out.printf("    __ _       ______  __  _________\n" +
                    "   / /| |     / / __ \\/  |/  /_  __/\n" +
                    "  / / | | /| / / / / / /|_/ / / /   \n" +
                    " / /__| |/ |/ / /_/ / /  / / / /    \n" +
                    "/_____/__/|__/_____/_/  /_/ /_/     \n" +
                    "                           by AlexhanWOW   ");

            System.out.println("\n\n========LWDMT MySQL管理工具 =========\n\n\n\n");
            if (connection == null) {
                System.out.println("\n[未连接] 请选择操作:");
            } else {
                System.out.println("\n[已连接: " + currentDatabase + "] 请选择操作:");
            }

            System.out.println("1. 连接到MySQL服务器");
            System.out.println("2. 创建新数据库");
            System.out.println("3. 显示所有数据库");
            System.out.println("4. 选择数据库");
            System.out.println("5. 显示当前数据库的所有表");
            System.out.println("6. 显示表内容");
            System.out.println("7. 创建表");
            System.out.println("8. 删除表");
            System.out.println("9. 插入数据");
            System.out.println("10. 更新数据");
            System.out.println("11. 删除数据");
            System.out.println("12. 执行自定义SQL");
            System.out.println("0. 退出");
            System.out.print("请输入选项: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    connectToMySQL();
                    break;
                case 2:
                    createDatabase();
                    break;
                case 3:
                    showDatabases();
                    break;
                case 4:
                    selectDatabase();
                    break;
                case 5:
                    showTables();
                    break;
                case 6:
                    showTableData();
                    break;
                case 7:
                    createTable();
                    break;
                case 8:
                    dropTable();
                    break;
                case 9:
                    insertData();
                    break;
                case 10:
                    updateData();
                    break;
                case 11:
                    deleteData();
                    break;
                case 12:
                    executeCustomSQL();
                    break;
                case 0:
                    running = false;
                    disconnect();
                    System.out.printf("\n\n\n\n\n");
                    System.out.printf("======================================================\n");
                    System.out.println("感谢使用LightWeight Database Manage Tool！--- LWDMT\n");
                    System.out.printf("======================================================\n");
                    break;
                default:
                    System.out.println("无效选项，请重新输入");
                    pressEnterToContinue();
            }
        }
    }

    // 清空控制台内容
    private static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                // 对于Windows系统
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // 对于Linux和macOS系统
                System.out.print("\033[H\033[2J");
            }
            System.out.flush();
        } catch (final Exception e) {
            // 如果清屏失败，打印50个空行作为备选方案
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    // 按回车继续
    private static void pressEnterToContinue() {
        System.out.print("\n按回车键继续...");
        scanner.nextLine();
    }

    private static void connectToMySQL() {
        clearConsole();
        System.out.println("===== 连接到MySQL服务器 =====");

        try {
            System.out.print("输入主机地址 (默认localhost): ");
            String host = scanner.nextLine().trim();
            if (host.isEmpty()) host = "localhost";

            System.out.print("输入端口号 (默认3306): ");
            String portInput = scanner.nextLine().trim();
            int port = portInput.isEmpty() ? 3306 : Integer.parseInt(portInput);

            System.out.print("输入用户名: ");
            String username = scanner.nextLine().trim();

            System.out.print("输入密码: ");
            String password = scanner.nextLine().trim();

            String url = "jdbc:mysql://" + host + ":" + port + "/";
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("\n连接成功！");
            pressEnterToContinue();

        } catch (Exception e) {
            System.out.println("连接失败: " + e.getMessage());
            pressEnterToContinue();
        }
    }

    private static void createDatabase() {
        if (!checkConnection()) {
            pressEnterToContinue();
            return;
        }

        clearConsole();
        System.out.println("===== 创建新数据库 =====");

        System.out.print("输入新数据库名称: ");
        String dbName = scanner.nextLine().trim();

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE " + dbName);
            System.out.println("\n数据库 '" + dbName + "' 创建成功！");
        } catch (SQLException e) {
            System.out.println("创建失败: " + e.getMessage());
        }

        pressEnterToContinue();
    }

    private static void showDatabases() {
        if (!checkConnection()) {
            pressEnterToContinue();
            return;
        }

        clearConsole();
        System.out.println("===== 数据库列表 =====");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {

            System.out.println("数据库列表:");
            System.out.println("-------------------");
            int count = 1;
            while (rs.next()) {
                System.out.println(count++ + ". " + rs.getString(1));
            }
            System.out.println("-------------------");
            System.out.println("共找到 " + (count-1) + " 个数据库");
        } catch (SQLException e) {
            System.out.println("获取数据库列表失败: " + e.getMessage());
        }

        pressEnterToContinue();
    }

    private static void selectDatabase() {
        if (!checkConnection()) {
            pressEnterToContinue();
            return;
        }

        clearConsole();
        System.out.println("===== 选择数据库 =====");

        System.out.print("输入数据库名称: ");
        String dbName = scanner.nextLine().trim();

        try {
            connection.setCatalog(dbName);
            currentDatabase = dbName;
            System.out.println("\n已选择数据库: " + dbName);
        } catch (SQLException e) {
            System.out.println("选择数据库失败: " + e.getMessage());
        }

        pressEnterToContinue();
    }

    // 显示当前数据库的所有表
    private static void showTables() {
        if (!checkDatabaseSelected()) {
            pressEnterToContinue();
            return;
        }

        clearConsole();
        System.out.println("===== 表列表 (" + currentDatabase + ") =====");

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {

            System.out.println("表列表:");
            System.out.println("-------------------");
            int count = 1;
            while (rs.next()) {
                System.out.println(count++ + ". " + rs.getString(1));
            }
            System.out.println("-------------------");
            System.out.println("共找到 " + (count-1) + " 个表");
        } catch (SQLException e) {
            System.out.println("获取表列表失败: " + e.getMessage());
        }

        pressEnterToContinue();
    }

    // 显示表内容
    private static void showTableData() {
        if (!checkDatabaseSelected()) {
            pressEnterToContinue();
            return;
        }

        clearConsole();
        System.out.println("===== 显示表内容 =====");

        System.out.print("输入要查看的表名: ");
        String tableName = scanner.nextLine().trim();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            clearConsole();
            System.out.println("===== 表内容: " + tableName + " =====");

            // 打印表头
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-20s", metaData.getColumnName(i));
            }
            System.out.println();

            // 打印分隔线
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("%-20s", "--------------------");
            }
            System.out.println();

            // 打印数据
            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if (value == null) value = "NULL";
                    System.out.printf("%-20s", value);
                }
                System.out.println();
                rowCount++;
            }

            System.out.println("-------------------");
            System.out.println("共找到 " + rowCount + " 行数据");

        } catch (SQLException e) {
            System.out.println("获取表内容失败: " + e.getMessage());
        }

        pressEnterToContinue();
    }

    private static void createTable() {
        if (!checkDatabaseSelected()) {
            pressEnterToContinue();
            return;
        }

        clearConsole();
        System.out.println("===== 创建表 =====");

        System.out.print("输入表名: ");
        String tableName = scanner.nextLine().trim();

        System.out.println("\n输入列定义 (格式: 列名 数据类型 [约束], 输入END结束)");
        System.out.println("示例: id INT PRIMARY KEY, name VARCHAR(50) NOT NULL");

        StringBuilder columns = new StringBuilder();
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("END")) break;
            columns.append(input).append(", ");
        }

        if (columns.length() == 0) {
            System.out.println("未定义列，取消创建表");
            pressEnterToContinue();
            return;
        }

        String sql = "CREATE TABLE " + tableName + " (" +
                columns.substring(0, columns.length() - 2) + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("\n表 '" + tableName + "' 创建成功！");
        } catch (SQLException e) {
            System.out.println("创建表失败: " + e.getMessage());
        }

        pressEnterToContinue();
    }

    private static void dropTable() {
        if (!checkDatabaseSelected()) {
            pressEnterToContinue();
            return;
        }

        clearConsole();
        System.out.println("===== 删除表 =====");

        System.out.print("输入要删除的表名: ");
        String tableName = scanner.nextLine().trim();

        System.out.print("\n确认删除表 '" + tableName + "'? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("操作取消");
            pressEnterToContinue();
            return;
        }

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DROP TABLE " + tableName);
            System.out.println("\n表 '" + tableName + "' 已删除");
        } catch (SQLException e) {
            System.out.println("删除失败: " + e.getMessage());
        }

        pressEnterToContinue();
    }

    private static void insertData() {
        if (!checkDatabaseSelected()) {
            pressEnterToContinue();
            return;
        }

        clearConsole();
        System.out.println("===== 插入数据 =====");

        System.out.print("输入表名: ");
        String tableName = scanner.nextLine().trim();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("DESCRIBE " + tableName)) {

            System.out.println("\n表结构:");
            System.out.println("-------------------");
            while (rs.next()) {
                System.out.println(rs.getString("Field") + " - " +
                        rs.getString("Type"));
            }
            System.out.println("-------------------");

            System.out.print("\n输入要插入的列名 (逗号分隔): ");
            String columns = scanner.nextLine().trim();

            System.out.print("输入对应值 (逗号分隔): ");
            String values = scanner.nextLine().trim();

            String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
            try (Statement insertStmt = connection.createStatement()) {
                int rows = insertStmt.executeUpdate(sql);
                System.out.println("\n" + rows + " 行数据插入成功");
            }

        } catch (SQLException e) {
            System.out.println("插入数据失败: " + e.getMessage());
        }

        pressEnterToContinue();
    }

    private static void updateData() {
        if (!checkDatabaseSelected()) {
            pressEnterToContinue();
            return;
        }

        clearConsole();
        System.out.println("===== 更新数据 =====");

        System.out.print("输入表名: ");
        String tableName = scanner.nextLine().trim();

        System.out.print("输入SET子句 (例: name='新值', age=25): ");
        String setClause = scanner.nextLine().trim();

        System.out.print("输入WHERE条件 (留空更新所有行): ");
        String whereClause = scanner.nextLine().trim();

        String sql = "UPDATE " + tableName + " SET " + setClause;
        if (!whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        System.out.print("\n确认执行: " + sql + " ? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("操作取消");
            pressEnterToContinue();
            return;
        }

        try (Statement stmt = connection.createStatement()) {
            int rows = stmt.executeUpdate(sql);
            System.out.println("\n" + rows + " 行数据更新成功");
        } catch (SQLException e) {
            System.out.println("更新失败: " + e.getMessage());
        }

        pressEnterToContinue();
    }

    private static void deleteData() {
        if (!checkDatabaseSelected()) {
            pressEnterToContinue();
            return;
        }

        clearConsole();
        System.out.println("===== 删除数据 =====");

        System.out.print("输入表名: ");
        String tableName = scanner.nextLine().trim();

        System.out.print("输入WHERE条件 (留空删除所有数据): ");
        String whereClause = scanner.nextLine().trim();

        String sql = "DELETE FROM " + tableName;
        if (!whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        System.out.print("\n确认执行: " + sql + " ? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("操作取消");
            pressEnterToContinue();
            return;
        }

        try (Statement stmt = connection.createStatement()) {
            int rows = stmt.executeUpdate(sql);
            System.out.println("\n" + rows + " 行数据删除成功");
        } catch (SQLException e) {
            System.out.println("删除失败: " + e.getMessage());
        }

        pressEnterToContinue();
    }

    private static void executeCustomSQL() {
        if (!checkConnection()) {
            pressEnterToContinue();
            return;
        }

        clearConsole();
        System.out.println("===== 执行自定义SQL =====");
        System.out.println("输入SQL语句 (输入END结束多行输入):");

        StringBuilder sql = new StringBuilder();
        String line;

        while (!(line = scanner.nextLine()).equalsIgnoreCase("END")) {
            sql.append(line);
            if (line.trim().endsWith(";")) break;
        }

        try (Statement stmt = connection.createStatement()) {
            boolean isResultSet = stmt.execute(sql.toString());

            clearConsole();
            System.out.println("===== SQL执行结果 =====");

            if (isResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // 打印列名
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.printf("%-20s", metaData.getColumnName(i));
                    }
                    System.out.println();

                    // 打印分隔线
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.printf("%-20s", "--------------------");
                    }
                    System.out.println();

                    // 打印数据
                    int rowCount = 0;
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            String value = rs.getString(i);
                            if (value == null) value = "NULL";
                            System.out.printf("%-20s", value);
                        }
                        System.out.println();
                        rowCount++;
                    }
                    System.out.println("-------------------");
                    System.out.println("共返回 " + rowCount + " 行数据");
                }
            } else {
                int rows = stmt.getUpdateCount();
                System.out.println("操作成功，影响行数: " + rows);
            }
        } catch (SQLException e) {
            System.out.println("SQL执行错误: " + e.getMessage());
        }

        pressEnterToContinue();
    }

    private static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("断开连接时出错: " + e.getMessage());
        }
    }

    private static boolean checkConnection() {
        if (connection == null) {
            System.out.println("错误：未连接到MySQL服务器");
            return false;
        }
        return true;
    }

    private static boolean checkDatabaseSelected() {
        if (!checkConnection()) return false;
        if (currentDatabase.isEmpty()) {
            System.out.println("错误：未选择数据库");
            return false;
        }
        return true;
    }

    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("请输入有效数字: ");
            }
        }
    }
}
