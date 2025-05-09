from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from core.database import engine, Base
from core.graph import load_graphs
from api import route
from core.exception_handlers import register_exception_handlers
from services.logger import setup_logger

@asynccontextmanager
async def lifespan(app: FastAPI):
    # startup
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
        print("✓ DB Tables created (if not exists)")

    load_graphs(app)
    print("✓ Graph loaded and attached to app.state.graph")
    
    yield
    
    # shutdown
    print("Server shutting down…")


app = FastAPI(
    debug=True,
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