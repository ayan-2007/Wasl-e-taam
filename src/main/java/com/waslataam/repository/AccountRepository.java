package com.waslataam.repository;

import java.util.Optional;

/**
 * Data access for credentials and the administrator allowlist.
 */
public interface AccountRepository {
    void save(AccountRecord account);

    Optional<AccountRecord> findByEmail(String email);

    boolean existsByEmail(String email);

    void allowlistAdmin(String email, String note);

    boolean isAdminAllowlisted(String email);
}
