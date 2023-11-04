package step.learning.androidspu121;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import step.learning.androidspu121.orm.ChatMessage;
import step.learning.androidspu121.orm.ChatResponse;

public class ChatActivity extends AppCompatActivity {
    private final static String chatHost = "https://chat.momentfor.fun" ;
    private final byte[] buffer = new byte[ 8192 ] ;
    private final Gson gson = new Gson() ;
    private final List<ChatMessage> chatMessages = new ArrayList<>() ;
    private EditText etNik ;
    private EditText etMessage ;
    private ScrollView svContainer ;
    private LinearLayout llContainer ;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_chat );
        new Thread( this::loadChatMessages ).start() ;
        etNik = findViewById( R.id.chat_et_nik ) ;
        etMessage = findViewById( R.id.chat_et_message ) ;
        svContainer = findViewById( R.id.chat_sv_container ) ;
        llContainer = findViewById( R.id.chat_ll_container ) ;
        findViewById( R.id.chat_btn_send ).setOnClickListener( this::sendButtonClick );
    }
    private void sendButtonClick( View view ) {
        String nik = etNik.getText().toString() ;
        String message = etMessage.getText().toString();
        if( nik.isEmpty() ) {
            Toast.makeText( this, "Введіть нік", Toast.LENGTH_SHORT ).show();
            return;
        }
        if( message.isEmpty() ) {
            Toast.makeText( this, "Введіть повідомлення", Toast.LENGTH_SHORT ).show();
            return;
        }
        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.setAuthor( nik );
        chatMessage.setText( message );
        new Thread( () -> postChatMessage(chatMessage) ).start();
    }
    private void postChatMessage( ChatMessage chatMessage ) {
        try {
            // POST повідомлення надсилається у декілька етапів
            // 1. Налаштування з'єднання
            URL url = new URL( chatHost );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection() ;
            connection.setDoOutput( true );  // у з'єднання можна писати (Output) - формувати тіло
            connection.setDoInput( true );   // можна читати - одержувати тіло відповіді
            connection.setRequestMethod( "POST" );
            // заголовки НТТР встановлюються як RequestProperty
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Accept", "*/*" );
            connection.setChunkedStreamingMode( 0 );  // не ділити на блоки - надсилати одним пакетом

            // 2. Формуємо тіло запиту (пишемо Output)
            OutputStream outputStream = connection.getOutputStream() ;
            // author=Nik&msg=Message   !! 2+2-> &msg=2+2 --> 2 2 (+ в url це код пробіла)
            // 2&2 --> author=Nik&msg=2&2  --> 'author'=Nik, 'msg'=2, '2'=null
            // My %20 --> My [space] -- %20 - код пробіла ==> перед надсиланням дані треба кодувати
            String body = String.format( "author=%s&msg=%s",
                    URLEncoder.encode( chatMessage.getAuthor(), StandardCharsets.UTF_8.name() ),
                    URLEncoder.encode( chatMessage.getText(), StandardCharsets.UTF_8.name() )
            ) ;
            outputStream.write( body.getBytes( StandardCharsets.UTF_8 ) );
            outputStream.flush();
            outputStream.close();

            // 3. Одержуємо відповідь, перевіряємо статус, за потреби читаємо тіло
            int statusCode = connection.getResponseCode() ;
            if( statusCode == 201 ) {  // у разі успіху приходить лише статус, тіла немає
                Log.d( "postChatMessage", "Sent OK" ) ;
            }
            else {  // якщо не успіх, то повідомлення про помилку - у тілі
                InputStream inputStream = connection.getInputStream() ;
                String responseBody = readString( inputStream ) ;
                inputStream.close();
                Log.e( "postChatMessage", statusCode + " " + responseBody ) ;
            }
        }
        catch( Exception ex ) {
            Log.e( "postChatMessage", ex.getMessage() ) ;
        }
    }
    private String readString( InputStream inputStream ) throws IOException {
        ByteArrayOutputStream builder = new ByteArrayOutputStream() ;
        int bytesRead ;
        while( ( bytesRead = inputStream.read(buffer) ) > 0 ) {
            builder.write( buffer, 0, bytesRead ) ;
        }
        String result = builder.toString( StandardCharsets.UTF_8.name() ) ;
        builder.close() ;
        return result ;
    }
    private void loadChatMessages() {
        try {
            URL chatUrl = new URL( chatHost ) ;
            InputStream chatStream = chatUrl.openStream() ;
            String data = readString( chatStream ) ;
            ChatResponse chatResponse = gson.fromJson( data, ChatResponse.class ) ;
            boolean wasNewMessage = false ;
            for( ChatMessage chatMessage : chatResponse.getData() ) {
                if( chatMessages.stream().noneMatch(
                        m -> m.getId().equals( chatMessage.getId() )
                ) ) {
                    chatMessages.add( chatMessage ) ;
                    wasNewMessage = true ;
                }
            }
            if( wasNewMessage ) {
                runOnUiThread( this::showChatMessages ) ;
            }
            chatStream.close();
        }
        catch( MalformedURLException ex ) {
            Log.d( "loadChatMessages", "MalformedURLException " + ex.getMessage() ) ;
        }
        catch( IOException ex ) {
            Log.d( "loadChatMessages", "IOException " + ex.getMessage() ) ;
        }
        catch( android.os.NetworkOnMainThreadException ex ) {
            Log.d( "loadChatMessages", "NetworkOnMainThreadException " + ex.getMessage() ) ;
        }
        catch( java.lang.SecurityException ex ) {
            Log.d( "loadChatMessages", "SecurityException " + ex.getMessage() ) ;
        }
    }
    private void showChatMessages() {
        for( ChatMessage chatMessage : this.chatMessages ) {
            llContainer.addView( createChatMessageView( chatMessage ) ) ;
        }
    }
    private View createChatMessageView( ChatMessage chatMessage ) {
        // Створення елемента програмно складається з кількох дій
        // 1. "Внутрішні" налаштування
        // 2. "Зовнішні" - правила "вбудови" елемента в інші елементи (Layout-и)
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ) ;
        layoutParams.setMargins( 5, 5, 5, 5 ) ;

        // "other"
        // layoutParams.gravity = Gravity.START ;
        // "mine"
        layoutParams.gravity = Gravity.END ;

        /*  margin
               ---messageLayout {
                       textView{ Author }
                       textView{ Text }
                   }
         */
        LinearLayout messageLayout = new LinearLayout( this ) ;
        messageLayout.setLayoutParams( layoutParams ) ;
        messageLayout.setPadding( 20, 10, 20, 10 ) ;
        messageLayout.setOrientation( LinearLayout.VERTICAL ) ;
        // "other"
        // messageLayout.setBackground(
        //         AppCompatResources.getDrawable(
        //                 ChatActivity.this,
        //                 R.drawable.chat_message_other ) ) ;
        // "mine"
        messageLayout.setBackground(
                   AppCompatResources.getDrawable(
                           ChatActivity.this,
                           R.drawable.chat_message_mine ) ) ;

        TextView textView = new TextView( this ) ;
        textView.setText( chatMessage.getAuthor() ) ;
        messageLayout.addView( textView ) ;

        textView = new TextView( this ) ;
        textView.setText( chatMessage.getText() ) ;
        messageLayout.addView( textView ) ;

        return messageLayout ;
    }
}

