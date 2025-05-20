from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, text
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

    query = (
        select(BicycleStation)
        .order_by(text("geom <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)"))
        .params(lon=lon, lat=lat)
        .limit(limit)
    )

    result = await db.execute(query, {"lon": lon, "lat": lat})
    return result.scalars().all()