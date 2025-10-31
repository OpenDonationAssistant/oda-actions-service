package io.github.opendonationassistant.view;

import io.github.opendonationassistant.commons.Amount;
import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionData;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Map;

@Controller
public class ActionController extends BaseController {

  private final ActionRepository repository;

  @Inject
  public ActionController(ActionRepository repository) {
    this.repository = repository;
  }

  @Get("/actions")
  @Secured(SecurityRule.IS_ANONYMOUS)
  public HttpResponse<List<ActionDto>> getActions(
    @Nonnull @QueryValue(
      value = "recipientId",
      defaultValue = ""
    ) String recipientId,
    @QueryValue(value = "game") @Nullable String game,
    @QueryValue(value = "enabled") @Nullable Boolean enabled,
    @QueryValue(value = "category") @Nullable String category,
    @QueryValue(value = "provider") @Nullable String provider
  ) {
    if (recipientId.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    return HttpResponse.ok(
      repository
        .findAll((root, builder) -> {
          return builder.and(
            builder.equal(root.get("recipient_id"), recipientId),
            game != null ? builder.equal(root.get("game"), game) : null,
            enabled != null
              ? builder.equal(root.get("enabled"), enabled)
              : null,
            category != null
              ? builder.equal(root.get("category"), category)
              : null,
            provider != null
              ? builder.equal(root.get("provider"), provider)
              : null
          );
        })
        .stream()
        .map(Action::data)
        .map(ActionDto::from)
        .toList()
    );
  }

  @Serdeable
  public static record ActionDto(
    String id,
    String name,
    Amount price,
    String category,
    String game,
    Boolean enabled,
    Map<String, Object> payload
  ) {
    public static ActionDto from(ActionData data) {
      return new ActionDto(
        data.id(),
        data.name(),
        data.amount(),
        data.category(),
        data.game(),
        data.enabled(),
        data.payload()
      );
    }
  }
}
