from geoalchemy2.functions import ST_DWithin, ST_Distance
from geoalchemy2.shape import from_shape
from shapely.geometry import Point
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from core.models import PublicToilet

async def fetch_closest_public_toilet(
    db: AsyncSession,
    lat: float,
    lon: float
) -> PublicToilet | None:
    user_point = from_shape(Point(lon, lat), srid=4326)

    query = (
        select(PublicToilet)
        .order_by(ST_Distance(PublicToilet.geom, user_point))
        .limit(1)
    )

    result = await db.execute(query)
    return result.scalar_one_or_none()