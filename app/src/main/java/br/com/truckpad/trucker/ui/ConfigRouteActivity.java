package br.com.truckpad.trucker.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import br.com.truckpad.trucker.R;
import br.com.truckpad.trucker.data.model.Post;

public class ConfigRouteActivity extends AppCompatActivity {

    public String selectLocationLatitude;
    public String selectLocationLongitude;
    public String selectLocationLatitudeDestiny;
    public String selectLocationLongitudeDestiny;
    public String consumo;
    public String json;
    public String partida;
    public String destino;
    public String fuelPrice;
    public String numEixos;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_route);

        Bundle extras = getIntent().getExtras();

        selectLocationLatitude = (String) extras.get("selectLocationLatitude");
        selectLocationLongitude = (String) extras.get("selectLocationLongitude");
        selectLocationLatitudeDestiny = (String) extras.get("selectLocationLatitudeDestiny");
        selectLocationLongitudeDestiny = (String) extras.get("selectLocationLongitudeDestiny");
        consumo = (String) extras.get("consumo");
        json = (String) extras.get("json");
        partida = (String) extras.get("partida");
        destino = (String) extras.get("destino");
        fuelPrice = (String) extras.get("fuelPrice");
        numEixos = (String) extras.get("numEixos");

        TextView tfJson = findViewById(R.id.textView_json);
        tfJson.setText("Viagem de: " + partida +", até " + destino);

    }

    @SuppressLint("SetTextI18n")
    public void sendPost(View view) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String urlAPI = "https://geo.api.truckpad.io/v1/route";
        URL url = null;

        try {
            url = new URL(urlAPI);
            Log.i("Output", "Conexão criada: " + urlAPI);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.i("Output", "Falhou na criação da conexão: " + urlAPI);
        }

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            Log.i("Output", "Conexão estabelecida!");
        } catch (IOException e) {
            Log.i("Output", "Conexão falhou!");
            e.printStackTrace();
        }
        try {
            connection.setRequestMethod("POST");
            Log.i("Output", "Método POST Selecionado!");
        } catch (ProtocolException e) {
            Log.i("Output", "Erro em selecionar o método!");
            e.printStackTrace();
        }

        connection.setRequestProperty("Content-type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        PrintStream printStream = null;

        String jsonFormated = json.replaceAll("\\\\", "");

        try {
            printStream = new PrintStream(connection.getOutputStream());
            printStream.println(jsonFormated); //seta o que voce vai enviar
            Log.i("Output", "JSON SETADO: " + jsonFormated);
        } catch (IOException e) {
            Log.i("Output", "Falha em setar o JSON!");
            e.printStackTrace();
        }

        try {
            connection.connect();
            Log.i("Output", "Enviou para o servidor!");
        } catch (IOException e) {
            Log.i("Output", "Falha em enviar para o servidor! " + e.getMessage() + ", " + e.getCause());
            e.printStackTrace();
        }

        try {
            Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

            StringBuilder stringBuilder = new StringBuilder();
            for (int c; (c = in.read()) >= 0;) {
                System.out.print((char) c);
                stringBuilder.append((char)c);
            }

            String response = stringBuilder.toString();

            JSONObject jsonObject = new JSONObject(stringBuilder.toString());

            String distance = jsonObject.getString("distance");
            double distanceKm = Double.parseDouble(distance) / 1000;
            TextView tfDistance = findViewById(R.id.textView_distance);
            tfDistance.setText("Distância: " + distanceKm + " Km");

            String duration = jsonObject.getString("duration");
            double durationHoras = Double.parseDouble(duration) / 3600;
            TextView tfDuration = findViewById(R.id.textView_duration);
            tfDuration.setText("Tempo de viagem: " + durationHoras + " Hrs");

            String fuel_usage = jsonObject.getString("fuel_usage");
            TextView tfFuelUsage = findViewById(R.id.textView_fuel_usage);
            tfFuelUsage.setText("Combustível gasto: " + fuel_usage + " L");

            String fuel_cost = jsonObject.getString("fuel_cost");
            TextView tfFuelCost = findViewById(R.id.textView_fuel_cost);
            tfFuelCost.setText("Combustível valor R$: " + fuel_cost);

            String total_cost = jsonObject.getString("total_cost");
            TextView tfTotalCost = findViewById(R.id.textView_total_cost);
            tfTotalCost.setText("Valor da viagem R$: " + total_cost);

            checkValueFreight(numEixos, distance);

            Log.i("Output", "Response: " + response);

        } catch (IOException e) {
            Log.i("Output", "Erro em receber a resposta!" + e.getMessage() + ", " + e.getCause());
            TextView res = findViewById(R.id.textView_json);
            res.setText("Não foi possível localizar a rota. Tente novamente!");
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    public void checkValueFreight(String axis, String distance){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String urlAPI = "https://tictac.api.truckpad.io/v1/antt_price/all";
        URL url = null;

        try {
            url = new URL(urlAPI);
            Log.i("Output", "Conexão criada: " + urlAPI);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.i("Output", "Falhou na criação da conexão: " + urlAPI);
        }

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            Log.i("Output", "Conexão estabelecida!");
        } catch (IOException e) {
            Log.i("Output", "Conexão falhou!");
            e.printStackTrace();
        }
        try {
            connection.setRequestMethod("POST");
            Log.i("Output", "Método POST Selecionado!");
        } catch (ProtocolException e) {
            Log.i("Output", "Erro em selecionar o método!");
            e.printStackTrace();
        }

        connection.setRequestProperty("Content-type", "application/json; utf-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        PrintStream printStream = null;

        String jsonFreight = "{\"axis\":"+axis+",\"distance\":"+distance+",\"has_return_shipment\":true}";

        try {
            printStream = new PrintStream(connection.getOutputStream());
            printStream.println(jsonFreight); //seta o que voce vai enviar
            Log.i("Output", "JSON SETADO: " + jsonFreight);
        } catch (IOException e) {
            Log.i("Output", "Falha em setar o JSON!");
            e.printStackTrace();
        }

        try {
            connection.connect();
            Log.i("Output", "Enviou para o servidor!");
        } catch (IOException e) {
            Log.i("Output", "Falha em enviar para o servidor! " + e.getMessage() + ", " + e.getCause());
            e.printStackTrace();
        }

        try {
            Reader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

            StringBuilder stringBuilder = new StringBuilder();
            for (int c; (c = in.read()) >= 0;) {
                System.out.print((char) c);
                stringBuilder.append((char)c);
            }

            String response = stringBuilder.toString();

            JSONObject jsonObject = new JSONObject(stringBuilder.toString());

            String frigorificada = jsonObject.getString("frigorificada");
            String geral = jsonObject.getString("geral");
            String granel = jsonObject.getString("granel");
            String neogranel = jsonObject.getString("neogranel");
            String perigosa = jsonObject.getString("perigosa");

            String res = "Frigorificada: " + frigorificada + ", "
                    + "Geral: " + geral + ", "
                    + "Granel: " + granel + ", "
                    + "Neogranel: " + neogranel + ", "
                    + "Perigosa: " + perigosa;

            TextView tfResponse = findViewById(R.id.textView_response);
            tfResponse.setText(res);

            Log.i("Output", "Response: " + response);

        } catch (IOException e) {
            Log.i("Output", "Erro em receber a resposta!" + e.getMessage() + ", " + e.getCause());
            TextView res = findViewById(R.id.textView_json);
            res.setText("Não foi calcular o valor do frete. Tente novamente!");
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
