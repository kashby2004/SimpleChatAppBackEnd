package handler;

import auth.AuthFilter;
import dao.BlockListDao;
import dao.UserDao;
import dto.BlockListDto;
import dto.UserDto;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

import java.util.Collections;
import java.util.List;

public class BlockListHandler implements BaseHandler {

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {
        // Retrieve the "action" parameter from the request URL
        String action = request.getQueryParam("action");
        if (action == null || action.trim().isEmpty()) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(false, null, "Action parameter is required"));
        }

        // If the action is "getBlockList", return the list of blocked users for the specified blocker.
        if ("getBlockList".equalsIgnoreCase(action)) {
            String blockerId = request.getQueryParam("blockerId");
            if (blockerId == null || blockerId.trim().isEmpty()) {
                return new ResponseBuilder()
                        .setStatus(StatusCodes.BAD_REQUEST)
                        .setBody(new RestApiAppResponse<>(false, null, "blockerId parameter is required"));
            }
            // Standardize by trimming and converting to lowercase.
            blockerId = blockerId.trim().toLowerCase();
            List<BlockListDto> blockList = BlockListDao.getInstance().getBlockList(blockerId);
            return new ResponseBuilder()
                    .setStatus(StatusCodes.OK)
                    .setBody(new RestApiAppResponse<>(true, blockList, null));
        }

        // If the action is "exists", check if a user exists.
        if ("exists".equalsIgnoreCase(action)) {
            String username = request.getQueryParam("username");
            if (username == null || username.trim().isEmpty()) {
                return new ResponseBuilder()
                        .setStatus(StatusCodes.BAD_REQUEST)
                        .setBody(new RestApiAppResponse<>(false, Collections.singletonList(new BlockListDto.ExistsDto(false)),
                                "Username parameter is required"));
            }
            // Standardize the username.
            username = username.trim().toLowerCase();
            // Try querying using two possible field names.
            List<UserDto> users = UserDao.getInstance().query("userName", username);
            if (users.isEmpty()) {
                users = UserDao.getInstance().query("username", username);
            }
            boolean exists = (users != null && !users.isEmpty());
            return new ResponseBuilder()
                    .setStatus(StatusCodes.OK)
                    .setBody(new RestApiAppResponse<>(true, Collections.singletonList(new BlockListDto.ExistsDto(exists)), null));
        }

        // For block and unblock operations, first verify user authentication.
        AuthFilter.AuthResult authResult = AuthFilter.doFilter(request);
        if (!authResult.isLoggedIn) {
            return new ResponseBuilder().setStatus(StatusCodes.UNAUTHORIZED);
        }

        // Retrieve the blockerId and blockedId from the request parameters.
        String blockerId = request.getQueryParam("blockerId");
        String blockedId = request.getQueryParam("blockedId");
        if (blockerId == null || blockedId == null) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(false, null, "Missing parameters: blockerId or blockedId"));
        }
        // Standardize the IDs.
        blockerId = blockerId.trim().toLowerCase();
        blockedId = blockedId.trim().toLowerCase();

        // Prevent a user from blocking themselves.
        if (blockerId.equals(blockedId)) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(false, null, "Cannot block yourself"));
        }

        // Query for the blocked user's details from the database.
        List<UserDto> users = UserDao.getInstance().query("userName", blockedId);
        if (users.isEmpty()) {
            users = UserDao.getInstance().query("username", blockedId);
        }
        UserDto blockedUser = (users != null && !users.isEmpty()) ? users.get(0) : null;
        if (blockedUser == null) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(false, null, "Blocked user does not exist"));
        }

        // Depending on the action ("block" or "unblock"), call the corresponding method on BlockListDao.
        BlockListDao blockListDao = BlockListDao.getInstance();
        boolean success;
        String message;
        if ("block".equalsIgnoreCase(action)) {
            success = blockListDao.addBlock(blockerId, blockedId);
            message = success ? "Block successful" : "Block failed";
        } else if ("unblock".equalsIgnoreCase(action)) {
            success = blockListDao.removeBlock(blockerId, blockedId);
            message = success ? "Unblock successful" : "Unblock failed";
        } else {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(false, null, "Unsupported action"));
        }

        // Return the result: OK if successful, otherwise SERVER_ERROR.
        return new ResponseBuilder()
                .setStatus(success ? StatusCodes.OK : StatusCodes.SERVER_ERROR)
                .setBody(new RestApiAppResponse<>(success, null, message));
    }
}
