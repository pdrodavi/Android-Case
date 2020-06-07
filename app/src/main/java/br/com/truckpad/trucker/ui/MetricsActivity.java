package br.com.truckpad.trucker.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import br.com.truckpad.trucker.R;

public class MetricsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metrics);
    }

    public void sendMetrics(View view) {
        EditText ETnumeroDeEixos = (EditText) findViewById(R.id.editText_num_eixos);
        Editable numEixos = ETnumeroDeEixos.getText();

        EditText ETconsumo = (EditText) findViewById(R.id.editText_consumo_medio);
        Editable consumo = ETconsumo.getText();

        EditText ETFuelPrice = (EditText) findViewById(R.id.editText_fuel_price);
        Editable fuel_price = ETFuelPrice.getText();

        Intent intent = new Intent();
        intent.putExtra("numEixos", numEixos.toString());
        intent.putExtra("consumo", consumo.toString());
        intent.putExtra("fuel_price", fuel_price.toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}
