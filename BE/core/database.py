from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker
from starlette.config import Config
from sqlalchemy.orm import declarative_base

Base = declarative_base()

config = Config('.env')
DATABASE_URL = config('DATABASE_URL')

engine = create_async_engine(DATABASE_URL, echo=True)
AsyncSessionLocal = sessionmaker(bind=engine, class_=AsyncSession, expire_on_commit=False)

async def get_db():
    #transation 설정 제거 (readonly session)
    session = session.execution_options(isolation_level="AUTOCOMMIT")
    yield session