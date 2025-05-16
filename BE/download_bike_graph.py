import osmnx as ox
import matplotlib.pyplot as plt

ox.settings.log_console = True
ox.settings.use_cache = True

# 자전거도로 그래프 생성
G_bike = ox.graph_from_place(
    "Seoul, South Korea",
    custom_filter='["highway"="cycleway"]'
)

# 시각화
ox.plot_graph(G_bike)

# 저장
ox.save_graphml(G_bike, "data/seoul_bike.graphml")

###
import osmnx as ox

ox.settings.use_cache = True
ox.settings.log_console = True

custom_filter = (
    '["highway"~"footway|pedestrian|path|residential|cycleway|living_street|unclassified|tertiary|secondary|primary"]'
    '["area"!~"yes"]'
    '["busway"!~"no"]'
)

G_bus_included = ox.graph_from_place("Seoul, South Korea", custom_filter=custom_filter)

# 저장
ox.save_graphml(G_bus_included, "data/seoul_combined.graphml")

# 시각화
ox.plot_graph(G_bus_included)