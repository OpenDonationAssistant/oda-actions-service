package io.github.opendonationassistant.api;

import io.github.opendonationassistant.commons.Amount;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public interface ActionControllerApi {
  @Get("/actions")
  @Secured(SecurityRule.IS_ANONYMOUS)
  @Operation(
    summary = "Get actions",
    description = "Retrieves actions filtered by recipientId and optional parameters"
  )
  @ApiResponse(
    responseCode = "200",
    description = "Actions retrieved successfully",
    content = @Content(
      mediaType = "application/json",
      array = @ArraySchema(schema = @Schema(implementation = ActionDto.class))
    )
  )
  default HttpResponse<List<ActionDto>> getActions(
    @Parameter(description = "Owner recipient ID") @Nonnull @QueryValue(
      value = "recipientId",
      defaultValue = ""
    ) String recipientId,
    @Parameter(description = "Filter by game") @QueryValue(
      value = "game"
    ) @Nullable String game,
    @Parameter(description = "Filter by enabled status") @QueryValue(
      value = "enabled"
    ) @Nullable Boolean enabled,
    @Parameter(description = "Filter by category") @QueryValue(
      value = "category"
    ) @Nullable String category,
    @Parameter(description = "Filter by provider") @QueryValue(
      value = "provider"
    ) @Nullable String provider
  ) {
    return execute(recipientId, game, enabled, category, provider);
  }

  HttpResponse<List<ActionDto>> execute(
    String recipientId,
    @Nullable String game,
    @Nullable Boolean enabled,
    @Nullable String category,
    @Nullable String provider
  );

  @Serdeable
  record ActionDto(
    String id,
    String name,
    Amount price,
    String category,
    String game,
    Boolean enabled,
    Map<String, Object> payload
  ) {
    public static ActionDto from(
      io.github.opendonationassistant.repository.ActionData data
    ) {
      return new ActionDto(
        data.id(),
        data.name(),
        data.amount(),
        Optional.ofNullable(data.category()).orElse(""),
        Optional.ofNullable(data.game()).orElse(""),
        data.enabled(),
        data.payload()
      );
    }
  }
}
