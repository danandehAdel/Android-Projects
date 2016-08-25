package com.example;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.example.response.EntireObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
    }

    public void getWeather(View v) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://luca-teaching.appspot.com/weather/")
                .addConverterFactory(GsonConverterFactory.create())	//parse Gson string
                .client(httpClient)	//add logging
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<EntireObject> queryResponseCall = service.getWeather();

        queryResponseCall.enqueue(new Callback<EntireObject>() {
            @Override
            public void onResponse(Response<EntireObject> response) {

                if (response.code() == 200) {
                    if (response.body().response.result.equals("ok")) {
                        showDetails(response.body());
                    }else{
                        toast("[200: result = \"error\"]");
                    }
                }else if (response.code() == 500) {
                    toast("[500 Server Error]");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Log error here since request failed
            }
        });
    }

	@Override
	public void onResume(){
		super.onResume();
    }

    public void showDetails(EntireObject obj){

        TextView cityView = (TextView) findViewById(R.id.cityView);
        cityView.setText(obj.response.conditions.observationLocation.city);
        cityView.setVisibility(View.VISIBLE);

        TextView weatherView = (TextView) findViewById(R.id.weatherView);
        weatherView.setText(obj.response.conditions.weather);
        weatherView.setVisibility(View.VISIBLE);

        TextView elevatioView = (TextView) findViewById(R.id.elevationView);
        elevatioView.setText(obj.response.conditions.observationLocation.elevation);
        elevatioView.setVisibility(View.VISIBLE);

        TextView tempView = (TextView) findViewById(R.id.tempView);
        String tempF = new Double(obj.response.conditions.tempF).toString();
        tempView.setText(tempF);
        tempView.setVisibility(View.VISIBLE);


        TextView relativeView = (TextView) findViewById(R.id.relativeView);
        relativeView.setText(obj.response.conditions.relativeHumidity);
        relativeView.setVisibility(View.VISIBLE);


        TextView windView = (TextView) findViewById(R.id.windView);
        String windMph = new Double(obj.response.conditions.windMph).toString();
        windView.setText(windMph);
        windView.setVisibility(View.VISIBLE);
    }

    public interface WeatherService {
        @GET("default/get_weather/")
        Call<EntireObject> getWeather();
    }

    // This is to show messages to the screen
    private void toast(String respond) {
        // This is to display a message.
        Toast.makeText(getApplicationContext(), respond, Toast.LENGTH_SHORT).show();
    }

}
