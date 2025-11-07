package com.clinic.modules.core.blog;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<BlogEntity, Long> {

    Optional<BlogEntity> findBySlug(String slug);

    Optional<BlogEntity> findBySlugAndStatus(String slug, BlogStatus status);

    List<BlogEntity> findByStatus(BlogStatus status);

    List<BlogEntity> findByStatusAndLocale(BlogStatus status, String locale);

    List<BlogEntity> findByLocale(String locale);

    List<BlogEntity> findByStatusOrderByPublishedAtDesc(BlogStatus status);

    List<BlogEntity> findByStatusAndLocaleOrderByPublishedAtDesc(BlogStatus status, String locale);

    boolean existsBySlug(String slug);

    @Modifying
    @Query("UPDATE BlogEntity b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Query("""
            select b from BlogEntity b
            where lower(b.title) like lower(concat('%', :term, '%'))
               or lower(b.slug) like lower(concat('%', :term, '%'))
               or lower(coalesce(b.excerpt, '')) like lower(concat('%', :term, '%'))
            order by coalesce(b.publishedAt, b.createdAt) desc
            """)
    List<BlogEntity> searchBlogs(@Param("term") String term, Pageable pageable);
}
