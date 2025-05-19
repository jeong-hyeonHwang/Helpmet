import osmnx as ox
import networkx as nx
import math

def nearest_nodes(G, from_lat, from_lon, to_lat, to_lon):
    return (
        ox.distance.nearest_nodes(G, X=from_lon, Y=from_lat),
        ox.distance.nearest_nodes(G, X=to_lon, Y=to_lat),
    )

def route_nodes(G, from_node, to_node):
    return nx.shortest_path(G, from_node, to_node, weight="length")

def edge_length(G, n1, n2):
    return G.edges[n1, n2, 0].get("length", 0)

def build_instruction_message(G, node_id, action, distance, lat, lon):
    if is_crosswalk_node(node_id, G):
        return "횡단보도를 건너세요"

    pois = get_nearest_poi(lat, lon)
    landmark = pois[0] if pois else None

    if landmark:
        return f"{landmark} 앞에서 {action}하세요"
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

def get_nearest_poi(lat, lon, radius=30):
    tags = {"building": True, "amenity": True, "shop": True}
    try:
        pois = ox.features_from_point((lat, lon), tags=tags, dist=radius)
        if "name" in pois.columns and not pois.empty:
            pois = pois[pois["name"].notna()]

            pois_proj = pois.to_crs(epsg=3857)
            pois["centroid"] = pois_proj.geometry.centroid.to_crs(epsg=4326)

            pois["distance"] = pois["centroid"].apply(
                lambda p: ((lat - p.y)**2 + (lon - p.x)**2)**0.5
            )
            nearest_poi = pois.sort_values("distance").iloc[0]
            return nearest_poi["name"]
    except Exception as e:
        print(f"[ERROR] POI 탐색 실패: {e}")
    return ""

def is_crosswalk_node(node_id, G):
    node_data = G.nodes[node_id]
    return node_data.get("highway") == "crossing"

def build_instruction_message(G, node_id, action, distance, lat, lon):
    if is_crosswalk_node(node_id, G):
        return "횡단보도를 건너세요"

    landmark = get_nearest_poi(lat, lon)

    if landmark:
        return f"{landmark} 앞에서 {action}하세요"
    else:
        return f"약 {distance}m 앞에서 {action}하세요"