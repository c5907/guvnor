package org.kie.workbench.common.screens.explorer.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import org.kie.commons.validation.PortablePreconditions;
import org.kie.workbench.common.screens.explorer.client.ExplorerPresenter;
import org.kie.workbench.common.screens.explorer.client.resources.images.ImageResources;
import org.uberfire.backend.vfs.Path;

/**
 * A widget representing a project
 */
public class ProjectWidget extends BaseItemWidget {

    public ProjectWidget( final Path path,
                          final ExplorerPresenter presenter ) {
        super( path, presenter );
    }

    public ProjectWidget( final Path path,
                          final String caption,
                          final ExplorerPresenter presenter ) {
        super( path,
               caption,
               presenter );
        PortablePreconditions.checkNotNull( "path",
                                            path );
        anchor.addClickHandler( new ClickHandler() {

            @Override
            public void onClick( ClickEvent event ) {
                presenter.setContext( path );
            }
        } );
    }

    @Override
    public Image getImage() {
        return new Image( ImageResources.INSTANCE.projectIcon() );
    }

}
