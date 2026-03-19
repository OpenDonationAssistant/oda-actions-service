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
public interface EnableActionsApi {
  @Post("/actions/commands/enable-actions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    summary = "Enable actions",
    description = "Enables actions by their IDs, category, provider, or game"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Actions successfully enabled",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = EnableActionsResult.class)
    )
  )
  default CompletableFuture<HttpResponse<EnableActionsResult>> enableActions(
    @Body EnableActionsCommand command,
    Authentication auth
  ) {
    return execute(command, auth);
  }

  @Post("/actions/commands/enableActions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Hidden
  default CompletableFuture<
    HttpResponse<EnableActionsResult>
  > deprecatedEnableActions(
    @Body EnableActionsCommand command,
    Authentication auth
  ) {
    return execute(command, auth);
  }

  CompletableFuture<HttpResponse<EnableActionsResult>> execute(
    EnableActionsCommand command,
    Authentication auth
  );

  @Serdeable
  record EnableActionsResult(
    Boolean success,
    String message,
    List<String> ids
  ) {}

  @Serdeable
  record EnableActionsCommand(
    @Nullable String category,
    @Nullable String provider,
    @Nullable String game,
    @Nullable List<String> ids
  ) {}
}
