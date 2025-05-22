import osmnx as ox

ox.settings.log_console = True
ox.settings.use_cache = True


place_name = "Seoul, South Korea"
graph_type = "walk"  # 도보 경로 , "bike"
filepath_walk = "data/seoul_walk2.graphml"
filepath_bike = "data/seoul_bike2.graphml"

print(f"Downloading {graph_type} graph for {place_name}...")
G_walk = ox.graph_from_place(place_name, network_type="walk")
# G_bike = ox.graph_from_place(place_name, network_type="bike")

# ox.plot_graph(G)

ox.save_graphml(G_walk, filepath_walk)
# ox.save_graphml(G_bike, filepath_bike)