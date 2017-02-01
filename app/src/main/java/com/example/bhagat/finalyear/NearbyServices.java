package com.example.bhagat.finalyear;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NearbyServices extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,LocationListener{

    RequestQueue requestQueue;
    ListView requestsList;
    ArrayList<ListData> arrayOfItems;


    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private int PROXIMITY_RADIUS = 10000;
    double latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_services);

        requestsList = (ListView) findViewById(R.id.list);
        //  ListView
        arrayOfItems = new ArrayList<ListData>();

        getLocation();

        Toast.makeText(getApplication(),latitude + " " + longitude,Toast.LENGTH_LONG).show();

        getServices();

        requestsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {

                    startActivity(new Intent(NearbyServices.this, MakeRequest.class).
                             putExtra("serviceId", arrayOfItems.get(i).jOb.getString("service_id"))
                            .putExtra("serviceName",arrayOfItems.get(i).jOb.getString("service_name"))
                            .putExtra("providerName",arrayOfItems.get(i).jOb.getString("provider_name"))
                            .putExtra("providerPhno",arrayOfItems.get(i).jOb.getString("provider_phno")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //temporary add
        //for (int i = 0; i < 10; i++)
          //  arrayOfItems.add(new ListData());
    }


    void getLocation(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
        if (location != null) {
            onLocationChanged(location);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            ;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
    }

    ///volley
    void getServices() {
        requestQueue = Volley.newRequestQueue(this);

        String url = UserDetails.getInstance().url + "fetch_services.php";

        //->GET REQUEST USING VOLLEY
        StringRequest request = new StringRequest( Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            Log.d("response_aaya",response);
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jOb = jsonArray.getJSONObject(i);
                                arrayOfItems.add(new ListData(jOb));
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                        NearbyServicesAdapter adapter = new NearbyServicesAdapter(NearbyServices.this, 0, arrayOfItems);
                        requestsList.setAdapter(adapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d("fetch_services error",error.toString());
                    }
                }
        )

        {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("consumer_locx", latitude + "");
                params.put("consumer_locy", longitude + "");
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this,"Permission denied",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.d("Longitude", longitude + " ");
        latitude = location.getLatitude();
        Log.d("Latitude", latitude + " ");


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}


