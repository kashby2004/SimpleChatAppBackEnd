package handler;

import auth.AuthFilter;
import dao.FriendListDao;
import dao.UserDao;
import dto.FriendListDto;
import dto.UserDto;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FriendListHandler implements BaseHandler {
    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {
        String action = request.getQueryParam("action");
        if (action == null || action.trim().isEmpty()) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(false, null, "Action parameter is required"));
        }

        AuthFilter.AuthResult authResult = AuthFilter.doFilter(request);
        if (!authResult.isLoggedIn) {
            return new ResponseBuilder().setStatus(StatusCodes.UNAUTHORIZED);
        }

        String userName = authResult.userName.trim().toLowerCase();

        if ("getFriends".equalsIgnoreCase(action)) {
            List<FriendListDto> friends = FriendListDao.getInstance().getFriends(userName);
            // Return a list of usernames (the other friend in each record)
            List<UserDto> friendUsers = friends.stream()
                    .map(dto -> {
                        String other = dto.getUserA().equals(userName) ? dto.getUserB() : dto.getUserA();
                        Optional<UserDto> userOpt = UserDao.getInstance()
                                .query("userName", other).stream().findFirst();
                        return userOpt.orElse(null);
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            return new ResponseBuilder()
                    .setStatus(StatusCodes.OK)
                    .setBody(new RestApiAppResponse<>(true, friendUsers, null));
        }

        if ("addFriend".equalsIgnoreCase(action)) {
            String friendUser = request.getQueryParam("friendUser");
            if (friendUser == null || friendUser.trim().isEmpty()) {
                return new ResponseBuilder()
                        .setStatus(StatusCodes.BAD_REQUEST)
                        .setBody(new RestApiAppResponse<>(false, null, "friendUser parameter is required"));
            }
            friendUser = friendUser.trim().toLowerCase();
            if (userName.equals(friendUser)) {
                return new ResponseBuilder()
                        .setStatus(StatusCodes.BAD_REQUEST)
                        .setBody(new RestApiAppResponse<>(false, null, "Cannot add yourself as friend"));
            }
            // Check user exists
            Optional<UserDto> userOpt = UserDao.getInstance()
                    .query("userName", friendUser).stream().findFirst();
            if (!userOpt.isPresent()) {
                userOpt = UserDao.getInstance()
                        .query("username", friendUser).stream().findFirst();
            }
            if (!userOpt.isPresent()) {
                return new ResponseBuilder()
                        .setStatus(StatusCodes.BAD_REQUEST)
                        .setBody(new RestApiAppResponse<>(false, null, "User does not exist"));
            }
            boolean success = FriendListDao.getInstance().addFriend(userName, friendUser);
            return new ResponseBuilder()
                    .setStatus(success ? StatusCodes.OK : StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(success, null, success ? "Friend added" : "Already friends"));
        }

        if ("removeFriend".equalsIgnoreCase(action)) {
            String friendUser = request.getQueryParam("friendUser");
            if (friendUser == null || friendUser.trim().isEmpty()) {
                return new ResponseBuilder()
                        .setStatus(StatusCodes.BAD_REQUEST)
                        .setBody(new RestApiAppResponse<>(false, null, "friendUser parameter is required"));
            }
            friendUser = friendUser.trim().toLowerCase();
            boolean success = FriendListDao.getInstance().removeFriend(userName, friendUser);
            return new ResponseBuilder()
                    .setStatus(success ? StatusCodes.OK : StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(success, null, success ? "Friend removed" : "Not friends"));
        }

        return new ResponseBuilder()
                .setStatus(StatusCodes.BAD_REQUEST)
                .setBody(new RestApiAppResponse<>(false, null, "Unsupported action"));
    }
}