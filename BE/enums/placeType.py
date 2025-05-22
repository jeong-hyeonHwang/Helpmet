from enum import Enum

class PlaceType(str, Enum):
    conv = "conv"
    toilet = "toilet"
    rental = "rental"