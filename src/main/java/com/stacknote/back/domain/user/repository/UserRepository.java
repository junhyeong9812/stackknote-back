package com.stacknote.back.domain.user.repository;

import com.stacknote.back.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 데이터 액세스 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회 (삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 사용자명으로 사용자 조회 (삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * 이메일 존재 여부 확인 (삭제되지 않은 사용자 중)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    /**
     * 사용자명 존재 여부 확인 (삭제되지 않은 사용자 중)
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 활성 상태인 사용자 조회 (삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true AND u.deletedAt IS NULL")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    /**
     * ID로 활성 사용자 조회 (삭제되지 않은 사용자만)
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true AND u.deletedAt IS NULL")
    Optional<User> findActiveUserById(@Param("id") Long id);

    /**
     * 이메일로 활성화된 사용자 조회 (삭제되지 않은) - CustomUserDetailsService용
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmailAndIsDeletedFalse(@Param("email") String email);

    /**
     * ID로 활성화된 사용자 조회 (삭제되지 않은) - CustomUserDetailsService용
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdAndIsDeletedFalse(@Param("id") Long id);

    /**
     * 이메일 존재 여부 확인 (삭제되지 않은) - CustomUserDetailsService용
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmailAndIsDeletedFalse(@Param("email") String email);

    /**
     * 사용자명 존재 여부 확인 (삭제되지 않은) - CustomUserDetailsService용
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsByUsernameAndIsDeletedFalse(@Param("username") String username);

    /**
     * 지정된 날짜 이전에 비활성화된 사용자들 조회 (스케줄러용)
     * 3개월 이상 비활성화된 계정을 찾기 위한 쿼리
     */
    @Query("SELECT u FROM User u WHERE u.isActive = false AND u.deactivatedAt IS NOT NULL AND u.deactivatedAt <= :date AND u.deletedAt IS NULL")
    List<User> findUsersDeactivatedBefore(@Param("date") LocalDateTime date);

    /**
     * 활성 사용자 수 조회 (통계용)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.deletedAt IS NULL")
    long countActiveUsers();

    /**
     * 비활성 사용자 수 조회 (통계용)
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = false AND u.deletedAt IS NULL")
    long countInactiveUsers();
}