package com.sh.sh.pos.system.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sh.sh.pos.system.domain.StoreStatus;
import com.sh.sh.pos.system.model.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
	Store findByStoreAdminId(Long adminId);

	List<Store> findByStatus(StoreStatus storeStatus);

    @Query("""
            SELECT s FROM Store s
            LEFT JOIN FETCH s.storeAdmin
            WHERE s.id = :id
            """)
    Optional<Store> findByIdWithAdmin(@Param("id") Long id);

	@Query("""
        SELECT COUNT(s)
        FROM Store s
        WHERE DATE(s.createdAt) = :date
    """)
     Long countByDate(@Param("date") LocalDate date);

    @Query("""
    SELECT s.createdAt AS regDate, COUNT(s) AS count
    FROM Store s
    WHERE s.createdAt >= :startDate
    GROUP BY s.createdAt
    ORDER BY regDate ASC
""")
	List<Object[]> getStoreRegistrationStats(@Param("startDate") LocalDateTime startDate);
}
