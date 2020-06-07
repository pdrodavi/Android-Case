package br.com.truckpad.trucker.ui;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.truckpad.trucker.R;
import br.com.truckpad.trucker.data.model.Constants;
import br.com.truckpad.trucker.data.network.FetchAddressService;

public class PrepareRouteActivity extends AppCompatActivity  {

    FusedLocationProviderClient client;
    AddressResultReceiver resultReceiver;

    public String latitude;
    public String longitude;
    public String endereco;

    public String selectLocationLatitude;
    public String selectLocationLongitude;

    public String selectLocationLatitudeDestiny;
    public String selectLocationLongitudeDestiny;

    public String consumo;
    public String numEixos;
    public String fuel_price;

    public String init1;
    public String init2;
    public String end1;
    public String end2;

    public String partida;
    public String destino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prepare_route);

        client = LocationServices.getFusedLocationProviderClient(this);
        resultReceiver = new AddressResultReceiver(null);
    }

    @Override
    protected void onResume(){
        super.onResume();
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    latitude = String.valueOf(location.getLatitude());
                    longitude = String.valueOf(location.getLongitude());
                } else {
                    Log.i("Debug", "null");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(15 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i("Debug", "isNetworkLocationPresent: " + locationSettingsResponse.getLocationSettingsStates().isNetworkLocationPresent());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ResolvableApiException){
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(PrepareRouteActivity.this, 10);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });

        LocationCallback locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult == null){
                    Log.i("Debug", "locationResult null");
                    return ;
                }
                for(Location location : locationResult.getLocations()){
                    if(!Geocoder.isPresent()) return;
                    startIntentService(location);
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                Log.i("Debug", "isLocationAvailable: " + locationAvailability.isLocationAvailable());
            }
        };

        client.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void startIntentService(Location location){
        Intent intent = new Intent(this, FetchAddressService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        this.startService(intent);
    }

    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if(resultData == null) return;

            final String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            if(addressOutput != null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        endereco = addressOutput;
                    }
                });
            }
        }
    }

    public void getCurrentLocation(View view) {
        TextView text = findViewById(R.id.textView_address);
        text.setText(endereco);
    }

    public void setLocation(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivityForResult(intent, 1);
    }

    public void setDestiny(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivityForResult(intent, 2);
    }

    public void getInsertMetrics(View view) {
        Intent intent = new Intent(this, MetricsActivity.class);
        startActivityForResult(intent, 3);
    }


    public void send(View view) {

        double lat = Double.parseDouble(selectLocationLatitude);
        double lon = Double.parseDouble(selectLocationLongitude);

        double latDest = Double.parseDouble(selectLocationLatitudeDestiny);
        double lonDest = Double.parseDouble(selectLocationLongitudeDestiny);

        if (lat < 0 || lon < 0){
            if (lat < lon){
                init1 = selectLocationLatitude;
                init2 = selectLocationLongitude;
            } else {
                init1 = selectLocationLongitude;
                init2 = selectLocationLatitude;
            }
        }

        if ( latDest < 0 || lonDest < 0){
            if ( latDest < lonDest ){
                end1 = selectLocationLatitudeDestiny;
                end2 = selectLocationLongitudeDestiny;
            } else {
                end1 = selectLocationLongitudeDestiny;
                end2 = selectLocationLatitudeDestiny;
            }
        }

        String json = "{\n \"places\": [{\n \"point\": [\n "+init1+",\n "+init2+" \n]\n},{\n \"point\": [\n "+end1+",\n "+end2+"\n]\n }],\n \"fuel_consumption\": "+consumo+",\n \"fuel_price\": "+fuel_price+"\n}";

        Bundle bundle = new Bundle();
        bundle.putString("json", json);
        bundle.putString("selectLocationLatitude", init1);
        bundle.putString("selectLocationLongitude", init2);
        bundle.putString("selectLocationLatitudeDestiny", end1);
        bundle.putString("selectLocationLongitudeDestiny", end2);
        bundle.putString("fuelPrice", fuel_price);
        bundle.putString("consumo", consumo);
        bundle.putString("partida", partida);
        bundle.putString("destino", destino);
        bundle.putString("numEixos", numEixos);

        Intent intent = new Intent(this, ConfigRouteActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1) {
            selectLocationLatitude =  data.getStringExtra("latitude");
            selectLocationLongitude =  data.getStringExtra("longitude");

            Geocoder myLocation = new Geocoder(getApplicationContext(), Locale.getDefault());

            List<Address> addresses = null;
            try {
                addresses = myLocation.getFromLocation(Double.parseDouble(selectLocationLatitude), Double.parseDouble(selectLocationLongitude), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(addresses == null || addresses.isEmpty()){
                Log.e("Debug", "Nenhum endereço encontrado!");
            } else {
                Address address = addresses.get(0);
                List<String> addressF = new ArrayList<>();
                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++){
                    addressF.add(address.getAddressLine(i));
                }
                TextUtils.join("|", addressF);
                partida = addressF.toString();
                TextView text = findViewById(R.id.textView_address);
                text.setText(addressF.toString());
            }
        }

        if (resultCode == RESULT_OK && requestCode == 2) {

            selectLocationLatitudeDestiny =  data.getStringExtra("latitude");
            selectLocationLongitudeDestiny =  data.getStringExtra("longitude");

            Geocoder myLocation = new Geocoder(getApplicationContext(), Locale.getDefault());

            List<Address> addresses = null;
            try {
                addresses = myLocation.getFromLocation(Double.parseDouble(selectLocationLatitudeDestiny), Double.parseDouble(selectLocationLongitudeDestiny), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(addresses == null || addresses.isEmpty()){
                Log.e("Debug", "Nenhum endereço encontrado!");
            } else {
                Address address = addresses.get(0);
                List<String> addressF = new ArrayList<>();
                for(int i = 0; i <= address.getMaxAddressLineIndex(); i++){
                    addressF.add(address.getAddressLine(i));
                }
                TextUtils.join("|", addressF);
                destino = addressF.toString();
                TextView text = findViewById(R.id.textView_address_destiny);
                text.setText(addressF.toString());
            }
        }

        if (resultCode == RESULT_OK && requestCode == 3) {
            consumo = data.getStringExtra("consumo");
            numEixos = data.getStringExtra("numEixos");
            fuel_price = data.getStringExtra("fuel_price");
        }
    }

}



