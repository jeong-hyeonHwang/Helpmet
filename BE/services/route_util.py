import osmnx as ox
import networkx as nx
import math
import logging

from shapely import Point

logger = logging.getLogger("services.route_util")

def nearest_nodes(G, from_lat, from_lon, to_lat, to_lon):
    return (
        ox.distance.nearest_nodes(G, X=from_lon, Y=from_lat),
        ox.distance.nearest_nodes(G, X=to_lon, Y=to_lat),
    )

def route_nodes(G, from_node, to_node):
    return nx.shortest_path(G, from_node, to_node, weight="length")

def edge_length(G, n1, n2):
    return G.edges[n1, n2, 0].get("length", 0)

def build_instruction_message(POIs, G, node_id, action, distance, lat, lon):
    if is_crosswalk_node(node_id, G):
        return "횡단보도를 건너세요"

    poi = get_nearest_poi(lat=lat, lon=lon, POIs=POIs)

    if poi:
        return f"{poi} 앞에서 {action}하세요"
    else:
        return f"약 {distance}m 앞에서 {action}하세요"

def calculate_angle(p1, p2, p3):
    def vector(a, b):
        return (b[1] - a[1], b[0] - a[0])

    v1 = vector(p1, p2)
    v2 = vector(p2, p3)

    dot = v1[0]*v2[0] + v1[1]*v2[1]
    mag1 = math.sqrt(v1[0]**2 + v1[1]**2)
    mag2 = math.sqrt(v2[0]**2 + v2[1]**2)

    if mag1 == 0 or mag2 == 0:
        return 0, "직진"

    cos_theta = dot / (mag1 * mag2)
    angle = math.acos(max(min(cos_theta, 1), -1))
    angle_deg = math.degrees(angle)

    cross = v1[0]*v2[1] - v1[1]*v2[0]
    if angle_deg < 20:
        turn = "직진"
    elif cross > 0:
        turn = "좌회전"
    else:
        turn = "우회전"

    return angle_deg, turn

def get_nearest_poi(lat: float, lon: float, POIs) -> str:
    if POIs.empty:
        return ""

    user_point = Point(lon, lat)
    user_proj = ox.projection.project_geometry(user_point, to_crs=POIs.crs)[0]

    idx_pair = POIs.sindex.nearest(user_proj, return_all=False)
    nearest_idx = idx_pair[1][0]
    return POIs.iloc[nearest_idx]["name"]

def is_crosswalk_node(node_id, G):
    node_data = G.nodes[node_id]
    return node_data.get("highway") == "crossing"