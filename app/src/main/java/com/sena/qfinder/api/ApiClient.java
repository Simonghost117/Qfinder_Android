package com.sena.qfinder.api;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://qfinder-production.up.railway.app/";
    private static Retrofit retrofit;
    private static final MyCookieJar cookieJar = new MyCookieJar();
    private static String authToken; // Variable para almacenar el token

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder();

                        // Añadir token de autorización si está disponible
                        if (authToken != null) {
                            builder.header("Authorization", "Bearer " + authToken);
                        }

                        return chain.proceed(builder.build());
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Método para establecer el token de autenticación
    public static void setAuthToken(String token) {
        authToken = token;
    }

    // Método para obtener el CookieJar
    public static MyCookieJar getCookieJar() {
        return cookieJar;
    }
}