import osmnx as ox
import networkx as nx
import psycopg2
import json

def is_foot_connected_node(G, node):
    """해당 노드가 도보 도로에 연결되어 있는지 확인"""
    for nbr in G.successors(node):
        hwy = G.edges[node, nbr, 0].get("highway")
        if hwy in ["footway", "path", "pedestrian", "steps"]:
            return True
    for nbr in G.predecessors(node):
        hwy = G.edges[nbr, node, 0].get("highway")
        if hwy in ["footway", "path", "pedestrian", "steps"]:
            return True
    return False

def classify_entry_exit_strict(G, u, v, edge_data):
    if edge_data.get("highway") != "cycleway":
        return []

    oneway = edge_data.get("oneway", False)

    types = []

    if is_foot_connected_node(G, u):
        types.append((u, edge_data))
        if not oneway:
            types.append((u, edge_data))

    if is_foot_connected_node(G, v):
        types.append((v, edge_data))
        if not oneway:
            types.append((v, edge_data))

    return types

def extract_connections(G):
    results = []

    for u, v, k, edge_data in G.edges(keys=True, data=True):
        if edge_data.get("highway") != "cycleway":
            continue

        conn_nodes = classify_entry_exit_strict(G, u, v, edge_data)
        if not conn_nodes:
            continue

        name = edge_data.get("name", "")
        edge_id = edge_data.get("osmid")

        linestring = edge_data.get("geometry")
        if not hasattr(linestring, "coords"):
            continue

        coords = [[lon, lat] for lon, lat in linestring.coords]
        edge_linestring = json.dumps({
            "type": "LineString",
            "coordinates": coords
        })

        for node_id, _ in conn_nodes:
            node = G.nodes[node_id]
            lat, lon = node["y"], node["x"]

            results.append((
                edge_id, lat, lon, node_id,
                edge_linestring, name
            ))

    return results

def insert_nodes_to_postgres(conn, table_name, rows):
    query = f"""
    INSERT INTO {table_name} (
        id, lat, lon, connect_node,
        edge_linestring, name, geom_point
    ) VALUES (
        %s, %s, %s, %s,
        %s, %s,
        ST_SetSRID(ST_MakePoint(%s, %s), 4326)::geography
    )
    ON CONFLICT (connect_node) DO NOTHING
    """
    with conn.cursor() as cur:
        cur.executemany(query, [
            (
                r[0], r[1], r[2], r[3],
                r[4], r[5],
                r[2], r[1]  # lon, lat for point
            )
            for r in rows
        ])
    conn.commit()

def main():
    print("📦 그래프 로드 중...")
    G = ox.load_graphml("data/seoul_combined.graphml")

    print("🔍 진입/진출 연결점 추출 중...")
    all_data = extract_connections(G)

    # 중복 제거
    seen_entry = set()
    seen_exit = set()
    entry_rows = []
    exit_rows = []

    for row in all_data:
        _, _, _, node_id, _, _ = row
        if node_id not in seen_entry:
            entry_rows.append(row)
            seen_entry.add(node_id)
        if node_id not in seen_exit:
            exit_rows.append(row)
            seen_exit.add(node_id)

    print(f"✅ 진입: {len(entry_rows)}개, 진출: {len(exit_rows)}개 추출 완료")

    print("🔗 PostgreSQL 연결...")
    conn = psycopg2.connect(
        dbname="helmet_db",
        user="postgres",
        password="helmeta303",
        host="127.0.0.1",
        port=5432
    )

    print("🚀 데이터 저장 중...")
    insert_nodes_to_postgres(conn, "bike_entry_connect", entry_rows)
    insert_nodes_to_postgres(conn, "bike_exit_connect", exit_rows)

    conn.close()
    print("🎉 저장 완료!")

if __name__ == "__main__":
    main()
