from fastapi import APIRouter, Query, HTTPException, Request, Depends
import traceback
from enums.placeType import PlaceType
from services.route_service import find_route, build_response_from_route
from services.bike_route_service import find_bike_route
from services.route_util import nearest_nodes, route_nodes

from core.database import get_db
from sqlalchemy.ext.asyncio import AsyncSession
from crud.bike_station import fetch_closest_bike_station
from crud.public_toilet import fetch_closest_public_toilet
from core.schemas import BikeStationOut

router = APIRouter(
    prefix= "/route"
)

@router.get("/walk")
async def get_route(
    request: Request,
    from_lat: float = Query(..., ge=-90, le=90, description="출발지 위도"),
    from_lon: float = Query(..., ge=-180, le=180, description="출발지 경도"),
    to_lat: float = Query(..., ge=-90, le=90, description="도착지 위도"),
    to_lon: float = Query(..., ge=-180, le=180, description="도착지 경도")
):
    try:
        route = find_route(request.app.state.G_walk, from_lat, from_lon, to_lat, to_lon)
        return build_response_from_route(request.app.state.G_walk, route)
    except Exception as e:
        print("[ERROR] 예외 발생:")
        traceback.print_exc()  # ✅ 콘솔에 전체 에러 스택 찍힘
        raise HTTPException(status_code=500, detail=str(e))
    
@router.get("/bike")
async def get_bike_from_nearest(
    lat: float = Query(..., ge=-90, le=90, description="출발지 위도"),
    lon: float = Query(..., ge=-180, le=180, description="출발지 경도"),
    max_minutes: int = Query(20, ge=10, le=60),
    request: Request = None
):
    try:
        G_bike = request.app.state.G_bike
        G_walk = request.app.state.G_walk

        bike_route = find_bike_route(lat, lon, max_minutes, G_bike)
        bike_result = build_response_from_route(G_bike, bike_route)

        bike_start_node = bike_route[0]
        w1, w2 = nearest_nodes(G_walk, lat, lon, G_bike.nodes[bike_start_node]["y"], G_bike.nodes[bike_start_node]["x"])
        walk_route = route_nodes(G_walk, w1, w2)

        walk_result1 = build_response_from_route(G_walk, walk_route)
        
        offset = len(walk_result1["route"])

        return [{
            "distance_m": round(walk_result1["distance_m"] + bike_result["distance_m"], 1),
            "estimated_time_sec": round(walk_result1["estimated_time_sec"] + bike_result["estimated_time_sec"], 1),
            "route": walk_result1["route"] + bike_result["route"],
            "instructions": walk_result1["instructions"] + [
                {**instr, "index": instr["index"] + offset}
                for instr in bike_result["instructions"]
            ],
        }]
    except Exception as e:
        return {"error": str(e)}@router.get("/nearby")
async def get_bike_from_nearest(
    lat: float = Query(..., ge=-90, le=90, description="출발지 위도"),
    lon: float = Query(..., ge=-180, le=180, description="출발지 경도"),
    place_type: PlaceType = Query(...),
    db: AsyncSession = Depends(get_db),
    request: Request = None
):
    if place_type == PlaceType.rental:
        place = await fetch_closest_bike_station(db, lat=lat, lon=lon)
    elif place_type == PlaceType.toilet:
        place = await fetch_closest_public_toilet(db, lat=lat, lon=lon)
    else:
        return {
            "status": 400,
            "message": "지원되지 않는 장소 타입: toilet, retal만 요청하세요.",
            "data": null
        }
    if place is None:
        raise HTTPException(status_code=404, detail="No nearby place found")
    
    route = find_routeroute = find_route(
        request.app.state.G_walk,
        from_lat=lat,
        from_lon=lon,
        to_lat=float(place.lat),   # ✅ Decimal → float
        to_lon=float(place.lon)
    )
    return build_response_from_route(request.app.state.G_walk, route)