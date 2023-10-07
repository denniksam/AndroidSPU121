package step.learning.androidspu121;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CalcActivity extends AppCompatActivity {
    private TextView tvExpression ;
    private TextView tvResult ;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_calc );

        tvExpression = findViewById( R.id.calc_tv_expression ) ;
        tvResult = findViewById( R.id.calc_tv_result ) ;
        clearClick( null ) ;

        findViewById( R.id.calc_btn_c ).setOnClickListener( this::clearClick ) ;
        // Пройти циклом по ідентифікаторах calc_btn_[i], всім вказати один обробник
        for( int i = 0; i < 10; i++ ) {
            findViewById(
                    getResources()  // R.
                            .getIdentifier(    // .id (R.id.calc_btn_[i])
                                    "calc_btn_" + i,
                                    "id",
                                    getPackageName()
                            )
            ).setOnClickListener( this::digitClick );
        }
    }
    private void digitClick( View view ) {  // для цифрових кнопок
        String result = tvResult.getText().toString() ;
        result += ( ( Button ) view ).getText() ;
        tvResult.setText( result ) ;
    }
    private void clearClick( View view ) {   // [ C ]
        tvExpression.setText( "" ) ;
        tvResult.setText( R.string.calc_btn_0 ) ;
    }

}
/*
Д.З. Виконати розмітку головної активності та активності калькулятора
 */