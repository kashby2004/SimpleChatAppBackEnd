package handler;

import auth.AuthFilter;
import dao.BlockListDao;
import dao.ConversationDao;
import dao.MessageDao;
import dao.UserDao;
import dto.ConversationDto;
import dto.MessageDto;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

import java.util.List;

public class SendMessageHandler implements BaseHandler {

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {
        MessageDto messageDto = GsonTool.GSON.fromJson(request.getBody(), MessageDto.class);
        AuthFilter.AuthResult authResult = AuthFilter.doFilter(request);
        if (!authResult.isLoggedIn) {
            return new ResponseBuilder().setStatus(StatusCodes.UNAUTHORIZED);
        }
        UserDao userDao = UserDao.getInstance();
        var userDto = userDao.query("userName", authResult.userName).get(0);
        var toUser = userDao.query("userName", messageDto.getToId()).stream()
                .findFirst()
                .orElse(null);
        if (toUser == null) {
            return new ResponseBuilder().setStatus(StatusCodes.OK)
                    .setBody(new RestApiAppResponse<>(false, null, "No id is invalid user"));
        }

        // No message to yourself
        if (userDto.getUserName().equalsIgnoreCase(toUser.getUserName())) {
            return new ResponseBuilder().setStatus(StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(false, null, "Cannot send message to yourself"));
        }

        // Standardize the usernames
        String senderName = userDto.getUserName().trim().toLowerCase();
        String recipientName = toUser.getUserName().trim().toLowerCase();

        System.out.println("SendMessageHandler: senderName=" + senderName + ", recipientName=" + recipientName);

        // Check the bidirectional block status.
        // If the recipient has blocked the sender
        if (BlockListDao.getInstance().isBlocked(recipientName, senderName)) {
            return new ResponseBuilder().setStatus(StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(false, null, "Message not sent: You have been blocked by the recipient."));
        }
        // If the sender has blocked the recipient
        if (BlockListDao.getInstance().isBlocked(senderName, recipientName)) {
            return new ResponseBuilder().setStatus(StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(false, null, "Message not sent: You have blocked that user."));
        }

        String conversationId = ConversationDto.makeUniqueId(senderName, recipientName);
        ConversationDao conversationDao = ConversationDao.getInstance();
        ConversationDto conversationDto = conversationDao.query("conversationId", conversationId).stream()
                .findFirst()
                .orElse(new ConversationDto(senderName, recipientName));
        conversationDto.setMessageCount(conversationDto.getMessageCount() + 1);
        conversationDao.put(conversationDto);
        messageDto.setConversationId(conversationId);
        messageDto.setFromId(senderName);
        MessageDao messageDao = MessageDao.getInstance();
        messageDao.put(messageDto);
        userDto.setMessagesSent(userDto.getMessagesSent() + 1);
        toUser.setMessagesRecieved(toUser.getMessagesRecieved() + 1);
        userDao.put(toUser);
        userDao.put(userDto);
        return new ResponseBuilder().setStatus(StatusCodes.OK)
                .setBody(new RestApiAppResponse<>(true, List.of(conversationDto), null));
    }
}
