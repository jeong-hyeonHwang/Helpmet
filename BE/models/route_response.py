from pydantic import BaseModel
from typing import List


class Coordinate(BaseModel):
    lat: float
    lon: float


class RouteSegment(BaseModel):
    from_: Coordinate  # 'from'은 예약어라서 변수명을 'from_'으로 변경
    to: Coordinate
    is_cycleway: bool
    distance_m: float

    class Config:
        fields = {
            'from_': 'from'  # 실제 JSON 키는 'from'
        }


class Instruction(BaseModel):
    index: int
    location: Coordinate
    distance_m: float
    action: str
    message: str


class RouteResponseDto(BaseModel):
    distance_m: float
    estimated_time_sec: int
    start_addr : str
    end_addr : str
    route: List[RouteSegment]
    instructions: List[Instruction]
