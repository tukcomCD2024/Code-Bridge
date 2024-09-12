package com.Backend.shareNote.domain.Oraganization.service;

import com.Backend.shareNote.domain.Oraganization.DTOs.notification.FcmMessage;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import lombok.RequiredArgsConstructor;
import okhttp3.*;

import org.springframework.stereotype.Service;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FirebaseCloudMessageService {
    private final String API_URL = "https://fcm.googleapis.com/v1/projects/" +
            "fcmalarm-258f5/messages:send";
    private final ObjectMapper objectMapper;

    public void sendMessageTo(String targetToken, String title, String body) throws IOException {
        String message = makeMessage(targetToken, title, body);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.body().string());
    }

    public String makeMessage(String targetToken, String title, String body) throws JsonParseException, JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .build()
                        ).build()).validateOnly(false).build();
        return objectMapper.writeValueAsString(fcmMessage);
    }
    private String getAccessToken() throws IOException {
        // GitHub Actions에서 생성된 firebase_service_key.json 파일의 경로
        String firebaseConfigPath = "./firebase_service_key.json";  // 프로젝트 루트 경로에서 파일을 찾음

        // 파일을 읽어 GoogleCredentials 생성
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new FileInputStream(firebaseConfigPath))
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
