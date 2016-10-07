package com.mercadopago.core;

import android.content.Context;

import com.mercadopago.adapters.ErrorHandlingCallAdapter;
import com.mercadopago.callbacks.Callback;
import com.mercadopago.model.CheckoutPreference;
import com.mercadopago.model.Customer;
import com.mercadopago.model.MerchantPayment;
import com.mercadopago.model.Payment;
import com.mercadopago.services.MerchantService;
import com.mercadopago.util.HttpClientUtil;
import com.mercadopago.util.JsonUtil;

import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MerchantServer {

    public static void createPreference(Context context, String merchantBaseUrl, String merchantCreatePreferenceUri, Map<String, Object> checkoutData, Callback<CheckoutPreference> callback) {

        MerchantService service = getService(context, merchantBaseUrl);
        service.createPreference(ripFirstSlash(merchantCreatePreferenceUri), checkoutData).enqueue(callback);
    }

    public static void getCustomer(Context context, String merchantBaseUrl, String merchantGetCustomerUri, String merchantAccessToken, Callback<Customer> callback) {

        MerchantService service = getService(context, merchantBaseUrl);
        service.getCustomer(ripFirstSlash(merchantGetCustomerUri), merchantAccessToken).enqueue(callback);
    }

    public static void createPayment(Context context, String merchantBaseUrl, String merchantCreatePaymentUri, MerchantPayment payment, Callback<Payment> callback) {

        MerchantService service = getService(context, merchantBaseUrl);
        service.createPayment(ripFirstSlash(merchantCreatePaymentUri), payment).enqueue(callback);
    }

    private static String ripFirstSlash(String uri) {

        return uri.startsWith("/") ? uri.substring(1, uri.length()) : uri;
    }

    private static Retrofit getRetrofit(Context context, String endPoint) {

        return new Retrofit.Builder()
                .baseUrl(endPoint)
                .client(HttpClientUtil.getClient(context, 20, 20, 20))
                .addConverterFactory(GsonConverterFactory.create(JsonUtil.getInstance().getGson()))
                .addCallAdapterFactory(new ErrorHandlingCallAdapter.ErrorHandlingCallAdapterFactory())
                .build();
    }

    private static MerchantService getService(Context context, String endPoint) {

        Retrofit retrofit = getRetrofit(context, endPoint);
        return retrofit.create(MerchantService.class);
    }
}
