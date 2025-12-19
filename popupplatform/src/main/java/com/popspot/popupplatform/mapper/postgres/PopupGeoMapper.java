package com.popspot.popupplatform.mapper.postgres;

import com.popspot.popupplatform.domain.popup.PopupStore;
import com.popspot.popupplatform.dto.popup.response.PopupNearbyItemResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PopupGeoMapper {

    List<PopupNearbyItemResponse> selectNearbyPopups(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm,
            @Param("limit") int limit
    );

    void upsertPopupGeo(@Param("p") PopupStore popup);



    void softDeletePopupGeo(@Param("popId") Long popId);


    void bulkUpsertPopupGeo(@org.apache.ibatis.annotations.Param("list") List<PopupStore> list);

}
