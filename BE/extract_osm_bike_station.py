import json
from pathlib import Path
from shapely.geometry import Point
import psycopg2
from psycopg2.extras import execute_values

# 파일 경로
json_path = Path("C:/Users/SSAFY/Downloads/osm_bike_station.geojson")

# JSON 불러오기
with open(json_path, "r", encoding="utf-8") as f:
    data = json.load(f)

features = data["features"]

# 레코드 추출
records = []
for f in features:
    props = f["properties"]
    geometry = f["geometry"]

    if not geometry or geometry["type"] != "Point":
        continue

    lon, lat = geometry["coordinates"]
    name = props.get("name")
    ref = props.get("ref")
    osmid_str = props.get("@id")  # 예: "node/3693412479"
    if osmid_str and osmid_str.startswith("node/"):
        osmid = int(osmid_str.split("/")[1])
    else:
        continue

    records.append((
        osmid,
        name,
        ref,
        lat,
        lon,
        f"SRID=4326;POINT({lon} {lat})"
    ))

# PostgreSQL 연결
conn = psycopg2.connect(
    dbname="helmet_db",
    user="postgres",
    password="helmeta303",
    host="127.0.0.1",
    port="5432"
)
cur = conn.cursor()

# 테이블 생성
cur.execute("""
    DROP TABLE IF EXISTS bicycle_station;
    CREATE TABLE bicycle_station (
        osmid BIGINT PRIMARY KEY,
        name TEXT,
        ref TEXT,
        lat DOUBLE PRECISION,
        lon DOUBLE PRECISION,
        geom GEOMETRY(Point, 4326)
    );
""")

# 데이터 삽입
insert_sql = """
    INSERT INTO bicycle_station (osmid, name, ref, lat, lon, geom)
    VALUES %s
    ON CONFLICT (osmid) DO NOTHING;
"""
execute_values(cur, insert_sql, records)
conn.commit()
cur.close()
conn.close()