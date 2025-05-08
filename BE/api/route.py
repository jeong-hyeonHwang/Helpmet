from fastapi import APIRouter, Query, HTTPException, Request
import traceback
from services.route_service import find_route, build_response_from_route
from services.bike_route_service import find_bike_route
from services.route_util import nearest_nodes, route_nodes

router = APIRouter(
    prefix= "/route"
)

@router.get("/walk")
async def get_route(
    request: Request,
    from_lat: float = Query(..., description="출발지 위도"),
    from_lon: float = Query(..., description="출발지 경도"),
    to_lat: float = Query(..., description="도착지 위도"),
    to_lon: float = Query(..., description="도착지 경도")
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
    lat: float = Query(...),
    lon: float = Query(...),
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
        return {"error": str(e)}