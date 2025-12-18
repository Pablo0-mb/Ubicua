package com.example.apppecl3;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("GetData")   // ruta del endpoint
    Call<List<Measurement>> getItems();
}
