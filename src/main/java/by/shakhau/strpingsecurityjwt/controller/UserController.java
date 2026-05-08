package by.shakhau.strpingsecurityjwt.controller;

import by.shakhau.strpingsecurityjwt.controller.response.Pageable;
import by.shakhau.strpingsecurityjwt.controller.response.PageableResponse;
import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.security.UserPrincipal;
import by.shakhau.strpingsecurityjwt.service.UserRoleService;
import by.shakhau.strpingsecurityjwt.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private UserRoleService userRoleService;
    private UserService service;

    @AllArgsConstructor
    @Getter
    public static class RegisterUserRequest {
        private String firstName;
        private String lastName;
        private String email;
        private char[] password;
    }

    @AllArgsConstructor
    @Getter
    public static class SearchUsersRequest {
        private String firstName;
        private String lastName;
        private String email;
        private int pageNumber;
        private int pageSize;
    }

    @AllArgsConstructor
    @Getter
    public static class DeleteUserRequest {
        private Long userId;
    }

    @AllArgsConstructor
    @Getter
    public static class UpdateSuperAdminPasswordRequest {
        private String email;
        private char[] password;
    }

    @AllArgsConstructor
    @Getter
    public static class UpdateUserPasswordRequest {
        private char[] oldPassword;
        private char[] newPassword;
    }

    @AllArgsConstructor
    @Getter
    public static class UpdateUserRoleRequest {
        private Long userId;
        private Long existingRoleId;
        private Long expectedRoleId;
    }

    @AllArgsConstructor
    @Getter
    public static class DeleteUserRoleRequest {
        private Long userId;
        private Long roleId;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> register(@RequestBody RegisterUserRequest request) {
        return Mono.fromCallable(() -> {
                    if (service.createUser(new User(
                            null,
                            request.getFirstName(),
                            request.getLastName(),
                            request.getEmail(),
                            null), request.getPassword()) == null) {
                        Arrays.fill(request.getPassword(), '0');
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User already exists");
                    }

                    Arrays.fill(request.getPassword(), '0');
                    return ResponseEntity.status(HttpStatus.OK).<String>body(null);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Mono<PageableResponse<User>> searchByFilterPageable(@RequestBody SearchUsersRequest request) {
        return Mono.fromCallable(() -> {
                    Page<User> userPage = service.searchUsers(
                            request.getFirstName(),
                            request.getLastName(),
                            request.getEmail(),
                            PageRequest.of(request.getPageNumber(), request.getPageSize()));
                    return new PageableResponse<>(
                            userPage.getContent(),
                            new Pageable<>(
                                    userPage.getNumber(),
                                    userPage.getSize(),
                                    userPage.getTotalPages()));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    public Mono<User> find(Authentication authentication) {
        return Mono.fromCallable(() -> {
                    var principal = (UserPrincipal) authentication.getPrincipal();
                    return service.findById(principal.getId());
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PatchMapping(value = "/password/init", produces = APPLICATION_JSON_VALUE)
    public Mono<Void> updateSuperAdminPassword(@RequestBody UpdateSuperAdminPasswordRequest request) {
        return Mono.fromCallable(() -> {
                    if (service.updatePassword(request.getEmail(), request.getPassword())) {
                        Arrays.fill(request.getPassword(), '0');
                        return ServerResponse.ok().build();
                    }
                    Arrays.fill(request.getPassword(), '0');
                    return ServerResponse.notFound().build();
                })
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    @PatchMapping(value = "/password", produces = APPLICATION_JSON_VALUE)
    public Mono<Void> updatePassword(@RequestBody UpdateUserPasswordRequest request, Authentication authentication) {
        return Mono.fromCallable(() -> {
                    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                    if (service.updatePassword(principal.getId(), request.getOldPassword(), request.getNewPassword())) {
                        Arrays.fill(request.getOldPassword(), '0');
                        Arrays.fill(request.getNewPassword(), '0');
                        return ServerResponse.ok().build();
                    }
                    Arrays.fill(request.getOldPassword(), '0');
                    Arrays.fill(request.getNewPassword(), '0');
                    return ServerResponse.notFound().build();
                })
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public Mono<User> updateUser(@RequestBody User user, Authentication authentication) {
        return Mono.fromCallable(() -> {
                    var principal = (UserPrincipal) authentication.getPrincipal();
                    return service.updateUser(principal.getId(), user);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping(value = "/role", produces = APPLICATION_JSON_VALUE)
    public Mono<Void> updateUserRole(@RequestBody UpdateUserRoleRequest request, Authentication authentication) {
        return Mono.fromCallable(() -> {
                    var principal = (UserPrincipal) authentication.getPrincipal();
                    if (userRoleService.updateUserRole(
                            principal.getId(),
                            request.getUserId(),
                            request.getExistingRoleId(),
                            request.getExpectedRoleId())) {
                        return ServerResponse.ok().build();
                    }
                    return ServerResponse.status(HttpStatus.FORBIDDEN).build();
                })
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping(value = "/role", produces = APPLICATION_JSON_VALUE)
    public Mono<Void> deleteUserRole(@RequestBody DeleteUserRoleRequest request, Authentication authentication) {
        return Mono.fromCallable(() -> {
                    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                    if (userRoleService.deleteByUserId(principal.getId(), request.getUserId(), request.getRoleId())) {
                        return ServerResponse.ok().build();
                    }
                    return ServerResponse.status(HttpStatus.FORBIDDEN).build();
                })
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    @DeleteMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    public Mono<Void> deleteUser(Authentication authentication) {
        return Mono.fromRunnable(() -> {
                    var principal = (UserPrincipal) authentication.getPrincipal();
                    service.deleteById(principal.getId(), principal.getId());
                })
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping(value = "/", produces = APPLICATION_JSON_VALUE)
    public Mono<Void> deleteUserById(@RequestBody DeleteUserRequest request, Authentication authentication) {
        return Mono.fromRunnable(() -> {
                    var principal = (UserPrincipal) authentication.getPrincipal();
                    service.deleteById(principal.getId(), request.getUserId());
                })
                .subscribeOn(Schedulers.boundedElastic()).then();
    }
}
