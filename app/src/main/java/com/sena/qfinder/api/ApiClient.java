package com.sena.qfinder.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static String resetToken = null;  // Variable para almacenar el token
    private static String userEmail = null;
    private static final MyCookieJar cookieJar = new MyCookieJar();

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://qfinder-production.up.railway.app/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Añade estos métodos para manejar el token
    public static void setResetToken(String token) {
        resetToken = token;
    }
    public static void setUserEmail(String email) {
        userEmail = email;
    }
    public static String getResetToken() {
        return resetToken;
    }

    public static MyCookieJar getCookieJar() {
        return cookieJar;
    }

    public static void clearCookies() {
        cookieJar.clear();
    }

    public static void clearCookiesForDomain(String domain) {
        cookieJar.clearForDomain(domain);
    }

    public static void clearResetToken() {
        resetToken = null;
    }
}