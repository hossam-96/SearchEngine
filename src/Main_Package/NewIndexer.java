package Main_Package; /**
 * Created by moham on 3/8/2017.
 */


import java.io.*;
import java.security.PublicKey;
import java.util.*;

public class NewIndexer {
    private long ALL_WORDS_NUM=0;
    private int Max_it;
    private Const Con=new Const();
    public MongoDB DB;
    private List<String> docs;
    private Cleaner C_Text;
    private List<String> unique;
    private Hashtable<String, Integer> Map;
    private ArrayList<Integer> pos;
    public NewIndexer(int max_it) throws Exception{
        this.Max_it=max_it;
        this.C_Text=new Cleaner();
        unique= new LinkedList<String>();
        Map = new Hashtable<String, Integer>();
        pos = new ArrayList<>();
        this.docs = new ArrayList<String>();
        this.DB=new MongoDB();
        this.DB.CreateDatabase();
    }

    private String[] Get_Doucment_Text(String Doucment_Name) {
        int i = 0;
        String[] Ret={"title","url","content"};
        String sCurrentLine = "";
        try (BufferedReader br = new BufferedReader(new FileReader(this.Con.Root_Path+"\\doucments\\" + Doucment_Name))) {
            while ((sCurrentLine = br.readLine()) != null) {
                Ret[i]=sCurrentLine;
                i++;
            }

        } catch (Exception e) {
            e.getMessage();
        }
        return Ret;
    }

    private void get_unique_trems(String[] Terms) {

        unique.clear();
        Map.clear();
        for (int i = 0; i < Terms.length; i++) {
            if (Map.containsKey(Terms[i]) == false) {
                if (Terms[i].trim().isEmpty() == false) {
                    unique.add(Terms[i]);
                    Map.put(Terms[i], 1);
                }
            }
        }
        // System.out.println(unique.size()+" Unique KeyWord");
    }

    private void get_term_pos(String Term, String Terms[])
    {
        this.pos.clear();
        for (int i = 0; i < Terms.length; i++) {
            if (Term.equals(Terms[i])) {
                pos.add(i);
            }
        }
    }

    public void Create_Word_to_Doucment()
    {
        File folder = new File(this.Con.Root_Path+"\\doucments");
        File[] listOfFiles = folder.listFiles();
        int Doucment_Number;
        String Doucment_Name;
        String[] Doucmet_elements;
        ArrayList<ArrayList<String>> L;
        ArrayList<String> L1;
        ArrayList<String> L2;
        int all=Math.min(this.Max_it,listOfFiles.length);
        int start=DB.DoucmentToWordsLastDoucment("");
        for (int i = start+1; i < all; i++)
        {
            if (listOfFiles[i].isFile())
            {
                Doucment_Name=listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length() - 4);
                Doucment_Number = Integer.parseInt(listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length() - 4));
                Doucmet_elements=this.Get_Doucment_Text(listOfFiles[i].getName());
                //System.out.println("Indexing "+listOfFiles[i].getName()+" "+Doucmet_elements[1]);
                String Terms[][]=C_Text.Triple_Clean(Doucmet_elements[2]);
                {
                    L1=new ArrayList<String>(Arrays.asList(Terms[2]));
                    L2=new ArrayList<String>(Arrays.asList(Terms[0]));
                    L1.add(Doucmet_elements[0]);
                    L1.add(Doucmet_elements[1]);
                    this.DB.DoucmentToWordsInsert(Doucment_Number,L1,L2,Doucmet_elements[0],Doucmet_elements[1],"_Lossless");
                }
                {
                    L1=new ArrayList<String>(Arrays.asList(Terms[2]));
                    L2=new ArrayList<String>(Arrays.asList(Terms[1]));
                    this.DB.DoucmentToWordsInsert(Doucment_Number,L1,L2,Doucmet_elements[0],Doucmet_elements[1],"");
                    this.ALL_WORDS_NUM+=Terms[1].length;
                    this.docs.add(String.join(" ",Terms[1]));
                }

                for(int k=0;k<2;k++)
                {
                    this.get_unique_trems(Terms[k]);
                    for (int j = 0; j < this.unique.size(); j++) {
                        if (this.unique.get(j).equals("Stopword"))
                            continue;
                        this.get_term_pos(this.unique.get(j), Terms[k]);
                        if (k==0)
                            DB.WordDoucmentToPosInsert(this.unique.get(j),Doucment_Name,this.pos,"_Lossless");
                        else
                            DB.WordDoucmentToPosInsert(this.unique.get(j),Doucment_Name,this.pos,"");

                        if (k==0)
                            DB.WordToDoucmentInsert(this.unique.get(j),Doucment_Number,"_Lossless");
                        else
                            DB.WordToDoucmentInsert(this.unique.get(j),Doucment_Number,"");
                    }
                }
            }
            System.out.print("\r"+Math.floor((i*100.000)/all)+"%");
        }
    }

    public void Get_New_Doucment()
    {
        int start=(new File(this.Con.Root_Path+"\\doucments")).listFiles().length;
        File[] listOfFiles = (new File(this.Con.Root_Path+"\\pages")).listFiles();
        for(int i=start;i<start+listOfFiles.length;i++)
        {
            String Name=Integer.toString(i);
            for(int j=Name.length();j<6;j++)
            {
                Name="0"+Name;
            }
            Name+=".txt";
            //System.out.println(Name);
            File source = new File(this.Con.Root_Path+"\\pages\\"+listOfFiles[i-start].getName());
            File destination = new File(this.Con.Root_Path+"\\doucments\\"+Name);
            if (!destination.exists()) {
                source.renameTo(destination);
            }
        }
        System.out.println("#"+listOfFiles.length +" New Doucments Added");
    }

    public void Create()
    {
        System.out.println("Increment With Last Indexer Is Start");
        this.Create_Word_to_Doucment();
        System.out.println();
        System.out.println("Increment With Last Indexer Is Done!");
    }
    public static void main(String[] args)throws Exception
    {
        NewIndexer I=new NewIndexer(5000);
        //I.Get_New_Doucment();
        //I.Create();
        I.DB.LdaWordDoucmentInsert();
    }
}

