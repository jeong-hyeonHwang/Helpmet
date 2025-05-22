from pydantic import BaseModel
from typing import Optional

class BikeStationBase(BaseModel):
    station_id: str
    addr1: Optional[str]
    addr2: Optional[str]
    lat: float
    lon: float

class BikeStationCreate(BikeStationBase):
    pass 

class BikeStationOut(BikeStationBase):
    class Config:
        orm_mode = True

class PublicToiletBase(BaseModel):
    toilet_id: int
    name: Optional[str]
    road_addr: Optional[str]
    lat: float
    lon: float

class PublicToiletCreate(PublicToiletBase):
    pass

class PublicToiletOut(PublicToiletBase):
    class Config:
        orm_mode = True
