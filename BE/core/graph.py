import osmnx as ox

def assign_travel_time(G):
    # 속도 설정
    AVERAGE_SPEED_KPH = 15

    # 주행 시간 가중치 추가
    for u, v, k, data in G.edges(keys=True, data=True):
        length_km = data["length"] / 1000
        data["travel_time"] = length_km / AVERAGE_SPEED_KPH * 60
    
def load_graphs(app):
    print("📂 Loading walk and bike graphs into app.state...")
    G_walk = ox.load_graphml("data/seoul_walk_raw.graphml")
    G_bike = ox.load_graphml("data/seoul_bicycle.graphml")

    assign_travel_time(G_bike)

    app.state.G_walk = G_walk
    app.state.G_bike = G_bike
    print("✅ Graphs loaded:", len(G_walk.nodes), "walk nodes,", len(G_bike.nodes), "bike nodes")
