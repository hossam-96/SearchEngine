package Main_Package;
import java.net.*;
/**
 * Created by moham on 3/15/2017.
 */
import java.io.*;
import java.util.*;
public class QRT
{
    public String URLs;
    public String TITLE;
    public int DOUCMENT_NUM;
    public String Favicon;
    public String Content;

    public QRT(String URLs, String TITLE, int DOUCMENT_NUM,String content) throws Exception{
        this.Content=content;
        this.URLs = URLs;
        this.TITLE = TITLE;
        if(TITLE.length()>30)this.TITLE = TITLE.substring(0,30)+"...";
        if(content.length()>100)this.Content = content.substring(0,100)+"...";
        this.DOUCMENT_NUM = DOUCMENT_NUM;
        URL aURL = new URL(URLs);
        this.Favicon="assets/img/favicon/"+aURL.getHost()+".ico";
        File f = new File("C:\\Users\\moham\\IdeaProjects\\Our Search Engine\\web\\"+this.Favicon);
        if(f.exists()==false) {
            this.Favicon="assets/img/favicon/unknown.ico";
        }
    }
}
