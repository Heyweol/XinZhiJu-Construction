package github.heyweol.demo.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
// import JSONObject


public class ApiClient {
  private static final String API_BASE_URL = "https://f511f637-0bf2-4be9-8546-8841ed42984d-00-1mat5hmeusjdz.janeway.replit.dev"; // Update this URL
  private static ApiClient instance;
  private final OkHttpClient client;
  private final ObjectMapper objectMapper;
  private String authToken;
  
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
  
  public void shareScreenshot(
          File screenshotFile, String nickname, String description,
          String machineId, Map<String, Integer> materials,
          Callback<ApiResponse> callback) {
    
    if (!isLoggedIn()) {
      callback.onComplete(new ApiResponse(false, "User not logged in"));
      return;
    }
    
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
            .addHeader("Authorization", "Bearer " + authToken)
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
  
  public void login(String email, String password, Callback<LoginResponse> callback) {
    RequestBody requestBody = new FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .build();
    
    Request request = new Request.Builder()
            .url(API_BASE_URL + "/login")
            .post(requestBody)
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .build();
    
    client.newCall(request).enqueue(new okhttp3.Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        callback.onComplete(new LoginResponse(false, null, "Network error: " + e.getMessage()));
      }
      
      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
          String responseBody = response.body().string();
          JsonNode jsonNode = objectMapper.readTree(responseBody);
          String token = jsonNode.get("access_token").asText();
          authToken = token; // Store the token
          callback.onComplete(new LoginResponse(true, token, null));
        } else {
          callback.onComplete(new LoginResponse(false, null, "Login failed: " + response.message()));
        }
      }
    });
  }
  
  public void logout() {
    authToken = null;
  }
  
  public boolean isLoggedIn() {
    return authToken != null;
  }
  
  public static class LoginResponse {
    private final boolean success;
    private final String token;
    private final String errorMessage;
    
    public LoginResponse(boolean success, String token, String errorMessage) {
      this.success = success;
      this.token = token;
      this.errorMessage = errorMessage;
    }
    
    // Add getters
  }
  
  // You might want to add a method to refresh the token if it expires
  public void refreshToken(Callback<LoginResponse> callback) {
    // Implementation depends on your backend's token refresh mechanism
  }
  
}