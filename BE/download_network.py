# 통합된 download_network.py

import osmnx as ox
import networkx as nx

# 설정
place_name = "Seoul, South Korea"
bike_graphml_file = "data/seoul_bike.graphml"
walk_graphml_file = "data/seoul_walk.graphml"
combined_graphml_file = "data/seoul_combined.graphml"
bike_safe_graphml_file = "data/seoul_bike_safe.graphml"

# 함수 정의
def build_pedestrian_bike_graph(place_name: str) -> nx.MultiDiGraph:
    """
    도보 기반 그래프를 만든 후 cycleway가 있는 엣지만 남김 (자전거 도로만 유지)
    """
    G = ox.graph_from_place(place_name, network_type="walk", simplify=True)
    G2 = G.copy()
    for u, v, k, data in G.edges(keys=True, data=True):
        cycleway = data.get("cycleway", "")
        if not cycleway:
            G2.remove_edge(u, v, k)
    return G2

def is_crossable_edge(u, v, G: nx.MultiDiGraph) -> bool:
    """
    노드 중 하나라도 crossing인 경우 통과 허용
    """
    u_tag = G.nodes[u].get("highway", "")
    v_tag = G.nodes[v].get("highway", "")
    return u_tag == "crossing" or v_tag == "crossing"

def remove_uncrossable_edges(G: nx.MultiDiGraph) -> nx.MultiDiGraph:
    """
    crossing이 없는 엣지는 삭제 (도로 무단횡단 방지)
    """
    G2 = G.copy()
    for u, v, k in G.edges(keys=True):
        if not is_crossable_edge(u, v, G):
            G2.remove_edge(u, v, k)
    return G2

# 1. 자전거도로 네트워크 다운로드
print("📥 Downloading bike network...")
G_bike = ox.graph_from_place(place_name, network_type='bike')
ox.save_graphml(G_bike, filepath=bike_graphml_file)

# 2. 도보도로 네트워크 다운로드
print("📥 Downloading walk network...")
G_walk = ox.graph_from_place(place_name, network_type='walk')
ox.save_graphml(G_walk, filepath=walk_graphml_file)

# 3. 통합 그래프 저장
print("🔗 Combining bike and walk networks...")
G_combined = nx.compose(G_bike, G_walk)
ox.save_graphml(G_combined, filepath=combined_graphml_file)

# 4. 자전거 전용 안전 그래프 생성
print("🚴 Building pedestrian + cycleway filtered graph...")
G_filtered = build_pedestrian_bike_graph(place_name)
G_safe = remove_uncrossable_edges(G_filtered)
ox.save_graphml(G_safe, filepath=bike_safe_graphml_file)

print("✅ All networks saved successfully!")
