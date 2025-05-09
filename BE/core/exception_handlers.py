from fastapi import Request, FastAPI
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException
import logging
from models.base_response import BaseResponse

logger = logging.getLogger("uvicorn.error")

def register_exception_handlers(app: FastAPI):
    @app.exception_handler(StarletteHTTPException)
    async def http_exception_handler(request: Request, exc: StarletteHTTPException):
        logger.warning(f"[HTTP Error] {exc.detail} at {request.url}")
        return JSONResponse(
            status_code=exc.status_code,
            content=BaseResponse[None](status=exc.status_code, message=exc.detail, data=None).dict()
        )

    @app.exception_handler(RequestValidationError)
    async def validation_exception_handler(request: Request, exc: RequestValidationError):
        logger.warning(f"[Validation Error] {exc.errors()} at {request.url}")
        return JSONResponse(
            status_code=422,
            content=BaseResponse[None](status=422, message="Validation Error", data=exc.errors()).dict()
        )

    @app.exception_handler(Exception)
    async def unhandled_exception_handler(request: Request, exc: Exception):
        logger.error(f"[Unhandled Error] {exc} at {request.url}", exc_info=True)
        return JSONResponse(
            status_code=500,
            content=BaseResponse[None](status=500, message="Internal Server Error", data=None).dict()
        )
