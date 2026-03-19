package io.github.opendonationassistant.view;

import io.github.opendonationassistant.commons.micronaut.BaseController;
import io.github.opendonationassistant.repository.Action;
import io.github.opendonationassistant.repository.ActionData;
import io.github.opendonationassistant.repository.ActionRepository;
import io.micronaut.http.HttpResponse;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ActionController extends BaseController implements ActionControllerApi {

  private final ActionRepository repository;

  @Inject
  public ActionController(ActionRepository repository) {
    this.repository = repository;
  }

  public HttpResponse<List<ActionDto>> execute(
    @Nonnull String recipientId,
    @Nullable String game,
    @Nullable Boolean enabled,
    @Nullable String category,
    @Nullable String provider
  ) {
    if (recipientId.isEmpty()) {
      return HttpResponse.unauthorized();
    }
    return HttpResponse.ok(
      repository
        .findAll((root, builder) -> {
          final ArrayList<Predicate> conditions = new ArrayList<>();
          conditions.add(builder.equal(root.get("recipientId"), recipientId));
          if (game != null) {
            conditions.add(builder.equal(root.get("game"), game));
          }
          if (enabled != null) {
            conditions.add(builder.equal(root.get("enabled"), enabled));
          }
          if (category != null) {
            conditions.add(builder.equal(root.get("category"), category));
          }
          if (provider != null) {
            conditions.add(builder.equal(root.get("provider"), provider));
          }
          return builder.and(
            conditions.toArray(new Predicate[conditions.size()])
          );
        })
        .stream()
        .map(Action::data)
        .map(ActionDto::from)
        .toList()
    );
  }
}
