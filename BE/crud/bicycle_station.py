from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from geoalchemy2.functions import ST_Distance
from shapely.geometry import Point
from geoalchemy2.shape import from_shape
from core.models import BicycleStation
from typing import List

async def fetch_top_n_bike_stations(
    db: AsyncSession,
    lat: float,
    lon: float,
    limit: int = 3
) -> List[BicycleStation]:
    """
    위경도를 기준으로 가까운 따릉이 대여소 N개를 거리순으로 가져옵니다.
    """
    user_point = from_shape(Point(lon, lat), srid=4326)

    query = (
        select(BicycleStation)
        .order_by(ST_Distance(BicycleStation.geom, user_point))
        .limit(limit)
    )

    result = await db.execute(query)
    return result.scalars().all()