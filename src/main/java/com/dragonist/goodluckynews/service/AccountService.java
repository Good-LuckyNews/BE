package com.dragonist.goodluckynews.service;


import com.dragonist.goodluckynews.config.JWTUtils;
import com.dragonist.goodluckynews.dto.IdTokenRequestDto;
import com.dragonist.goodluckynews.repository.AccountRepository;
import com.dragonist.goodluckynews.user.Account;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final JWTUtils jwtUtils;
    private final GoogleIdTokenVerifier verifier;

    public AccountService(@Value("${app.googleClientId}") String clientId, AccountRepository accountRepository,
                          JWTUtils jwtUtils) {
        this.accountRepository = accountRepository;
        this.jwtUtils = jwtUtils;
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public Account getAccount(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    public String loginOAuthGoogle(IdTokenRequestDto requestBody) {
        Account account = verifyIDToken(requestBody.getIdToken());
        if (account == null) {
            throw new IllegalArgumentException();
        }
        account = createOrUpdateUser(account);
        return jwtUtils.createToken(account, false);
    }

    @Transactional
    public Account createOrUpdateUser(Account account) {
        Account existingAccount = accountRepository.findByEmail(account.getEmail()).orElse(null);
        if (existingAccount == null) {
            account.setRoles("ROLE_USER");
            accountRepository.save(account);
            return account;
        }
        existingAccount.setFirstName(account.getFirstName());
        existingAccount.setLastName(account.getLastName());
        existingAccount.setPictureUrl(account.getPictureUrl());
        accountRepository.save(existingAccount);
        return existingAccount;
    }

    private Account verifyIDToken(String idToken) {
        try {
            GoogleIdToken idTokenObj = verifier.verify(idToken);
            if (idTokenObj == null) {
                return null;
            }
            GoogleIdToken.Payload payload = idTokenObj.getPayload();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String email = payload.getEmail();
            String pictureUrl = (String) payload.get("picture");

            return new Account(firstName, lastName, email, pictureUrl);
        } catch (GeneralSecurityException | IOException e) {
            return null;
        }
    }
}
