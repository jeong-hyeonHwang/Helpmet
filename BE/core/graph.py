import osmnx as ox
import logging

logger = logging.getLogger("core.graph")
    
def load_graphs(app):
    logger.info("그래프 로딩 중...")

    G_walk = ox.load_graphml("data/time_assigned.graphml")

    app.state.G_walk = G_walk
    
    logger.info("Graphs loaded: %s, %s", len(G_walk.nodes), "walk nodes")
