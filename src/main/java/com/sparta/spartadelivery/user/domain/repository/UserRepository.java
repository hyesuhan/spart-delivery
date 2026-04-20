package com.sparta.spartadelivery.user.domain.repository;

import com.sparta.spartadelivery.user.domain.entity.Role;
import com.sparta.spartadelivery.user.domain.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmailAndDeletedAtIsNull(String email);

    Optional<UserEntity> findByIdAndDeletedAtIsNull(Long aLong);

    @Query("""
            select u
            from UserEntity u
            where u.deletedAt is null
              and (
                    :keyword is null
                    or lower(u.username) like lower(concat('%', :keyword, '%'))
                    or lower(u.nickname) like lower(concat('%', :keyword, '%'))
              )
              and (:role is null or u.role = :role)
            """)
    Page<UserEntity> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            Pageable pageable
    );

    boolean existsByEmail(String email);
}
