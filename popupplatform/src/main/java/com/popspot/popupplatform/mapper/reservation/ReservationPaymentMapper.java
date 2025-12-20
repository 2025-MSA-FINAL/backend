package com.popspot.popupplatform.mapper.reservation;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReservationPaymentMapper {

    boolean existsByMerchantUid(@Param("merchantUid") String merchantUid);

    void insertPending(
            @Param("merchantUid") String merchantUid,
            @Param("paymentId") String paymentId,
            @Param("popId") Long popId,
            @Param("ptsId") Long ptsId,
            @Param("userId") Long userId,
            @Param("amount") int amount
    );

    void markPaid(
            @Param("merchantUid") String merchantUid
    );

    void markFailed(
            @Param("merchantUid") String merchantUid
    );

    // ✅ 결제 검증용(서버가 PortOne에서 조회한 paidAmount와 비교)
    Integer selectAmountByMerchantUid(@Param("merchantUid") String merchantUid);

    void updateReservationId(
            @Param("merchantUid") String merchantUid,
            @Param("reservationId") Long reservationId
    );

    int markCancelledByReservationId(@Param("reservationId") Long reservationId);

    String selectPaymentIdByReservationId(@Param("reservationId") Long reservationId);


}
