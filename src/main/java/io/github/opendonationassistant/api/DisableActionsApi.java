package io.github.opendonationassistant.api;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Controller
public interface DisableActionsApi {
  @Post("/actions/commands/disable-actions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    summary = "Disable actions",
    description = "Disables actions by their IDs, category, provider, or game"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Actions successfully disabled",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = DisableActionsResult.class)
    )
  )
  default CompletableFuture<HttpResponse<DisableActionsResult>> disableActions(
    @Body DisableActionsCommand command,
    Authentication auth
  ) {
    return execute(command, auth);
  }

  @Post("/actions/commands/disableActions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Hidden
  default CompletableFuture<
    HttpResponse<DisableActionsResult>
  > deprecatedDisableActions(
    @Body DisableActionsCommand command,
    Authentication auth
  ) {
    return execute(command, auth);
  }

  CompletableFuture<HttpResponse<DisableActionsResult>> execute(
    DisableActionsCommand command,
    Authentication auth
  );

  @Serdeable
  record DisableActionsResult(
    Boolean success,
    String message,
    List<String> ids
  ) {}

  @Serdeable
  record DisableActionsCommand(
    @Nullable String category,
    @Nullable String provider,
    @Nullable String game,
    @Nullable List<String> ids
  ) {}
}
