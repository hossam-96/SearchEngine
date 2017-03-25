package Crawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.sql.*;
import java.net.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import Crawler.com.shekhargulati.urlcleaner.UrlCleaner;
import Main_Package.Const;
import java.util.concurrent.atomic.AtomicInteger;
/* * Created by Hosam on 06/03/17.
 */
public class RThread extends Thread {
    static Const con=new Const();
    private int threadNum, numOfThreads, maxPages, seedsNum = 500;
    private java.sql.Connection conn = null;
    private Set<String> seeds;
    private AtomicInteger sharedCounter;

    public RThread(int threadNum, int numOfThreads, Collection<String> c, AtomicInteger sharedCounter, int maxPages){
        this.threadNum = threadNum;
        this.numOfThreads = numOfThreads;
        seeds = new HashSet<String>(c);
        this.sharedCounter = sharedCounter;
        this.maxPages = maxPages;

        try {
            String jdbcDriver = "com.mysql.jdbc.Driver";
            String db_url = "jdbc:mysql://localhost/link";

            String username = "root";
            String password = "Moha4422med";

            Class.forName(jdbcDriver);
            conn = DriverManager.getConnection(db_url, username, password);
        }
        catch(Exception e){
            e.getMessage();
        }
    }

    public void run(){
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs;
            String sqlQuery;
            String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
            int numOfPages = 0;
            int row = 0;
            boolean seed = true;

            sqlQuery = "SELECT * FROM threads WHERE thread = " + threadNum + ";";
            rs = stmt.executeQuery(sqlQuery);
            if(rs.next()) {
                seed = rs.getBoolean("seed");
                numOfPages = rs.getInt("numOfPages");
                row = rs.getInt("row");
            }
            else{
                sqlQuery = "INSERT INTO threads (thread) VALUES (" + threadNum + ");";
                stmt.executeUpdate(sqlQuery);
            }


            while (true){
                if(seed){
                    sqlQuery = "SELECT * FROM seeds " +
                            "LIMIT " + (int)(((threadNum - 1) * Math.ceil(seedsNum / (double)numOfThreads)) + row) + ", " + (int)(Math.ceil(seedsNum / (double)numOfThreads)) + ";";
                }
                else {
                    sqlQuery = "SELECT * FROM links WHERE thread = " + (int)threadNum + ";";
                }
                rs = stmt.executeQuery(sqlQuery);
                while (rs.next()){
                    try {
                        if(sharedCounter.get() > maxPages)
                            return;
                        if(seed)
                            row++;
                        org.jsoup.Connection.Response connection = Jsoup.connect(rs.getString("link"))
                                .ignoreHttpErrors(true)
                                .timeout(1000)
                                .ignoreContentType(true)
                                .followRedirects(true)
                                .execute();
                        Document htmlDocument = connection.parse();
                        Node taglang = htmlDocument.select("html").first();
                        if (!connection.contentType().contains("text/html") || !taglang.attr("lang").contains("en")) {
                            continue;
                        }

                        Statement stmt2 = conn.createStatement();
                        if(seed) {
                            robotsTxt(rs.getString("link"));
                            sqlQuery = "UPDATE threads SET row = " + (int)row
                                    + " WHERE thread = " + (int) threadNum + ";";
                            stmt2.executeUpdate(sqlQuery);
                        }
                        else{
                            sqlQuery = "DELETE FROM links WHERE link = '" + rs.getString("link") + "';";
                            stmt2.executeUpdate(sqlQuery);
                        }

                        Elements linksOnPage = htmlDocument.select("a[href]");


                        for (Element link : linksOnPage) {
                            try {
                                URL url = new URL(link.absUrl("href"));
                                String urlToString = UrlCleaner.normalizeUrl(url.toString());
                                if (urlToString.matches(regex) && !disallowed(urlToString) && seeds.contains("http://" + url.getHost() + "/")) {
                                    sqlQuery = "INSERT INTO links VALUES ('" + urlToString + "'," + threadNum + ")";
                                    stmt2.executeUpdate(sqlQuery);
                                    connection = Jsoup.connect(urlToString)
                                            .ignoreHttpErrors(true)
                                            .timeout(1000)
                                            .ignoreContentType(true)
                                            .followRedirects(true)
                                            .execute();
                                    htmlDocument = connection.parse();
                                    taglang = htmlDocument.select("html").first();
                                    if (!connection.contentType().contains("text/html") || !taglang.attr("lang").contains("en")) {
                                        sqlQuery = "DELETE FROM links WHERE link = '" + urlToString + "';";
                                        stmt2.executeUpdate(sqlQuery);
                                        continue;
                                    }
                                    if(sharedCounter.incrementAndGet() > maxPages)
                                        return;

                                    FileWriter wr = new FileWriter(con.Root_Path+"/pages/" + threadNum + "_" + numOfPages++ + ".txt");
                                    String htmlString = htmlDocument.text();
                                    if(htmlDocument.text().indexOf('\n') < 0)
                                        wr.write(htmlDocument.title() + "\n" + urlToString + "\n" + htmlString);
                                    else{
                                        wr.write(htmlDocument.title() + "\n" + urlToString + "\n" + htmlString.substring(0,htmlString.indexOf('\n')));
                                    }
                                    wr.close();


                                      
                                    sqlQuery = "UPDATE threads SET numOfPages = " + (int)numOfPages
                                            + " WHERE thread = " + (int)threadNum + ";";
                                    stmt2.executeUpdate(sqlQuery);

                                    sqlQuery = "INSERT INTO refresh(thread, pageNum, link) VALUES (" +
                                            threadNum + ", " + (int)(numOfPages - 1) + ", '" + urlToString + "');";
                                    stmt2.executeUpdate(sqlQuery);
                                }
                            }
                            catch (Exception e){
                                System.out.println(e.getMessage());
                            }
                        }

                        stmt2.close();
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
                seed = false;
                sqlQuery = "UPDATE threads SET seed = FALSE " +
                        "WHERE thread = " + (int)threadNum + ";";
                stmt.executeUpdate(sqlQuery);
            }
        }
        catch (Exception e){

        }
    }
    private void robotsTxt(String s){
        try {
            URL url = new URL(s);
            String robot = "http://" + url.getHost() + "/robots.txt";
            String base = "http://" + url.getHost();
            String sqlQuery;

            org.jsoup.Connection.Response connection = Jsoup.connect(robot).execute();
            Document robotsFile = connection.parse();
            Statement robotInsertStmt = conn.createStatement();
            String[] Disallow = robotsExtract(robotsFile.toString());

            for (int j = 0; j < Disallow.length; j++) {
                try {
                    if(!Disallow[j].isEmpty())
                        if(Disallow[j].charAt(0) == '/' && Disallow[j].matches("\\A\\p{ASCII}*\\z")) {
                            if(Disallow[j].charAt(Disallow[j].length() - 1) == '/')
                                sqlQuery = "INSERT INTO robots VALUES ('" + base + Disallow[j] + "')";
                            else
                                sqlQuery = "INSERT INTO robots VALUES ('" + base + Disallow[j] + "/')";
                            robotInsertStmt.executeUpdate(sqlQuery);
                        }
                }catch (Exception e){
                    if (j == 0) break;
                }
            }
        }catch (Exception e){}
    }
    private boolean disallowed(String s){
        int index = s.lastIndexOf('.');
        try {
            Statement stmt = conn.createStatement();
            String sqlQuery;
            while (index >= 0) {
                index = s.indexOf('/', index + 1);
                if(index >= 0){
                    sqlQuery = "SELECT * FROM robots WHERE disallow = '" + s.substring(0,index + 1) + "';";
                    ResultSet rs = stmt.executeQuery(sqlQuery);
                    if(rs.next()) return true;
                }
            }
            return false;
        }catch (Exception e){

        }
        return false;
    }
    private static String[] robotsExtract(String s){
        String[] right = s.split("User-agent: \\*");

        String[] left = right[1].split("User-agent:");

        String[] d = left[0].split("(Disallow:| )");
        for (int i = 0; i < d.length; i++) {
            d[i] = d[i].toLowerCase();
        }
        return d;
    }
}
