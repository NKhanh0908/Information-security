package com.infomationsecurity.mfa.mapper;

import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public Account createDTOToEntity(AccountCreateDTO accountCreateDTO){
        return Account.builder()
                .accountUsername(accountCreateDTO.getUsername())
                .accountPassword(accountCreateDTO.getPassword())
                .accountEmail(accountCreateDTO.getEmail())
                .accountIsLocked(false)
                .build();
    }

    public AccountDTO entityToDTO(Account account) {
        return AccountDTO.builder()
                .accountId(account.getAccountId())
                .userId(account.getUser().getUserId())
                .accountUsername(account.getAccountUsername())
                .accountEmail(account.getAccountEmail())
                .accountIsLocked(account.getAccountIsLocked())
                .accountLockedTime(account.getAccountLockedTime() != null ? account.getAccountLockedTime() : null)
                .accountLastLogin(account.getAccountLastLogin() != null ? account.getAccountLastLogin() : null)
                .accountCreatedAt(account.getAccountCreatedAt())
                .accountUpdatedAt(account.getAccountUpdatedAt() != null ? account.getAccountUpdatedAt() : null)
                .build();
    }
}
