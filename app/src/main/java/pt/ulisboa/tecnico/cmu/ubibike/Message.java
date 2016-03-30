package pt.ulisboa.tecnico.cmu.ubibike;

/**
 * Created by vicente on 30-Mar-16.
 */
//@ParseClassName("Message")
public class Message {
    String user;
    String body;

    public void Message (String bodyId ) {
        body = bodyId;
    }
    public String getUserId() {
        return user;
    }

    public String getBody() {
        return body;
    }

    public void setUserId(String userId) {
        user = userId;
    }

    public void setBody(String bodyId) {
        body = bodyId;
    }
}
