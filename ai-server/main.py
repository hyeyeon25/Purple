import json
import psycopg2
import uvicorn
import urllib.parse
import os
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from webdriver_manager.chrome import ChromeDriverManager
from bs4 import BeautifulSoup
import time
from openai import OpenAI
import re

# =========================================================
# [설정] 환경 변수 로드 (.env 파일 읽기)
# =========================================================
# 현재 파일 위치 기준으로 .env 파일 로드
load_dotenv()

DB_CONFIG = {
  "host": os.getenv("DB_HOST", "localhost"),
  "database": os.getenv("DB_NAME", "tripick_db"),
  "user": os.getenv("DB_USER", "postgres"),
  "password": os.getenv("DB_PASSWORD"), # ★ 파일에서 읽어옴
  "port": os.getenv("DB_PORT", "5432")
}

# API Key도 파일에서 읽어옴
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

if not OPENAI_API_KEY:
  print("⚠️ 경고: .env 파일에서 OPENAI_API_KEY를 찾을 수 없습니다.")

client = OpenAI(api_key=OPENAI_API_KEY)

# FastAPI 앱 생성
app = FastAPI()


# =========================================================
# 네이버 ID 자동 찾기 함수 (이름 + 주소 검색)
# =========================================================
def find_naver_id_by_name(driver, place_name, address):
  queries = []
  # 전략 1: 전체주소 + 이름 (Strict)
  queries.append({"q": f"{address} {place_name}", "type": "strict"})

  # 이름 + 동네 (Loose)
  short_address = ""
  for part in address.split():
    if part.endswith("동") or part.endswith("읍") or part.endswith("면"):
      short_address = part
      break
  if short_address:
    queries.append({"q": f"{place_name} {short_address}", "type": "loose"})
  else:
    region = address.split()[1] if len(address.split()) > 1 else ''
    queries.append({"q": f"{place_name} {region}", "type": "loose"})

  for query_obj in queries:
    search_query = query_obj["q"]
    search_type = query_obj["type"]

    print(f"🔎 [검색 시도] 검색어: '{search_query}' (모드: {search_type})")

    try:
      encoded_query = urllib.parse.quote(search_query)
      search_url = f"https://m.map.naver.com/search2/search.naver?query={encoded_query}"
      driver.get(search_url)
      time.sleep(5)

      if "place.naver.com/restaurant/" in driver.current_url:
        match = re.search(r'restaurant/(\d+)', driver.current_url)
        if match:
          print(f"   ✅ (자동이동) 상세페이지 도착! ID: {match.group(1)}")
          return match.group(1)

      # ---------------------------------------------------------
      # 후보군 수집 (제목 클래스 우선)
      # ---------------------------------------------------------
      candidates = []

      # 1. 제목 클래스
      candidates.extend(driver.find_elements(By.CLASS_NAME, "search_title"))
      # 2. strong 태그
      candidates.extend(driver.find_elements(By.TAG_NAME, "strong"))
      # 3. data-id
      candidates.extend(driver.find_elements(By.CSS_SELECTOR, "[data-id]"))

      # 중복 제거 및 필터링
      valid_elements = []
      seen_ids = set()

      for el in candidates:
        try:
          if not el.is_displayed(): continue
          if el.id in seen_ids: continue

          text = el.text.strip()
          if len(text) < 2: continue # 너무 짧은 글자 제외

          # 주소 텍스트('충남...', '충청...')는 클릭 금지
          if text.startswith("충남") or text.startswith("충청") or text.startswith("천안"):
            continue

          # '거리순', '관련도순' 같은 필터 버튼 제외
          if text in ["거리순", "관련도순", "지도", "길찾기", "공유"]:
            continue

          seen_ids.add(el.id)
          valid_elements.append(el)
        except: continue

      if not valid_elements:
        print("   ⚠️ 클릭할만한 가게 이름이 안 보입니다.")
        continue

      # ---------------------------------------------------------
      # 이름 비교 -> 강제 클릭
      # ---------------------------------------------------------
      simple_name = place_name.split()[0]
      clicked = False

      # 1. 이름이 비슷하면 클릭
      for el in valid_elements:
        try:
          text = el.text.strip()
          # 화면 글자에 내 검색어가 있거나, 내 검색어에 화면 글자가 있거나 (서로 포함 관계면 OK)
          if (simple_name in text) or (text in simple_name):
            print(f"   👆 이름 일치 항목 발견! ('{text}') 클릭 시도...")
            el.click()
            time.sleep(3)
            clicked = True
            break
        except: continue

      # 첫 번째 유효 요소 클릭
      if not clicked and search_type == "strict":
        first_el = valid_elements[0] # 주소 텍스트는 위에서 다 걸러냈음!
        print(f"   ⚠️ 이름 불일치. 하지만 주소 검색이므로 첫 번째 가게('{first_el.text}')를 강제 클릭합니다.")
        try:
          first_el.click()
          time.sleep(3)
          clicked = True
        except Exception as e:
          print(f"   ❌ 강제 클릭 실패: {e}")

      # 결과 확인
      if clicked:
        if "place.naver.com/restaurant/" in driver.current_url:
          match = re.search(r'restaurant/(\d+)', driver.current_url)
          if match:
            print(f"   ✅ (클릭성공) ID 발견: {match.group(1)}")
            return match.group(1)
        else:
          print("   ❌ 클릭했는데 상세페이지가 아닙니다. 뒤로가기.")
          driver.back()
          time.sleep(2)

    except Exception as e:
      print(f"   ⚠️ 에러 발생: {e}")
      continue

  print("   ❌ 모든 검색 방법 실패.")
  return None

