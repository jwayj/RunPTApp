package com.example.routing_module;

import com.graphhopper.util.CustomModel;
import java.util.Set;

public class PenalizedEdgeCustomModelBuilder {
    public static CustomModel build(Set<Integer> penalizedEdges, double penaltyFactor) {
        CustomModel customModel = new CustomModel();

        // 💡 CustomModel에서 특정 edge_id에 패널티를 적용하는 방식은 지원되지 않음
        // 대신에 실제 라우팅 시 avoidEdges 힌트에 포함시켜 회피하도록 처리해야 함
        // 따라서 이 메서드는 현재는 단순히 빈 CustomModel을 리턴하거나, 다른 속성 조정 용도로만 사용
        return customModel;
    }
}