package br.com.truckpad.trucker.data.network;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.truckpad.trucker.data.model.Constants;

public class FetchAddressService extends IntentService {

    protected ResultReceiver receiver;

    public FetchAddressService() {
        super("fetchAddressService");
        //super("fetchAddressService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null) return;

        // transformar as coordenadas em endereços legíveis
        //Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Geocoder myLocation = new Geocoder(getApplicationContext(), Locale.getDefault());
        // latitude e longitude do objeto
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        // receiver devolve em formato de texto o resultado
        receiver = intent.getParcelableExtra(Constants.RECEIVER);

        //List<Address> addresses = null;
        List<Address> addresses = null;
        try {
            addresses = myLocation.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //try {  // transformando coordenadas em texto
        //    geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        //} catch (IOException e) {
        //    Log.e("Teste", "Serviço indisponível!", e);
        //} catch (IllegalArgumentException e){ // tratando coordenadas inválidas
        //    Log.e("Teste", "Coordenadas Inválidas!", e);
        //}

        // se a lista vier vazia nao encontro endereço pra posição
        if(addresses == null || addresses.isEmpty()){
            Log.e("Teste", "Nenhum endereço encontrado!");
            deliverResultToReceiver(Constants.FAILURE_RESULT, "Nenhum endereço encontrado!");
        } else { // result success
            Address address = addresses.get(0);
            List<String> addressF = new ArrayList<>();
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++){
                addressF.add(address.getAddressLine(i));
            }
            deliverResultToReceiver(Constants.SUCCESS_RESULT, TextUtils.join("|", addressF));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message){
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        receiver.send(resultCode, bundle);
    }
}
