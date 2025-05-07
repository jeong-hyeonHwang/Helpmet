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
ox.save_graphml(G_bike, "data/seoul_bicycle.graphml")