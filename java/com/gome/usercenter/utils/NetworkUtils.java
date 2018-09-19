package com.gome.usercenter.utils;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

//import com.google.gson.Gson;

import com.gome.usercenter.helper.HttpCallbackStringListener;
import com.gome.usercenter.helper.HttpResponseCall;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetworkUtils {
    private static final String TAG = Constants.TAG_HEADER + "NetworkUtils";

    static ExecutorService threadPool = Executors.newFixedThreadPool(8);
    //static Gson gson = new Gson();
    private static final int DEFAULT_REQUEST_TIMEOUT = 5000; // 5 seconds


    public static void doGet(final Context context,
                              final String urlString, final HttpCallbackStringListener listener) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                URL url;
                HttpURLConnection httpURLConnection = null;
                try {
                    url = new URL(urlString);
                    if(url.getProtocol().toUpperCase().equals("HTTPS")) {
                        trustAllHosts();
                        HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                        https.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                        httpURLConnection = https;
                    }else{
                        httpURLConnection = (HttpURLConnection) url.openConnection();
                    }
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setReadTimeout(DEFAULT_REQUEST_TIMEOUT);
                    httpURLConnection.setConnectTimeout(DEFAULT_REQUEST_TIMEOUT);
                    httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
                    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    httpURLConnection.connect();

                    Log.d(TAG, "getResponseCode = " + httpURLConnection.getResponseCode());
                    if (httpURLConnection.getResponseCode() == 200 ) {
                        InputStream is = httpURLConnection.getInputStream();
                        BufferedReader bf = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        StringBuffer buffer = new StringBuffer();
                        String line = "";
                        while ( (line = bf.readLine()) != null ) {
                            buffer.append(line);
                        }
                        bf.close();
                        is.close();
                        new HttpResponseCall(context, listener).doSuccess(buffer.toString());
                    } else {
                        new HttpResponseCall(context, listener).doFail(
                                new NetworkErrorException("response err code:" +
                                        httpURLConnection.getResponseCode()));
                    }
                } catch ( MalformedURLException e ) {
                    if ( listener != null ) {
                        new HttpResponseCall(context, listener).doFail(e);
                    }
                } catch ( IOException e ) {
                    if ( listener != null ) {
                        new HttpResponseCall(context, listener).doFail(e);
                    }
                } finally {
                    if ( httpURLConnection != null ) {
                        httpURLConnection.disconnect();
                    }
                }
            }
        });
    }

    public static void doPost(final Context context,
                              final String urlString, final HttpCallbackStringListener listener,
                              final String requestString) {
        if (requestString == null) {
            Log.d(TAG, "doPost failed at urlString [" + urlString + "], requestString [" + requestString + "]");
            return;
        }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                URL url;
                HttpURLConnection httpURLConnection = null;
                try {
                    url = new URL(urlString);
                    if(url.getProtocol().toUpperCase().equals("HTTPS")) {
                        trustAllHosts();
                        HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                        https.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                        httpURLConnection = https;
                    }else{
                        httpURLConnection = (HttpURLConnection) url.openConnection();
                    }
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setReadTimeout(DEFAULT_REQUEST_TIMEOUT);
                    httpURLConnection.setConnectTimeout(DEFAULT_REQUEST_TIMEOUT);
                    httpURLConnection.connect();

                    PrintWriter printWriter = new PrintWriter(httpURLConnection.getOutputStream());
                    printWriter.write(requestString);
                    printWriter.flush();
                    printWriter.close();

                    Log.d(TAG, "getResponseCode = " + httpURLConnection.getResponseCode());
                    if (httpURLConnection.getResponseCode() == 200 ) {
                        InputStream is = httpURLConnection.getInputStream();
                        BufferedReader bf = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        StringBuffer buffer = new StringBuffer();
                        String line = "";
                        while ( (line = bf.readLine()) != null ) {
                            buffer.append(line);
                        }
                        bf.close();
                        is.close();
                        new HttpResponseCall(context, listener).doSuccess(buffer.toString());
                    } else {
                        new HttpResponseCall(context, listener).doFail(
                                new NetworkErrorException("response err code:" +
                                        httpURLConnection.getResponseCode()));
                    }
                } catch ( MalformedURLException e ) {
                    if ( listener != null ) {
                        new HttpResponseCall(context, listener).doFail(e);
                    }
                } catch ( IOException e ) {
                    if ( listener != null ) {
                        new HttpResponseCall(context, listener).doFail(e);
                    }
                } finally {
                    if ( httpURLConnection != null ) {
                        httpURLConnection.disconnect();
                    }
                }
            }
        });
    }

    public static String getEncryptString(String str){
        String strRequestEncrypt = null;
        try {
            strRequestEncrypt = DesUtil.encrypt3DES(str);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.i(TAG, "getEncryptString(..)  error:"+e.toString());
            e.printStackTrace();
        }
        return strRequestEncrypt;
    }

    public static String getDecryptString(String str){
        String strResult = str;
        try {
            strResult = DesUtil.decrypt3DES(str);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.i(TAG, "getDecryptString error:"+e.toString());
            e.printStackTrace();
        }
        return strResult;
    }

    public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android use X509 cert
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
                // TODO Auto-generated method stub

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                // TODO Auto-generated method stub

            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    /**
      * Build request string with md5 encrypt
      * @param key params key
      * @param value params value
      * @return request string with sign
      */
       public static String requestBuilder(@NonNull String[] key, @NonNull String[] value) throws JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
                if (key.length != value.length) {
                        throw new IllegalArgumentException("key and value must have same length");
                    }
                JSONObject json= new JSONObject();
                for (int i = 0; i < key.length; i++) {
                        json.put(key[i], value[i]);
                    }
                String sign = signGenerater(json);
                json.put("sign", sign);
                return json.toString();
            }
    /**
      * Build request string for non-params
      * @return request string with sign
      */
    public static String requestBuilder() throws JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {

        JSONObject json= new JSONObject();
        String sign = signGenerater(null);
        json.put("sign", sign);
        return json.toString();
    }

    /**
      * Build sign string according params json
      * @param json be null, just return md5 encrypt string of salt
      * @return request sign
      */
    private static String signGenerater(JSONObject json) throws JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
        String salt = "gome";
        if (json == null) {
            return MD5Encode(salt);
        }
        List<String> keys = new ArrayList<String>();
        Iterator<String> keyIterator = json.keys();
        while (keyIterator.hasNext()) {
            keys.add(keyIterator.next());
        }
        keys.sort(new Comparator() {
            public int compare(Object o1, Object o2) {
                                String s1 = (String) o1;
                                String s2 = (String) o2;
                                return  (s1.compareTo(s2));
                            }
        });
        StringBuilder builder = new StringBuilder();
                builder.append(salt);
                for (String key : keys) {
                        builder.append(key);
                        builder.append(json.getString(key));
                }
        builder.append(salt);
        return MD5Encode(builder.toString());
    }

    private static String MD5Encode(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("md5");
        byte[] md5Bytes = md5.digest(s.getBytes("utf-8"));
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++){
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString().toUpperCase();
    }

    public static boolean requestSuccess(String response) {
        try {
            JSONObject json = new JSONObject(response);
            String resultcode = json.getString("resultcode");
            if ("200".equals(resultcode)) {
                return  true;
            } else {
                Log.d(TAG, "request code: [" + resultcode + "], ErrorMessage: [" + json.getString("message") +"]");
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

}
