from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.future import select
from core.models import ExitNode, EntryNode
from sqlalchemy import text

async def fetch_closest_entry_node(
    db: AsyncSession,
    lat: float,
    lon: float
) -> EntryNode | None:
    query = (
        select(EntryNode)
        .order_by(text("geom_point <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)"))
        .limit(1)
    )

    result = await db.execute(query)
    return result.scalar_one_or_none()

async def fetch_closest_exit_node(
    db: AsyncSession,
    lat: float,
    lon: float
) -> ExitNode | None:
    query = (
        select(ExitNode)
        .order_by(text("geom_point <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)"))
        .limit(1)
    )

    result = await db.execute(query)
    return result.scalar_one_or_none()
