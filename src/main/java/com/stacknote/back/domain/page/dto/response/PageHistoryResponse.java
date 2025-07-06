package com.stacknote.back.domain.page.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.stacknote.back.domain.page.entity.PageHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 페이지 히스토리 응답 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "페이지 히스토리 정보")
public class PageHistoryResponse {

    @Schema(description = "히스토리 ID")
    private Long id;

    @Schema(description = "버전 번호")
    private Integer version;

    @Schema(description = "페이지 제목")
    private String title;

    @Schema(description = "페이지 내용")
    private String content;

    @Schema(description = "수정한 사용자 ID")
    private Long modifiedById;

    @Schema(description = "수정한 사용자명")
    private String modifiedByName;

    @Schema(description = "변경 타입")
    private PageHistory.ChangeType changeType;

    @Schema(description = "변경 설명")
    private String changeDescription;

    @Schema(description = "생성 시간")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}