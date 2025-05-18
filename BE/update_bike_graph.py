import asyncio
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import text
import osmnx as ox

async def get_bike_exit_geojson(db: AsyncSession):
    query = text("""
        SELECT
            connect_node,
            name,
            lat,
            lon,
            ST_AsGeoJSON(geom_point)::json AS geometry
        FROM bike_exit_connect
    """)
    result = await db.execute(query)
    return result.mappings().all()

async def get_bike_entry_geojson(db: AsyncSession):
    query = text("""
        SELECT
            connect_node,
            name,
            lat,
            lon,
            ST_AsGeoJSON(geom_point)::json AS geometry
        FROM bike_entry_connect
    """)
    result = await db.execute(query)
    return result.mappings().all()


async def main():
    from core.database import AsyncSessionLocal
    async with AsyncSessionLocal() as db:
        G_bike = ox.load_graphml("data/seoul_combined.graphml")

        # exit nodes
        exit_rows = await get_bike_exit_geojson(db)
        for row in exit_rows:
            node_id = row['connect_node']
            if node_id in G_bike.nodes:
                G_bike.nodes[node_id].update({
                    'is_exit': True,
                    'name': row['name'],
                })
        print(exit_rows.count)

        # entry nodes
        entry_rows = await get_bike_entry_geojson(db)
        for row in entry_rows:
            node_id = row['connect_node']
            if node_id in G_bike.nodes:
                G_bike.nodes[node_id].update({
                    'is_entry': True,
                    'name': row['name'],
                })

        ox.save_graphml(G_bike, "data/seoul_combined2.graphml")

# 실제 실행
if __name__ == "__main__":
    asyncio.run(main())