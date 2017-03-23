package Main_Package;

/**
 * Created by moham on 3/17/2017.
 */
import com.mongodb.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class MongoDB {

    Mongo mongo = null;
    DB db=null;
    DBCollection table=null;

    public MongoDB()throws Exception
    {
        // Connection to the MongoDB-Server
        this.mongo = new Mongo("localhost", 27017);
        // Connect to Database
        this.db = mongo.getDB("Idx");
    }

    public void Insert_Word_to_Doucment(Hashtable<String,Set<Integer>> Word_to_Doucment)
    {
        table=db.getCollection("Word_to_Doucment");
        table.drop();
        BasicDBObject document;
        for (String key: Word_to_Doucment.keySet()) {
            document = new BasicDBObject();
            document.put("Word",key);
            document.put("Doucments",Word_to_Doucment.get(key));
            table.insert(document);
        }
    }

    public Hashtable<String,Set<Integer>> Get_Word_to_Doucment()
    {
        Hashtable<String,Set<Integer>> Word_to_Doucment=new Hashtable<String,Set<Integer>>();
        table=db.getCollection("Word_to_Doucment");
        DBCursor Cursor=table.find();
        while(Cursor.hasNext())
        {
            DBObject CN=Cursor.next();
            Word_to_Doucment.put((String)CN.get("Word"),new HashSet<Integer>((ArrayList<Integer>)CN.get("Doucments")));
        }
        return Word_to_Doucment;
    }

    public void Insert_Doucment_To_Words(Hashtable<Integer, ArrayList<ArrayList<String>>> Doucment_To_Words)
    {
        table=db.getCollection("Doucment_To_Words");
        table.drop();
        BasicDBObject document;
        for (Integer key: Doucment_To_Words.keySet()) {
            document = new BasicDBObject();
            document.put("Doucment",key);
            document.put("WordsBeforCleaning",Doucment_To_Words.get(key).get(0));
            document.put("WordsAfterCleaning",Doucment_To_Words.get(key).get(1));
            table.insert(document);
        }
    }

    public Hashtable<Integer,ArrayList<ArrayList<String>>> Get_Doucment_To_Words()
    {
        Hashtable<Integer, ArrayList<ArrayList<String>>> Doucment_To_Words=new Hashtable<Integer, ArrayList<ArrayList<String>>>();
        table=db.getCollection("Doucment_To_Words");
        DBCursor Cursor=table.find();
        while(Cursor.hasNext())
        {
            DBObject CN=Cursor.next();
            ArrayList<ArrayList<String>> x =new ArrayList<ArrayList<String>>();
            x.add((ArrayList<String>)CN.get("WordsBeforCleaning"));
            x.add((ArrayList<String>)CN.get("WordsAfterCleaning"));
            Doucment_To_Words.put((Integer) CN.get("Doucment"),x);
        }
        return Doucment_To_Words;
    }

    public void Insert_Distance_Between_Word_and_Doucment(Hashtable<String,ArrayList<Float>> Distance_Between_Word_and_Doucment)
    {
        table=db.getCollection("Distance_Between_Word_and_Doucment");
        table.drop();
        BasicDBObject document;
        for (String key: Distance_Between_Word_and_Doucment.keySet()) {
            document = new BasicDBObject();
            document.put("Word",key);
            document.put("Distance_to_Doucments",Distance_Between_Word_and_Doucment.get(key));
            table.insert(document);
        }
    }

    public Hashtable<String,ArrayList<Float>> GET_Distance_Between_Word_and_Doucment() {
        Hashtable<String, ArrayList<Float>> Distance_Between_Word_and_Doucment = new Hashtable<String, ArrayList<Float>>();
        table = db.getCollection("Distance_Between_Word_and_Doucment");
        DBCursor Cursor = table.find();
        while (Cursor.hasNext()) {
            DBObject CN = Cursor.next();
            Distance_Between_Word_and_Doucment.put((String) CN.get("Word"), (ArrayList<Float>) CN.get("Distance_to_Doucments"));
        }
        return Distance_Between_Word_and_Doucment;
    }

    public void Insert_WordDoucment_to_pos(Hashtable<String,Hashtable<Integer,Set<Integer>>> WordDoucment_to_pos)
    {
        table=db.getCollection("WordDoucment_to_pos");
        table.drop();
        BasicDBObject document;
        for (String key: WordDoucment_to_pos.keySet()) {
            document = new BasicDBObject();
            document.put("Word",key);
            document.put("Doucments_Pos",WordDoucment_to_pos.get(key).toString());
            table.insert(document);
        }
    }

    public Hashtable<String,Hashtable<Integer,Set<Integer>>> Get_WordDoucment_to_pos()
    {
        Hashtable<String,Hashtable<Integer,Set<Integer>>> WordDoucment_to_pos=new  Hashtable<String,Hashtable<Integer,Set<Integer>>>();
        table=db.getCollection("WordDoucment_to_pos");
        DBCursor Cursor = table.find();
        while (Cursor.hasNext()) {
            DBObject CN = Cursor.next();
            String Step1=(String)CN.get("Doucments_Pos");
            System.out.println((Step1).substring(1,Step1.length()-1));
            WordDoucment_to_pos.put((String)CN.get("Word"), new Hashtable<Integer,Set<Integer>>());
        }
        return WordDoucment_to_pos;
    }


    public void MongoDB_Save(Indexer I)
    {
        this.Insert_Distance_Between_Word_and_Doucment(I.Distance_Between_Word_and_Doucment);
        this.Insert_Doucment_To_Words(I.Doucment_To_Words);
        this.Insert_Word_to_Doucment(I.Word_to_Doucment);
        this.Insert_WordDoucment_to_pos(I.WordDoucment_to_pos);
    }



    public static void main(String[] args)throws Exception
    {

        //Indexer I=new Indexer();
        //I.Load();
        MongoDB DB=new MongoDB();
        //DB.MongoDB_Save(I);
        DB.Get_WordDoucment_to_pos();
        //DB.MongoDB_Save(I);
        /*table = db.getCollection("Names");

        //create document and insert
        BasicDBObject document = new BasicDBObject();
        document.put("name", "Andre");
        document.put("age", 34);

        BasicDBObject document2 = new BasicDBObject();
        document2.put("name", "Beatrix");
        document2.put("age", 19);

        table.insert(document);
        table.insert(document2);
        System.out.println("a8");*/
    }
}