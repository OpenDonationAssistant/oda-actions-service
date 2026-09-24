package io.github.opendonationassistant.api;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.api.ActionControllerApi.ActionDto;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public interface AddActionApi {
  @Post("/actions/commands/add-actions")
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Operation(
    summary = "Add actions",
    description = "Add new actions to the system"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Actions successfully created",
    content = @Content(
      mediaType = "application/json",
      schema = @Schema(implementation = AddActionResult.class)
    )
  )
  public default CompletableFuture<
    HttpResponse<List<AddActionResult>>
  > addAction(@Valid @Body AddActionsCommand command, Authentication auth) {
    return execute(command, auth);
  }

  @Post("/actions/commands/addActions")
  @Hidden
  @Secured(SecurityRule.IS_AUTHENTICATED)
  public default CompletableFuture<
    HttpResponse<List<AddActionResult>>
  > deprecatedAddAction(@Valid @Body AddActionsCommand command, Authentication auth) {
    return execute(command, auth);
  }

  CompletableFuture<HttpResponse<List<AddActionResult>>> execute(
    AddActionsCommand command,
    Authentication auth
  );

  @Serdeable
  public static record AddActionsCommand(
    @Valid @NotNull List<NewAction> actions
  ) {}

  @Serdeable
  public static record AddActionResult(
    Boolean success,
    String message,
    ActionDto action
  ) {}

  @Serdeable
  public static record ActionParameter(
    @NotBlank String name,
    String displayName,
    String type
  ) {}

  @Serdeable
  public static record NewAction(
    @NotBlank String name,
    @NotBlank String category,
    String game,
    @NotNull Amount price,
    @NotNull Map<String, Object> payload,
    @Valid List<ActionParameter> parameters
  ) {}
}
