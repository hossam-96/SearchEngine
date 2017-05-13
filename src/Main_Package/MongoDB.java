package Main_Package;

/**
 * Created by moham on 3/17/2017.
 */
import Crawler.Database;
import com.mongodb.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;


public class MongoDB {

    private Const Con=new Const();
    Map<Integer, Integer> PharsingQueryFirstPos;
    Hashtable<Integer,Integer> MaxPr;
    Mongo mongo = null;
    DB db=null;
    DBCollection table=null;
    long UniqueWord=0;
    public MongoDB()throws Exception
    {
        // Connection to the MongoDB-Server
        this.mongo = new Mongo("localhost", 27017);
        // Connect to Database
        this.db = mongo.getDB("Idx");
    }
    public void Drop()
    {
        db.getCollection("Word_to_Doucment").drop();
        db.getCollection("Doucment_To_Words").drop();
        db.getCollection("WordDoucment_to_pos").drop();
        db.getCollection("Word_to_Doucment_Lossless").drop();
        db.getCollection("Doucment_To_Words_Lossless").drop();
        db.getCollection("WordDoucment_to_pos_Lossless").drop();
    }
    public void CreateDatabase()
    {

        db.getCollection("Word_to_Doucment").createIndex(new BasicDBObject("Word", 1),new BasicDBObject("unique", true));


        db.getCollection("Doucment_To_Words").createIndex(new BasicDBObject("Doucment_Number", 1),new BasicDBObject("unique", true));
        DBObject document = new BasicDBObject();
        document.put("Doucment_Number",-1);
        document.put("WordsBeforCleaning",new ArrayList<>());
        document.put("WordsAfterCleaning",new ArrayList<>());
        document.put("Url","");
        document.put("Title","");
        db.getCollection("Doucment_To_Words").insert(document);

        db.getCollection("WordDoucment_to_pos").createIndex(new BasicDBObject("WordDoucment", 1),new BasicDBObject("unique", true));


        db.getCollection("Word_to_Doucment_Lossless").createIndex(new BasicDBObject("Word", 1),new BasicDBObject("unique", true));


        db.getCollection("Doucment_To_Words_Lossless").createIndex(new BasicDBObject("Doucment_Number", 1),new BasicDBObject("unique", true));
        document = new BasicDBObject();
        document.put("Doucment_Number",-1);
        document.put("WordsBeforCleaning",new ArrayList<>());
        document.put("WordsAfterCleaning",new ArrayList<>());
        db.getCollection("Doucment_To_Words_Lossless").insert(document);
        document.put("Url","");
        document.put("Title","");
        db.getCollection("WordDoucment_to_pos_Lossless").createIndex(new BasicDBObject("WordDoucment", 1),new BasicDBObject("unique", true));


        db.getCollection("Lda_Word_Doucment").createIndex(new BasicDBObject("WordDoucment", 1),new BasicDBObject("unique", true));
        document = new BasicDBObject();
        document.put("Doucment_Number",-1);
        document.put("Close",0);
        document.put("PopularRank",0);
        document.put("WordDoucment","");
        db.getCollection("Lda_Word_Doucment").insert(document);

    }

    public void WordToDoucmentInsert(String Word, int DocNum,String Loss)
    {

        DBObject document = new BasicDBObject();
        document.put("Word",Word);
        int[] data = {DocNum};
        document.put("Doucments_Num",data);
        try {
            db.getCollection("Word_to_Doucment"+Loss).insert(WriteConcern.SAFE,document);
        }catch (Exception e)
        {
            //System.out.println(e.getMessage());
            DBObject findQuery = new BasicDBObject("Word", Word);
            DBObject listItem = new BasicDBObject("Doucments_Num",DocNum);
            DBObject updateQuery = new BasicDBObject("$addToSet", listItem);
            db.getCollection("Word_to_Doucment"+Loss).update(findQuery, updateQuery);
        }
    }

    public void DoucmentToWordsInsert(Integer DocNum,ArrayList<String> L1,ArrayList<String>L2,String Title,String Url,String Loss)
    {
        DBObject document = new BasicDBObject();
        document.put("Doucment_Number",DocNum);
        document.put("WordsBeforCleaning",L1);
        document.put("WordsAfterCleaning",L2);
        document.put("Url",Url);
        document.put("Title",Title);
        db.getCollection("Doucment_To_Words"+Loss).insert(document);
    }

    public int DoucmentToWordsLastDoucment(String Loss)
    {
        DBObject CN = db.getCollection("Doucment_To_Words"+Loss).find().sort(new BasicDBObject("Doucment_Number",-1)).limit(1).next();
        int MaxDocNum=(Integer) CN.get("Doucment_Number");
        return MaxDocNum;
    }

