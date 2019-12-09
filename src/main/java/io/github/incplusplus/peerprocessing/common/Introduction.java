package io.github.incplusplus.peerprocessing.common;

import java.util.UUID;

/**
 * A class that gives both parties of a particular communication details about one another. The
 * <i>typical</i> way this is transmitted is in a faux-handshake manner. The initiating partner
 * demands an Introduction from the other partner. That partner sends an Introduction back to the
 * partner who demanded it. Then, the sender turns the table and demands an Introduction back from
 * the original demand-ee. Below is an example of this interaction with two parties, a client and a
 * server. <br>
 * <br>
 * Client: connects to server Server: demands Introduction of client Client: sends Introduction to
 * server Client: demands Introduction of server Server: demands Introduction of server <br>
 * <br>
 * The reason this is done in such a roundabout manner is to allow the server to tell a client what
 * that client's UUID is as opposed to the client specifying one themselves.
 */
public class Introduction {
  /** The id of the member who sent this Introduction (if determined; else null) */
  private UUID senderId;
  /** The id of the member who will receive this Introduction (if known; else null) */
  private UUID receiverId;
  /** The name of the member sending this Introduction */
  private String senderName;
  /** The {@link MemberType} of the member sending this Introduction */
  private MemberType senderType;

  public UUID getSenderId() {
    return senderId;
  }

  public void setSenderId(UUID senderId) {
    this.senderId = senderId;
  }

  public UUID getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(UUID receiverId) {
    this.receiverId = receiverId;
  }

  public String getSenderName() {
    return senderName;
  }

  public void setSenderName(String senderName) {
    this.senderName = senderName;
  }

  public MemberType getSenderType() {
    return senderType;
  }

  public void setSenderType(MemberType senderType) {
    this.senderType = senderType;
  }
}
