package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import dto.FriendListDto;
import org.bson.Document;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

public class FriendListDao extends BaseDao<FriendListDto> {
    private static FriendListDao instance;
    private static Supplier<FriendListDao> instanceSupplier =
            () -> new FriendListDao(MongoConnection.getCollection("FriendList"));

    private FriendListDao(MongoCollection<Document> collection) {
        super(collection);
    }

    public static FriendListDao getInstance() {
        if (instance != null) return instance;
        instance = instanceSupplier.get();
        return instance;
    }

    public static void setInstanceSupplier(Supplier<FriendListDao> supplier) {
        instanceSupplier = supplier;
    }

    @Override
    Supplier<FriendListDto> getFromDocument(Document document) {
        FriendListDto dto = new FriendListDto();
        dto.fromDocument(document);
        return () -> dto;
    }

    // Helper to normalize usernames for undirected friendship
    private String[] normalizeUsers(String userA, String userB) {
        userA = userA.trim().toLowerCase();
        userB = userB.trim().toLowerCase();
        if (userA.compareTo(userB) > 0) {
            String tmp = userA;
            userA = userB;
            userB = tmp;
        }
        return new String[]{userA, userB};
    }

    // Undirected friendship: always store (smallerName, largerName)
    public boolean addFriend(String userA, String userB) {
        String[] users = normalizeUsers(userA, userB);
        userA = users[0];
        userB = users[1];
        // Don't add duplicate
        if (isFriends(userA, userB)) return false;
        FriendListDto dto = new FriendListDto(userA, userB, new Date());
        put(dto);
        return true;
    }

    public boolean removeFriend(String userA, String userB) {
        String[] users = normalizeUsers(userA, userB);
        userA = users[0];
        userB = users[1];
        Document query = new Document("userA", userA).append("userB", userB);
        DeleteResult result = collection.deleteOne(query);
        return result.getDeletedCount() > 0;
    }

    public boolean isFriends(String userA, String userB) {
        String[] users = normalizeUsers(userA, userB);
        userA = users[0];
        userB = users[1];
        Document query = new Document("userA", userA).append("userB", userB);
        Document doc = collection.find(query).first();
        return doc != null;
    }

    // Get all friends for a user (either userA or userB)
    public List<FriendListDto> getFriends(String user) {
        user = user.trim().toLowerCase();
        List<FriendListDto> a = query("userA", user);
        List<FriendListDto> b = query("userB", user);
        a.addAll(b);
        return a;
    }
}