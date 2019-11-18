package net.coru.kloadgen.test;

import java.io.Serializable;

public class Message  implements Serializable{

    private long messageId;
    private String messageBody;
    private String messageStatus;
    private String messageCategory;
    private long messageTime;

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getMessageCategory() {
        return messageCategory;
    }

    public void setMessageCategory(String messageCategory) {
        this.messageCategory = messageCategory;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", messageBody='" + messageBody + '\'' +
                ", messageStatus='" + messageStatus + '\'' +
                ", messageCategory='" + messageCategory + '\'' +
                ", messageTime=" + messageTime +
                '}';
    }
}
