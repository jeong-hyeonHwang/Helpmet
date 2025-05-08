from geoalchemy2.functions import ST_DWithin, ST_Distance
from geoalchemy2.shape import from_shape
from shapely.geometry import Point
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from core.database import BikeStation

async def get_nearby_bike_stations(
    db: AsyncSession,
    lat: float,
    lon: float,
    radius: float  # 반경(m 단위)
) -> list[BikeStation]:
    user_point = from_shape(Point(lon, lat), srid=4326)
    
    query = select(BikeStation).where(
        ST_DWithin(BikeStation.geom, user_point, radius)
    )

    result = await db.execute(query)
    return result.scalars().all()

async def get_closest_bike_station(
    db: AsyncSession,
    lat: float,
    lon: float
) -> BikeStation | None:
    user_point = from_shape(Point(lon, lat), srid=4326)

    query = (
        select(BikeStation)
        .order_by(ST_Distance(BikeStation.geom, user_point))
        .limit(1)
    )

    result = await db.execute(query)
    return result.scalar_one_or_none()