    public void WordDoucmentToPosInsert(String Word,String DocNum,ArrayList<Integer>Pos,String Loss)
    {
        DBObject document = new BasicDBObject();
        document.put("WordDoucment",Word+DocNum);
        document.put("Postions",Pos);
        db.getCollection("WordDoucment_to_pos"+Loss).insert(document);
    }

    public int WordDoucmentToPosGetFirstPos(String Word,String DocNum,String Loss)
    {
        BasicDBObject query = new BasicDBObject("WordDoucment", Word+DocNum);
        BasicDBObject fields = new BasicDBObject("Postions", new BasicDBObject("$slice", new int[] { 0, 1 }));
        DBCursor Cursor = db.getCollection("WordDoucment_to_pos"+Loss).find(query,fields);
        if(Cursor.hasNext())
        {
            return ((ArrayList<Integer>)Cursor.next().get("Postions")).get(0);
        }
        return -1;
    }

    public Set<Integer> WordToDoucmentGetUnion(String[] Terms)
    {
        Set<Integer>Ret=new HashSet<>();
        Set<Integer>Intersection=new HashSet<>();
        HashMap<Integer,Integer>Map=new HashMap<>();
        DBCursor Cursor=db.getCollection("Word_to_Doucment").find(new BasicDBObject("Word",new BasicDBObject("$in",Terms)));
        if(Cursor.hasNext())
        {
            ArrayList<Integer> Docs=(ArrayList<Integer>) Cursor.next().get("Doucments_Num");
            Ret.addAll(Docs);
        }
        WordToDoucmentGetIntersectionNoOrder(Terms);
        return Ret;
    }
    public void WordToDoucmentGetIntersectionNoOrder(String[] Terms) {
        Set<Integer> Ret = new HashSet<>();
        for (int i = 0; i < Terms.length; i++) {
            DBCursor Cursor = db.getCollection("Word_to_Doucment").find(new BasicDBObject("Word", Terms[i]));
            ArrayList<Integer> Docs;
            if (Cursor.hasNext())
                Docs = (ArrayList<Integer>) Cursor.next().get("Doucments_Num");
            else Docs = new ArrayList<Integer>();
            if (i == 0) Ret.addAll(Docs);
            else
                Ret.retainAll(Docs);//intersection
        }
        MaxPr=new Hashtable<>();
        for(Integer DocNum:Ret)
        {
            MaxPr.put(DocNum,100);
        }
    }
    public Set<Integer> WordToDoucmentGetIntersection(String[] Terms)
    {
        Set<Integer>Ret=new HashSet<>();
        for(int i=0;i<Terms.length;i++)
        {
            DBCursor Cursor=db.getCollection("Word_to_Doucment_Lossless").find(new BasicDBObject("Word",Terms[i]));
            ArrayList<Integer> Docs;
            if(Cursor.hasNext())
            Docs=(ArrayList<Integer>) Cursor.next().get("Doucments_Num");
            else Docs=new ArrayList<Integer>();
            if(i==0)Ret.addAll(Docs);
            else
                Ret.retainAll(Docs);//intersection
        }
        Set<Integer> HaveAllInorder=new HashSet<>();
        PharsingQueryFirstPos = new HashMap<Integer, Integer>();
        if(Terms.length>1)
        {
            for(Integer DocNum:Ret)
            {
                Set<Integer> PosSet=new HashSet<>();
                String DoucmentNumberString=Integer.toString(DocNum);
                while (DoucmentNumberString.length()!=6)DoucmentNumberString="0"+DoucmentNumberString;
                for(int i=0;i<Terms.length;i++)
                {
                    DBCursor Cursor=db.getCollection("WordDoucment_to_pos_Lossless").find(new BasicDBObject("WordDoucment",Terms[i]+DoucmentNumberString));
                    ArrayList<Integer> Pos = (ArrayList<Integer>) Cursor.next().get("Postions");

                    for (int j = 0; j < Pos.size(); j++) {
                        Pos.set(j, Pos.get(j) - i);
                    }
                    if (i == 0)
                        PosSet.addAll(Pos);
                    else
                        PosSet.retainAll(Pos);

                }
                if(PosSet.size()>0) {
                    HaveAllInorder.add(DocNum);
                    PharsingQueryFirstPos.put(DocNum,PosSet.iterator().next());
                }
            }
        }else
        {
            HaveAllInorder.addAll(Ret);
            for(Integer DocNum:Ret) {
                String DoucmentNumberString = Integer.toString(DocNum);
                while (DoucmentNumberString.length() != 6) DoucmentNumberString = "0" + DoucmentNumberString;
                for (int i = 0; i < Terms.length; i++)
                {
                    PharsingQueryFirstPos.put(DocNum,WordDoucmentToPosGetFirstPos(Terms[i],DoucmentNumberString,"_Lossless"));
                }
            }
        }
        return HaveAllInorder;
    }

