package step.learning.androidspu121;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import step.learning.androidspu121.orm.ChatMessage;
import step.learning.androidspu121.orm.ChatResponse;

public class ChatActivity extends AppCompatActivity {
    private final static String chatHost = "https://chat.momentfor.fun" ;
    private static final String savesFilename = "saves.chat";
    private final byte[] buffer = new byte[ 8192 ] ;
    private final Gson gson = new Gson() ;
    private final List<ChatMessage> chatMessages = new ArrayList<>() ;
    private EditText etNik ;
    private EditText etMessage ;
    private ScrollView svContainer ;
    private LinearLayout llContainer ;
    private Handler handler ;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_chat );
        etNik = findViewById( R.id.chat_et_nik ) ;
        etMessage = findViewById( R.id.chat_et_message ) ;
        svContainer = findViewById( R.id.chat_sv_container ) ;
        llContainer = findViewById( R.id.chat_ll_container ) ;
        findViewById( R.id.chat_btn_send ).setOnClickListener( this::sendButtonClick );
        findViewById( R.id.chat_btn_save_nik ).setOnClickListener( this::saveNikClick );
        handler = new Handler();
        handler.post( this::updateChat ) ;
        if( ! loadNik() ) {
            etNik.requestFocus();
            Toast.makeText( this, "Виберіть собі нік та збережіть його", Toast.LENGTH_LONG ).show();
        }
    }
    private void saveNikClick( View view ) {
        String nik = etNik.getText().toString() ;
        if( nik.isEmpty() ) {
            Toast.makeText( this, "Необхідно заповнити поле", Toast.LENGTH_SHORT ).show();
            etNik.requestFocus();
            return;
        }
        try( FileOutputStream writer = openFileOutput( savesFilename, Context.MODE_PRIVATE ) ) {
            writer.write( nik.getBytes( StandardCharsets.UTF_8 ) );
        }
        catch( IOException ex ) {
            Log.e( "saveNikClick", ex.getMessage() ) ;
        }
    }
    private boolean loadNik() {
        try( FileInputStream reader = openFileInput( savesFilename ) ) {
            String nik = readString( reader ) ;
            if( nik.isEmpty() ) {
                return false ;
            }
            etNik.setText( nik );
            return true ;
        }
        catch( IOException ex ) {
            Log.e( "loadNik", ex.getMessage() ) ;
            return false ;
        }
    }  // /data/user/0/step.learning.androidspu121/files/saves.chat
    private void updateChat() {
        new Thread( this::loadChatMessages ).start() ;
        handler.postDelayed( this::updateChat, 3000 ) ;
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
                new Thread( this::loadChatMessages ).start();
                runOnUiThread( () -> {
                    etMessage.setText( "" );
                    etMessage.requestFocus() ;
                });
            }
            else {  // якщо не успіх, то повідомлення про помилку - у тілі
                InputStream inputStream = connection.getInputStream() ;
                String responseBody = readString( inputStream ) ;
                inputStream.close();
                Log.e( "postChatMessage", statusCode + " " + responseBody ) ;
            }
            // 4. Закриваємо підключення, звільняємо ресурс
            connection.disconnect();
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
            // впорядковуємо відповідь - останні повідомлення "знизу", тобто за зростанням дати-часу
            chatResponse.getData().sort( Comparator.comparing( ChatMessage::getMomentAsDate ) );

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
        boolean needScroll = false ;
        for( ChatMessage chatMessage : this.chatMessages ) {
            if( chatMessage.getView() == null ) {
                View messageView = createChatMessageView( chatMessage );
                chatMessage.setView( messageView );
                llContainer.addView( messageView );
                needScroll = true ;  // додавання View вниз вимагає прокрутки контейнера
            }
        }
        if( needScroll ) {
            // Прокрутка контейнера (ScrollView) до самого нижнього положення.
            // !! Але додавання елементів до контейнера у попередніх операціях (addView)
            //    ще не "відпрацьована" на UI - як елементи вони є, але їх розміри ще не
            //    прораховані. Команду прокрутки треба ставити у чергу за відображенням
            svContainer.post( () -> svContainer.fullScroll( View.FOCUS_DOWN ) );
        }
    }
    private View createChatMessageView( ChatMessage chatMessage ) {
        // у цьому варіанті свої повідомлення визначаються за збігом автора
        boolean isMine = chatMessage.getAuthor().contentEquals( etNik.getText() ) ;
        // Створення елемента програмно складається з кількох дій
        // 1. "Внутрішні" налаштування
        // 2. "Зовнішні" - правила "вбудови" елемента в інші елементи (Layout-и)
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ) ;
        layoutParams.setMargins( 5, 5, 5, 5 ) ;

        layoutParams.gravity = isMine
            ? Gravity.END
            : Gravity.START;

        LinearLayout messageLayout = new LinearLayout( this ) ;
        messageLayout.setLayoutParams( layoutParams ) ;
        messageLayout.setPadding( 20, 10, 20, 10 ) ;
        messageLayout.setOrientation( LinearLayout.VERTICAL ) ;
        messageLayout.setBackground(
                   AppCompatResources.getDrawable(
                           ChatActivity.this,
                           isMine
                               ? R.drawable.chat_message_mine
                               : R.drawable.chat_message_other ) ) ;

        TextView textView = new TextView( this ) ;
        textView.setText( chatMessage.getMoment() ) ;
        textView.setTextSize( 12 ) ;
        textView.setTypeface(null, Typeface.ITALIC ) ;
        messageLayout.addView( textView ) ;

        textView = new TextView( this ) ;
        textView.setText( chatMessage.getAuthor() ) ;
        textView.setTypeface(null, Typeface.BOLD ) ;
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
Періодичний запуск. Особливості.
Є дві поширені схеми періодичного запуску - таймер та подійний цикл
Таймер, як правило, створюється як окремий потік, який подає хроно-імпульси.
Подійний цикл використовує внутрішній цикл застосунку, плануючи відтермінований
запуск.
Для управління внутрішнім циклом використовують об'єкти Handler
 */
/*
Д.З. Завершити Проєкт "Чат"

Завантажити Юніті
1) Хаб
2) Installs
 */