# =========================================================
# 1. 네이버 리뷰 크롤링 함수
# =========================================================
def get_naver_reviews(place_name, address):
  print(f"🔄 [1단계] 크롤링 시작: {place_name} ({address})")

  options = webdriver.ChromeOptions()
  options.add_argument('headless') # 창 띄워서 확인하려면 주석 처리 유지
  options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36")

  driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
  reviews = []

  try:
    # 1. 이름과 주소로 네이버 ID 먼저 찾기
    naver_place_id = find_naver_id_by_name(driver, place_name, address)

    if not naver_place_id:
      print("   ❌ 네이버 ID를 찾지 못해 크롤링을 종료합니다.")
      return []

    # 2. 찾은 ID로 리뷰 페이지 접속
    url = f"https://m.place.naver.com/restaurant/{naver_place_id}/review/visitor"
    driver.get(url)
    time.sleep(4) # 페이지 로딩 대기

    # 스크롤 내리기
    body = driver.find_element(By.TAG_NAME, 'body')
    for _ in range(5):
      body.send_keys(Keys.END)
      time.sleep(1)

    # HTML 가져와서 분석
    soup = BeautifulSoup(driver.page_source, 'html.parser')

    # 클래스 후보군
    candidates = ['zPfVt', 'xHaT3', 'n5Y52', 'w4jE1', 'pui__vn15t2', 'review_content']
    found_elements = []

    for candidate in candidates:
      found = soup.find_all(class_=candidate)
      if found:
        print(f"   🔍 찾았다! 사용된 클래스명: {candidate} ({len(found)}개)")
        found_elements = found
        break

        # 텍스트 추출
    for item in found_elements:
      try:
        if isinstance(item, str): text = item.strip()
        else: text = item.get_text().strip()

        if len(text) > 10:
          reviews.append(text)
      except:
        continue

    print(f"   ✅ 리뷰 {len(reviews)}개 수집 완료")
    return reviews

  except Exception as e:
    print(f"   ❌ 크롤링 에러 발생: {e}")
    return []
  finally:
    driver.quit()

# =========================================================
#  2. GPT 요약 및 분석 함수
# =========================================================
def analyze_reviews_with_gpt(reviews):
  if not reviews:
    return None

  print("🤖 [2단계] GPT에게 분석 요청 중...")

  full_text = "\n".join(reviews[:20])

  prompt = f"""
    아래 리뷰들을 분석해서 JSON으로 답해줘.
    {{
        "summary": "장소 분위기와 특징 3줄 요약",
        "keywords": ["키워드1", "키워드2", "키워드3"],
        "one_line_recommend": "매력적인 한 줄 추천 문구"
    }}
    
    [리뷰 데이터]
    {full_text}
    """

  try:
    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[
          {"role": "system", "content": "JSON 포맷으로만 응답해."},
          {"role": "user", "content": prompt}
        ],
        response_format={"type": "json_object"}
    )

    result_json = response.choices[0].message.content
    return json.loads(result_json)

  except Exception as e:
    print(f"   ❌ GPT 오류: {e}")
    return None

# =========================================================
#  3. DB 저장 함수
# =========================================================
def save_to_db(place_id, ai_data):
  print(f"💾 [3단계] DB 저장 시도 (Place ID: {place_id})")

  conn = psycopg2.connect(**DB_CONFIG)
  cur = conn.cursor()

  try:
    sql = """
            INSERT INTO place_analysis (place_id, review_summary, keywords, one_line_recommend)
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (place_id) DO UPDATE 
            SET review_summary = EXCLUDED.review_summary,
                keywords = EXCLUDED.keywords,
                one_line_recommend = EXCLUDED.one_line_recommend;
        """

    cur.execute(sql, (
      place_id,
      ai_data['summary'],
      json.dumps(ai_data['keywords'], ensure_ascii=False),
      ai_data['one_line_recommend']
    ))

    conn.commit()
    print("   🎉 DB 저장 성공!")
    return True

  except Exception as e:
    print(f"   ❌ DB 저장 실패: {e}")
    conn.rollback()
    return False
  finally:
    cur.close()
    conn.close()

# =========================================================
# API 엔드포인트
# =========================================================
@app.get("/analyze")
async def analyze_place(db_place_id: int, place_name: str, address: str):
  # ★ 이제 자바가 'naver_place_id' 대신 'place_name'과 'address'를 줍니다.
  print(f"\n🔔 [요청 도착] 자바가 분석을 요청했습니다!")
  print(f"   - DB ID: {db_place_id}")
  print(f"   - 장소명: {place_name}")
  print(f"   - 주소: {address}")

  # 1. 리뷰 수집 (이름과 주소 전달)
  reviews = get_naver_reviews(place_name, address)
  if not reviews:
    raise HTTPException(status_code=404, detail="리뷰를 찾을 수 없거나 네이버 ID를 못 찾음")

  # 2. AI 분석
  ai_result = analyze_reviews_with_gpt(reviews)
  if not ai_result:
    raise HTTPException(status_code=500, detail="GPT 분석 실패")

  # 3. DB 저장
  success = save_to_db(db_place_id, ai_result)
  if not success:
    raise HTTPException(status_code=500, detail="DB 저장 실패")

  return {"result": "success", "message": "분석 완료 및 DB 저장 끝!"}

if __name__ == "__main__":
  print("🚀 파이썬 서버가 시작되었습니다! (http://localhost:8000)")
  uvicorn.run(app, host="0.0.0.0", port=8000)