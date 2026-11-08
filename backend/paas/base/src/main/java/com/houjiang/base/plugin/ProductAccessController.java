package com.houjiang.base.plugin;

import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

/** Authentication is resolved by auth; org headers never establish tenant membership. */
@RestController
public class ProductAccessController {
    public record Identity(UserInfoVO user) {}
    private final ProductAccessService service;
    private final RestTemplate identityClient;
    public ProductAccessController(ProductAccessService service, @Qualifier("productIdentityClient") RestTemplate identityClient) {
        this.service=service; this.identityClient=identityClient;
    }
    @GetMapping("/internal/plugin-access/{code}")
    public ProductAccessService.Snapshot access(@PathVariable String code, @RequestHeader("Authorization") String token) {
        HttpHeaders headers = new HttpHeaders(); headers.set("Authorization",token);
        Identity identity = identityClient.exchange("http://auth/internal/session/identity",HttpMethod.GET,new HttpEntity<>(headers),Identity.class).getBody();
        if (identity == null || identity.user() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        UserInfoVO user = identity.user();
        String instance = user.getOrgId();
        if (instance == null || instance.isBlank()) {
            if (user.getUserId() == null || !user.getUserId().matches("[0-9]+")) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            instance = "u-"+user.getUserId();
        }
        try { return service.snapshot(code,instance); }
        catch (IllegalArgumentException unknown) { throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
    }
}
