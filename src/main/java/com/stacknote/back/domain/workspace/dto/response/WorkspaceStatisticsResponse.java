package com.stacknote.back.domain.workspace.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 워크스페이스 통계 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "워크스페이스 통계 정보")
public class WorkspaceStatisticsResponse {

    @Schema(description = "전체 워크스페이스 수")
    private long totalWorkspaces;

    @Schema(description = "소유한 워크스페이스 수")
    private long ownedWorkspaces;

    @Schema(description = "참여한 워크스페이스 수")
    private long sharedWorkspaces;

    @Schema(description = "전체 페이지 수")
    private long totalPages;

    @Schema(description = "전체 멤버 수")
    private long totalMembers;

    @Schema(description = "활성 워크스페이스 수")
    private long activeWorkspaces;

    @Schema(description = "공개 워크스페이스 수")
    private long publicWorkspaces;

    @Schema(description = "비공개 워크스페이스 수")
    private long privateWorkspaces;

    @Schema(description = "최근 7일간 생성된 워크스페이스 수")
    private long recentlyCreatedCount;

    @Schema(description = "가장 활발한 워크스페이스 ID")
    private Long mostActiveWorkspaceId;

    @Schema(description = "가장 활발한 워크스페이스 이름")
    private String mostActiveWorkspaceName;
}