import os
from typing import Counter
import osmnx as ox
import logging
import geopandas as gpd

logger = logging.getLogger("core.graph")

TARGET_CRS = "EPSG:3857"
filepath = "data/pois_seoul.gpkg"
    
def load_graphs(app):
    logger.info("그래프 로딩 중...")

    G_walk = ox.load_graphml("data/seoul_projection.graphml")

    app.state.G_walk = G_walk

    logger.info("Graphs loaded: %s, %s", len(G_walk.nodes), "walk nodes")

def drop_case_insensitive_duplicates(gdf: gpd.GeoDataFrame) -> gpd.GeoDataFrame:
    seen = set()
    drop_cols = []
    for col in gdf.columns:
        key = col.lower()
        if key in seen:
            drop_cols.append(col)
        else:
            seen.add(key)
    if drop_cols:
        logger.warning(f"중복 컬럼 제거됨: {drop_cols}")
    return gdf.drop(columns=drop_cols)

def download_and_save_pois(place_name: str, tags: dict):
    logger.info(f"📡 파일 없음 → POI 다운로드 시작")
    gdf = ox.features_from_place(place_name, tags=tags)
    gdf = gdf[gdf["name"].notna() & gdf.geometry.notna()]
    gdf = drop_case_insensitive_duplicates(gdf)
    gdf = gdf.to_crs(TARGET_CRS)

    gdf.to_file(filepath, layer="pois", driver="GPKG", encoding="UTF-8")
    logger.info(f"✅ 저장 완료: {filepath} (총 {len(gdf)}개 POI)")

def load_pois(app, place_name, tags):
    if not os.path.exists(filepath):
        download_and_save_pois(place_name=place_name, tags=tags)

    gdf = gpd.read_file(filepath, layer="pois")
    _ = gdf.sindex
    app.state.POIs = gdf