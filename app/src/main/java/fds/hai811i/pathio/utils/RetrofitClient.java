package fds.hai811i.pathio.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;

import fds.hai811i.pathio.model.ApiService;
import fds.hai811i.pathio.model.RoutingService;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit instance;
    private static Retrofit routingInstance;
    private static Retrofit overpassInstance;
    private static final String BASE_URL = "https://www.zerohour.fr/";
    private static final String OSRM_URL = "https://router.project-osrm.org/";
    private static final String OVERPASS_URL = "https://overpass-api.de/api/";

    public static ApiService getApi(Context context) {
        if (instance == null) {

            Interceptor authInterceptor = new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request originalRequest = chain.request();

                    SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

                    String token = sharedPreferences.getString("jwt_token", null);

                    // if a token exists, clone the request and attach the Authorization header
                    if (token != null) {
                        Request newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(newRequest);
                    }

                    // no token, normal behaviour
                    return chain.proceed(originalRequest);
                }
            };

            // add interceptor to custom client
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            // retrofit client building
            instance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance.create(ApiService.class);
    }

    public static RoutingService getRoutingApi() {
        if (routingInstance == null) {
            routingInstance = new Retrofit.Builder()
                    .baseUrl(OSRM_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return routingInstance.create(RoutingService.class);
    }

    public static fds.hai811i.pathio.model.OverpassService getOverpassApi() {
        if (overpassInstance == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .header("User-Agent", "PathioAndroidApp/1.0")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            overpassInstance = new Retrofit.Builder()
                    .baseUrl(OVERPASS_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return overpassInstance.create(fds.hai811i.pathio.model.OverpassService.class);
    }
}
