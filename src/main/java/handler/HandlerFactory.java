package handler;

import request.ParsedRequest;

public class HandlerFactory {
    // 根據 path 來路由不同的 handler
    public static BaseHandler getHandler(ParsedRequest request) {
        switch (request.getPath()){
            case "/createUser":
                return new CreateUserHandler();
            case "/sendMessage":
                return new SendMessageHandler();
            case "/getConversations":
                return new GetConversationsHandler();
            case "/getConversation":
                return new GetConversationHandler();
            case "/login":
                return new LoginHandler();
            case "/blockList":
                return new BlockListHandler();
            case "/friendList":
                return new FriendListHandler();
            default:
                return new FallbackHandler();
        }
    }
}
