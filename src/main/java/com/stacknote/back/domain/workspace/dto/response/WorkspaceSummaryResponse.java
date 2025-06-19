package com.stacknote.back.domain.workspace.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 워크스페이스 요약 응답 DTO (목록용)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceSummaryResponse {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private String coverImageUrl;
    private String ownerName;
    private String visibility;
    private Boolean isActive;
    private String currentUserRole;
    private Long memberCount;
    private Long pageCount;

    /**
     * 워크스페이스 요약 정보 생성
     */
    public static WorkspaceSummaryResponse of(
            Long id, String name, String description, String icon, String coverImageUrl,
            String ownerName, String visibility, Boolean isActive, String currentUserRole,
            Long memberCount, Long pageCount) {
        return WorkspaceSummaryResponse.builder()
                .id(id)
                .name(name)
                .description(description)
                .icon(icon)
                .coverImageUrl(coverImageUrl)
                .ownerName(ownerName)
                .visibility(visibility)
                .isActive(isActive)
                .currentUserRole(currentUserRole)
                .memberCount(memberCount)
                .pageCount(pageCount)
                .build();
    }
}