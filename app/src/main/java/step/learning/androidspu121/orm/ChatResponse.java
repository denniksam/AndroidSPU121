package step.learning.androidspu121.orm;

import java.util.List;

public class ChatResponse {
    private int status ;
    private List<ChatMessage> data ;

    public int getStatus() {
        return status;
    }
    public void setStatus( int status ) {
        this.status = status;
    }
    public List<ChatMessage> getData() {
        return data;
    }
    public void setData( List<ChatMessage> data ) {
        this.data = data;
    }
}
/*
ORM for chat response
{
  "status": 1,
  "data": [ orm.ChatMessage ]
}
 */