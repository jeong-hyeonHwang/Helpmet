from fastapi import FastAPI
from core.graph import load_graph
from api import route
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(debug=True)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 개발 중에는 전체 허용
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


app.include_router(route.router)

@app.on_event("startup")
async def startup_event():
    graphml_path = "data/seoul_combined.graphml"  # 네트워크 파일 경로
    load_graph(graphml_path)

@app.on_event("shutdown")
async def shutdown_event():
    print("Server shutting down...")

@app.get("/ping")
async def ping():
    return {"message": "pong"}

