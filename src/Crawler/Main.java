package Crawler;
import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import Main_Package.Const;
public class Main{
    static Const con=new Const();
    public static void main(String[] args) {
        Database database = new Database();
        database.create("root","Moha4422med");
        database.insert(con.Root_Path+"\\seeds.txt");
        Set<String> seeds = new HashSet<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(con.Root_Path+"\\seeds.txt"));
            String line = br.readLine();
            while (line != null){
                seeds.add(line);
                line = br.readLine();
            }
        }catch (Exception e){}

        AtomicInteger counter = new AtomicInteger(getCounter("root","Moha4422med"));
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter number of threads: ");
        int num = reader.nextInt();
        for (int i = 0; i < num; i++) {
            RThread t = new RThread(i + 1, num, seeds, counter, 10000);
            t.start();
        }
    }
    public static int getCounter(String user, String pass){
        try{
            String jdbcDriver = "com.mysql.jdbc.Driver";
            String db_url = "jdbc:mysql://localhost/link";

            String username = user;
            String password = pass;

            Class.forName(jdbcDriver);
            Connection conn = DriverManager.getConnection(db_url, username, password);
            Statement stmt = conn.createStatement();
            String sqlQuery = "SELECT COUNT(*) FROM refresh;";
            ResultSet rs = stmt.executeQuery(sqlQuery);
            rs.next();
            return rs.getInt(1);
        }catch (Exception e){

        }
        return 0;
    }
}