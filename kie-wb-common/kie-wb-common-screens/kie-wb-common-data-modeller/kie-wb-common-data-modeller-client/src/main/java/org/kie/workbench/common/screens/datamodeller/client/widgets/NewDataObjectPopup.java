/**
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datamodeller.client.widgets;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import org.kie.workbench.common.screens.datamodeller.client.DataModelerContext;
import org.kie.workbench.common.screens.datamodeller.client.util.DataModelerUtils;
import org.kie.workbench.common.screens.datamodeller.client.validation.ValidatorService;
import org.kie.workbench.common.screens.datamodeller.client.resources.i18n.Constants;
import org.kie.workbench.common.screens.datamodeller.client.validation.ValidatorCallback;
import org.kie.workbench.common.screens.datamodeller.events.DataModelerEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectCreatedEvent;
import org.kie.workbench.common.screens.datamodeller.model.AnnotationDefinitionTO;
import org.kie.workbench.common.screens.datamodeller.model.DataModelTO;
import org.kie.workbench.common.screens.datamodeller.model.DataObjectTO;
import org.kie.workbench.common.widgets.client.popups.footers.ModalFooterOKCancelButtons;
import org.uberfire.workbench.events.NotificationEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;


public class NewDataObjectPopup extends Modal {

    interface NewDataObjectPopupUIBinder extends
            UiBinder<Widget, NewDataObjectPopup> {

    }

    private static NewDataObjectPopupUIBinder uiBinder = GWT.create(NewDataObjectPopupUIBinder.class);

    @UiField
    ControlGroup nameGroup;

    @UiField
    TextBox name;

    @UiField
    TextBox label;

    @UiField
    ControlGroup newPackageGroup;

    @UiField
    TextBox newPackage;

    @UiField
    PackageSelector packageSelector;

    @UiField
    SuperclassSelector superclassSelector;

    @UiField
    ControlGroup errorMessagesGroup;

    @UiField
    HelpInline errorMessages;

    @Inject
    private Event<DataModelerEvent> dataModelerEvent;

    @Inject
    private ValidatorService validatorService;

    @Inject
    private Event<NotificationEvent> notification;

    private DataObjectTO dataObject;

    private DataModelerContext context;

    public NewDataObjectPopup() {

        setTitle(Constants.INSTANCE.new_dataobject_popup_title());
        setMaxHeigth((Window.getClientHeight() * 0.75) + "px");
        setBackdrop( BackdropType.STATIC );
        setKeyboard( true );
        setAnimation( true );
        setDynamicSafe( true );
        //setHideOthers( false );

        add( uiBinder.createAndBindUi( this ) );

        add( new ModalFooterOKCancelButtons(
                (new Command() {
                    @Override
                    public void execute() {
                        onOk();
                    }
                }),
                (new Command() {
                    @Override
                    public void execute() {
                        onCancel();
                    }
                })
        ));

        packageSelector.enableCreatePackage(false);
        final ListBox packageList = packageSelector.getPackageList();
        packageList.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                String selectedValue = packageList.getValue();
                if (!PackageSelector.NOT_SELECTED.equals(selectedValue)) {
                    newPackage.setText(selectedValue);
                } else {
                    newPackage.setText("");
                }
            }
        });
    }

    public DataModelerContext getContext() {
        return context;
    }

    public void setContext(DataModelerContext context) {
        this.context = context;
        superclassSelector.setContext(context);
        packageSelector.setContext(context);
    }

    private DataModelTO getDataModel() {
        return getContext() != null ? getContext().getDataModel() : null;
    }

    private void onOk() {

        final String newName[] = new String[1];
        final String newLabel[] = new String[1];
        final String newPackageName[] = new String[1];
        final String superClass[] = new String[1];

        newName[0] = name.getText() != null ? name.getText().trim() : "";
        newLabel[0] = label.getText() != null ? label.getText().trim() : "";
        newPackageName[0] = newPackage.getText() != null && !"".equals(newPackage.getText().trim()) ?  newPackage.getText().trim() : null;

        superClass[0] = superclassSelector.getSuperclassList().getValue();
        if (SuperclassSelector.NOT_SELECTED.equals(superClass[0])) superClass[0] = null;

        cleanErrors();

        //1) validate className
        validatorService.isValidIdentifier(newName[0], new ValidatorCallback() {
            @Override
            public void onFailure() {
                setErrorMessage(nameGroup, Constants.INSTANCE.validation_error_invalid_object_identifier(newName[0]));
            }

            @Override
            public void onSuccess() {

                //2) if classname is ok, validate the package name.
                if (newPackageName[0] != null) {
                    validatorService.isValidPackageIdentifier(newPackageName[0], new ValidatorCallback() {
                        @Override
                        public void onFailure() {
                            setErrorMessage(newPackageGroup, Constants.INSTANCE.validation_error_invalid_package_identifier(newPackageName[0]));
                        }

                        @Override
                        public void onSuccess() {
                            validatorService.isUniqueEntityName(newPackageName[0], newName[0], getDataModel(), new ValidatorCallback() {
                                @Override
                                public void onFailure() {
                                    setErrorMessage(nameGroup,  Constants.INSTANCE.validation_error_object_already_exists(newName[0], newPackageName[0]));
                                }

                                @Override
                                public void onSuccess() {
                                    createDataObject(newPackageName[0], newName[0], newLabel[0], superClass[0]);
                                    clean();
                                    hide();
                                }
                            });

                        }
                    });
                } else {
                    validatorService.isUniqueEntityName(newPackageName[0], newName[0], getDataModel(), new ValidatorCallback() {
                        @Override
                        public void onFailure() {
                            setErrorMessage(nameGroup, Constants.INSTANCE.validation_error_object_already_exists(newName[0], ""));
                        }

                        @Override
                        public void onSuccess() {
                            createDataObject(newPackageName[0], newName[0], newLabel[0], superClass[0]);
                            clean();
                            hide();
                        }
                    });
                }
            }
        });

    }

    private void createDataObject(String packageName, String name, String label, String superClass) {
        DataObjectTO dataObject = new DataObjectTO(name, packageName, superClass);
        if (label != null && !"".equals(label)) {
            dataObject.addAnnotation(getContext().getAnnotationDefinitions().get(AnnotationDefinitionTO.LABEL_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, label);
        }
        getDataModel().getDataObjects().add(dataObject);
        notifyObjectCreated(dataObject);
    }

    private void clean() {
        name.setText("");
        label.setText("");
        newPackage.setText("");
        cleanErrors();
    }

    private void cleanErrors() {
        errorMessages.setText("");
        nameGroup.setType(ControlGroupType.NONE);
        newPackageGroup.setType(ControlGroupType.NONE);
        errorMessagesGroup.setType(ControlGroupType.NONE);
    }

    private void setErrorMessage(ControlGroup controlGroup, String errorMessage) {
        controlGroup.setType(ControlGroupType.ERROR);
        errorMessages.setText(errorMessage);
        errorMessagesGroup.setType(ControlGroupType.ERROR);
    }

    private void onCancel() {
        clean();
        hide();
    }

    private void notifyObjectCreated(DataObjectTO createdObjectTO) {
        getContext().getHelper().dataObjectCreated(createdObjectTO.getClassName());
        dataModelerEvent.fire(new DataObjectCreatedEvent(DataModelerEvent.NEW_DATA_OBJECT_POPUP, getDataModel(), createdObjectTO));
        notification.fire(new NotificationEvent(Constants.INSTANCE.modelEditor_notification_dataObject_created(DataModelerUtils.getDataObjectUILabel(createdObjectTO))));
    }

}
