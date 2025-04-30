import osmnx as ox
import networkx as nx
from core.graph import get_graph
from services.route_util import calculate_angle, get_poi_nearby

def find_route(from_lat: float, from_lon: float, to_lat: float, to_lon: float) -> dict:
    G = get_graph()

    # 출발지/도착지 좌표 → G의 노드 ID
    from_node = ox.distance.nearest_nodes(G, X=from_lon, Y=from_lat)
    to_node = ox.distance.nearest_nodes(G, X=to_lon, Y=to_lat)

    # 경로 탐색
    try:
        route = nx.shortest_path(G, from_node, to_node, weight="length")
    except nx.NetworkXNoPath:
        raise ValueError("경로를 찾을 수 없습니다.")

    # 좌표 리스트 (lat, lon)
    coords = [(G.nodes[n]["y"], G.nodes[n]["x"]) for n in route]

    # 거리 계산 (m)
    total_length = sum(
        G.edges[route[i], route[i + 1], 0].get("length", 0)
        for i in range(len(route) - 1)
    )

    # 예상 소요 시간 계산 (단위: 분)
    average_speed_kmh = 15  # 자전거 평균 속도
    estimated_time_min = (total_length / 1000) / average_speed_kmh * 60

    return {
        "route": coords,
        "distance_m": round(total_length, 1),
        "estimated_time_min": round(estimated_time_min, 1)
    }


def find_route_with_instructions(from_lat, from_lon, to_lat, to_lon):
    G = get_graph()
    from_node = ox.distance.nearest_nodes(G, X=from_lon, Y=from_lat)
    to_node = ox.distance.nearest_nodes(G, X=to_lon, Y=to_lat)
    route = nx.shortest_path(G, from_node, to_node, weight="length")
    coords = [(G.nodes[n]["y"], G.nodes[n]["x"]) for n in route]

    total_length = sum(G.edges[route[i], route[i + 1], 0].get("length", 0) for i in range(len(route) - 1))
    average_speed_kmh = 15
    estimated_time_min = (total_length / 1000) / average_speed_kmh * 60

    cumulative_distances = [0]
    for i in range(1, len(route)):
        edge_length = G.edges[route[i - 1], route[i], 0].get("length", 0)
        cumulative_distances.append(cumulative_distances[-1] + edge_length)

    # 1. 회전지점만 먼저 추출
    turn_instructions = []
    turn_indexes = []

    for i in range(1, len(coords) - 1):
        angle, action = calculate_angle(coords[i - 1], coords[i], coords[i + 1])
        if angle > 30:
            distance = round(cumulative_distances[i])
            lat, lon = coords[i]
            pois = get_poi_nearby(lat, lon)
            landmark = pois[0] if pois else None
            message = f"{landmark} 앞에서 {action}하세요" if landmark else f"약 {distance}m 앞에서 {action}하세요"

            turn_instructions.append({
                "index": i,
                "location": {"lat": lat, "lon": lon},
                "distance_to_here_m": distance,
                "action": action,
                "landmark": landmark,
                "message": message
            })
            turn_indexes.append(i)

    # 2. 직진 구간 안내 생성
    linear_instructions = []
    bounds = [0] + turn_indexes + [len(coords) - 1]

    for i in range(len(bounds) - 1):
        start, end = bounds[i], bounds[i + 1]
        if end - start < 1:
            continue

        segment_distance = sum(
            G.edges[route[j], route[j + 1], 0].get("length", 0)
            for j in range(start, end)
        )
        message = f"{round(segment_distance)}m 직진하세요"
        lat, lon = coords[start]

        linear_instructions.append({
            "index": start,
            "location": {"lat": lat, "lon": lon},
            "distance_m": round(segment_distance),
            "action": "직진",
            "message": message
        })

    all_instructions = linear_instructions + turn_instructions
    all_instructions.sort(key=lambda x: x["index"])

    return {
        "route": coords,
        "distance_m": round(total_length, 1),
        "estimated_time_min": round(estimated_time_min, 1),
        "instructions": all_instructions
    }