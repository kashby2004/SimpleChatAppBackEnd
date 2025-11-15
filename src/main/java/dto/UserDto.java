package dto;

import org.bson.Document;

public class UserDto extends BaseDto {

    private String userName;
    private String password;
    private Integer totalConversations = 0;
    private Integer messagesSent = 0;
    private Integer messagesRecieved = 0;

    public UserDto() {
        super();
    }

    @Override
    public void fromDocument(Document document) {
        // todo
        this.userName = document.getString("userName");
        this.password = document.getString("password");
        this.totalConversations = document.getInteger("totalConversations");
        this.messagesSent = document.getInteger("messagesSent");
        this.messagesRecieved = document.getInteger("messagesReceived");
    }

    @Override
    public Document toDocument() {
        // todo
        return new Document()
                .append("userName", userName)
                .append("password", password)
                .append("totalConversations", totalConversations)
                .append("messagesSent", messagesSent)
                .append("messagesReceived", messagesRecieved);
    }

    public UserDto(String uniqueId) {
        super(uniqueId);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public UserDto setPassword(String password) {
        this.password = password;
        return this;
    }

    public Integer getTotalConversations() {
        return totalConversations;
    }

    public UserDto setTotalConversations(Integer totalConversations) {
        this.totalConversations = totalConversations;
        return this;
    }

    public Integer getMessagesSent() {
        return messagesSent;
    }

    public UserDto setMessagesSent(Integer messagesSent) {
        this.messagesSent = messagesSent;
        return this;
    }

    public Integer getMessagesRecieved() {
        return messagesRecieved;
    }

    public UserDto setMessagesRecieved(Integer messagesRecieved) {
        this.messagesRecieved = messagesRecieved;
        return this;
    }
}
