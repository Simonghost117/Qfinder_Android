package com.sena.qfinder.api;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class MyCookieJar implements CookieJar {
    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        String host = url.host();
        List<Cookie> storedCookies = cookieStore.getOrDefault(host, new ArrayList<>());

        for (Cookie newCookie : cookies) {
            // Actualizar cookies existentes
            storedCookies.removeIf(cookie -> cookie.name().equals(newCookie.name()));
            storedCookies.add(newCookie);
            Log.d("COOKIE_JAR", "Guardada cookie: " + newCookie.name() + "=" + newCookie.value());
        }

        cookieStore.put(host, storedCookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.getOrDefault(url.host(), new ArrayList<>());
        Log.d("COOKIE_JAR", "Enviando " + cookies.size() + " cookies para " + url.host());
        return cookies;
    }

    public String getCookies(String host) {
        List<Cookie> cookies = cookieStore.getOrDefault(host, new ArrayList<>());
        StringBuilder sb = new StringBuilder();
        for (Cookie cookie : cookies) {
            sb.append(cookie.name()).append("=").append(cookie.value()).append("; ");
        }
        return sb.toString();
    }
}