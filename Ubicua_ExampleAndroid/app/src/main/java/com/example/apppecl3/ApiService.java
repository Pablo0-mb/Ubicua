package com.example.apppecl3;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.DELETE;

public interface ApiService {
    @GET("GetData")   // ruta del endpoint
    Call<List<Measurement>> getItems();

    @DELETE("DeleteData")
    Call<Void> deleteItems();
}