    private String Get_Doucment_Text(String Doucment_Name) {
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
        return Ret[2];
    }

    public List<String> DoucmentToWordsGetWordsAfterCleaning()
    {
        File folder = new File(this.Con.Root_Path+"\\doucments");
        File[] listOfFiles = folder.listFiles();
        long All=listOfFiles.length;
        long Done=0;
        long f=All/100;

        this.UniqueWord=0;
        List<String> Ret=new ArrayList<>();
        DBObject CN;
        DBCursor Cursor = db.getCollection("Doucment_To_Words").find();
        System.out.println("Getting Training Set From Database is Starting");
        while (Cursor.hasNext())
        {
            CN=Cursor.next();
            ArrayList<String> list=(ArrayList<String>) CN.get("WordsAfterCleaning");
            this.UniqueWord+=list.size();
            String listString = String.join(" ", list);
            Ret.add(listString);
            if(Done%f==0)
                System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
            Done++;
        }

        Cleaner C_Text=new Cleaner();
        String[] NewList;
        for (int i = Ret.size(); i < listOfFiles.length; i++)
        {
            if (listOfFiles[i].isFile())
            {
                String Doucmet_Content=this.Get_Doucment_Text(listOfFiles[i].getName());
                NewList=C_Text.Clean_Text(Doucmet_Content);
                this.UniqueWord+=NewList.length;
                Ret.add(String.join(" ",NewList));
                if(Done%f==0)
                    System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
                Done++;
            }
        }
        System.out.println();
        System.out.println("Getting Training Set From Database is Done");
        return Ret;
    }

    public DBObject DoucmentToWordsGetSlice(int start,int DocNum,String Loss)
    {
        ArrayList<ArrayList<String>>Ret;
        ArrayList<String>L1=new ArrayList<>();
        ArrayList<String>L2=new ArrayList<>();
        BasicDBObject query = new BasicDBObject("Doucment_Number", DocNum);
        BasicDBObject fields = new BasicDBObject("WordsBeforCleaning", new BasicDBObject("$slice", new int[] { start, 30 }));
        fields.append("WordsAfterCleaning", new BasicDBObject("$slice", new int[] { start, 30 }));
        DBCursor Cursor = db.getCollection("Doucment_To_Words"+Loss).find(query,fields);
        return Cursor.next();
    }

    public void LdaWordTopicsInsert(Hashtable<String , ArrayList<Float>> wordTopicProb)
    {
        db.getCollection("Lda_Word_Topics").drop();
        db.getCollection("Lda_Word_Topics").createIndex(new BasicDBObject("Word", 1),new BasicDBObject("unique", true));

        db.getCollection("Lda_Word_Doucment").drop();
        db.getCollection("Lda_Word_Doucment").createIndex(new BasicDBObject("WordDoucment", 1),new BasicDBObject("unique", true));
        DBObject document = new BasicDBObject();
        document.put("Doucment_Number",-1);
        document.put("Close",0);
        document.put("PopularRank",0);
        document.put("WordDoucment","");
        db.getCollection("Lda_Word_Doucment").insert(document);




        System.out.println("Adding Training Results To Database is Starting");
        Enumeration<String> Words = wordTopicProb.keys();
        while(Words.hasMoreElements()) {
            String Word = Words.nextElement();
            ArrayList<Float> Prob = wordTopicProb.get(Word);

            document = new BasicDBObject();
            document.put("Word",Word);
            document.put("Probability",Prob);
            db.getCollection("Lda_Word_Topics").insert(document);
        }
        System.out.println("Adding Training Results To Database is Done");

    }

