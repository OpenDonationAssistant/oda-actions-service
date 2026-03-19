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
public interface DeleteActionsApi {
  @Post("/actions/commands/delete-actions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    summary = "Delete actions",
    description = "Deletes actions by their IDs, category, or provider"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Actions successfully deleted",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = DeleteActionsResult.class)
    )
  )
  default CompletableFuture<HttpResponse<DeleteActionsResult>> deleteActions(
    @Body DeleteActionsCommand command,
    Authentication auth
  ) {
    return execute(command, auth);
  }

  @Post("/actions/commands/deleteActions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Hidden
  default CompletableFuture<
    HttpResponse<DeleteActionsResult>
  > deprecatedDeleteActions(
    @Body DeleteActionsCommand command,
    Authentication auth
  ) {
    return execute(command, auth);
  }

  CompletableFuture<HttpResponse<DeleteActionsResult>> execute(
    DeleteActionsCommand command,
    Authentication auth
  );

  @Serdeable
  record DeleteActionsResult(
    Boolean success,
    String message,
    List<String> ids
  ) {}

  @Serdeable
  record DeleteActionsCommand(
    @Nullable String category,
    @Nullable String provider,
    @Nullable List<String> ids
  ) {}
}
