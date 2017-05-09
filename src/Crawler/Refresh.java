package Crawler;
import jdk.internal.dynalink.beans.StaticClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import Main_Package.Const;
import static java.lang.Thread.sleep;

/**
 * Created by Hosam on 20/03/17.
 */
public class Refresh {
    static Const con=new Const();
    public static void refreshPage(String pageName, String link){
        try {
            org.jsoup.Connection.Response connection = Jsoup.connect(link)
                    .ignoreHttpErrors(true)
                    .timeout(30000)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .execute();
            Document htmlDocument = connection.parse();
            String htmlString = htmlDocument.text();

            FileWriter wr = new FileWriter(con.Root_Path+"/pages/" + pageName + ".txt");

            if(htmlDocument.text().indexOf('\n') < 0)
                wr.write(htmlDocument.title() + "\n" + link + "\n" + htmlString);
            else{
                wr.write(htmlDocument.title() + "\n" + link + "\n" + htmlString.substring(0,htmlString.indexOf('\n')));
            }
            wr.close();

        }catch (Exception e){
            System.out.println(e.getMessage());}
    }
    public static void main(String[] args) {
        int[] intervals = {4, 8, 4, 16};
        int index = 0;
        try {
            index = index++ % intervals.length;
            String jdbcDriver = "com.mysql.jdbc.Driver";
            String db_url = "jdbc:mysql://localhost/link";

            String username = "root";
            String password = "Moha4422med";

            Class.forName(jdbcDriver);
            Connection conn = DriverManager.getConnection(db_url, username, password);
            Statement stmt = conn.createStatement();
            String sqlQuery = "SELECT * FROM refresh WHERE refresh <= " + intervals[index] + ";";
            ResultSet rs =  stmt.executeQuery(sqlQuery);
            while(rs.next()){
                refreshPage(rs.getInt("thread") + "_" + rs.getInt("pageNum"), rs.getString("link"));
            }
            System.out.println("it will sleep now " + intervals[0] + " hours");
            sleep(intervals[0] * 60 * 60 * 1000);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
