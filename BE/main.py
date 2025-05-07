from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from core.graph import load_graphs           # ← 기존 로직 재사용
from api import route                       # ← 라우터 모듈

@asynccontextmanager
async def lifespan(app: FastAPI):
    # startup 
    load_graphs(app)  # 필요하면 반환값 저장
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

@app.get("/ping")
async def ping():
    return {"message": "pong"}

