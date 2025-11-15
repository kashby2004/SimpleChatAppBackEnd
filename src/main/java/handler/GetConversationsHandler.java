package handler;

import auth.AuthFilter;
import dao.ConversationDao;
import dao.UserDao;
import dto.ConversationDto;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

import java.util.List;

public class GetConversationsHandler implements BaseHandler {

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {
        // todo
        AuthFilter.AuthResult authResult = AuthFilter.doFilter(request);
        if(!authResult.isLoggedIn){
            return new ResponseBuilder().setStatus(StatusCodes.UNAUTHORIZED);
        }
        UserDao userDao = UserDao.getInstance();
        var userDto = userDao.query("userName", authResult.userName).get(0);
        ConversationDao conversationDao = ConversationDao.getInstance();
        List<ConversationDto> conversations = conversationDao.query("toId", userDto.getUserName());
        conversations.addAll(conversationDao.query("fromId", userDto.getUserName()));
        return new ResponseBuilder().setStatus(StatusCodes.OK)
                .setBody(new RestApiAppResponse<>(true, conversations, null));
    }
}
