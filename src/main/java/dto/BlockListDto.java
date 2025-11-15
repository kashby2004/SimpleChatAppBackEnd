package dto;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Date;

public class BlockListDto extends BaseDto {
    // The ID of the user who performs the block.
    private String blockerId;
    // The ID of the user who is being blocked.
    private String blockedId;
    // The date and time when the block was performed.
    private Date blockedAt;

    // Default constructor.
    public BlockListDto() {
    }

    // Constructor that initializes the blocker and blocked IDs, and sets the block time to now.
    public BlockListDto(String blockerId, String blockedId) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
        this.blockedAt = new Date(); // Sets to current timestamp.
    }

    // Getter for blockerId.
    public String getBlockerId() {
        return blockerId;
    }

    // Setter for blockerId.
    public void setBlockerId(String blockerId) {
        this.blockerId = blockerId;
    }

    // Getter for blockedId.
    public String getBlockedId() {
        return blockedId;
    }

    // Setter for blockedId.
    public void setBlockedId(String blockedId) {
        this.blockedId = blockedId;
    }

    // Getter for blockedAt.
    public Date getBlockedAt() {
        return blockedAt;
    }

    // Setter for blockedAt.
    public void setBlockedAt(Date blockedAt) {
        this.blockedAt = blockedAt;
    }

    // Populate this DTO from a MongoDB Document.
    @Override
    public void fromDocument(Document document) {
        loadUniqueId(document); // Loads the unique identifier from the document into BaseDto.
        this.blockerId = document.getString("blockerId");  // Extracts the blockerId field.
        this.blockedId = document.getString("blockedId");    // Extracts the blockedId field.
        this.blockedAt = document.getDate("blockedAt");        // Extracts the block timestamp.
    }

    // Convert this DTO into a MongoDB Document.
    @Override
    public Document toDocument() {
        Document doc = new Document();
        if (uniqueId != null) { // If a unique ID exists, include it in the document.
            doc.put("_id", new ObjectId(uniqueId));
        }
        doc.append("blockerId", blockerId)
                .append("blockedId", blockedId)
                .append("blockedAt", blockedAt);
        return doc;
    }

    // This inner DTO is used to encapsulate the result of a "user existence" check.
    // It wraps a boolean flag indicating whether the queried user exists.
    public static class ExistsDto extends BaseDto {
        private boolean exists;

        // Constructor that sets the existence flag.
        public ExistsDto(boolean exists) {
            this.exists = exists;
        }

        // Getter for the existence flag.
        public boolean isExists() {
            return exists;
        }

        // Setter for the existence flag.
        public void setExists(boolean exists) {
            this.exists = exists;
        }

        // This method is not implemented because this DTO is typically not reconstructed from a Document.
        @Override
        public void fromDocument(Document document) {
        }

        // Convert this ExistsDto to a Document with a single field "exists".
        @Override
        public Document toDocument() {
            return new Document("exists", exists);
        }
    }
}
