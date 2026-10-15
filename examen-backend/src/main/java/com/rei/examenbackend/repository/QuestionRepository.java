package com.rei.examenbackend.repository;

import com.rei.examenbackend.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCategoryId(Long categoryId);

    boolean existsByCategoryIdAndArchivedFalse(Long categoryId);

    List<Question> findByOwnerIdAndArchivedFalse(Long ownerId);

    List<Question> findByOwnerIsNull();

    @Query("""
            select q from Question q
            where q.archived = false and (q.owner is null or q.owner.id = :userId)
            """)
    Page<Question> findVisibleTo(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            select q from Question q
            where q.archived = false and q.category.id = :categoryId
              and (q.owner is null or q.owner.id = :userId)
            order by q.orderNumber asc, q.id asc
            """)
    List<Question> findVisibleInCategory(@Param("categoryId") Long categoryId, @Param("userId") Long userId);

    List<Question> findByOwnerId(Long ownerId);
}
