package step.learning.androidspu121;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game );
        findViewById( R.id.game_layout ).setOnTouchListener(
                new OnSwipeListener( GameActivity.this ) {
                    @Override public void onSwipeBottom() {
                        Toast.makeText(   // повідомлення, що з'являється та зникає
                                GameActivity.this,   // контекст
                                "onSwipeBottom",     // повідомлення
                                Toast.LENGTH_SHORT   // тривалість (довжина у часі)
                        ).show();
                    }
                    @Override public void onSwipeLeft() {
                        Toast.makeText( GameActivity.this, "onSwipeLeft", Toast.LENGTH_SHORT ).show();
                    }
                    @Override public void onSwipeRight() {
                        Toast.makeText( GameActivity.this, "onSwipeRight", Toast.LENGTH_SHORT ).show();
                    }
                    @Override public void onSwipeTop() {
                        Toast.makeText( GameActivity.this, "onSwipeTop", Toast.LENGTH_SHORT ).show();
                    }
                } ) ;
    }
}