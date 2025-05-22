from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from core.database import engine, Base
from core.graph import load_graphs, load_pois
from api import route
from core.exception_handlers import register_exception_handlers
from services.logger import setup_logger
import logging

logger = logging.getLogger("main")

PLACE = "Seoul, South Korea"
TAGS = {"amenity": True, "shop": True, "tourism": True, "building": True}

@asynccontextmanager
async def lifespan(app: FastAPI):
    # startup
    try:
        async with engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)
            logger.info("✓ DB Tables created (if not exists)")

        load_graphs(app)
        logger.info("✓ Graph loaded and attached to app.state.graph")
        load_pois(app, place_name=PLACE, tags=TAGS) 
        logger.info("✓ Features loaded")
        
        yield
    
         # shutdown
        logger.info("Server shutting down…")

    except Exception as e:
        logger.warning("Lifespan 에러: %s", e)
        raise e

app = FastAPI(
    debug=False,
    lifespan=lifespan, 
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],      # 개발 단계에서는 전체 허용
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(route.router)

register_exception_handlers(app)

setup_logger()