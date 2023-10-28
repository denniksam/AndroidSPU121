package step.learning.androidspu121;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView tvHello ;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        // підключення розмітки - робота з UI тільки після цієї команди
        setContentView( R.layout.activity_main );

        // View - пробатько всіх UI елементів
        tvHello = findViewById( R.id.main_tv_hello ) ;
        // tvHello.setText( R.string.main_tv_hello_text );

        Button btnHello = findViewById( R.id.main_button_hello );
        btnHello.setOnClickListener( this::helloClick );

        findViewById( R.id.main_button_game ).setOnClickListener( this::startGame ) ;
        findViewById( R.id.main_button_chat ).setOnClickListener( this::startChat ) ;
    }

    // всі обробники подій повинні мати такий прототип, view - sender
    private void helloClick( View view ) {
        // tvHello.setText( tvHello.getText() + "!" ) ;
        Intent calcIntent = new Intent( this.getApplicationContext(), CalcActivity.class ) ;
        startActivity( calcIntent );
    }
    private void startGame( View view ) {
        startActivity( new Intent( this.getApplicationContext(), GameActivity.class ) );
    }
    private void startChat( View view ) {
        startActivity( new Intent( this.getApplicationContext(), ChatActivity.class ) );
    }
}