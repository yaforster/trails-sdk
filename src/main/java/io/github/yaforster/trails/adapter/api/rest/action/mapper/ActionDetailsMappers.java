package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import java.util.List;

/** Provides the complete, closed REST mapping catalogue for Trails Actions. */
public final class ActionDetailsMappers {

    private ActionDetailsMappers() {}

    public static List<ActionDetailsVariantMapper> all() {
        return List.of(new CheckExistenceActionDetailsMapper(), new ClickActionDetailsMapper(),
                new CoordinateClickActionDetailsMapper(), new SelectionActionDetailsMapper(),
                new ResizeViewportActionDetailsMapper(), new TypingActionDetailsMapper(),
                new SwitchWebsiteActionDetailsMapper(), new TextCheckActionDetailsMapper(),
                new SessionStorageSearchActionDetailsMapper(), new LocalStorageSearchActionDetailsMapper(),
                new CookieSearchActionDetailsMapper(), new ExplicitWaitActionDetailsMapper(),
                new ElementValueCheckActionDetailsMapper(), new DownloadedFileCheckActionDetailsMapper(),
                new DownloadedDocumentTextCheckActionDetailsMapper(), new MoveViewportActionDetailsMapper());
    }

}
