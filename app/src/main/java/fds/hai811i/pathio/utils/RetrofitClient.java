package fds.hai811i.pathio.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fds.hai811i.pathio.model.services.ApiService;
import fds.hai811i.pathio.model.services.RoutingService;
import fds.hai811i.pathio.model.services.OverpassService;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static ApiService apiService;
    private static RoutingService routingService;
    private static OverpassService overpassService;

    private static final String BASE_URL = "https://www.zerohour.fr/";
    private static final String OSRM_URL = "https://router.project-osrm.org/";
    private static final String OVERPASS_URL = "https://overpass-api.de/api/";

    public static ApiService getApi(Context context) {
        if (apiService == null) {

            // Intercepteur en Lambda
            Interceptor authInterceptor = chain -> {
                Request originalRequest = chain.request();

                SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("jwt_token", null);

                // S'il y a un token, on l'ajoute
                if (token != null) {
                    Request newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(newRequest);
                }

                // Sans token (visiteur anonyme)
                return chain.proceed(originalRequest);
            };

            // Ajout des timeouts pour gérer les fichiers (images, audio)
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    public static RoutingService getRoutingApi() {
        if (routingService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(OSRM_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            routingService = retrofit.create(RoutingService.class);
        }
        return routingService;
    }

    public static OverpassService getOverpassApi() {
        if (overpassService == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .header("User-Agent", "PathioAndroidApp/1.0")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(OVERPASS_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            overpassService = retrofit.create(OverpassService.class);
        }
        return overpassService;
    }
}