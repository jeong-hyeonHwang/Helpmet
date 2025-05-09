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
        highway = edge.get("highway")
        cycleway = edge.get("cycleway")

        segment = RouteSegment(
            from_=Coordinate(lat=G.nodes[n1]["y"], lon=G.nodes[n1]["x"]),
            to=Coordinate(lat=G.nodes[n2]["y"], lon=G.nodes[n2]["x"]),
            is_cycleway=(highway == "cycleway") or (cycleway in {"track", "lane"}),
            distance_m=edge.get("length", 0)
        )
        segments.append(segment)
    return segments

def _cumulative_distances(G, route):
    cum = [0]
    for n1, n2 in zip(route, route[1:]):
        cum.append(cum[-1] + edge_length(G, n1, n2))
    return cum

def _turn_instructions(G, route, coords, cum_dist):
    turns, turn_idxs = [], []
    for i in range(1, len(coords) - 1):
        angle, action = calculate_angle(coords[i - 1], coords[i], coords[i + 1])
        if angle <= 30:
            continue

        distance = round(cum_dist[i])
        lat, lon = coords[i]

        instruction = Instruction(
            index=i,
            location=Coordinate(lat=lat, lon=lon),
            distance_m=distance,
            action=action,
            message=build_instruction_message(G, route[i], action, distance, lat, lon)
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
        instruction = Instruction(
            index=start,
            location=Coordinate(lat=lat, lon=lon),
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

def build_response_from_route(G , route) -> RouteResponseDto:
    coords = [(G.nodes[n]["y"], G.nodes[n]["x"]) for n in route]
    cum_dist = _cumulative_distances(G, route)
    total_len = cum_dist[-1]
    est_time = int((total_len / 1000) / 15 * 3600)

    segments = _build_route_segments(G, route)
    turn_instr, turn_idx = _turn_instructions(G, route, coords, cum_dist)

    bounds = [0] + turn_idx + [len(coords) - 1]
    linear_instr = _linear_instructions(G, route, coords, bounds)

    instructions = sorted(linear_instr + turn_instr, key=lambda x: x.index)

    start_addr = get_nearest_poi(G.nodes[route[0]]["y"], G.nodes[route[0]]["x"])
    end_addr = get_nearest_poi(G.nodes[route[0]]["y"], G.nodes[route[-1]]["x"])

    return RouteResponseDto(
        start_addr=start_addr,
        end_addr=end_addr,
        distance_m=round(total_len, 1),
        estimated_time_sec=est_time,
        route=segments,
        instructions=instructions
    )
