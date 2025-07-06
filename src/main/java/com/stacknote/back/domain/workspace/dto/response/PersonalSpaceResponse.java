package com.stacknote.back.domain.workspace.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 개인 공간 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "개인 공간 정보")
public class PersonalSpaceResponse {

    @Schema(description = "워크스페이스 ID")
    private Long workspaceId;

    @Schema(description = "워크스페이스 이름", example = "김철수의 워크스페이스")
    private String name;

    @Schema(description = "워크스페이스 아이콘", example = "👤")
    private String icon;

    @Schema(description = "페이지 목록")
    @Builder.Default
    private List<PageTreeResponse> pages = new ArrayList<>();

    @Schema(description = "전체 페이지 수")
    private int totalPageCount;
}