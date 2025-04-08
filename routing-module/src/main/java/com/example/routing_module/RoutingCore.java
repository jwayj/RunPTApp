package com.example.routing_module;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.util.Log;
import android.content.Context;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.json.Statement;
import static com.graphhopper.json.Statement.If;
import static com.graphhopper.json.Statement.Op.LIMIT;
import static com.graphhopper.json.Statement.Op.MULTIPLY;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.graphhopper.util.Translation;
import com.graphhopper.util.details.PathDetail;
import com.graphhopper.util.shapes.GHPoint;

public class RoutingCore {
    public static void calculateRoute(
            Context context,
            double startLat,
            double startLon,
            double endLat,
            double endLon
    ) {
        Log.d("RouteCore", "경로 계산 시작");
        String currentDir = System.getProperty("user.dir");

        // 상대 경로 설정 (현재 디렉토리 기준)
        String relativePath = "\\routing-module\\seoul-non-military.osm.pbf"; // 상대 경로
        String filePath = currentDir + relativePath;

        // GraphHopper 인스턴스 생성
        GraphHopper hopper = createGraphHopperInstance(filePath);

        GHPoint start = new GHPoint(37.5571, 126.9414); // 이화여자대학교
        GHPoint end = new GHPoint(37.56639, 126.93881); // 연세대학교

        double desiredDistance = 5000;

        ResponsePath path1 = routingWithDesiredDistance(hopper, desiredDistance, start, end);
        if (path1 != null) {
            System.out.println("경로 거리: " + path1.getDistance() + " 미터");

            String geoJson1 = GeoJsonExporter1.toGeoJSON(path1);
            System.out.println("GeoJSON:\n" + geoJson1);
            try {
                SaveGeoJson.uploadToFirebase(geoJson1, "route1.geojson");
                System.out.println("GeoJSON1 saved to route1.geojson");
            } catch (Exception e) {
                System.err.println("Error saving GeoJSON: " + e.getMessage());
            }
        } else {
            System.out.println("원하는 거리의 경로를 찾을 수 없습니다.");
        }

        // 🚀 랜덤 경유지 생성 (500m ~ 1500m 범위 내 3개)
        PointList randomWaypoints = generateRandomWaypoints(hopper, start, 3, 500, 1500);
        System.out.println("🔍 route()에서 강제 삽입된 랜덤 경유지: " + randomWaypoints);

        // 🚀 랜덤 경유지를 사용하여 경로 탐색
        ResponsePath path2 = findPathWithWaypoints(hopper, start, randomWaypoints);

        if (path2 != null) {
            System.out.println("✅ 새로운 경로 거리: " + path2.getDistance() + " 미터");

            String geoJson2 = GeoJsonExporter1.toGeoJSON(path2);
            System.out.println("GeoJSON:\n" + geoJson2);
            try {
                SaveGeoJson.uploadToFirebase(geoJson2, "route2.geojson");
                System.out.println("📂 GeoJSON2 saved to route2.geojson");
            } catch (Exception e) {
                System.err.println("❌ Error saving GeoJSON: " + e.getMessage());
            }
        } else {
            System.out.println("❌ 경로를 찾을 수 없습니다.");
        }

        try {
            // 출발지 정의
            GHPoint startPoint = new GHPoint(37.5665, 126.9780); // 서울 시청 근처
            List<Integer> globalAvoidEdges = new ArrayList<>(); // 🔥 전역적으로 Edge 회피 저장
            PointList globalAvoidPoints = new PointList(); // 🔥 전역적으로 Point 회피 저장
            ResponsePath previousPath = null;

            for (int attempt = 0; attempt < 3; attempt++) { // 🔥 3번의 다른 경로 탐색 시도
                System.out.println("🚀 " + (attempt + 1) + "번째 경로 탐색 시작...");

                ResponsePath diversePath = findDiverseOptimalPath(hopper, startPoint, desiredDistance, globalAvoidEdges, globalAvoidPoints);

                if (diversePath != null) {
                    System.out.println("✅ 최종 경로 거리: " + diversePath.getDistance() + " 미터");

                    // 📌 경로가 동일하면 다시 시도하도록 설정
                    if (previousPath != null && Math.abs(diversePath.getDistance() - previousPath.getDistance()) < 5) {
                        System.out.println("⚠️ 동일한 경로가 감지됨. 다시 탐색...");
                        continue;
                    }

                    // 📌 diversePath에서 PointList 추출
                    PointList pathPoints = diversePath.getPoints();

                    // 📌 GeoJSON 생성
                    String geoJson = GeoJsonExporter2.toGeoJSON(diversePath, new PointList(), pathPoints);
                    System.out.println("GeoJSON:\n" + geoJson);

                    // 📌 GeoJSON 저장
                    SaveGeoJson.uploadToFirebase(geoJson, "route.geojson");
                    System.out.println("GeoJSON saved to route.geojson");

                    // 🔥 회피할 Edge 및 Points 저장
                    for (int i = 0; i < pathPoints.size(); i++) {
                        globalAvoidPoints.add(pathPoints.getLat(i), pathPoints.getLon(i));
                    }
                    diversePath.getPathDetails().getOrDefault("edge_id", new ArrayList<>())
                            .forEach(detail -> globalAvoidEdges.add((Integer) detail.getValue()));

                    previousPath = diversePath;
                } else {
                    System.out.println("❌ 적절한 경로를 찾을 수 없습니다.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            hopper.close();
        }

    }


    public static ResponsePath routingWithDesiredDistance(GraphHopper hopper, double desiredDistance, GHPoint start, GHPoint end) {
        double tolerance = 100; // 200미터 오차 허용
        double searchRadius = desiredDistance * 0.75; // 원하는 거리의 75%로 검색 반경 설정

        ResponsePath bestPath = null;
        double closestDifference = Double.MAX_VALUE;

        LocationIndex locationIndex = hopper.getLocationIndex();
        List<GHPoint> nearbyPoints = new ArrayList<>();

        DistanceCalc distCalc = DistanceCalcEarth.DIST_EARTH;

        // 여러 개의 가까운 지점을 찾기
        for (int i = 0; i < 10000; i++) { // 포인트 생성 수 증가
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * searchRadius;
            double lat = start.lat + (distance / 111000) * Math.cos(angle);
            double lon = start.lon + (distance / (111000 * Math.cos(Math.toRadians(start.lat)))) * Math.sin(angle);
            Snap qr = locationIndex.findClosest(lat, lon, EdgeFilter.ALL_EDGES);
            if (qr.isValid()) {
                GHPoint nearbyPoint = qr.getSnappedPoint();
                nearbyPoints.add(nearbyPoint);
            }
        }

        // 랜덤으로 지점 선택
        Collections.shuffle(nearbyPoints);

        // 각 랜덤 지점에 대해 경로 찾기
        for (GHPoint intermediatePoint : nearbyPoints) {
            ResponsePath path1 = findPath(hopper, start, intermediatePoint, "foot");
            ResponsePath path2 = findPath(hopper, intermediatePoint, end, "foot");


            if (path1 != null && path2 != null) {
                double totalDistance = path1.getDistance() + path2.getDistance();
                double difference = Math.abs(totalDistance - desiredDistance);

                if (difference < closestDifference) {
                    closestDifference = difference;
                    bestPath = combinePaths(path1, path2);

                    if (difference <= tolerance) {
                        return bestPath; // 충분히 가까운 경로를 찾았으면 즉시 반환
                    }
                }
            }
        }

        if (bestPath == null) {
            System.out.println("원하는 거리의 경로를 찾을 수 없습니다. 직접 연결 경로를 반환합니다.");
            return findPath(hopper, start, end, "foot");
        }

        System.out.println("가장 가까운 경로를 찾았습니다. 차이: " + closestDifference + " 미터");
        return bestPath;
    }

    static PointList generateOptimizedWaypoints(GraphHopper hopper, GHPoint start, int numWaypoints, double desiredDistance) {
        Random random = new Random();
        LocationIndex locationIndex = hopper.getLocationIndex();
        PointList waypoints = new PointList();

        for (int i = 0; i < numWaypoints; i++) {
            for (int attempts = 0; attempts < 100; attempts++) {
                double distance = 400 + (1000 - 400) * random.nextDouble(); // 🔥 400m ~ 1000m로 제한
                double angle = random.nextDouble() * 2 * Math.PI;

                double deltaLat = (distance / 111000) * Math.cos(angle);
                double deltaLon = (distance / (111000 * Math.cos(Math.toRadians(start.lat)))) * Math.sin(angle);

                double lat = start.lat + deltaLat;
                double lon = start.lon + deltaLon;

                GHPoint candidate = new GHPoint(lat, lon);
                Snap snap = locationIndex.findClosest(candidate.lat, candidate.lon, EdgeFilter.ALL_EDGES);

                if (snap.isValid()) {
                    GHPoint snappedPoint = snap.getSnappedPoint();
                    boolean isValid = true;

                    for (int j = 0; j < waypoints.size(); j++) {
                        GHPoint existingPoint = new GHPoint(waypoints.getLat(j), waypoints.getLon(j));
                        double dist = calculateDistance(existingPoint, snappedPoint);

                        // 🔥 경유지가 너무 가깝거나 멀면 제외
                        if (dist < 400 || dist > 1000) {
                            isValid = false;
                            break;
                        }
                    }

                    if (isValid) {
                        waypoints.add(snappedPoint.lat, snappedPoint.lon);
                        break;
                    }
                }
            }
        }
        return waypoints;
    }

    static PointList generateRandomWaypoints(GraphHopper hopper, GHPoint start, int numWaypoints, double minDistance, double maxDistance) {
        Random random = new Random();
        LocationIndex locationIndex = hopper.getLocationIndex();
        PointList waypoints = new PointList();

        for (int i = 0; i < numWaypoints; i++) {
            for (int attempts = 0; attempts < 100; attempts++) { // 최대 100번 시도
                double distance = minDistance + (maxDistance - minDistance) * random.nextDouble();
                double angle = random.nextDouble() * 2 * Math.PI;

                double deltaLat = (distance / 111000) * Math.cos(angle);
                double deltaLon = (distance / (111000 * Math.cos(Math.toRadians(start.lat)))) * Math.sin(angle);

                double lat = start.lat + deltaLat;
                double lon = start.lon + deltaLon;

                if (Double.isNaN(lat) || Double.isNaN(lon)) continue;

                GHPoint candidate = new GHPoint(lat, lon);
                Snap snap = locationIndex.findClosest(candidate.lat, candidate.lon, EdgeFilter.ALL_EDGES);

                if (snap.isValid()) {
                    boolean isTooClose = false;
                    GHPoint snappedPoint = snap.getSnappedPoint();

                    for (int j = 0; j < waypoints.size(); j++) {
                        double existingLat = waypoints.getLat(j);
                        double existingLon = waypoints.getLon(j);
                        GHPoint existingPoint = new GHPoint(existingLat, existingLon);

                        // calculateDistance 메서드 호출
                        if (calculateDistance(existingPoint, snappedPoint) < minDistance / 2) {
                            isTooClose = true;
                            break;
                        }
                    }

                    if (!isTooClose) {
                        waypoints.add(snappedPoint.lat, snappedPoint.lon);
                        break;
                    }
                }
            }
        }

        return waypoints;
    }

    private static ResponsePath findPath(GraphHopper hopper, GHPoint start, GHPoint end, String profile) {
        GHRequest req = new GHRequest(start, end)
                .setAlgorithm(Parameters.Algorithms.ASTAR_BI)
                .setProfile(profile);
        GHResponse rsp = hopper.route(req);
        if (rsp.hasErrors()) {
            return null;
        }
        return rsp.getBest();
    }


    private static ResponsePath combinePaths(ResponsePath path1, ResponsePath path2) {
        ResponsePath combinedPath = new ResponsePath();

        // 포인트 리스트 병합
        PointList combinedPoints = new PointList(path1.getPoints().size() + path2.getPoints().size() - 1, path1.getPoints().is3D());
        combinedPoints.add(path1.getPoints());
        combinedPoints.add(path2.getPoints().copy(1, path2.getPoints().size()));
        combinedPath.setPoints(combinedPoints);

        // 거리, 시간, 가중치 합산
        combinedPath.setDistance(path1.getDistance() + path2.getDistance());
        combinedPath.setTime(path1.getTime() + path2.getTime());
        combinedPath.setRouteWeight(path1.getRouteWeight() + path2.getRouteWeight());

        // 안내 정보 병합
        InstructionList combinedInstructions = new InstructionList(path1.getInstructions().getTr());
        combinedInstructions.addAll(path1.getInstructions());
        combinedInstructions.addAll(path2.getInstructions());
        combinedPath.setInstructions(combinedInstructions);

        // 경로 세부 정보 병합
        Map<String, List<PathDetail>> combinedDetails = new HashMap<>();
        for (Map.Entry<String, List<PathDetail>> entry : path1.getPathDetails().entrySet()) {
            combinedDetails.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        for (Map.Entry<String, List<PathDetail>> entry : path2.getPathDetails().entrySet()) {
            combinedDetails.merge(entry.getKey(), entry.getValue(), (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            });
        }
        combinedPath.addPathDetails(combinedDetails);


        // 기타 필요한 정보 설정
        combinedPath.setAscend(path1.getAscend() + path2.getAscend());
        combinedPath.setDescend(path1.getDescend() + path2.getDescend());

        return combinedPath;
    }


    static ResponsePath findPathAvoiding(GraphHopper hopper, GHPoint start, GHPoint end, PointList avoidPoints) {
        GHRequest request = new GHRequest()
                .addPoint(start)
                .addPoint(end)
                .setProfile("foot")
                .setAlgorithm("astarbi")  // 🔥 CH와 호환되는 알고리즘으로 변경
                .putHint("ch.disable", true);  // 🔥 CH 비활성화


        if (avoidPoints != null && !avoidPoints.isEmpty()) {
            request.putHint("routing.avoid_points", avoidPoints);
        }

        GHResponse response = hopper.route(request);
        if (response.hasErrors()) {
            response.getErrors().forEach(error -> System.err.println("Error during routing: " + error.getMessage()));
            return null;
        }

        return response.getBest();
    }

    static double calculateDistance(GHPoint point1, GHPoint point2) {
        double earthRadius = 6371000; // 지구 반지름 (미터 단위)
        double dLat = Math.toRadians(point2.lat - point1.lat);
        double dLon = Math.toRadians(point2.lon - point1.lon);
        double lat1 = Math.toRadians(point1.lat);
        double lat2 = Math.toRadians(point2.lat);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

    static boolean pointListContains(PointList points, double lat, double lon) {
        for (int i = 0; i < points.size(); i++) {
            if (points.getLat(i) == lat && points.getLon(i) == lon) {
                return true;
            }
        }
        return false;
    }


    static GraphHopper createGraphHopperInstance(String ghLoc) {
        // 기존 캐시 삭제
        File cacheDir = new File("target/routing-graph-cache");
        if (cacheDir.exists()) {
            for (File file : cacheDir.listFiles()) {
                file.delete();
            }
            cacheDir.delete();
            System.out.println("Existing cache deleted.");
        }

        // GraphHopper 인스턴스 생성 및 설정
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(ghLoc);
        hopper.setGraphHopperLocation("target/routing-graph-cache");

        // 필요한 모든 Encoded Values 추가
        hopper.setEncodedValuesString("foot_access, foot_average_speed, road_class, max_speed");

        // CustomModel 설정
        CustomModel customModel = new CustomModel();

        // 우선순위 설정
        customModel.getPriority().add(Statement.If("road_class == RESIDENTIAL", Statement.Op.MULTIPLY, "0.8"));
        customModel.getPriority().add(Statement.ElseIf("road_class == FOOTWAY", Statement.Op.MULTIPLY, "1.2"));
        customModel.getPriority().add(Statement.Else(Statement.Op.MULTIPLY, "1.0")); // 기본값

        // 속도 설정
        customModel.getSpeed().add(Statement.If("max_speed > 50", Statement.Op.LIMIT, "50"));
        customModel.getSpeed().add(Statement.Else(Statement.Op.LIMIT, "30")); // 기본 속도 제한

        // 거리 영향도 설정
        customModel.setDistanceInfluence(70.0);

        // Profile 설정
        Profile footProfile = new Profile("foot")
                .setWeighting("custom")
                .setCustomModel(customModel);
        hopper.setProfiles(footProfile);

        // CH 및 LM 설정
        hopper.getCHPreparationHandler().setCHProfiles(new CHProfile("foot"));
        hopper.getLMPreparationHandler().setLMProfiles(new LMProfile("foot"));

        hopper.importOrLoad();
        return hopper;
    }


    public static ResponsePath routing(GraphHopper hopper, GHPoint start, GHPoint end) {
        GHRequest req = new GHRequest(start, end)
                .setProfile("foot")  // 보행자 프로필 사용
                .setLocale(Locale.US);

        GHResponse rsp = hopper.route(req);

        if (rsp.hasErrors()) {
            throw new RuntimeException(rsp.getErrors().toString());
        }

        ResponsePath path = rsp.getBest();

        // 경로 정보 출력
        System.out.println("총 거리: " + path.getDistance() + " 미터");
        System.out.println("예상 소요 시간: " + (path.getTime() / 60000) + " 분");

        // 경로 안내 출력
        Translation tr = hopper.getTranslationMap().getWithFallBack(Locale.UK);
        InstructionList il = path.getInstructions();
        for (Instruction instruction : il) {
            System.out.println(instruction.getDistance() + "m: " + instruction.getTurnDescription(tr));
        }

        return path;
    }


    public static void speedModeVersusFlexibleMode(GraphHopper hopper) {
        GHRequest req = new GHRequest(42.508552, 1.532936, 42.507508, 1.528773).
                setProfile("car").setAlgorithm(Parameters.Algorithms.ASTAR_BI).putHint(Parameters.CH.DISABLE, true);
        GHResponse res = hopper.route(req);
        if (res.hasErrors())
            throw new RuntimeException(res.getErrors().toString());
        assert Helper.round(res.getBest().getDistance(), -2) == 600;
    }

    public static void alternativeRoute(GraphHopper hopper) {
        // calculate alternative routes between two points (supported with and without CH)
        GHRequest req = new GHRequest().setProfile("car").
                addPoint(new GHPoint(42.502904, 1.514714)).addPoint(new GHPoint(42.508774, 1.537094)).
                setAlgorithm(Parameters.Algorithms.ALT_ROUTE);
        req.getHints().putObject(Parameters.Algorithms.AltRoute.MAX_PATHS, 3);
        GHResponse res = hopper.route(req);
        if (res.hasErrors())
            throw new RuntimeException(res.getErrors().toString());
        assert res.getAll().size() == 2;
        assert Helper.round(res.getBest().getDistance(), -2) == 2200;
    }

    /**
     * To customize profiles in the config.yml file you can use a json or yml file or embed it directly. See this list:
     * web/src/test/resources/com/graphhopper/application/resources and https://www.graphhopper.com/?s=customizable+routing
     */
    public static void customizableRouting(String ghLoc) {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(ghLoc);
        hopper.setGraphHopperLocation("target/routing-custom-graph-cache");
        hopper.setEncodedValuesString("car_access, car_average_speed");
        hopper.setProfiles(new Profile("car_custom").setCustomModel(GHUtility.loadCustomModelFromJar("car.json")));

        // The hybrid mode uses the "landmark algorithm" and is up to 15x faster than the flexible mode (Dijkstra).
        // Still it is slower than the speed mode ("contraction hierarchies algorithm") ...
        hopper.getLMPreparationHandler().setLMProfiles(new LMProfile("car_custom"));
        hopper.importOrLoad();

        // ... but for the hybrid mode we can customize the route calculation even at request time:
        // 1. a request with default preferences
        GHRequest req = new GHRequest().setProfile("car_custom").
                addPoint(new GHPoint(42.506472, 1.522475)).addPoint(new GHPoint(42.513108, 1.536005));

        GHResponse res = hopper.route(req);
        if (res.hasErrors())
            throw new RuntimeException(res.getErrors().toString());

        assert Math.round(res.getBest().getTime() / 1000d) == 94;

        // 2. now avoid the secondary road and reduce the maximum speed, see docs/core/custom-models.md for an in-depth explanation
        // and also the blog posts https://www.graphhopper.com/?s=customizable+routing
        CustomModel model = new CustomModel();
        model.addToPriority(If("road_class == SECONDARY", MULTIPLY, "0.5"));

        // unconditional limit to 20km/h
        model.addToSpeed(If("true", LIMIT, "30"));


        req.setCustomModel(model);
        res = hopper.route(req);
        if (res.hasErrors())
            throw new RuntimeException(res.getErrors().toString());

        assert Math.round(res.getBest().getTime() / 1000d) == 184;
    }

    //-----------------여기서부터가 cycle 만들때 필요한 함수 추가(수정)---------------------
    static GHPoint generateIntermediateWaypoint(GraphHopper hopper, GHPoint start, GHPoint end, double maxDistance) {
        LocationIndex locationIndex = hopper.getLocationIndex();
        DistanceCalc distCalc = DistanceCalcEarth.DIST_EARTH;

        for (int i = 0; i < 100; i++) { // 여러 번 시도
            double ratio = 0.3 + Math.random() * 0.4; // 30% ~ 70% 지점에서 랜덤 선택
            double lat = start.lat + ratio * (end.lat - start.lat);
            double lon = start.lon + ratio * (end.lon - start.lon);

            Snap snap = locationIndex.findClosest(lat, lon, EdgeFilter.ALL_EDGES);
            if (snap.isValid()) {
                GHPoint candidate = snap.getSnappedPoint();
                double distance = distCalc.calcDist(start.lat, start.lon, candidate.lat, candidate.lon);

                if (distance <= maxDistance) {
                    return candidate;
                }
            }
        }
        return null; // 적절한 지점을 찾지 못한 경우
    }

    //거리 벗어나면 다시 탐색
    static ResponsePath findOptimalPath(GraphHopper hopper, GHPoint startPoint, double desiredDistance) {
        int maxWaypoints = 5;
        double lowerBound = desiredDistance * 0.9;
        double upperBound = desiredDistance * 1.1;

        PointList waypoints = new PointList();
        PointList avoidPoints = new PointList();
        ResponsePath fullPath = null;
        GHPoint previousPoint = startPoint;

        for (int i = 0; i < maxWaypoints; i++) {
            GHPoint newWaypoint = generateIntermediateWaypoint(hopper, previousPoint, startPoint, desiredDistance * 0.2);
            if (newWaypoint == null) break;

            double distToPrev = calculateDistance(previousPoint, newWaypoint);

            // 🔥 경유지 간 거리 제한 (400m ~ 1000m)
            if (distToPrev < 300 || distToPrev > 2000) {
                System.out.println("⚠️ 경유지가 너무 가까움/멀어 제외: " + distToPrev + "m");
                continue;
            }

            ResponsePath outboundPath = findPathAvoiding(hopper, previousPoint, newWaypoint, avoidPoints);
            ResponsePath returnPath = findPathAvoiding(hopper, newWaypoint, startPoint, avoidPoints);

            if (outboundPath == null || returnPath == null) continue;

            double totalDistance = (fullPath == null ? 0 : fullPath.getDistance()) + outboundPath.getDistance() + returnPath.getDistance();
            System.out.println("🔍 현재 경로 거리: " + totalDistance + "m (목표: " + lowerBound + " ~ " + upperBound + ")");

            // ✅ 목표 거리 범위 도달하면 즉시 반환
            if (totalDistance >= lowerBound && totalDistance <= upperBound) {
                fullPath = (fullPath == null) ? outboundPath : combinePaths(fullPath, outboundPath);
                fullPath = combinePaths(fullPath, returnPath);
                System.out.println("✅ 적절한 경로 발견! 최종 거리: " + fullPath.getDistance() + "m");
                break;
            }

            // 🔥 경로가 너무 길면 새로운 경유지를 찾도록 루프 재설정
            if (totalDistance > upperBound) {
                System.out.println("⚠️ 경로가 너무 깁니다. 새로운 경유지를 찾습니다...");
                waypoints.clear();
                avoidPoints.clear();
                fullPath = null;
                i = -1;  // 루프를 처음부터 다시 시작
                continue;
            }

            fullPath = (fullPath == null) ? outboundPath : combinePaths(fullPath, outboundPath);
            fullPath = combinePaths(fullPath, returnPath);
            previousPoint = newWaypoint;
            waypoints.add(newWaypoint.lat, newWaypoint.lon);
            avoidPoints.add(newWaypoint.lat, newWaypoint.lon);
        }

        if (fullPath == null) {
            System.out.println("❌ 유효한 경로를 찾을 수 없습니다.");
        } else {
            System.out.println("✅ 최종 선택된 경로 거리: " + fullPath.getDistance() + " 미터");
        }

        return fullPath;
    }

    //다양한 경로 생성
    // 🚀 1. 새로운 랜덤 경유지 생성 (더 넓은 범위에서)
    static PointList generateDiverseWaypoints(GraphHopper hopper, GHPoint start, int numWaypoints, double minDistance, double maxDistance) {
        Random random = new Random();
        LocationIndex locationIndex = hopper.getLocationIndex();
        PointList waypoints = new PointList();

        List<GHPoint> usedPoints = new ArrayList<>();

        for (int i = 0; i < numWaypoints; i++) {
            for (int attempts = 0; attempts < 50; attempts++) {  // 🔥 시도 횟수 줄이기
                double distance = minDistance + (maxDistance - minDistance) * random.nextDouble();
                double angle = random.nextDouble() * 2 * Math.PI;

                double deltaLat = (distance / 111000) * Math.cos(angle);
                double deltaLon = (distance / (111000 * Math.cos(Math.toRadians(start.lat)))) * Math.sin(angle);

                double lat = start.lat + deltaLat;
                double lon = start.lon + deltaLon;

                GHPoint candidate = new GHPoint(lat, lon);
                Snap snap = locationIndex.findClosest(candidate.lat, candidate.lon, EdgeFilter.ALL_EDGES);

                if (snap.isValid()) {
                    GHPoint snappedPoint = snap.getSnappedPoint();

                    // 📌 **중복된 지점 회피 + 거리 조건 완화**
                    boolean isValid = true;
                    for (GHPoint used : usedPoints) {
                        double dist = calculateDistance(used, snappedPoint);
                        if (dist < minDistance * 0.8 || dist > maxDistance * 1.2) { // 🔥 오차 허용 범위 추가
                            isValid = false;
                            break;
                        }
                    }

                    if (isValid) {
                        waypoints.add(snappedPoint.lat, snappedPoint.lon);
                        usedPoints.add(snappedPoint);
                        break;
                    }
                }
            }
        }

        return waypoints;
    }


    // 🚀 2. 경로 탐색 시 동일한 경로 회피 (강제적으로 다른 경로 찾기)
    static ResponsePath findDifferentPath(GraphHopper hopper, GHPoint start, GHPoint end, PointList avoidPoints, List<Integer> avoidEdges) {
        GHRequest request = new GHRequest()
                .addPoint(start)
                .addPoint(end)
                .setProfile("foot")
                .setAlgorithm("astarbi")  // 🔥 CH와 호환되는 알고리즘으로 변경
                .putHint("ch.disable", true);  // 🔥 CH 비활성화

        if (!avoidPoints.isEmpty()) {
            request.putHint("routing.avoid_points", avoidPoints);
        }

        if (!avoidEdges.isEmpty()) {
            request.putHint("routing.avoid_edges", avoidEdges);
            System.out.println("🚧 Avoiding edges: " + avoidEdges);
        }

        GHResponse response = hopper.route(request);
        if (response.hasErrors()) {
            System.err.println("❌ 경로 탐색 오류: " + response.getErrors());
            return null;
        }

        ResponsePath bestPath = response.getBest();
        if (bestPath == null || bestPath.getDistance() < 50) { // 🔥 너무 짧은 경로면 다시 시도
            System.out.println("⚠️ 경로가 너무 짧음. 다시 탐색...");
            return null;
        }

        // 🔥 **모든 지나온 Edge를 회피하도록 설정 (강력한 회피 적용)**
        List<PathDetail> edgeDetails = bestPath.getPathDetails().getOrDefault("edge_id", new ArrayList<>());
        for (PathDetail detail : edgeDetails) {
            avoidEdges.add((Integer) detail.getValue());
        }

        return bestPath;
    }


    static ResponsePath findDiverseOptimalPath(GraphHopper hopper, GHPoint startPoint, double desiredDistance,
                                               List<Integer> avoidEdges, PointList avoidPoints) {
        int numWaypoints = 3;  // 🔥 경유지 개수 줄이기
        double minDistance = desiredDistance * 0.15;
        double maxDistance = desiredDistance * 0.4;  // 🔥 최대 거리 확장
        double lowerBound = desiredDistance * 0.9;
        double upperBound = desiredDistance * 1.1;

        // 🚀 경유지 생성
        PointList waypoints = generateDiverseWaypoints(hopper, startPoint, numWaypoints, minDistance, maxDistance);
        ResponsePath fullPath = null;
        GHPoint previousPoint = startPoint;
        double totalDistance = 0;

        if (waypoints.isEmpty()) {
            System.out.println("❌ 유효한 경유지를 찾지 못했습니다. 기본 경로를 사용합니다.");
            return findPath(hopper, startPoint, startPoint, "foot");
        }

        for (int i = 0; i < waypoints.size(); i++) {
            GHPoint waypoint = new GHPoint(waypoints.getLat(i), waypoints.getLon(i));

            // ✅ 기존 경로 회피
            ResponsePath segment = findDifferentPath(hopper, previousPoint, waypoint, avoidPoints, avoidEdges);
            if (segment == null) continue;

            fullPath = (fullPath == null) ? segment : combinePaths(fullPath, segment);
            totalDistance += segment.getDistance();

            // ✅ 지나온 Edge를 기록하여 반드시 회피하도록 설정
            segment.getPathDetails().getOrDefault("edge_id", new ArrayList<>())
                    .forEach(detail -> avoidEdges.add((Integer) detail.getValue()));

            avoidPoints.add(waypoint.lat, waypoint.lon);
            previousPoint = waypoint;
        }

        // 🚀 **출발지로 돌아오는 경로는 완전히 다른 루트 탐색 (새로운 방식 적용)**
        ResponsePath returnSegment = findAlternativeReturnPath(hopper, previousPoint, startPoint, avoidEdges, avoidPoints);
        if (returnSegment != null) {
            fullPath = (fullPath == null) ? returnSegment : combinePaths(fullPath, returnSegment);
            totalDistance += returnSegment.getDistance();
        }

        // ✅ 경로 길이가 원하는 범위를 벗어나면 다시 생성
        if (totalDistance < lowerBound || totalDistance > upperBound) {
            System.out.println("❌ 경로 거리 초과 또는 부족. 다시 탐색...");
            return findDiverseOptimalPath(hopper, startPoint, desiredDistance, avoidEdges, avoidPoints);
        }

        return fullPath;
    }


    //cycle용 waypoint 생성 로직 새로 추가
    static PointList generateCircularWaypoints(GraphHopper hopper, GHPoint start, int numWaypoints, double radius) {
        PointList waypoints = new PointList();
        Random random = new Random();

        // 🔥 numWaypoints를 랜덤화 (예: 3~6개)
        numWaypoints = 3 + random.nextInt(4);

        // 🔥 angleStep을 360도 고정이 아니라 랜덤 간격으로 설정
        double baseAngleStep = 360.0 / numWaypoints;

        for (int i = 0; i < numWaypoints; i++) {
            double angle = Math.toRadians(i * baseAngleStep + random.nextDouble() * 30 - 15);  // 🔥 ±15도 추가 랜덤 오차
            double randomRadius = radius * (0.7 + (random.nextDouble() * 0.6));  // 🔥 반경에 ±30% 랜덤성 추가

            double lat = start.lat + (randomRadius / 111000) * Math.cos(angle);
            double lon = start.lon + (randomRadius / (111000 * Math.cos(Math.toRadians(start.lat)))) * Math.sin(angle);

            Snap snap = hopper.getLocationIndex().findClosest(lat, lon, EdgeFilter.ALL_EDGES);
            if (snap.isValid()) {
                GHPoint snappedPoint = snap.getSnappedPoint();
                waypoints.add(snappedPoint.lat, snappedPoint.lon);
            }
        }

        return waypoints;
    }


    static ResponsePath findPathWithWaypoints(GraphHopper hopper, GHPoint start, PointList waypoints) {
        GHRequest req = new GHRequest()
                .setProfile("foot")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE)  // 🔥 대체 경로 탐색 적용
                .putHint("ch.disable", true)  // 🔥 CH 비활성화하여 경로 다양화
                .putHint("alternative_route.max_paths", 3)  // 🔥 최대 3개의 대체 경로 탐색
                .putHint("alternative_route.max_weight_factor", 2.0) // 🔥 최단 경로보다 2배 긴 경로도 허용
                .addPoint(start);

        // ✅ 중간 경유지 추가
        for (int i = 0; i < waypoints.size(); i++) {
            req.addPoint(new GHPoint(waypoints.getLat(i), waypoints.getLon(i)));
        }

        // ✅ 최종 목적지를 '출발지'로 설정하여 순환 경로 형성
        req.addPoint(start);

        GHResponse rsp = hopper.route(req);

        if (rsp.hasErrors()) {
            System.out.println("❌ 경로 탐색 실패: " + rsp.getErrors());
            return null;
        }

        return rsp.getBest();
    }

    static ResponsePath findAlternativeReturnPath(GraphHopper hopper, GHPoint start, GHPoint end, List<Integer> avoidEdges, PointList avoidPoints) {
        GHRequest request = new GHRequest()
                .addPoint(start)
                .addPoint(end)
                .setProfile("foot")
                .setAlgorithm(Parameters.Algorithms.ALT_ROUTE)  // 🔥 기존 경로를 강하게 회피
                .putHint("ch.disable", true)  // 🔥 CH 비활성화하여 다양한 경로 탐색 가능
                .putHint("alternative_route.max_paths", 3)  // 🔥 최대 3개의 대체 경로 탐색
                .putHint("alternative_route.max_weight_factor", 3.0) // 🔥 최단 경로보다 3배 긴 경로도 허용

                // 🔥 지나온 Edge는 강제로 회피하도록 설정
                .putHint("routing.avoid_edges", avoidEdges);

        GHResponse response = hopper.route(request);

        if (response.hasErrors()) {
            System.out.println("❌ 대체 경로 탐색 실패: " + response.getErrors());
            return null;
        }

        return response.getBest();
    }
}