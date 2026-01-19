package Purple.Purple.user.service;


import Purple.Purple.folder.repository.FolderRepository;
import Purple.Purple.preferences.entity.PreferencesEntity;
import Purple.Purple.preferences.repository.PreferencesRepository;
import Purple.Purple.user.dto.*;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.jwt.JwtUtil;
import Purple.Purple.user.repository.RefreshRepository;
import Purple.Purple.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


/**
 * 유저 관련 비즈니스 로직 처리 서비스
 * - 회원가입, 로그인, 정보 수정 및 회원 탈퇴 처리함
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PreferencesRepository preferencesRepository;
    private final FolderRepository folderRepository;
    private final RefreshRepository refreshRepository;


    @Transactional
    // 회원가입: 이메일 중복 검사 후 비밀번호 암호화하여 유저 정보 저장함
    public UserResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }


        // 엔티티 생성 및 저장
        UserPersonalInfo user = UserPersonalInfo.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role("USER")
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .build();
        userRepository.save(user);

        return new UserResponse(user.getUserId(), user.getEmail(), user.getNickname(), user.getGender(), user.getBirthDate());
    }

    // 로그인: 계정 확인 및 비밀번호 일치 검증 후 JWT(Access/Refresh) 발급함
    public LoginResponse login(LoginRequest request) {
        //이메일 존재 여부 확인
        UserPersonalInfo user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        //암호화된 비밀번호 대조
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        //Access Token 발급 (추후 서비스 완성 시 10분으로 변경할 예정)
        String accessToken = jwtUtil.createJwt(
                "access",
                user.getEmail(),
                user.getRole(),
                user.getUserId(),
                10 * 60 * 10000L);

        //Refresh Token 발급 및 DB 저장
        String refreshToken = jwtUtil.createJwt(
                "refresh",
                user.getEmail(),
                user.getRole(),
                user.getUserId(),
                7 * 24 * 60 * 60 * 1000L);

        jwtUtil.addRefreshEntity(user.getEmail(), refreshToken, 86400000L);

        return new LoginResponse("로그인 성공", accessToken, user.getNickname(), user.getUserId());
    }

    //사용자 정보 조회: 유저 ID로 상세 프로필 정보 조회함
    public UserResponse getUserInfo(Long userId) {
        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new UserResponse(user.getUserId(), user.getEmail(), user.getNickname(), user.getGender(), user.getBirthDate());
    }


    //유저 정보 수정: 닉네임 변경 사항 반영
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        return new UserResponse(user.getUserId(), user.getEmail(), user.getNickname(), user.getGender(), user.getBirthDate());
    }

    //비밀번호 변경: 현재 비밀번호 검증 후 새 비밀번호로 암호화하여 교체
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    //회원 탈퇴: 유저와 연관된 모든 데이터(성향, 폴더, 토큰) 삭제
    @Transactional
    public void withdraw(Long userId) {
        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //유저 맞춤 성향(Preferences) 데이터 삭제
        preferencesRepository.findByUser_UserId(userId).ifPresent(preferencesRepository::delete);

        //생성한 폴더 데이터 삭제 (연결된 일정, 장소는 CASCADE 설정에 따름)
        folderRepository.findAllByUserOrderByFolderIdDesc(user).forEach(folderRepository::delete);

        //DB에 남아있는 해당 유저의 Refresh 토큰 모두 삭제
        refreshRepository.findAll().stream()
                .filter(refresh -> refresh.getUsername().equals(user.getEmail()))
                .forEach(refreshRepository::delete);

        //최종적으로 유저 정보 삭제
        userRepository.delete(user);
    }


}
