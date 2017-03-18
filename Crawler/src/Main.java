import com.shekhargulati.urlcleaner.UrlCleaner;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;


import java.io.*;
import java.util.*;
import java.util.regex.*;

import java.net.*;

public class Main{
    public static void main(String[] args) {
        DatabaseInserter databaseInserter = new DatabaseInserter();
        databaseInserter.create("root","123456");
        databaseInserter.insert("seeds.txt");
//        Set<String> seeds = new HashSet<String>();
//        try {
//            BufferedReader br = new BufferedReader(new FileReader("seeds.txt"));
//            String line = br.readLine();
//            while (line != null){
//                seeds.add(line);
//                line = br.readLine();
//            }
//        }catch (Exception e){}
//
//        Scanner reader = new Scanner(System.in);
//        System.out.println("Enter number of threads: ");
//        int num = reader.nextInt();
//        for (int i = 0; i < num; i++) {
//            RThread t = new RThread(i + 1, num, seeds);
//            t.start();
//        }
    }
}