package dto;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Date;

public class FriendListDto extends BaseDto {
    private String userA;
    private String userB;
    private Date since;

    public FriendListDto() {}
    public FriendListDto(String userA, String userB, Date since) {
        this.userA = userA; this.userB = userB; this.since = since;
    }

    public String getUserA() { return userA; }
    public String getUserB() { return userB; }
    /**
     * Returns the date this friendship was created.
     * Currently not used, but kept for possible 'friends since' UI or analytics.
     */
    public Date getSince() { return since; }

    @Override
    public void fromDocument(Document document) {
        loadUniqueId(document);
        this.userA = document.getString("userA");
        this.userB = document.getString("userB");
        this.since = document.getDate("since");
    }

    @Override
    public Document toDocument() {
        Document doc = new Document();
        if (uniqueId != null) doc.put("_id", new ObjectId(uniqueId));
        doc.append("userA", userA).append("userB", userB).append("since", since);
        return doc;
    }
}