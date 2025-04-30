from fastapi import APIRouter, Query, HTTPException
import traceback
from services.route_service import find_route, find_route_with_instructions

router = APIRouter()

@router.get("/route")
async def get_route(
    from_lat: float = Query(..., description="출발지 위도"),
    from_lon: float = Query(..., description="출발지 경도"),
    to_lat: float = Query(..., description="도착지 위도"),
    to_lon: float = Query(..., description="도착지 경도")
):
    print("[ROUTE] /route 진입함")
    try:
        # result = find_route(from_lat, from_lon, to_lat, to_lon)
        # print("[ROUTE] result 계산 완료")
        # return result
        # # return {"msg": "진입성공"}
        result = find_route_with_instructions(from_lat, from_lon, to_lat, to_lon)
        return result
    except Exception as e:
        print("[ERROR] 예외 발생:")
        traceback.print_exc()  # ✅ 콘솔에 전체 에러 스택 찍힘
        raise HTTPException(status_code=500, detail=str(e))
