import osmnx as ox
import networkx as nx
import json
from shapely.geometry import Point
from pathlib import Path

from shapely.geometry import Point, LineString
import networkx as nx

def cut_linestring_at_point(line: LineString, point: Point):
    if not line.intersects(point):
        point = line.interpolate(line.project(point))

    coords = list(line.coords)
    proj_dist = line.project(point)
    if proj_dist <= 0 or proj_dist >= line.length:
        return None

    for i in range(1, len(coords)):
        seg = LineString([coords[i - 1], coords[i]])
        if seg.project(point) < seg.length:
            coords1 = coords[:i] + [(point.x, point.y)]
            coords2 = [(point.x, point.y)] + coords[i:]
            return LineString(coords1), LineString(coords2)
    return None

def snap_and_split_edge(G: nx.MultiDiGraph, station_point: Point, station_node_id: int, station_name: str = "") -> bool:
    # 1. 가장 가까운 edge 탐색
    min_dist = float("inf")
    closest_edge = None
    for u, v, k, data in G.edges(keys=True, data=True):
        geom = data.get("geometry")
        if not isinstance(geom, LineString):
            continue
        dist = geom.distance(station_point)
        if dist < min_dist:
            min_dist = dist
            closest_edge = (u, v, k, data)

    if closest_edge is None:
        return False

    u, v, k, edge_data = closest_edge
    original_geom = edge_data["geometry"]

    # 2. 투영 지점 계산 및 edge 분할
    projected_point = original_geom.interpolate(original_geom.project(station_point))
    split_result = cut_linestring_at_point(original_geom, projected_point)
    if split_result is None:
        return False
    geom1, geom2 = split_result

    # 3. 속성 복사 및 필수 값 계산
    common_attrs = edge_data.copy()
    for key in ["geometry", "length", "travel_time"]:
        common_attrs.pop(key, None)

    length1 = geom1.length * 111000
    length2 = geom2.length * 111000
    travel_time1 = length1 / 1.4
    travel_time2 = length2 / 1.4

    # 4. 기존 edge 제거
    G.remove_edge(u, v, k)

    # 5. 새 노드 추가
    G.add_node(
        station_node_id,
        x=station_point.x,
        y=station_point.y,
        is_bike_station=True,
        station_name=station_name,
    )

    # 6. 새 edge 추가
    G.add_edge(u, station_node_id, **common_attrs, geometry=geom1, length=length1, travel_time=travel_time1)
    G.add_edge(station_node_id, v, **common_attrs, geometry=geom2, length=length2, travel_time=travel_time2)

    return True

# 1. 기존 도보 그래프 로드
G_walk = ox.load_graphml("data/seoul_combined2.graphml")

# 2. 따릉이 대여소 GeoJSON 로드 (Overpass Turbo에서 저장한 파일)
geojson_path = Path("C:/Users/SSAFY/Downloads/osm_bike_station.geojson")
with open(geojson_path, "r", encoding="utf-8") as f:
    station_data = json.load(f)

stations = station_data["features"]

# 3. 삽입 루프

added_count = 0
for feature in stations:
    props = feature["properties"]
    geometry = feature["geometry"]

    if geometry["type"] != "Point":
        continue

    lon, lat = geometry["coordinates"]
    station_point = Point(lon, lat)

    osmid_str = props.get("@id")
    if not osmid_str or not osmid_str.startswith("node/"):
        continue

    osmid = int(osmid_str.split("/")[1])
    station_node_id = osmid
    station_name = props.get("name", "")

    try:
        success = snap_and_split_edge(G_walk, station_point, station_node_id, station_name)
        if success:
            added_count += 1
        else:
            print(station_name)
    except Exception as e:
        print(f"[ERROR] {station_name} (osmid={osmid}): {e}")

# 4. 저장
output_path = Path("data/seoul_projection.graphml")
ox.save_graphml(G_walk, output_path)

print(f"✅ 총 {added_count}개 대여소 노드를 추가 완료했습니다.")
print(f"📁 저장 경로: {output_path}")