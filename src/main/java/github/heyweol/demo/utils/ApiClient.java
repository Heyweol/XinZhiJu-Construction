package github.heyweol.demo.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApiClient {
  private static final String API_BASE_URL = "https://xinzhiju.replit.app"; // Update this URL
  private static ApiClient instance;
  private final OkHttpClient client;
  private final ObjectMapper objectMapper;
  
  private ApiClient() {
    client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    objectMapper = new ObjectMapper();
  }
  
  public static synchronized ApiClient getInstance() {
    if (instance == null) {
      instance = new ApiClient();
    }
    return instance;
  }
  
  public void shareScreenshot(File screenshotFile, String nickname, String description, String machineId, Map<String, Integer> materials, Callback<ApiResponse> callback) {
    RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("screenshot", screenshotFile.getName(),
                    RequestBody.create(MediaType.parse("image/png"), screenshotFile))
            .addFormDataPart("nickname", nickname)
            .addFormDataPart("description", description)
            .addFormDataPart("machineId", machineId)
            .addFormDataPart("materials", materialsToJson(materials))
            .build();
    
    Request request = new Request.Builder()
            .url(API_BASE_URL + "/share")
            .post(requestBody)
            .build();
    
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onComplete(new ApiResponse(false, "Network error: " + e.getMessage()));
      }
      
      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
          callback.onComplete(new ApiResponse(true, null));
        } else {
          callback.onComplete(new ApiResponse(false, "API error: " + response.message()));
        }
      }
    });
  }
  
  private String materialsToJson(Map<String, Integer> materials) {
    try {
      return objectMapper.writeValueAsString(materials);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "{}";
    }
  }
  
  public interface Callback<T> {
    void onComplete(T result);
  }
  
  public static class ApiResponse {
    private final boolean success;
    private final String errorMessage;
    
    public ApiResponse(boolean success, String errorMessage) {
      this.success = success;
      this.errorMessage = errorMessage;
    }
    
    public boolean isSuccess() {
      return success;
    }
    
    public String getErrorMessage() {
      return errorMessage;
    }
  }
}