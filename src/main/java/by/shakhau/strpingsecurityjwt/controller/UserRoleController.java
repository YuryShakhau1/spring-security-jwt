package by.shakhau.strpingsecurityjwt.controller;

import by.shakhau.strpingsecurityjwt.domain.model.UserRole;
import by.shakhau.strpingsecurityjwt.security.UserPrincipal;
import by.shakhau.strpingsecurityjwt.service.UserRoleService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/users/roles")
@AllArgsConstructor
public class UserRoleController {

    private UserRoleService userRoleService;

    @AllArgsConstructor
    @Getter
    public static class FindUserRoleRequest {
        private Long userId;
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

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping(value = "/fetch", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Flux<UserRole> findUserRoles(@RequestBody FindUserRoleRequest request) {
        return Flux.defer(() -> Flux.fromIterable(userRoleService.findByUserId(request.getUserId())))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
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
    @DeleteMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
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
}
