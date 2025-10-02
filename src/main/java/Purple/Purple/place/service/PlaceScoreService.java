package Purple.Purple.place.service;

import Purple.Purple.place.entity.PlaceEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class PlaceScoreService {

    // "해당 없음"을 나타내는 상수
    private static final int NOT_APPLICABLE = -1;

    // --- 사용자 선호도 코드와 일치하는 카테고리 코드 상수 정의 ---
    // 0:한식, 1:양식, 2:아시아&일식&중식, 3:이색/퓨전, 4:분식, 5:건강식, 6:주류
    public static final int FOOD_CODE_KOREAN = 0;
    public static final int FOOD_CODE_WESTERN = 1;
    public static final int FOOD_CODE_ASIAN = 2;
    public static final int FOOD_CODE_JAPAN = 3;
    public static final int FOOD_CODE_CHINA = 4;
    public static final int FOOD_CODE_UNIQUE = 5;
    public static final int FOOD_CODE_SNACK = 6;
    public static final int FOOD_CODE_HEALTH = 7;
    public static final int FOOD_CODE_PUB = 8;

    // --- 카페 카테고리 코드 정의 ---
    public static final int CAFE_CODE_COFFEE_SHOP = 0; // 커피전문점
    public static final int CAFE_CODE_DESSERT = 1;     // 디저트/베이커리
    public static final int CAFE_CODE_THEME = 2;       // 테마카페 (북카페, 애견카페 등)

    // --- 문화 카테고리 코드 정의 ---
    public static final int CULTURE_CODE_EXHIBITION = 0; // 전시/미술/박물관
    public static final int CULTURE_CODE_PERFORMANCE = 1;  // 공연장
    public static final int CULTURE_CODE_LIBRARY = 2;      // 도서관/서점

    // --- 액티비티 카테고리 코드 정의 ---
    public static final int ACTIVITY_CODE_OUTDOOR = 0;   // 자연/공원/명소
    public static final int ACTIVITY_CODE_INDOOR = 1;    // 실내 액티비티 (PC방, 방탈출 등)
    public static final int ACTIVITY_CODE_SPORTS = 2;    // 스포츠 시설


    // --- 카테고리별 키워드 집합 ---
    private static final Set<String> FOOD_KOREAN_KEYWORDS = new HashSet<>(Arrays.asList("한식", "국밥", "찌개", "갈비", "삼겹살", "국수", "한정식", "닭요리", "해물", "생선", "순대", "해장국", "곱창", "막창", "족발", "보쌈", "감자탕", "냉면", "쌈밥", "두부", "죽", "설렁탕", "곰탕"));
    private static final Set<String> FOOD_WESTERN_KEYWORDS = new HashSet<>(Arrays.asList("양식", "이탈리안", "스테이크", "피자", "버거", "파스타", "브런치"));
    private static final Set<String> FOOD_ASIAN_KEYWORDS = new HashSet<>(Arrays.asList("아시아음식", "동남아음식", "베트남음식", "인도음식", "튀르키예음식", "태국음식"));
    private static final Set<String> FOOD_JAPAN_KEYWORDS = new HashSet<>(Arrays.asList("일식", "초밥", "일본식라면", "참치회"));
    private static final Set<String> FOOD_CHINA_KEYWORDS = new HashSet<>(Arrays.asList("중식", "중국요리", "마라탕", "양꼬치"));
    private static final Set<String> FOOD_SNACK_KEYWORDS = new HashSet<>(Arrays.asList("분식", "떡볶이", "김밥", "만두", "토스트", "닭강정", "돈까스", "우동"));
    private static final Set<String> FOOD_HEALTH_KEYWORDS = new HashSet<>(Arrays.asList("건강식", "샐러드"));
    private static final Set<String> FOOD_PUB_KEYWORDS = new HashSet<>(Arrays.asList("술집", "주점", "포장마차"));

    // --- 카페 세부 카테고리별 키워드 ---
    private static final Set<String> CAFE_DESSERT_KEYWORDS = new HashSet<>(Arrays.asList(
            "디저트", "베이커리", "도넛", "아이스크림", "설빙", "요거트", "와플", "마카롱", "케이크", "생과일전문점", "전통찻집",
            "크리스피크림도넛", "배스킨라빈스", "파리바게뜨", "뚜레쥬르", "던킨", "요거프레소", "요거트월드", "요거트아이스크림의정석", "쥬씨"
    ));
    private static final Set<String> CAFE_THEME_KEYWORDS = new HashSet<>(Arrays.asList("테마카페", "북카페", "갤러리카페", "애견카페", "고양이카페", "만화카페", "라이브카페", "무인카페", "보드게임카페"));
    // 위의 두가지에 속하지 않는 '커피', '커피전문점', '다방' 등은 CAFE_CODE_COFFEE_SHOP (0번)으로 분류


    // --- 문화 세부 카테고리별 키워드 ---
    private static final Set<String> CULTURE_EXHIBITION_KEYWORDS = new HashSet<>(Arrays.asList(
            "미술관", "전시관", "박물관", "기념관", "갤러리카페", "문화유적", "향교", "서당", "생가", "고택", "유적지", "사당", "제단", "산성", "성곽"
    ));
    private static final Set<String> CULTURE_PERFORMANCE_KEYWORDS = new HashSet<>(Arrays.asList("공연장", "연극극장"));
    private static final Set<String> CULTURE_LIBRARY_KEYWORDS = new HashSet<>(Arrays.asList("도서관", "서점", "북카페", "학습시설"));


    // --- 액티비티 세부 카테고리별 키워드 ---
    private static final Set<String> ACTIVITY_OUTDOOR_KEYWORDS = new HashSet<>(Arrays.asList(
            "여행", "공원", "관광", "명소", "산", "계곡", "하천", "저수지", "전망대", "광장", "수목원", "식물원", "자연휴양림",
            "유원지", "생태보존", "서식지", "등산로", "드라이브코스", "도보여행", "테마거리", "동물원", "온천", "호수", "산봉우리",
            "천문대", "연못", "바위", "테마파크", "워터파크", "관광농원"
    ));
    private static final Set<String> ACTIVITY_INDOOR_KEYWORDS = new HashSet<>(Arrays.asList("클라이밍", "PC방", "게임방", "키즈카페", "스터디카페", "방탈출", "볼링", "만화카페", "실내동물원", "노래방", "서바이벌게임", "오락실", "공간대여"));
    private static final Set<String> ACTIVITY_SPORTS_KEYWORDS = new HashSet<>(Arrays.asList("스포츠", "농구장", "축구장", "테니스장", "수영장", "운동장", "풋살장"));

    // PlaceEntity의 기존 score 컬럼에 세부 카테고리 코드를 할당합니다.
    public void assignScores(PlaceEntity place) {
        String category = place.getPlaceCategory();

        // 1. 모든 점수를 "해당 없음"(-1)으로 초기화
        place.setScoreFood(NOT_APPLICABLE);
        place.setScoreCafe(NOT_APPLICABLE);
        place.setScoreCulture(NOT_APPLICABLE);
        place.setScoreActivity(NOT_APPLICABLE);

        // 2. 각 대분류에 대해 세부 카테고리 코드를 찾아 할당
        //    (장소는 여러 속성을 가질 수 있으므로, 각 분류를 독립적으로 검사)
        place.setScoreFood(getFoodCategoryCode(category));
        place.setScoreCafe(getCafeCategoryCode(category));
        place.setScoreCulture(getCultureCategoryCode(category));
        place.setScoreActivity(getActivityCategoryCode(category));
    }

    // 카테고리 문자열을 분석하여 해당하는 음식 세부 카테고리 코드를 반환합니다.
    // @return 매칭되는 음식 카테고리 코드, 없으면 -1
    private int getFoodCategoryCode(String category) {
        if (containsAnyKeyword(category, FOOD_KOREAN_KEYWORDS)) return FOOD_CODE_KOREAN;
        if (containsAnyKeyword(category, FOOD_WESTERN_KEYWORDS)) return FOOD_CODE_WESTERN;
        if (containsAnyKeyword(category, FOOD_CHINA_KEYWORDS)) return FOOD_CODE_CHINA;
        if (containsAnyKeyword(category, FOOD_JAPAN_KEYWORDS)) return FOOD_CODE_JAPAN;
        if (containsAnyKeyword(category, FOOD_ASIAN_KEYWORDS)) return FOOD_CODE_ASIAN;
        if (containsAnyKeyword(category, FOOD_SNACK_KEYWORDS)) return FOOD_CODE_SNACK;
        if (containsAnyKeyword(category, FOOD_HEALTH_KEYWORDS)) return FOOD_CODE_HEALTH;
        if (containsAnyKeyword(category, FOOD_PUB_KEYWORDS)) return FOOD_CODE_PUB;

        if (category.contains("음식점")) return FOOD_CODE_UNIQUE;

        return NOT_APPLICABLE;
    }
    private int getCafeCategoryCode(String category) {
        if (!category.contains("카페") && !containsAnyKeyword(category, CAFE_DESSERT_KEYWORDS)) {
            return NOT_APPLICABLE;
        }
        if (containsAnyKeyword(category, CAFE_THEME_KEYWORDS)) return CAFE_CODE_THEME;
        if (containsAnyKeyword(category, CAFE_DESSERT_KEYWORDS)) return CAFE_CODE_DESSERT;
        return CAFE_CODE_COFFEE_SHOP; // 위의 조건에 안 걸리면 기본 커피전문점으로 분류
    }

    private int getCultureCategoryCode(String category) {
        if (containsAnyKeyword(category, CULTURE_EXHIBITION_KEYWORDS)) return CULTURE_CODE_EXHIBITION;
        if (containsAnyKeyword(category, CULTURE_PERFORMANCE_KEYWORDS)) return CULTURE_CODE_PERFORMANCE;
        if (containsAnyKeyword(category, CULTURE_LIBRARY_KEYWORDS)) return CULTURE_CODE_LIBRARY;
        return NOT_APPLICABLE;
    }

    private int getActivityCategoryCode(String category) {
        if (containsAnyKeyword(category, ACTIVITY_SPORTS_KEYWORDS)) return ACTIVITY_CODE_SPORTS;
        if (containsAnyKeyword(category, ACTIVITY_INDOOR_KEYWORDS)) return ACTIVITY_CODE_INDOOR;
        if (containsAnyKeyword(category, ACTIVITY_OUTDOOR_KEYWORDS)) return ACTIVITY_CODE_OUTDOOR;
        return NOT_APPLICABLE;
    }

    private boolean containsAnyKeyword(String text, Set<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}