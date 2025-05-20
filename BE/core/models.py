from sqlalchemy import BigInteger, Column, Float, String, Numeric, Integer, Text
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

from sqlalchemy import Column, BigInteger, String, Float
from geoalchemy2 import Geometry
from core.database import Base

class BicycleStation(Base):
    __tablename__ = "bicycle_station"

    osmid = Column(BigInteger, primary_key=True)
    name = Column(String)
    ref = Column(String)
    lat = Column(Float)
    lon = Column(Float)
    geom = Column(Geometry(geometry_type="POINT", srid=4326))

class PublicToilet(Base):
    __tablename__ = "public_toilet"

    toilet_id = Column(Integer, primary_key=True, index=True)
    name = Column(String)
    road_addr = Column(String)
    lon = Column(Numeric)
    lat = Column(Numeric)
    geom = Column(Geography(geometry_type="POINT", srid=4326))

class EntryNode(Base):
    __tablename__ = "bike_entry_connect"

    connect_node = Column(BigInteger, primary_key=True)
    lat = Column(Float)
    lon = Column(Float)
    name = Column(Text)
    geom_point = Column(Geography(geometry_type='POINT', srid=4326))

class ExitNode(Base):
    __tablename__ = "bike_exit_connect"

    connect_node = Column(BigInteger, primary_key=True)
    lat = Column(Float)
    lon = Column(Float)
    name = Column(Text)
    geom_point = Column(Geography(geometry_type='POINT', srid=4326))