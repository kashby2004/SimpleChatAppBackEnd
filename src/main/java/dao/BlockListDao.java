package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import dto.BlockListDto;
import org.bson.Document;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

public class BlockListDao extends BaseDao<BlockListDto> {

    // Singleton instance
    private static BlockListDao instance;

    // Supplier to create a new BlockListDao with the "BlockList" collection
    private static Supplier<BlockListDao> instanceSupplier =
            () -> new BlockListDao(MongoConnection.getCollection("BlockList"));

    // Private constructor to prevent external instantiation
    private BlockListDao(MongoCollection<Document> collection) {
        super(collection);
    }

    // Returns the singleton instance of BlockListDao
    public static BlockListDao getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = instanceSupplier.get();
        return instance;
    }

    // Allows overriding the instance supplier (useful for testing)
    public static void setInstanceSupplier(Supplier<BlockListDao> supplier) {
        instanceSupplier = supplier;
    }

    // Converts a Document into a BlockListDto
    @Override
    Supplier<BlockListDto> getFromDocument(Document document) {
        BlockListDto dto = new BlockListDto();
        dto.fromDocument(document);
        return () -> dto;
    }

    // ********** Add Block Record **********
    // Adds a new block record by standardizing the IDs, setting the block time, and saving the DTO.
    public boolean addBlock(String blockerId, String blockedId) {
        try {
            blockerId = blockerId.trim().toLowerCase();
            blockedId = blockedId.trim().toLowerCase();

            BlockListDto dto = new BlockListDto(blockerId, blockedId);
            dto.setBlockedAt(new Date()); // Set current time as block time.
            put(dto); // Save the record in the database.
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ********** Remove Block Record **********
    // Removes an existing block record based on the standardized blocker and blocked IDs.
    public boolean removeBlock(String blockerId, String blockedId) {
        try {
            blockerId = blockerId.trim().toLowerCase();
            blockedId = blockedId.trim().toLowerCase();
            Document query = new Document("blockerId", blockerId)
                    .append("blockedId", blockedId);
            DeleteResult result = collection.deleteOne(query);
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ********** Check Block Status **********
    // Checks if there is a block record between the given blocker and blocked.
    public boolean isBlocked(String blockerId, String blockedId) {
        blockerId = blockerId.trim().toLowerCase();
        blockedId = blockedId.trim().toLowerCase();
        Document query = new Document("blockerId", blockerId)
                .append("blockedId", blockedId);
        Document doc = collection.find(query).first();
        return doc != null;
    }

    // ********** Retrieve Block List **********
    // Retrieves all block records for a given blocker by standardizing the blockerId.
    public List<BlockListDto> getBlockList(String blockerId) {
        return query("blockerId", blockerId.trim().toLowerCase());
    }
}
