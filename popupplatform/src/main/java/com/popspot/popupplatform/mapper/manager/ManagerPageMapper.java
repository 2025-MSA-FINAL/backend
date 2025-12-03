package com.popspot.popupplatform.mapper.manager;

import com.popspot.popupplatform.dto.popup.response.PopupListItemResponse;
import com.popspot.popupplatform.dto.user.response.ManagerProfileResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ManagerPageMapper {

    /**
     * 1. 매니저 프로필 조회
     */
    Optional<ManagerProfileResponse> findProfileByUserId(@Param("userId") Long userId);

    /**
     * 2. 내가 등록한 팝업 목록 조회
     */
    List<PopupListItemResponse> findMyPopups(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("status") String status,
            @Param("sort") String sort
    );

    /**
     * 3. 내가 등록한 팝업 전체 개수
     */
    long countMyPopups(
            @Param("userId") Long userId,
            @Param("status") String status
    );
}