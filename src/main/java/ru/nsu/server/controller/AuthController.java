package ru.nsu.server.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.nsu.server.configuration.security.jwt.JwtUtils;
import ru.nsu.server.model.user.RefreshToken;
import ru.nsu.server.payload.requests.AuthRequest;
import ru.nsu.server.payload.response.JwtAuthResponse;
import ru.nsu.server.payload.response.MessageResponse;
import ru.nsu.server.services.RefreshTokenService;
import ru.nsu.server.services.UserDetailsImpl;
import ru.nsu.server.services.UserService;

import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api")
@Tag(name = "00. Auth controller", description = "Контроллер авторизации, в котором человек может получить свой jwt и refresh токен.")
public class AuthController {

    private final AuthenticationManager authenticationManager;


    private final JwtUtils jwtUtils;

    private final UserService userService;

    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthController(
            AuthenticationManager authenticationManager,
            UserService userService,
            JwtUtils jwtUtils,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    @Operation(
            summary = "Запрос на авторизацию в аккаунт.",
            description = """
                    Проверяются данные пользователя и выдаётся jwt с рефреш токеном.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = JwtAuthResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Пользователя с такой почтой не существует.", content = {@Content(schema = @Schema(implementation = MessageResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", content = @Content)})
    @PostMapping("/auth")
    @Transactional
    public ResponseEntity<?> auth(@Valid @RequestBody AuthRequest authRequest) {
        String userEmail = authRequest.getEmail();

        if (!userService.existByEmailCheck(userEmail)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Пожалуйста, проверьте введённые данные."));
        }

        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(userEmail, authRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String access_token = jwtUtils.generateJwtToken(userDetails);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
            long expires_in = refreshToken.getExpiryDate().getEpochSecond() - Instant.now().getEpochSecond();

            var user = userService.findById(userDetails.getId());

            List<String> userStringRoles = new ArrayList<>();
            for (var role : user.getRoles()) {
                userStringRoles.add(role.toString());
            }
            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse(access_token, refreshToken.getToken(),
                    "Bearer", expires_in, userStringRoles);

            return ResponseEntity.ok(jwtAuthResponse);
        } catch (AuthenticationException e) {
            log.info("Auth error : {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Ошибка! Пожалуйста, проверьте введённые данные."));
        }
    }
}
