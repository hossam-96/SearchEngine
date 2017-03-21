import java.io.*;
import java.util.*;

public class Main{
    public static void main(String[] args) {
        Database database = new Database();
        database.create("root","123456");
        database.insert("seeds.txt");
        Set<String> seeds = new HashSet<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("seeds.txt"));
            String line = br.readLine();
            while (line != null){
                seeds.add(line);
                line = br.readLine();
            }
        }catch (Exception e){}

        Scanner reader = new Scanner(System.in);
        System.out.println("Enter number of threads: ");
        int num = reader.nextInt();
        for (int i = 0; i < num; i++) {
            RThread t = new RThread(i + 1, num, seeds);
            t.start();
        }
    }
}