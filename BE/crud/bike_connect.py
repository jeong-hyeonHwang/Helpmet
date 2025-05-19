from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from geoalchemy2.functions import ST_Distance
from geoalchemy2.shape import from_shape
from shapely.geometry import Point
from core.models import ExitNode, EntryNode

async def fetch_closest_entry_node(
    db: AsyncSession,
    lat: float,
    lon: float
) -> EntryNode | None:
    user_point = from_shape(Point(lon, lat), srid=4326)

    query = (
        select(EntryNode)
        .order_by(ST_Distance(EntryNode.geom_point, user_point))
        .limit(1)
    )

    result = await db.execute(query)
    return result.scalar_one_or_none()

async def fetch_closest_exit_node(
    db: AsyncSession,
    lat: float,
    lon: float
) -> ExitNode | None:
    user_point = from_shape(Point(lon, lat), srid=4326)

    query = (
        select(ExitNode)
        .order_by(ST_Distance(ExitNode.geom_point, user_point))
        .limit(1)
    )

    result = await db.execute(query)
    return result.scalar_one_or_none()
