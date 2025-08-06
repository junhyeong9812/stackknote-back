package com.stacknote.back.domain.workspace.dto.response;

import com.stacknote.back.domain.workspace.entity.WorkspaceMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 팀 스페이스 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "팀 스페이스 정보")
public class TeamSpaceResponse {

    @Schema(description = "워크스페이스 ID")
    private Long workspaceId;

    @Schema(description = "워크스페이스 이름")
    private String name;

    @Schema(description = "워크스페이스 아이콘", example = "🏢")
    private String icon;

    @Schema(description = "워크스페이스 설명")
    private String description;

    @Schema(description = "공개 상태")
    private String visibility;

    @Schema(description = "멤버 수")
    private Integer memberCount;

    @Schema(description = "현재 사용자의 역할")
    private WorkspaceMember.Role currentUserRole;  // 필드명 변경 및 타입 변경

    @Schema(description = "페이지 목록")
    @Builder.Default
    private List<PageTreeResponse> pages = new ArrayList<>();

    @Schema(description = "전체 페이지 수")
    private Integer totalPageCount;

    @Schema(description = "확장 상태", defaultValue = "false")
    @Builder.Default
    private Boolean isExpanded = false;  // 새로운 필드 추가
}