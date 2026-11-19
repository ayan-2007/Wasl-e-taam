package com.waslataam.auth;

import com.waslataam.domain.Admin;
import com.waslataam.domain.User;
import com.waslataam.domain.UserRole;
import com.waslataam.repository.AccountRepository;
import com.waslataam.repository.AccountRecord;

/**
 * Defence-in-depth check for administrator sign-in.
 *
 * <p>An account may reach the admin workspace only when <em>all</em> of the
 * following are true:</p>
 * <ol>
 *   <li>the credentials row is flagged as an administrator,</li>
 *   <li>the domain user has the ADMIN role,</li>
 *   <li>the role is backed by an {@link Admin} object, and</li>
 *   <li>the email appears in the separately stored administrator allowlist.</li>
 * </ol>
 *
 * <p>The allowlist is what makes "only a few people" concrete: public sign-up
 * can never add an entry to it.</p>
 */
public final class AdminPolicy {

    private final AccountRepository accounts;

    public AdminPolicy(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public boolean isProvisionedAdmin(AccountRecord account, User user) {
        return account != null
                && account.admin()
                && user != null
                && user.getRole() == UserRole.ADMIN
                && user instanceof Admin
                && accounts.isAdminAllowlisted(account.email());
    }
}
