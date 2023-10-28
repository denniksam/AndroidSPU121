package step.learning.androidspu121.orm;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    private String id ;
    private String author ;
    private String text ;
    private String moment ;

    private static final SimpleDateFormat momentFormat =
            new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss", Locale.UK ) ;
    public Date getMomentAsDate() {
        try {
            return momentFormat.parse( this.getMoment() ) ;
        }
        catch( ParseException ex ) {
            Log.d( "ChatMessage::getMomentAsDate",
                    ex.getMessage() + " " + this.getMoment() ) ;
            return null ;
        }
    }
    public String getId() {
        return id;
    }
    public void setId( String id ) {
        this.id = id;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor( String author ) {
        this.author = author;
    }
    public String getText() {
        return text;
    }
    public void setText( String text ) {
        this.text = text;
    }
    public String getMoment() {
        return moment;
    }
    public void setMoment( String moment ) {
        this.moment = moment;
    }
}
/*
ORM for chat message
{
  "id": "2139",
  "author": "Max",
  "text": "123 :)",
  "moment": "2023-10-27 10:00:38"
},
 */