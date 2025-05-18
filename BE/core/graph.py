import osmnx as ox

def assign_travel_time(G):
    BIKE_SPEED_KPH = 15      # 자전거 평균 속도
    WALK_SPEED_KPH = 5       # 도보 속도

    for u, v, k, data in G.edges(keys=True, data=True):
        highway = data.get("highway")
        if isinstance(highway, list):
            is_cycleway = "cycleway" in highway
        else:
            is_cycleway = (highway == "cycleway")

        data["is_cycleway"] = is_cycleway

        speed_kph = BIKE_SPEED_KPH if is_cycleway else WALK_SPEED_KPH
        length_km = data.get("length", 0) / 1000
        travel_time_min = length_km / speed_kph * 60

        data["travel_time"] = travel_time_min
    
def load_graphs(app):
    print("📂 Loading walk and bike graphs into app.state...")
    # G_walk = ox.load_graphml("data/seoul_walk_raw.graphml")
    G_walk = ox.load_graphml("data/seoul_combined2.graphml")
    G_bike = ox.load_graphml("data/seoul_bicycle.graphml")

    assign_travel_time(G_walk)

    app.state.G_walk = G_walk
    app.state.G_bike = G_bike
    print("✅ Graphs loaded:", len(G_walk.nodes), "walk nodes,", len(G_bike.nodes), "bike nodes")
