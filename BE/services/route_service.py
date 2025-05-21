from services.route_util import (
    calculate_angle,
    build_instruction_message,
    edge_length,
    nearest_nodes,
    route_nodes,
    get_nearest_poi
)
from models.route_response import (
    Coordinate,
    RouteSegment,
    Instruction,
    RouteResponseDto
)

def _build_route_segments(G, route):
    segments = []
    for n1, n2 in zip(route, route[1:]):
        edge = G.edges[n1, n2, 0]
        is_cycleway = edge.get("highway") == "cycleway"

        if "geometry" in edge:
            coords_raw = list(edge["geometry"].coords)
            coords = [Coordinate.model_construct(lat=lat, lon=lon) for lon, lat in coords_raw]
        else:
            coords = [
                Coordinate.model_construct(lat=G.nodes[n1]["y"], lon=G.nodes[n1]["x"]),
                Coordinate.model_construct(lat=G.nodes[n2]["y"], lon=G.nodes[n2]["x"])
            ]

        segment = RouteSegment.model_construct(
            coords=coords,
            is_cycleway=is_cycleway,
            distance_m=edge.get("length", 0)
        )
        segments.append(segment)

    return segments

def compute_distances_and_time(G, route):
    speed_walk = 1.4  # m/s
    speed_bike = 4.1  # m/s

    cum_distances = [0]
    total_time = 0.0
    total_length = 0.0

    for n1, n2 in zip(route, route[1:]):
        edge = G.edges[n1, n2, 0]
        length = edge.get("length", 0)

        is_cycleway = edge.get("highway") == "cycleway"
        speed = speed_bike if is_cycleway else speed_walk

        travel_time = length / speed if speed > 0 else 0

        total_length += length
        total_time += travel_time
        cum_distances.append(total_length)

    return cum_distances, int(total_time), round(total_length, 1)

def _turn_instructions(POIs, G, route, coords, cum_dist):
    turns, turn_idxs = [], []
    for i in range(1, len(coords) - 1):
        angle, action = calculate_angle(coords[i - 1], coords[i], coords[i + 1])
        if angle <= 30:
            continue

        distance = round(cum_dist[i])
        lat, lon = coords[i]

        instruction = Instruction.model_construct(
            index=i,
            location=Coordinate.model_construct(lat=lat, lon=lon),
            distance_m=distance,
            action=action,
            message=build_instruction_message(POIs, G, route[i], action, distance, lat, lon)
        )
        turns.append(instruction)
        turn_idxs.append(i)
    return turns, turn_idxs

def _linear_instructions(G, route, coords, bounds):
    linears = []
    for start, end in zip(bounds, bounds[1:]):
        if end - start < 1:
            continue
        seg_dist = sum(edge_length(G, route[j], route[j + 1]) for j in range(start, end))
        lat, lon = coords[start]
        instruction = Instruction.model_construct(
            index=start,
            location=Coordinate.model_construct(lat=lat, lon=lon),
            distance_m=round(seg_dist),
            action="직진",
            message=f"{round(seg_dist)}m 직진하세요"
        )
        linears.append(instruction)
    return linears

def find_route(G, from_lat, from_lon, to_lat, to_lon):
    from_node, to_node = nearest_nodes(G, from_lat, from_lon, to_lat, to_lon)
    route = route_nodes(G, from_node, to_node)
    return route

def build_response_from_route(POIs, G , route) -> RouteResponseDto:
    coords = [(G.nodes[n]["y"], G.nodes[n]["x"]) for n in route]
    cum_dist, est_time, total_len = compute_distances_and_time(G, route)

    segments = _build_route_segments(G, route)
    turn_instr, turn_idx = _turn_instructions(POIs, G, route, coords, cum_dist)

    bounds = [0] + turn_idx + [len(coords) - 1]
    linear_instr = _linear_instructions(G, route, coords, bounds)

    def instruction_priority(instr):
        return (instr.index, 0 if instr.action in ("좌회전", "우회전") else 1)

    instructions = sorted(linear_instr + turn_instr, key=instruction_priority)

    start_addr = get_nearest_poi(lat=G.nodes[route[0]]["y"], lon=G.nodes[route[0]]["x"], POIs=POIs)
    end_addr = get_nearest_poi(lat=G.nodes[route[-1]]["y"], lon=G.nodes[route[-1]]["x"], POIs=POIs)

    return RouteResponseDto.model_construct(
        start_addr=start_addr,
        end_addr=end_addr,
        distance_m=round(total_len, 1),
        estimated_time_sec=est_time,
        route=segments,
        instructions=instructions
    )
