import asyncio
from luma.core.interface.serial import spi, noop
from luma.led_matrix.device import max7219
from luma.core.render import canvas

serial = spi(port=0, device=0, gpio=noop())
device = max7219(serial, cascaded=2, block_orientation=-90, rotate=0)

arrow_left_coords = [
    (4, 1), (4, 2), (5, 2),
    (0, 3), (1, 3), (2, 3), (3, 3), (4, 3), (5, 3), (6, 3), (7, 3),
    (0, 4), (1, 4), (2, 4), (3, 4), (4, 4), (5, 4), (6, 4), (7, 4),
    (4, 5), (5, 5), (4, 6)
]

arrow_right_coords = [
    (11, 1),
    (10, 2), (11, 2),
    (8, 3), (9, 3), (10, 3), (11, 3), (12, 3), (13, 3), (14, 3), (15, 3),
    (8, 4), (9, 4), (10, 4), (11, 4), (12, 4), (13, 4), (14, 4), (15, 4),
    (10, 5), (11, 5),
    (11, 6)
]

arrow_state = {
    "left": 0,
    "right": 0
}

INTERVAL = 0.5
MAX_REPEAT = 500

arrow_lock = asyncio.Lock()

async def arrow_loop():
    while True:
        async with arrow_lock:
            with canvas(device) as draw:
                for direction, coords in [("left", arrow_left_coords), ("right", arrow_right_coords)]:
                    if arrow_state[direction] > 0:
                        for x, y in coords:
                            draw.point((x, y), fill="white")
                            arrow_state[direction] -= 1

            await asyncio.sleep(INTERVAL)

            with canvas(device):
                pass
            await asyncio.sleep(INTERVAL)

        await asyncio.sleep(0.05)

def turn_on_arrow(direction):
    key = "left" if direction == 0 else "right"
    reverse_key = "right" if direction == 0 else "left"
    arrow_state[key] = MAX_REPEAT
    arrow_state[reverse_key] = 0

def turn_off_arrow(direction):
    key = "left" if direction == 0 else "right"
    arrow_state[key] = 0