package com.stacknote.back.domain.workspace.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 사이드바 전체 구조 응답 DTO
 * 노션 스타일의 사이드바에 필요한 모든 데이터를 포함
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사이드바 전체 구조")
public class WorkspaceSidebarResponse {

    @Schema(description = "개인 공간 정보")
    private PersonalSpaceResponse personalSpace;

    @Schema(description = "팀 워크스페이스 목록")
    @Builder.Default
    private List<TeamSpaceResponse> teamSpaces = new ArrayList<>();

    @Schema(description = "최근 방문 페이지 목록")
    @Builder.Default
    private List<RecentPageResponse> recentPages = new ArrayList<>();

    @Schema(description = "즐겨찾기 페이지 목록")
    @Builder.Default
    private List<FavoritePageResponse> favoritePages = new ArrayList<>();
}