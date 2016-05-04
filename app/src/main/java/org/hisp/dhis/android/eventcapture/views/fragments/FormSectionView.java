package org.hisp.dhis.android.eventcapture.views.fragments;

import org.hisp.dhis.android.eventcapture.views.View;
import org.hisp.dhis.client.sdk.ui.models.FormSection;

import java.util.List;

public interface FormSectionView extends View {
    void showFormSections(List<FormSection> formSections);

    void showTitle(String title);
}
