package com.sparta.spartadelivery.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    // 레코드 생성 시간
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 레코드 생성자 (username)
    @CreatedBy
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    // 레코드 수정 시간
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 레코드 수정자 (username)
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // 레코드 삭제 시간 (soft delete)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 레코드 삭제자 (username)
    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    // soft delete
    public void markDeleted(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}
