import osmnx as ox
import networkx as nx
from typing import Optional, Tuple

from core.models import BicycleStation
from services.route_service import build_response_from_route
# from crud.bike_station import fetch_top_n_bike_stations
from crud.bicycle_station import fetch_top_n_bike_stations
from crud.bike_connect import fetch_closest_entry_node
from services.route_util import route_nodes
from models.route_response import Instruction, RouteResponseDto
import heapq

EXTRA_MINUTES : int = 10

async def find_bike_route(db, from_lat: float, from_lon: float, limit_minutes: int, G):
    entry_obj = await fetch_closest_entry_node(db, lat=from_lat, lon=from_lon)

    if entry_obj is None:
        raise ValueError("No nearby entry node found")

    entry_node = entry_obj.connect_node

    # 평균 자전거 속도 (m/s)
    bike_speed_mps = 5.0
    max_distance_m = limit_minutes * 60 * bike_speed_mps

    visited = set()
    heap = [(0, entry_node, [])]
    exit_candidates = []

    while heap:
        curr_dist, curr_node, path = heapq.heappop(heap)

        if curr_node in visited:
            continue
        visited.add(curr_node)

        path = path + [curr_node]
        if G.nodes[curr_node].get("is_exit"):
            exit_candidates.append((curr_dist, path))

        if curr_dist > max_distance_m:
            continue  # cutoff

        for _, neighbor, edge_data in G.edges(curr_node, data=True):
            if not edge_data.get("is_cycleway"):
                continue

            if neighbor not in visited:
                edge_len = edge_data.get("length", 0)
                heapq.heappush(heap, (curr_dist + edge_len, neighbor, path))

    # 가장 먼 출입노드 경로 선택
    if not exit_candidates:
        return []

    best_dist, best_path = max(exit_candidates, key=lambda x: x[0])
    return best_path

async def select_best_bike_station_by_walk(
    db,
    G_walk,
    from_node: int,
    from_lat: float,
    from_lon: float,
    top_k: int = 3
) -> Optional[Tuple[int, BicycleStation, list[int]]]:
    """
    도보 그래프에서 from_node부터 가장 가까운 따릉이 대여소까지의 최단 경로를 찾는다.
    :return: (도착 노드, 대여소 객체, 경로 노드 리스트)
    """
    station_candidates = await fetch_top_n_bike_stations(db, from_lat, from_lon, top_k)

    best_node = None
    best_station = None
    best_path = []
    min_dist = float("inf")

    for station in station_candidates:
        try:
            to_node = ox.distance.nearest_nodes(G_walk, X=float(station.lon), Y=float(station.lat))
            path = nx.shortest_path(G_walk, from_node, to_node, weight="length")
            gdf = ox.utils_graph.route_to_gdf(G_walk, path)
            dist = gdf["length"].sum()

            if dist < min_dist:
                best_node = to_node
                best_station = station
                best_path = path
                min_dist = dist

        except nx.NetworkXNoPath:
            continue

    if best_node and best_station:
        return best_node, best_station, best_path
    return None

async def find_full_route(db, request, lat : float, lon : float, max_minutes : int):
    # G_bike = request.app.state.G_bike
    G_walk = request.app.state.G_walk

    bike_route = await find_bike_route(db, lat, lon, max_minutes, G_walk)
    bike_result = build_response_from_route(G_walk, bike_route)
    
    bike_entry_node = bike_route[0]
    walk_start_node = ox.distance.nearest_nodes(G_walk, X=lon, Y=lat)
    walk_route1 = route_nodes(G_walk, walk_start_node, bike_entry_node)

    walk_result1 = build_response_from_route(G_walk, walk_route1)
        
    bike_exit_node = bike_route[-1]
    result = await select_best_bike_station_by_walk(
        db=db,
        G_walk=G_walk,
        from_node=bike_exit_node,
        from_lat=G_walk.nodes[bike_exit_node]["y"],
        from_lon=G_walk.nodes[bike_exit_node]["x"],
        top_k=5
    )

    if result:
        walk_end_node, bike_station, walk_route2 = result

    walk_result2 = build_response_from_route(G_walk, walk_route2)
        
    result = build_combined_response(walk_result1=walk_result1, bike_result=bike_result, walk_result2=walk_result2)
    result.end_addr = bike_station.name

    return result

def build_combined_response(walk_result1 : RouteResponseDto, bike_result : RouteResponseDto, walk_result2 : RouteResponseDto) -> RouteResponseDto:
    total_distance = round(
        walk_result1.distance_m +
        bike_result.distance_m +
        walk_result2.distance_m, 1
    )

    total_time = round(
        walk_result1.estimated_time_sec +
        bike_result.estimated_time_sec +
        walk_result2.estimated_time_sec
    )

    # 구간 합치기 (RouteSegment 객체 리스트)
    all_segments = walk_result1.route + bike_result.route + walk_result2.route

    # 지시문 index 오프셋 계산
    offset1 = len(walk_result1.instructions)
    offset2 = offset1 + len(bike_result.instructions)

    # 지시문 합치기 (Instruction 객체 리스트)
    all_instructions_raw = (
        walk_result1.instructions +
        [
            Instruction(
                index=instr.index + offset1,
                location=instr.location,
                distance_m=instr.distance_m,
                action=instr.action,
                message=instr.message
            ) for instr in bike_result.instructions
        ] +
        [
            Instruction(
                index=instr.index + offset2,
                location=instr.location,
                distance_m=instr.distance_m,
                action=instr.action,
                message=instr.message
            ) for instr in walk_result2.instructions
        ]
    )

    return RouteResponseDto(
        start_addr=walk_result1.start_addr,
        end_addr=walk_result2.end_addr, 
        distance_m=total_distance,
        estimated_time_sec=total_time,
        route=all_segments,
        instructions=all_instructions_raw
    )