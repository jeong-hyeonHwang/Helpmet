import osmnx as ox

# 전역 G 객체
G = None

def load_graph(graphml_path: str):
    """서버 부팅 시 호출해서 네트워크(G)를 메모리에 로드하는 함수"""
    global G
    print(f"Loading network from {graphml_path} ...")
    G = ox.load_graphml(graphml_path)
    print(f"✅ Network loaded successfully. Nodes: {len(G.nodes)}, Edges: {len(G.edges)}")

def get_graph():
    """API나 서비스에서 메모리에 올라온 G를 가져오는 함수"""
    if G is None:
        raise ValueError("Graph has not been loaded yet!")
    return G
