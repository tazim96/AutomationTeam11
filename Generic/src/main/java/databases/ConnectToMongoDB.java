package databases;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ConnectToMongoDB {

    public List<String> readMongoDbData(String database, String profile) {
        List<String> list = new ArrayList<String>();
        MongoClient mongoClient = new MongoClient();
        MongoDatabase mdb = mongoClient.getDatabase(database);
        MongoCollection collection = mdb.getCollection(profile);
        BasicDBObject basicDBObject = new BasicDBObject();
        FindIterable<Document> iterable = collection.find(basicDBObject);
        for (Document doc : iterable) {
            String item = (String) doc.get("items");
            list.add(item);
        }
        return list;
    }

}
