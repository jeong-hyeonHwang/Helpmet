from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from core.models import PublicToilet
from sqlalchemy import text

async def fetch_closest_public_toilet(
    db: AsyncSession,
    lat: float,
    lon: float
) -> PublicToilet | None:
    query = (
        select(PublicToilet)
        .order_by(text("geom <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)"))
        .params(lat=lat, lon=lon)
        .limit(1)
    )
    result = await db.execute(query, {"lon": lon, "lat": lat})
    return result.scalar_one_or_none()