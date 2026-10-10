package com.rei.examenbackend.repository;

import java.util.List;

import com.rei.examenbackend.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Category> findByNameIgnoreCase(String name);

    Optional<Category> findByNameIgnoreCaseAndOwnerIsNull(String name);

    @Query("select c from Category c where c.owner is null or c.owner.id = :userId")
    Page<Category> findVisibleTo(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            select count(c) > 0 from Category c
            where lower(c.name) = lower(:name)
              and (c.owner is null or c.owner.id = :userId)
              and (:excludeId is null or c.id <> :excludeId)
            """)
    boolean nameTakenFor(@Param("name") String name, @Param("userId") Long userId, @Param("excludeId") Long excludeId);

    List<Category> findByOwnerId(Long ownerId);
}
