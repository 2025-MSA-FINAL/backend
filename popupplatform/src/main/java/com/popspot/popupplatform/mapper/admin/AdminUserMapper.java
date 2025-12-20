package com.popspot.popupplatform.mapper.admin;

import com.popspot.popupplatform.dto.admin.AdminUserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface AdminUserMapper {

        List<AdminUserDTO> getUserList(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("searchType") String searchType,
            @Param("offset") int offset,
            @Param("size") int size
    );


    List<AdminUserDTO> getManagerList(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("searchType") String searchType,
            @Param("offset") int offset,
            @Param("size") int size
    );


    int getUserCount(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("searchType") String searchType
    );

    int getManagerCount(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("searchType") String searchType
    );

    int updateUserStatus(
            @Param("userId") Long userId,
            @Param("status") String status

    );

    int updateUserRole(
            @Param("userId") Long userId,
            @Param("role") String role
    );
}