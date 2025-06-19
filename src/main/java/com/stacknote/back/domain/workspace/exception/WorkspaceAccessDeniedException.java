package com.stacknote.back.domain.workspace.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.BusinessException;

/**
 * 워크스페이스 접근 권한이 없을 때 발생하는 예외
 */
public class WorkspaceAccessDeniedException extends BusinessException {

    public WorkspaceAccessDeniedException(String message) {
        super(ErrorCode.WORKSPACE_ACCESS_DENIED, message);
    }

    public WorkspaceAccessDeniedException() {
        super(ErrorCode.WORKSPACE_ACCESS_DENIED, "워크스페이스 접근 권한이 없습니다.");
    }
}