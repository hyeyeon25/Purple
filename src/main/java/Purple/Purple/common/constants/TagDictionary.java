package Purple.Purple.common.constants;

import java.util.Arrays;
import java.util.List;

/**
 * 중앙 관리되는 태그 사전 (Tag Dictionary)
 *
 * 56개의 세분화된 태그로 구성되어 있으며, 사용자의 성향과 선호도를 정확하게 표현합니다.
 * 모든 벡터화 작업은 이 사전을 기반으로 수행됩니다.
 */
public class TagDictionary {

    /**
     * 전체 태그 목록 (56개)
     * 순서가 중요합니다 - 벡터의 각 차원은 이 순서와 일치합니다.
     */
    public static final List<String> TAGS = Arrays.asList(
            // === 음식 (Food) - 8개 ===
            "한식",           // Korean food
            "양식",           // Western food
            "아시안",         // Asian food
            "이색/퓨전",      // Fusion/Unique
            "분식",           // Korean snacks
            "건강식",         // Healthy food
            "카페",           // Cafe
            "주류",           // Alcohol

            // === 음료/주류 (Beverage/Alcohol) - 4개 ===
            "디저트",         // Dessert
            "베이커리",       // Bakery
            "술집",           // Pub
            "바",             // Bar

            // === 액티비티/문화 (Activity/Culture) - 12개 ===
            "액티비티",       // Activity
            "문화생활",       // Cultural life
            "스포츠",         // Sports
            "게임",           // Game
            "오락",           // Entertainment
            "체험",           // Experience
            "전시",           // Exhibition
            "공연",           // Performance
            "영화관",         // Cinema
            "미술관",         // Art museum
            "박물관",         // Museum
            "서점",           // Bookstore

            // === 기타 (Others) - 3개 ===
            "쇼핑",           // Shopping
            "공원",           // Park
            "산책",           // Walk

            // === 인원 (Group Size) - 3개 ===
            "혼자",           // Solo
            "친구",           // Friends
            "단체",           // Group

            // === 환경 (Environment) - 2개 ===
            "실내",           // Indoor
            "실외",           // Outdoor

            // === 분위기 (Atmosphere) - 16개 ===
            "조용한",         // Quiet
            "시끌벅적한",     // Lively/Noisy
            "분위기좋은",     // Good atmosphere
            "감성있는",       // Emotional/Sentimental
            "힙한",           // Hip/Trendy
            "모던한",         // Modern
            "레트로",         // Retro
            "편안한",         // Comfortable
            "고급스러운",     // Luxurious
            "럭셔리",         // Luxury
            "뷰가좋은",       // Good view
            "야경",           // Night view
            "사진맛집",       // Instagrammable
            "신상",           // New/Recently opened
            "이색적인",       // Unique/Unusual
            "숨은맛집",       // Hidden gem

            // === 시간 (Time) - 7개 ===
            "새벽",           // Dawn
            "아침",           // Morning
            "오전",           // Forenoon
            "점심",           // Lunch
            "오후",           // Afternoon
            "저녁",           // Evening
            "밤"              // Night
    );

    /**
     * 태그 사전의 크기 (벡터 차원)
     */
    public static final int DIMENSION = TAGS.size();

    /**
     * 특정 태그의 인덱스를 반환합니다.
     *
     * @param tag 검색할 태그
     * @return 태그의 인덱스, 존재하지 않으면 -1
     */
    public static int indexOf(String tag) {
        return TAGS.indexOf(tag);
    }

    /**
     * 특정 인덱스의 태그를 반환합니다.
     *
     * @param index 인덱스
     * @return 태그 이름
     * @throws IndexOutOfBoundsException 인덱스가 범위를 벗어난 경우
     */
    public static String getTag(int index) {
        return TAGS.get(index);
    }

    /**
     * 태그가 사전에 존재하는지 확인합니다.
     *
     * @param tag 확인할 태그
     * @return 존재하면 true, 아니면 false
     */
    public static boolean contains(String tag) {
        return TAGS.contains(tag);
    }

    // === 카테고리별 태그 그룹 (선택적 사용) ===

    public static final List<String> FOOD_TAGS = Arrays.asList(
            "한식", "양식", "아시안", "이색/퓨전", "분식", "건강식", "카페", "주류"
    );

    public static final List<String> BEVERAGE_TAGS = Arrays.asList(
            "카페", "주류", "디저트", "베이커리", "술집", "바"
    );

    public static final List<String> ACTIVITY_TAGS = Arrays.asList(
            "액티비티", "문화생활", "스포츠", "게임", "오락", "체험",
            "전시", "공연", "영화관", "미술관", "박물관", "서점"
    );

    public static final List<String> GROUP_SIZE_TAGS = Arrays.asList(
            "혼자", "친구", "단체"
    );

    public static final List<String> ENVIRONMENT_TAGS = Arrays.asList(
            "실내", "실외"
    );

    public static final List<String> ATMOSPHERE_TAGS = Arrays.asList(
            "조용한", "시끌벅적한", "분위기좋은", "감성있는", "힙한", "모던한",
            "레트로", "편안한", "고급스러운", "럭셔리", "뷰가좋은", "야경",
            "사진맛집", "신상", "이색적인", "숨은맛집"
    );

    public static final List<String> TIME_TAGS = Arrays.asList(
            "새벽", "아침", "오전", "점심", "오후", "저녁", "밤"
    );
}