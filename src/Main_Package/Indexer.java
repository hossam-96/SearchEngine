package Main_Package; /**
 * Created by moham on 3/8/2017.
 */


import java.io.*;
import java.util.*;

public class Indexer {
    private long ALL_WORDS_NUM=0;
    private int Max_it;
    private Const Con=new Const();
    protected Hashtable<String,Set<Integer>> Word_to_Doucment;
    protected Hashtable<String,Hashtable<Integer,Set<Integer>>> WordDoucment_to_pos;
    //protected Hashtable<String,Set<Integer>> Word_to_Doucment_Lossless;
    //protected Hashtable<String,Hashtable<Integer,Set<Integer>>> WordDoucment_to_pos_LossLess;
    protected Hashtable<String,ArrayList<Float>> Distance_Between_Word_and_Doucment;
    protected Hashtable<Integer, ArrayList<ArrayList<String>>> Doucment_To_Words;
    //protected Hashtable<Integer, ArrayList<ArrayList<String>>> Doucment_To_Words_Lossless;
    private List<String> docs;
    private Cleaner C_Text;
    private  List<String> unique;
    private Hashtable<String, Integer> Map;
    private Set<Integer> pos;
    public Indexer(int max_it) {
        this.Max_it=max_it;
        this.C_Text=new Cleaner();
        Distance_Between_Word_and_Doucment=new Hashtable<String,ArrayList<Float>>();
        Word_to_Doucment = new Hashtable<String,Set<Integer>>();
        //Word_to_Doucment_Lossless = new Hashtable<String,Set<Integer>>();
        WordDoucment_to_pos = new Hashtable<String,Hashtable<Integer,Set<Integer>>> ();
        //WordDoucment_to_pos_LossLess = new Hashtable<String,Hashtable<Integer,Set<Integer>>> ();
        Doucment_To_Words=new Hashtable<Integer, ArrayList<ArrayList<String>>>();
        //Doucment_To_Words_Lossless=new Hashtable<Integer, ArrayList<ArrayList<String>>>();
        unique= new LinkedList<String>();
        Map = new Hashtable<String, Integer>();
        pos = new HashSet<Integer>();
        this.docs = new ArrayList<String>();

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
        this.pos=new HashSet<Integer>();
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
        String[] Doucmet_elements;
        ArrayList<ArrayList<String>> L;
        ArrayList<String> L1;
        ArrayList<String> L2;
        int all=Math.min(this.Max_it,listOfFiles.length);
        for (int i = 0; i < all; i++)
        {
            if (listOfFiles[i].isFile())
            {
                Doucment_Number = Integer.parseInt(listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length() - 4));
                if(this.Doucment_To_Words.containsKey(Doucment_Number))
                {
                    this.ALL_WORDS_NUM+=this.Doucment_To_Words.get(Doucment_Number).get(1).size();
                    this.docs.add(String.join(" ",this.Doucment_To_Words.get(Doucment_Number).get(1)));
                    continue;
                }
                Doucmet_elements=this.Get_Doucment_Text(listOfFiles[i].getName());
                //System.out.println("Indexing "+listOfFiles[i].getName()+" "+Doucmet_elements[1]);
                String Terms[][]=C_Text.Triple_Clean(Doucmet_elements[2]);
                /*{
                    L=new ArrayList<ArrayList<String>>();
                    L1=new ArrayList<String>(Arrays.asList(Terms[2]));
                    L2=new ArrayList<String>(Arrays.asList(Terms[0]));
                    L1.add(Doucmet_elements[0]);
                    L1.add(Doucmet_elements[1]);
                    L.add(L1);
                    L.add(L2);
                    Doucment_To_Words_Lossless.put(Doucment_Number,L);
                }*/
                {
                    L=new ArrayList<ArrayList<String>>();
                    L1=new ArrayList<String>(Arrays.asList(Terms[2]));
                    L2=new ArrayList<String>(Arrays.asList(Terms[1]));
                    L1.add(Doucmet_elements[0]);
                    L1.add(Doucmet_elements[1]);
                    L.add(L1);
                    L.add(L2);
                    Doucment_To_Words.put(Doucment_Number,L);
                    this.ALL_WORDS_NUM+=Terms[1].length;
                    this.docs.add(String.join(" ",Terms[1]));
                }

                for(int k=1;k<2;k++)
                {
                    this.get_unique_trems(Terms[k]);
                    for (int j = 0; j < this.unique.size(); j++) {
                        if (this.unique.get(j).equals("Stopword"))
                            continue;
                        this.get_term_pos(this.unique.get(j), Terms[k]);
                        if (k==0) {
                            /*if (this.WordDoucment_to_pos_LossLess.containsKey(this.unique.get(j)) == false) {
                                this.WordDoucment_to_pos_LossLess.put(this.unique.get(j), new Hashtable<Integer, Set<Integer>>());
                            }
                            this.WordDoucment_to_pos_LossLess.get(this.unique.get(j)).put(Doucment_Number, this.pos);*/
                        } else {
                            if (this.WordDoucment_to_pos.containsKey(this.unique.get(j)) == false) {
                                this.WordDoucment_to_pos.put(this.unique.get(j), new Hashtable<Integer, Set<Integer>>());
                            }
                            this.WordDoucment_to_pos.get(this.unique.get(j)).put(Doucment_Number, this.pos);
                        }

                        if (k==0) {
                           /* if (this.Word_to_Doucment_Lossless.containsKey(this.unique.get(j)) == false) {
                                this.Word_to_Doucment_Lossless.put(this.unique.get(j), new HashSet<Integer>());
                            }
                            this.Word_to_Doucment_Lossless.get(this.unique.get(j)).add(Doucment_Number);*/
                        } else {
                            if (this.Word_to_Doucment.containsKey(this.unique.get(j)) == false) {
                                this.Word_to_Doucment.put(this.unique.get(j), new HashSet<Integer>());
                            }
                            this.Word_to_Doucment.get(this.unique.get(j)).add(Doucment_Number);
                        }
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


    public void Create_Distance_Between_Word_and_Doucment()
    {
        try {
            ldaAlgo Model=new ldaAlgo(100,this.docs);
            Model.Train_LDA(4,this.ALL_WORDS_NUM);
            System.out.println("Starting Pre");
            Model.optimize();
            System.out.println("LDA buildWordTopicsProb Start");
            Model.buildWordTopicsProb();
            System.out.println("LDA buildTopicDocProb Start");
            Model.buildTopicDocProb();
            this.Distance_Between_Word_and_Doucment=Model.buildInvertedIndexDistribution();

        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public void save_Word_to_Doucment(String File_Name) {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        try {

            fout = new FileOutputStream(this.Con.Root_Path+"\\SavedFiles\\"+File_Name+".ser");
            oos = new ObjectOutputStream(fout);

            if(File_Name.equals("Word_to_Doucment"))
                 oos.writeObject(this.Word_to_Doucment);
            else
            if(File_Name.equals("Word_to_Doucment_Lossless"))
                 ;//oos.writeObject(this.Word_to_Doucment_Lossless);
            else
            if(File_Name.equals("WordDoucment_to_pos"))
                oos.writeObject(this.WordDoucment_to_pos);
            else
            if(File_Name.equals("WordDoucment_to_pos_LossLess"))
                ;//oos.writeObject(this.WordDoucment_to_pos_LossLess);
            else
            if(File_Name.equals("Distance_Between_Word_and_Doucment"))
                oos.writeObject(this.Distance_Between_Word_and_Doucment);
            else
            if(File_Name.equals("Doucment_To_Words"))
                oos.writeObject(this.Doucment_To_Words);
            else
            if(File_Name.equals("Doucment_To_Words_Lossless"))
                ;//oos.writeObject(this.Doucment_To_Words_Lossless);
             else System.out.println(File_Name);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public Hashtable<String,Set<Integer>> Read_Saved_Word_to_Doucment(String File_Name) {
        Hashtable<String,Set<Integer>> Saved_HashTable = null;
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        String filename = this.Con.Root_Path+"\\SavedFiles\\"+File_Name+".ser";
        try {

            fin = new FileInputStream(filename);
            ois = new ObjectInputStream(fin);
            Saved_HashTable = (Hashtable<String,Set<Integer>>) ois.readObject();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return Saved_HashTable;
    }

    public Hashtable<String,Hashtable<Integer,Set<Integer>>> Read_Saved_WordDoucment_to_pos(String File_Name) {
        Hashtable<String,Hashtable<Integer,Set<Integer>>> Saved_HashTable = null;
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        String filename = this.Con.Root_Path+"\\SavedFiles\\"+File_Name+".ser";
        try {

            fin = new FileInputStream(filename);
            ois = new ObjectInputStream(fin);
            Saved_HashTable = (Hashtable<String,Hashtable<Integer,Set<Integer>>>) ois.readObject();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return Saved_HashTable;
    }

    public Hashtable<String,ArrayList<Float>> Read_Distance(String File_Name) {
        Hashtable<String,ArrayList<Float>> Saved_HashTable = null;
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        String filename = this.Con.Root_Path+"\\SavedFiles\\"+File_Name+".ser";
        try {

            fin = new FileInputStream(filename);
            ois = new ObjectInputStream(fin);
            Saved_HashTable = (Hashtable<String,ArrayList<Float>>) ois.readObject();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return Saved_HashTable;
    }

    public Hashtable<Integer, ArrayList<ArrayList<String>>> Read_Saved_Doucment_To_Words(String File_Name) {
        Hashtable<Integer, ArrayList<ArrayList<String>>> Saved_HashTable = null;
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        String filename = this.Con.Root_Path+"\\SavedFiles\\"+File_Name+".ser";
        try {

            fin = new FileInputStream(filename);
            ois = new ObjectInputStream(fin);
            Saved_HashTable = (Hashtable<Integer, ArrayList<ArrayList<String>>>) ois.readObject();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return Saved_HashTable;
    }


    public void Create()
    {
        this.Load();
        System.out.println("Increment With Last Indexer");
        this.Create_Word_to_Doucment();
        System.out.println();
        System.out.println("Increment With Last Indexer Is Done!");
        System.out.println("Unique Words In Indexer#"+this.Word_to_Doucment.size());
        this.Create_Distance_Between_Word_and_Doucment();
    }
    public void Save()
    {
        System.out.println("Start Saving Word_to_Doucment");
        this.save_Word_to_Doucment("Word_to_Doucment");
        //System.out.println("Start Saving Word_to_Doucment_Lossless");
        //this.save_Word_to_Doucment("Word_to_Doucment_Lossless");
        System.out.println("Start Saving WordDoucment_to_pos");
        this.save_Word_to_Doucment("WordDoucment_to_pos");
        //System.out.println("Start Saving WordDoucment_to_pos_LossLess");
        //this.save_Word_to_Doucment("WordDoucment_to_pos_LossLess");
        System.out.println("Start Saving Distance_Between_Word_and_Doucment");
        this.save_Word_to_Doucment("Distance_Between_Word_and_Doucment");
        //System.out.println("Start Saving Doucment_To_Words_Lossless");
        //this.save_Word_to_Doucment("Doucment_To_Words_Lossless");
        System.out.println("Start Saving Doucment_To_Words");
        this.save_Word_to_Doucment("Doucment_To_Words");
        System.out.println("Saving Is Done!");
    }


    public void Load()
    {
        System.out.println("Loding Pre Created Inderxer ....");
        if((new File(this.Con.Root_Path+"\\SavedFiles\\"+"Word_to_Doucment"+".ser")).exists())
        this.Word_to_Doucment=this.Read_Saved_Word_to_Doucment("Word_to_Doucment");

        //if((new File(this.Con.Root_Path+"\\SavedFiles\\"+"Word_to_Doucment_lossless"+".ser")).exists())
          //  this.Word_to_Doucment_Lossless=this.Read_Saved_Word_to_Doucment("Word_to_Doucment_lossless");

        if((new File(this.Con.Root_Path+"\\SavedFiles\\"+"WordDoucment_to_pos"+".ser")).exists())
            this.WordDoucment_to_pos=this.Read_Saved_WordDoucment_to_pos("WordDoucment_to_pos");

        //if((new File(this.Con.Root_Path+"\\SavedFiles\\"+"WordDoucment_to_pos_lossless"+".ser")).exists())
            //this.WordDoucment_to_pos_LossLess=this.Read_Saved_WordDoucment_to_pos("WordDoucment_to_pos_lossless");

        if((new File(this.Con.Root_Path+"\\SavedFiles\\"+"Distance_Between_Word_and_Doucment"+".ser")).exists())
            this.Distance_Between_Word_and_Doucment = this.Read_Distance("Distance_Between_Word_and_Doucment");

        if((new File(this.Con.Root_Path+"\\SavedFiles\\"+"Doucment_To_Words"+".ser")).exists())
            this.Doucment_To_Words=this.Read_Saved_Doucment_To_Words("Doucment_To_Words");

        //if((new File(this.Con.Root_Path+"\\SavedFiles\\"+"Doucment_To_Words_Lossless"+".ser")).exists())
          //  this.Doucment_To_Words_Lossless=this.Read_Saved_Doucment_To_Words("Doucment_To_Words_Lossless");

        System.out.println("Loding Pre Created Is Done!");
    }
    public void Print()
    {
        System.out.println(this.Word_to_Doucment);
        //System.out.println(this.Word_to_Doucment_Lossless);
        System.out.println(this.WordDoucment_to_pos);
        //System.out.println(this.WordDoucment_to_pos_LossLess);
        System.out.println(this.Distance_Between_Word_and_Doucment);
        System.out.println(this.Doucment_To_Words);
        //System.out.println(this.Doucment_To_Words_Lossless);
    }
}

