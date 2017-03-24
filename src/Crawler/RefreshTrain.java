/**
 * Created by Hosam on 20/03/17.
 */
package Crawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;
import Main_Package.Cleaner;
import Main_Package.Const;
public class RefreshTrain extends Thread{
    private int thread, hours;
    static Const con=new Const();
    public RefreshTrain(int thread, int hours){
        this.thread = thread;
        this.hours = hours;
    }
    private String[] Get_Document_Text(String Document_Name) {
        int i = 0;
        String[] Ret={"title","url","content"};
        String sCurrentLine = "";
        try (BufferedReader br = new BufferedReader(new FileReader(con.Root_Path+"/pages/"+ Document_Name + ".txt"))) {
            while ((sCurrentLine = br.readLine()) != null) {
                Ret[i]=sCurrentLine;
                i++;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return Ret;
    }
    public boolean isChanged(String document_name){
        try {
            String[] file = Get_Document_Text(document_name);
            org.jsoup.Connection.Response connection = Jsoup.connect(file[1])
                    .ignoreHttpErrors(true)
                    .timeout(1000)
                    .ignoreContentType(true)
                    .followRedirects(true)
                    .execute();
            Document htmlDocument = connection.parse();
            String htmlString = htmlDocument.text();

            if(htmlString.indexOf('\n') > 0) {
                htmlString = htmlString.substring(0,htmlString.indexOf('\n'));
            }

            Map<String,Pair> table = new HashMap<String,Pair>();
            Pair pair;
            Cleaner cleaner = new Cleaner();
            String[] oldDoc = cleaner.Clean_Text(file[2]);
            for (int i = 0; i < oldDoc.length; i++) {
                if(table.containsKey(oldDoc[i])){
                    pair = table.get(oldDoc[i]);
                    pair.f++;
                    table.put(oldDoc[i],pair);
                }
                else{
                    pair = new Pair();
                    pair.f++;
                    table.put(oldDoc[i],pair);
                }
            }
            String[] newDoc = cleaner.Clean_Text(htmlString);
            for (int i = 0; i < newDoc.length; i++) {
                if(table.containsKey(newDoc[i])){
                    pair = table.get(newDoc[i]);
                    pair.s++;
                    table.put(newDoc[i],pair);
                }
                else{
                    pair = new Pair();
                    pair.s++;
                    table.put(newDoc[i],pair);
                }
            }

            double difference = 0, union = 0;
            for(Map.Entry<String,Pair> word : table.entrySet()){
                difference += Math.abs(word.getValue().f - word.getValue().s);
                union += Math.max(word.getValue().f, word.getValue().s);
                System.out.println(word.getKey());
            }
            System.exit(0);
            double threshold = 0.1;
            if(difference / union > threshold)
                return true;
            else
                return false;

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    @Override
    public void run() {
        try{
            String jdbcDriver = "com.mysql.jdbc.Driver";
            String db_url = "jdbc:mysql://localhost/link";

            String username = "root";
            String password = "Moha4422med";

            Class.forName(jdbcDriver);
            Connection conn = DriverManager.getConnection(db_url, username, password);
            Statement stmt = conn.createStatement();
            String sqlQuery = "SELECT COUNT(*) FROM refresh WHERE thread = " + thread + ";";
            ResultSet rs =  stmt.executeQuery(sqlQuery);
            rs.next();
            int numOfPages = rs.getInt(1);

            stmt = conn.createStatement();

            for (int i = 0; i < numOfPages; i++) {
                if(isChanged(thread + "_" + i)){
                    System.out.println("page " + thread + "_" + i);
                    sqlQuery = "UPDATE refresh SET refresh = " + hours +
                            " Where thread = " + thread + " AND pageNum = " + i + ";";
                    stmt.executeUpdate(sqlQuery);
                }
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }


    }

    public static void main(String[] args) {
        try{
            Scanner reader = new Scanner(System.in);
            System.out.println("Enter number of threads: ");
            int num = reader.nextInt();
            RefreshTrain[] t = new RefreshTrain[num];
            int hours = 4;
            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < num; i++) {
                    t[i] = new RefreshTrain(i + 1, hours);
                    t[i].start();
                }
                for (int i = 0; i < num; i++) {
                    t[i].join();
                }
                System.out.println("it will sleep now");
                sleep(hours  * 60 * 1000);
                hours /= 2;
            }


        }catch (Exception e){}

    }
}
class Pair{
    public int f, s;
    public Pair(){
        f = s = 0;
    }
}
