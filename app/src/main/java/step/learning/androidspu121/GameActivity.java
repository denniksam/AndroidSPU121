package step.learning.androidspu121;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game );

        // Задати ігровому полю висоту таку ж як ширину
        // Проблема: на етапі onCreate розміри ще не відомі
        TableLayout gameField = findViewById( R.id.game_field ) ;
        gameField.post(   // поставити задачу у чергу, вона буде виконана
                // коли gameField виконає усі попередні задачі, у т.ч.
                // розрахунок розміру та рисування.
                () -> {
                    int windowWidth = this.getWindow().getDecorView().getWidth() ;
                    int margin =
                        ((LinearLayout.LayoutParams)gameField.getLayoutParams()).leftMargin;
                    int fieldSize = windowWidth - 2 * margin ;
                    LinearLayout.LayoutParams layoutParams =
                            new LinearLayout.LayoutParams( fieldSize, fieldSize ) ;
                    layoutParams.setMargins( margin, margin, margin, margin );
                    gameField.setLayoutParams( layoutParams ) ;
                }
        ) ;


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
/*
Д.З. Підібрати кольори для оформлення всіх станів гри (2048)
Реалізувати їх ресурси за єдиним правилом "префікс_{значення}"
Випробувати через впровадження стилів
 */