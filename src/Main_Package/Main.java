package Main_Package;

import java.io.*;
import Crawler.Crawler;
import Crawler.RThread;
import java.net.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
public class Main {

    static Const Con=new Const();
    static String[] Get_Doucment_Text(String Doucment_Name) {
        int i = 0;
        String[] Ret={"title","url","content"};
        String sCurrentLine = "";
        try (BufferedReader br = new BufferedReader(new FileReader(Con.Root_Path+"\\doucments\\" + Doucment_Name))) {
            while ((sCurrentLine = br.readLine()) != null) {
                Ret[i]=sCurrentLine;
                i++;
            }

        } catch (Exception e) {
            e.getMessage();
        }
        return Ret;
    }
    public static void main(String[] args)throws Exception
    {
        //Crawler C = new Crawler();
        //C.RunCrawler(50);
        Indexer I=new Indexer(100);
        //I.Get_New_Doucment();
        //I.Create();
        //I.Print();
        //I.Save();
        I.Load();
        System.out.println(I.Word_to_Doucment);
//        Cleaner C =new Cleaner();
//        File folder = new File(Con.Root_Path+"\\doucments");
//        File[] listOfFiles = folder.listFiles();
//        for (int i = 0; i < 50; i++)
//        {
//        if (listOfFiles[i].isFile())
//        {
//            String[] Doucmet_elements = Get_Doucment_Text(listOfFiles[i].getName());
//            //if(C.Clean_Text(Doucmet_elements[2]).length!=C.Clean_Text_V2(Doucmet_elements[2]).length)
//            System.out.println(C.Clean_Text(Doucmet_elements[2]).length + " "+ C.Clean_Text_V2(Doucmet_elements[2]).length);
//        }
//        }


    }
}
