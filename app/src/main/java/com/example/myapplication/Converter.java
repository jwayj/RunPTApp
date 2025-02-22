package com.example.myapplication;

public class Converter {

    /**
     * 주어진 초 단위 시간을 "HH:mm:ss" 형식의 문자열로 변환합니다.
     *
     * @param totalSeconds 총 시간(초)
     * @return "HH:mm:ss" 형식의 문자열
     */
    public static String secondsToHMS(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long remainder = totalSeconds % 3600;
        long minutes = remainder / 60;
        long seconds = remainder % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * 주어진 초 단위 시간을 "mm:ss" 형식의 문자열로 변환합니다.
     *
     * @param totalSeconds 총 시간(초)
     * @return "mm:ss" 형식의 문자열
     */
    public static String secondsToMS(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    /**
     * 전체 시간(초)과 거리(킬로미터)를 받아 1km당 소요 시간을 계산하여
     * "mm:ss" 형식의 페이스 문자열을 반환합니다.
     *
     * @param totalTimeSeconds 전체 시간(초)
     * @param distanceKm 거리(킬로미터)
     * @return 1km당 페이스("mm:ss")
     */
    public static String calculatePace(long totalTimeSeconds, double distanceKm) {
        if (distanceKm <= 0) {
            return "0:00"; // 거리 값이 0 이하인 경우 처리
        }
        long paceSeconds = Math.round((double) totalTimeSeconds / distanceKm);
        return secondsToMS(paceSeconds);
    }

}