    public void LdaWordDoucmentInsert()throws Exception
    {
        Database Mysqldb=new Database();
        Mysqldb.SetMaxPopularRank();



        int All=DoucmentToWordsLastDoucment("");
        int Done=0;

        System.out.println("New Documents Classification Start");
        //Get First Document Not Classified
        DBObject CN = db.getCollection("Lda_Word_Doucment").find().sort(new BasicDBObject("Doucment_Number",-1)).limit(1).next();
        int LastAdded=(Integer) CN.get("Doucment_Number");
        All-=LastAdded;
        long f=All/100;
        //Get ALL Unique Words In Lda
        long WordNum=db.getCollection("Lda_Word_Topics").count();
        //Get All Document Words That Not Classified
        DBCursor Cursor=db.getCollection("Doucment_To_Words").find(new BasicDBObject("Doucment_Number",new BasicDBObject("$gt",LastAdded)));
        //Loop Over Not Classified Documents
        while (Cursor.hasNext())
        {
            CN=Cursor.next();
            ArrayList<String> DocumentsWords=(ArrayList<String>) CN.get("WordsAfterCleaning");

            Map<String, Integer> DocumentsWordsFreq = new HashMap<String, Integer>();
            for (String temp : DocumentsWords) {
                Integer count = DocumentsWordsFreq.get(temp);
                DocumentsWordsFreq.put(temp, (count == null) ? 1 : count + 1);
            }


            int DoucmentNumber=(Integer)CN.get("Doucment_Number");

            float PopularRank=Mysqldb.GetPopularRank((String) CN.get("Url"));

            String DoucmentNumberString=Integer.toString(DoucmentNumber);
            while (DoucmentNumberString.length()!=6)DoucmentNumberString="0"+DoucmentNumberString;

            Double[] DoucmentProbability = new Double[100];

            Arrays.fill(DoucmentProbability,new Double(0.0));

            //Get Topic Distribution For All Words In Topic
            DBCursor ProbCursor=db.getCollection("Lda_Word_Topics").find(new BasicDBObject("Word",new BasicDBObject("$in",DocumentsWords)));
            //Loop Over Words In Document And add Topic Distribution For Words
            //To get Topic Distribution For Document
            ArrayList<ArrayList<Double>>WordsTopic=new ArrayList<>();
            ArrayList<String>WordsDocument=new ArrayList<>();
            long NumRows=0;
            while(ProbCursor.hasNext())
            {
                DBObject ProbCN=ProbCursor.next();
                ArrayList<Double>WordProbability=(ArrayList<Double>)ProbCN.get("Probability");
                String LdaWord=(String)ProbCN.get("Word");
                WordsDocument.add(LdaWord+DoucmentNumberString);
                WordsTopic.add(WordProbability);
                for(int i=0;i<WordProbability.size();i++)
                {
                    DoucmentProbability[i]+=WordProbability.get(i)*DocumentsWordsFreq.get(LdaWord);
                }
                NumRows+=DocumentsWordsFreq.get(LdaWord);
            }
            for(int i=0;i<100;i++)
            {
                DoucmentProbability[i]/=NumRows;
            }
            for(int j=0;j<WordsTopic.size();j++)
            {
                ArrayList<Double>WordProbability=WordsTopic.get(j);
                Double WordDocument=0.0;
                Double Mag1=0.0,Mag2=0.0;
                for(int i=0;i<100;i++)
                {
                    WordDocument+=(DoucmentProbability[i]*WordProbability.get(i));
                    Mag1+=DoucmentProbability[i]*DoucmentProbability[i];
                    Mag2+=WordProbability.get(i)*WordProbability.get(i);
                }
                Mag1=Math.sqrt(Mag1);
                Mag2=Math.sqrt(Mag2);
                WordDocument/=(Mag1*Mag2);

                DBObject document = new BasicDBObject();
                document.put("Doucment_Number",DoucmentNumber);
                document.put("Close",WordDocument);
                document.put("PopularRank",PopularRank);
                document.put("WordDoucment",WordsDocument.get(j));
                db.getCollection("Lda_Word_Doucment").insert(document);

            }

            if(Done%f==0)
                System.out.print("\r" + Math.floor((Done * 100.0) / All) + "%");
            Done++;
        }
        System.out.println();
    }

    public double LdaWordDoucmentGet(ArrayList<String> WordDocuments)
    {
        double Relevance=0.0;
        DBCursor Cursor=db.getCollection("Lda_Word_Doucment").find(new BasicDBObject("WordDoucment",new BasicDBObject("$in",WordDocuments)));
        while(Cursor.hasNext())
        {
            DBObject CN=Cursor.next();
            int DocNum=(int)CN.get("Doucment_Number");
            Relevance+=(double) CN.get("Close");
            if(this.MaxPr!=null)
            if(this.MaxPr.containsKey(DocNum))
                Relevance+=1000;
        }
        Relevance/=WordDocuments.size();
        return Relevance;
    }

    public static void main(String[] args)throws Exception
    {
        MongoDB DB=new MongoDB();
        int x=0;
        DBCursor Cursor = DB.db.getCollection("Doucment_To_Words").find();
        while (Cursor.hasNext())
        {
            DBObject CN=Cursor.next();
            ArrayList<String> L1=(ArrayList<String>) CN.get("WordsAfterCleaning");
            ArrayList<String> L2=(ArrayList<String>) CN.get("WordsBeforCleaning");
            if(L1.size()!=L2.size())
            {
                x++;
            }
        }
        System.out.println(x);
    }
}