from fastapi import HTTPException
from networkx import shortest_path, single_source_dijkstra_path_length
import osmnx as ox

def find_bike_route(from_lat:float, from_lon:float, limit_minutes:int, G):
    source = ox.distance.nearest_nodes(G, X=from_lon, Y=from_lat)

    # 제한 시간 내 도달 가능한 노드 탐색
    travel_times = single_source_dijkstra_path_length(G, source, weight="travel_time")
    reachable_nodes = [n for n, t in travel_times.items() if t <= limit_minutes]

    if not reachable_nodes:
        raise HTTPException(status_code=404, detail="시간 내 도달 가능한 자전거도로가 없습니다.")

    # 가장 멀리 간 노드 선택
    target = max(reachable_nodes, key=lambda n: travel_times[n])
    route = shortest_path(G, source, target, weight="travel_time")

    return route