package handler;

import auth.AuthFilter;
import dao.MessageDao;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

public class GetConversationHandler implements BaseHandler {

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {
        // todo
        AuthFilter.AuthResult authResult = AuthFilter.doFilter(request);
        if(!authResult.isLoggedIn){
            return new ResponseBuilder().setStatus(StatusCodes.UNAUTHORIZED);
        }
        MessageDao messageDao = MessageDao.getInstance();
        var allMessages = messageDao.query("conversationId", request.getQueryParam("conversationId"));
        return new ResponseBuilder().setStatus(StatusCodes.OK)
                .setBody(new RestApiAppResponse<>(true, allMessages, null));
    }
}
