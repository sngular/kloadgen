package net.coru.kloadgen.test;

public class MessageKey {
    private long messageId;

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                '}';
    }
}
