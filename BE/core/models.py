from sqlalchemy import Column, String, Numeric, Integer
from geoalchemy2 import Geography
from core.database import Base

class BikeStation(Base):
    __tablename__ = "bike_station"

    station_id = Column(String, primary_key=True, index=True)
    addr1 = Column(String)
    addr2 = Column(String)
    lon = Column(Numeric)
    lat = Column(Numeric)
    geom = Column(Geography(geometry_type="POINT", srid=4326))

class PublicToilet(Base):
    __tablename__ = "public_toilet"

    toilet_id = Column(Integer, primary_key=True, index=True)
    name = Column(String)
    road_addr = Column(String)
    lon = Column(Numeric)
    lat = Column(Numeric)
    geom = Column(Geography(geometry_type="POINT", srid=4326))