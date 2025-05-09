from fastapi import APIRouter, Query, HTTPException, Request, Depends
from typing import List
import traceback
from enums.placeType import PlaceType
from services.route_service import find_route, build_response_from_route
from services.bike_route_service import find_full_route

from core.database import get_db
from sqlalchemy.ext.asyncio import AsyncSession
from crud.bike_station import fetch_closest_bike_station
from crud.public_toilet import fetch_closest_public_toilet

from models.base_response import BaseResponse
from models.route_response import RouteResponseDto

router = APIRouter(
    prefix= "/route"
)

@router.get("/walk", response_model=BaseResponse[RouteResponseDto])
async def get_route(
    request: Request,
    from_lat: float = Query(..., ge=-90, le=90, description="출발지 위도"),
    from_lon: float = Query(..., ge=-180, le=180, description="출발지 경도"),
    to_lat: float = Query(..., ge=-90, le=90, description="도착지 위도"),
    to_lon: float = Query(..., ge=-180, le=180, description="도착지 경도")
):
    try:
        route = find_route(request.app.state.G_walk, from_lat, from_lon, to_lat, to_lon)
        result =  build_response_from_route(request.app.state.G_walk, route)

        return BaseResponse(status=200, message="success", data=result)
    except Exception as e:
        print("[ERROR] 예외 발생:")
        traceback.print_exc()  # ✅ 콘솔에 전체 에러 스택 찍힘
        raise HTTPException(status_code=500, detail=str(e))
    
@router.get("/bike", response_model=BaseResponse[RouteResponseDto])
async def get_bike_from_nearest(
    lat: float = Query(..., ge=-90, le=90, description="출발지 위도"),
    lon: float = Query(..., ge=-180, le=180, description="출발지 경도"),
    max_minutes: int = Query(20, ge=10, le=60),
    db: AsyncSession = Depends(get_db),
    request: Request = None
):
    result = await find_full_route(db, request, lat, lon, max_minutes)
    print(type(result))
    return BaseResponse(status=200, message="success", data=result)
    
@router.get("/nearby", response_model=BaseResponse[RouteResponseDto])
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
        return BaseResponse(status=400, message="지원되지 않는 장소 타입: toilet, retal만 요청하세요.") 
    if place is None:
        raise HTTPException(status_code=404, detail="No nearby place found")
    
    route = find_route(
        request.app.state.G_walk,
        from_lat=lat,
        from_lon=lon,
        to_lat=float(place.lat),   # ✅ Decimal → float
        to_lon=float(place.lon)
    )
   
    result = build_response_from_route(request.app.state.G_walk, route)

    return BaseResponse(status=200, message="success", data=result)