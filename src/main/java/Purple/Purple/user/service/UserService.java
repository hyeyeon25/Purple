package Purple.Purple.user.service;


import Purple.Purple.user.dto.*;
import Purple.Purple.user.entity.UserPersonalInfo;
import Purple.Purple.user.jwt.JwtUtil;
import Purple.Purple.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Transactional
    //회원가입
    public UserResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        // 닉네임 중복 체크
        if (userRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 엔티티 생성 및 저장
        UserPersonalInfo user = UserPersonalInfo.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userName(request.getUserName())
                .nickname(request.getNickname())
                .role("USER")
                .build();
        userRepository.save(user);

        // 응답 DTO 반환
        return new UserResponse(user.getUserId(), user.getEmail(), user.getUserName(), user.getNickname());
    }

    //로그인
    public Map<String, String> login(LoginRequest request) {
        UserPersonalInfo user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        String accessToken = jwtUtil.createJwt("access", user.getEmail(), user.getRole(), 10 * 60 * 1000L);
        String refreshToken = jwtUtil.createJwt("refresh", user.getEmail(), user.getRole(), 7 * 24 * 60 * 60 * 1000L);//아직 권한은 추가 안했어욥

        jwtUtil.addRefreshEntity(user.getEmail(), refreshToken, 86400000L);

        return Map.of(
                "access", accessToken,
                "refresh", refreshToken
        );
    }

    //사용자 정보 조회
    public UserResponse getUserInfo(Long userId) {
        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return new UserResponse(user.getUserId(), user.getEmail(), user.getUserName(), user.getNickname());
    }


    //닉네임 수정
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        return new UserResponse(user.getUserId(), user.getEmail(), user.getUserName(), user.getNickname());
    }

    //비밀번호 변경
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        UserPersonalInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    }

    //회원 탈퇴
    @Transactional
    public void withdraw(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        userRepository.deleteById(userId);
    }


}
