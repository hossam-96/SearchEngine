package Main_Package;

/**
 * Created by moham on 3/17/2017.
 */
import com.mongodb.*;

public class MongoDB {

    public static void main(String[] args) {

        Mongo mongo = null;
        DB db=null;
        DBCollection table=null;

        // Connection to the MongoDB-Server
        try {
            mongo = new Mongo("localhost", 27017);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //insert data
        db = mongo.getDB("SE");
        table = db.getCollection("cus");

        //create document and insert
        BasicDBObject document = new BasicDBObject();
        document.put("name", "Andre");
        document.put("age", 34);

        BasicDBObject document2 = new BasicDBObject();
        document2.put("name", "Beatrix");
        document2.put("age", 19);

        table.insert(document);
        table.insert(document2);

    }
}