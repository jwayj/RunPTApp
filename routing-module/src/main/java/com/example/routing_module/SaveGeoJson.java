package com.example.routing_module;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.nio.charset.StandardCharsets;

public class SaveGeoJson {
    public static void uploadToFirebase(String geoJson, String fileName) {
        try {
            // 1. 파라미터 유효성 검사
            validateParameters(geoJson, fileName);

            // 2. Firebase 스토리지 참조 생성
            StorageReference fileRef = createStorageReference(fileName);

            // 3. 메타데이터 설정
            StorageMetadata metadata = buildMetadata();

            // 4. 바이트 배열 변환 및 업로드
            byte[] data = geoJson.getBytes(StandardCharsets.UTF_8);
            executeUpload(fileRef, data, metadata);

        } catch (IllegalArgumentException e) {
            handleError("파라미터 오류: " + e.getMessage());
        } catch (Exception e) {
            handleError("업로드 실패: " + e.getMessage());
        }
    }

    private static void validateParameters(String geoJson, String fileName) {
        if (geoJson == null || geoJson.isEmpty()) {
            throw new IllegalArgumentException("GeoJSON 데이터가 없습니다");
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다");
        }
    }

    private static StorageReference createStorageReference(String fileName) {
        return FirebaseStorage.getInstance()
                .getReference()
                .child("geojson")
                .child(fileName);
    }

    private static StorageMetadata buildMetadata() {
        return new StorageMetadata.Builder()
                .setContentType("application/geo+json")
                .setCustomMetadata("source", "RunPT_App")
                .build();
    }

    private static void executeUpload(StorageReference fileRef,
                                      byte[] data,
                                      StorageMetadata metadata) {
        UploadTask uploadTask = fileRef.putBytes(data, metadata);

        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            System.out.printf("업로드 진행률: %.2f%%%n", progress);
        }).addOnSuccessListener(taskSnapshot -> {
            System.out.println("성공: " + taskSnapshot.getMetadata().getPath());
        }).addOnFailureListener(e -> {
            throw new RuntimeException("업로드 실패: " + e.getMessage());
        });
    }

    private static void handleError(String message) {
        System.err.println(message);
        // Android 환경에서는 Log.e() 사용 권장
        // Log.e("FirebaseUpload", message);
    }
}
