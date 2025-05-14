from pydantic import BaseModel, field_serializer
from typing import List


class Coordinate(BaseModel):
    lat: float
    lon: float


class RouteSegment(BaseModel):
    from_: Coordinate
    to: Coordinate
    is_cycleway: bool
    distance_m: float


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