/*
Робота з мережею Інтернет
Основний об'єкт для роботи з мережею - URL. Він дещо нагадує File у контексті
надання доступу до даних, а також у тому, що створення об'єкту не спричинює
мережної активності. Звернення до мережі починається при зверненні до методів
цього об'єкта
Особливості
1) android.os.NetworkOnMainThreadException
     Звертатись до мережі не можна з UI потоку, необхідно створювати новий потік
2) java.lang.SecurityException: Permission denied (missing INTERNET permission?)
     Для роботи з мережею необхідно зазначити дозвіл у маніфесті
     <uses-permission android:name="android.permission.INTERNET"/>
3) android.view.ViewRootImpl$CalledFromWrongThreadException:
     Only the original thread that created a view hierarchy can touch its views.
     Оскільки робота з мережею ведеться з окремого потоку, прямі звернені до
     елементів UI не дозволяються. Делегування запуску здійснюється методом
     runOnUiThread(...)
 */
/*
Д.З. Проєкт "Чат"
- зробити одночасне відображення повідомлень у двох стилях (власні та інші)
   (через один або за випадковим алгоритмом)
- додати іконку "нове повідомлення" (дзвоник або конверт або ...), реалізувати для
   неї анімацію, програвати її при натисканні кнопки "надіслати"
- додати звук нового повідомлення, програвати його разом з анімацією
 */
