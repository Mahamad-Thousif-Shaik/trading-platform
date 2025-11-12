package com.thousif.trading.repository;

import com.thousif.trading.entity.Order;
import com.thousif.trading.entity.User;
import com.thousif.trading.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);

    Optional<Order> findByKiteOrderId(String kiteOrderId);

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserAndStatus(User user, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.createdAt BETWEEN :StartDate AND :endDate")
    List<Order> findByUserOrdersBetweenDates(@Param("user") User user,
                                             @Param("startDate")LocalDateTime startDate,
                                             @Param("endDate")LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user AND o.status = :status")
    long countUserOrdersByStatus(@Param("user") User user, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE status IN :status ORDER BY o.createdAt ASC")
    List<Order> findPendingOrders(@Param("statuses") List<OrderStatus> statuses);
}
