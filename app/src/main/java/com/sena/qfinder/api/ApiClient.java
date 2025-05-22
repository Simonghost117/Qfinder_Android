package com.sena.qfinder.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static String resetToken = null;  // Para almacenar el token de autorización
    private static String userEmail = null;
    private static final MyCookieJar cookieJar = new MyCookieJar();

    private static final String BASE_URL = "https://qfinder-production.up.railway.app/"; // Cambia por tu URL base correcta

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Métodos para manejar el token y el email
    public static String getToken() {
        return resetToken;
    }

    public static void setResetToken(String token) {
        resetToken = token;
    }

    public static String getUserEmail() {
        return userEmail;
    }

    public static void setUserEmail(String email) {
        userEmail = email;
    }

    // Métodos para manejo de cookies
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

    // Obtener instancia del servicio API
    public static AuthService getAuthService() {
        return getClient().create(AuthService.class);
    }

    // Obtener token formateado para Authorization header ("Bearer <token>")
    public static String getBearerToken() {
        if (resetToken == null || resetToken.isEmpty()) {
            return "";
        }
        return "Bearer " + resetToken;
    }
}
