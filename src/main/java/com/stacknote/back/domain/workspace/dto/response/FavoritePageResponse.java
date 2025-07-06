package com.stacknote.back.domain.workspace.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 페이지 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "즐겨찾기 페이지 정보")
public class FavoritePageResponse {

    @Schema(description = "페이지 ID")
    private Long pageId;

    @Schema(description = "페이지 제목")
    private String title;

    @Schema(description = "페이지 아이콘", example = "⭐")
    private String icon;

    @Schema(description = "워크스페이스 ID")
    private Long workspaceId;

    @Schema(description = "워크스페이스 이름")
    private String workspaceName;

    @Schema(description = "즐겨찾기 등록 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime favoritedAt;
}