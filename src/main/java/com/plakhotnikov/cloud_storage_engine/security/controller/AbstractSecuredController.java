package com.plakhotnikov.cloud_storage_engine.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Абстрактный контроллер, обеспечивающий доступ к текущему пользователю.
 *
 * @see SecurityContextHolder
 */
public abstract class AbstractSecuredController {
    /**
     * Возвращает имя текущего аутентифицированного пользователя.
     *
     * @return Имя пользователя или null, если пользователь не аутентифицирован.
     */
    protected String getUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? null : auth.getName();
    }
}
