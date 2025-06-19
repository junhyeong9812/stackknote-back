package com.stacknote.back.domain.workspace.exception;

import com.stacknote.back.global.exception.ErrorCode;
import com.stacknote.back.global.exception.custom.EntityNotFoundException;

/**
 * 워크스페이스를 찾을 수 없을 때 발생하는 예외
 */
public class WorkspaceNotFoundException extends EntityNotFoundException {

    public WorkspaceNotFoundException(String message) {
        super(ErrorCode.WORKSPACE_NOT_FOUND, message);
    }

    public WorkspaceNotFoundException() {
        super(ErrorCode.WORKSPACE_NOT_FOUND, "워크스페이스를 찾을 수 없습니다.");
    }
}