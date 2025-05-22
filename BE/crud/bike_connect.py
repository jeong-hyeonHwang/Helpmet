from typing import List
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from core.models import ExitNode, EntryNode
from geoalchemy2.functions import ST_Distance
from geoalchemy2.shape import from_shape
from shapely.geometry import Point

async def fetch_top_n_close_entry_nodes(
    db: AsyncSession,
    lat: float,
    lon: float,
    limit: int = 1
) -> List[EntryNode] | None:
    user_point = from_shape(Point(lon, lat), srid=4326)

    query = (
        select(EntryNode)
        .order_by(ST_Distance(EntryNode.geom_point, user_point))
        .limit(limit)
    )

    result = await db.execute(query)
    return result.scalars().all()

async def fetch_top_n_close_exit_nodes(
    db: AsyncSession,
    lat: float,
    lon: float,
    limit: int = 1
) -> List[ExitNode] | None:
    user_point = from_shape(Point(lon, lat), srid=4326)

    query = (
        select(EntryNode)
        .order_by(ST_Distance(EntryNode.geom_point, user_point))
        .limit(limit)
    )

    result = await db.execute(query)
    return result.scalars().all()
