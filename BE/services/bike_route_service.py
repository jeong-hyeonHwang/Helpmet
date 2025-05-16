from fastapi import HTTPException
from networkx import shortest_path, single_source_dijkstra_path_length
import osmnx as ox

from services.route_service import build_response_from_route
from crud.bike_station import fetch_closest_bike_station
from services.route_util import nearest_nodes, route_nodes
from models.route_response import RouteSegment, Instruction, Coordinate, RouteResponseDto

EXTRA_MINUTES : int = 10

def find_bike_route(from_lat:float, from_lon:float, limit_minutes:int, G):
    source = ox.distance.nearest_nodes(G, X=from_lon, Y=from_lat)
    # print(source)

    # 제한 시간 내 도달 가능한 노드 탐색
    travel_times = single_source_dijkstra_path_length(G, source, weight="travel_time")
    reachable_nodes = [n for n, t in travel_times.items() if t <= limit_minutes - EXTRA_MINUTES]

    if not reachable_nodes:
        raise HTTPException(status_code=404, detail="시간 내 도달 가능한 자전거도로가 없습니다.")

    # 가장 멀리 간 노드 선택
    target = max(reachable_nodes, key=lambda n: travel_times[n])
    route = shortest_path(G, source, target, weight="travel_time")

    return route

async def find_full_route(db, request, lat : float, lon : float, max_minutes : int):
    G_bike = request.app.state.G_bike
    G_walk = request.app.state.G_walk

    bike_route = find_bike_route(lat, lon, max_minutes, G_bike)
    bike_result = build_response_from_route(G_bike, bike_route)

    bike_start_node = bike_route[0]
    w1, w2 = nearest_nodes(G_walk, lat, lon, G_bike.nodes[bike_start_node]["y"], G_bike.nodes[bike_start_node]["x"])
    walk_route1 = route_nodes(G_walk, w1, w2)

    walk_result1 = build_response_from_route(G_walk, walk_route1)
        
    bike_end_node = bike_route[-1]
    bike_station = await fetch_closest_bike_station(db, G_bike.nodes[bike_end_node]["y"], G_bike.nodes[bike_end_node]["x"])
        
    w3, w4 = nearest_nodes(G_walk, G_bike.nodes[bike_end_node]["y"], G_bike.nodes[bike_end_node]["x"], float(bike_station.lat), float(bike_station.lon))
    walk_route2 = route_nodes(G_walk, w3, w4)
    walk_result2 = build_response_from_route(G_walk, walk_route2)
        
    result = build_combined_response(walk_result1=walk_result1, bike_result=bike_result, walk_result2=walk_result2)
    result.end_addr = bike_station.addr1 if bike_station.addr2 is None or len(bike_station.addr2) == 0 else bike_station.addr2 + ' 대여소'

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