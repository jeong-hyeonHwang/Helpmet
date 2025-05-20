import osmnx as ox
import networkx as nx
from sqlalchemy.ext.asyncio import AsyncSession
from typing import List, Optional, Tuple
import logging

from core.models import BicycleStation
from services.route_service import build_response_from_route
from crud.bicycle_station import fetch_top_n_bike_stations
from crud.bike_connect import fetch_top_n_close_entry_nodes
from services.route_util import route_nodes
from models.route_response import Instruction, RouteResponseDto
import heapq

EXTRA_MINUTES : int = 10

logger = logging.getLogger("main")

async def find_bike_routes(
    entry_node: int,
    limit_minutes: float,
    G: nx.MultiDiGraph,
    top_n: int = 3
) -> List[List[int]]:
    bike_speed_mps = 5.0
    max_distance_m = limit_minutes * 60 * bike_speed_mps

    heap = [(0, entry_node, [])]
    exit_candidates: List[Tuple[float, List[int]]] = []

    while heap:
        curr_dist, curr_node, path = heapq.heappop(heap)

        if curr_node in path:
            continue

        if(len(path) > 20):
            break

        new_path = path + [curr_node]

        if G.nodes[curr_node].get("is_exit"):
            exit_candidates.append((curr_dist, new_path))
        
        if curr_dist > max_distance_m:
            print(f"끝!!!!!!!!!!!!! 현재 노드: {curr_node}, 거리: {curr_dist} > {max_distance_m}, path: {new_path}")
            continue

        print(f"현재 노드: {curr_node}, 거리: {curr_dist}, path: {new_path}")
        print(f"이웃 노드 수: {len(list(G.edges(curr_node)))}")

        for _, neighbor, edge_data in G.edges(curr_node, data=True):
            print("\t", neighbor, edge_data)

            if edge_data.get("highway") != "cycleway":
                continue

            if neighbor not in new_path:
                edge_len = edge_data.get("length", 0)
                heapq.heappush(heap, (curr_dist + edge_len, neighbor, new_path))

    # 경로 후보가 없으면 빈 리스트 반환
    if not exit_candidates:
        return []

    # 상위 N개 경로 선택 (거리 긴 순)
    exit_candidates.sort(key=lambda x: x[0], reverse=True)
    top_paths = [path for _, path in exit_candidates[:top_n]]
    print(len(top_paths))
    print(top_paths)
    return top_paths

async def select_best_entry_node_by_walk(
    db: AsyncSession,
    G_walk: nx.MultiDiGraph,
    from_lat: float,
    from_lon: float,
    top_n: int = 3
) -> Optional[Tuple[int, list[int]]]:
    """
    도보 그래프에서 사용자의 위치로부터 가장 가까운 자전거도로 진입 노드를 찾는다.
    :return: (선택된 진입노드 ID, 경로 노드 리스트)
    """
    from_node = ox.distance.nearest_nodes(G_walk, X=from_lon, Y=from_lat)

    # 진입노드 후보: G_walk에서 is_entry=True인 노드들
    entry_nodes = await fetch_top_n_close_entry_nodes(db=db, lat=from_lat, lon=from_lon, limit=top_n)

    best_entry_node = None
    best_path = []
    min_dist = float("inf")

    for entry_node in entry_nodes:
        try:
            if G_walk.has_node(entry_node.connect_node):
                entry_id = entry_node.connect_node
            else:
                entry_id = ox.distance.nearest_nodes(
                    G_walk, X=entry_node.lon, Y=entry_node.lat
                )

            path = nx.shortest_path(G_walk, from_node, entry_id, weight="length")

            if(from_node == entry_id):
                return entry_id, path
            
            gdf = ox.utils_graph.route_to_gdf(G_walk, path)
            dist = gdf["length"].sum()

            if dist < min_dist:
                best_entry_node = entry_node.connect_node
                best_path = path
                min_dist = dist

        except nx.NetworkXNoPath:
            continue

    if best_entry_node:
        return best_entry_node, best_path
    return None

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
            if station.osmid in G_walk.nodes:
                to_node = station.osmid
            else:
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

async def find_full_routes(
    db, request, lat: float, lon: float, max_minutes: int, top_n: int = 3
) -> List[RouteResponseDto]:
    G_walk = request.app.state.G_walk
    POIs = request.app.state.POIs

    entry_node, walk_route1 = await select_best_entry_node_by_walk(db=db, G_walk=G_walk, from_lon=lon, from_lat=lat, top_n=3)
    walk_result1 = build_response_from_route(POIs, G_walk, walk_route1)

    # 남은 시간 계산
    adjusted_minutes = max_minutes - (walk_result1.estimated_time_sec / 60)

    # bike: 진입노드에서 출구노드로 향하는 다양한 자전거도로 경로 찾기
    bike_routes = await find_bike_routes(entry_node, max_minutes, G_walk, top_n=top_n)

    results: List[RouteResponseDto] = []

    for bike_route in bike_routes:
        try:
            bike_result = build_response_from_route(POIs, G_walk, bike_route)
            bike_exit_node = bike_route[-1]

            # walk2: 출구지점 → 가장 가까운 대여소
            result = await select_best_bike_station_by_walk(
                db=db,
                G_walk=G_walk,
                from_node=bike_exit_node,
                from_lat=G_walk.nodes[bike_exit_node]["y"],
                from_lon=G_walk.nodes[bike_exit_node]["x"],
                top_k=3
            )
            if result is None:
                continue

            walk_end_node, bike_station, walk_route2 = result
            walk_result2 = build_response_from_route(POIs, G_walk, walk_route2)

            # 최종 경로 구성
            combined = build_combined_response(
                walk_result1=walk_result1,
                bike_result=bike_result,
                walk_result2=walk_result2
            )
            combined.end_addr = bike_station.name

            results.append(combined)

        except Exception as e:
            logger.info(f"[경로 조합 실패] exit_node={bike_route[-1]}, error={e}")
            continue

    return results


def build_combined_response(walk_result1: RouteResponseDto, bike_result: RouteResponseDto, walk_result2: RouteResponseDto) -> RouteResponseDto:
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

    all_segments = walk_result1.route + bike_result.route + walk_result2.route

    offset1 = len(walk_result1.instructions)
    offset2 = offset1 + len(bike_result.instructions)

    all_instructions = (
        walk_result1.instructions +
        [Instruction.model_construct(
            index=instr.index + offset1,
            location=instr.location,
            distance_m=instr.distance_m,
            action=instr.action,
            message=instr.message
        ) for instr in bike_result.instructions] +
        [Instruction.model_construct(
            index=instr.index + offset2,
            location=instr.location,
            distance_m=instr.distance_m,
            action=instr.action,
            message=instr.message
        ) for instr in walk_result2.instructions]
    )

    return RouteResponseDto.model_construct(
        start_addr=walk_result1.start_addr,
        end_addr=walk_result2.end_addr,
        distance_m=total_distance,
        estimated_time_sec=total_time,
        route=all_segments,
        instructions=all_instructions
    )