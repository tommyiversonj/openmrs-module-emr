package org.openmrs.module.emr.fragment.controller.visit;

import org.junit.Test;
import org.openmrs.*;
import org.openmrs.module.emr.TestUiUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class VisitDetailsFragmentControllerTest {

    @Test
    public void shouldReturnEncountersForVisit() throws ParseException {
        Visit visit = new Visit();
        Location visitLocation = new Location();
        visitLocation.setName("Visit Location");
        visit.setLocation(visitLocation);
        visit.setStartDatetime(new Date());
        visit.setStopDatetime(new Date());
        Location encounterLocation = new Location();
        encounterLocation.setName("Location");
        EncounterType encounterType = new EncounterType();
        encounterType.setName("Encounter Type");
        Provider provider = new Provider();
        provider.setName("Provider");
        EncounterProvider encounterProvider = new EncounterProvider();
        encounterProvider.setProvider(provider);
        encounterProvider.setEncounterRole(new EncounterRole());

        Encounter encounter = new Encounter();
        encounter.setEncounterId(7);
        encounter.setEncounterDatetime(new Date());
        encounter.setLocation(encounterLocation);
        encounter.setEncounterType(encounterType);
        encounter.setEncounterProviders(new LinkedHashSet<EncounterProvider>());
        encounter.getEncounterProviders().add(encounterProvider);

        visit.addEncounter(encounter);

        UiUtils uiUtils = new TestUiUtils();
        VisitDetailsFragmentController controller = new VisitDetailsFragmentController();

        SimpleObject response = controller.getVisitDetails(visit, uiUtils);
        List<SimpleObject> actualEncounters = (List<SimpleObject>) response.get("encounters");
        SimpleObject actualEncounter = actualEncounters.get(0);

        assertThat(response.get("startDatetime"), notNullValue());
        assertThat(response.get("stopDatetime"), notNullValue());
        assertThat((String) response.get("location"), is("Visit Location"));

        assertThat(actualEncounters.size(), is(1));
        assertThat((Integer) actualEncounter.get("encounterId"), is(7));
        assertThat((String) actualEncounter.get("location"), is("Location"));
        assertThat((String) actualEncounter.get("encounterType"), is("Encounter Type"));
        assertThat(actualEncounter.get("encounterDatetime"), notNullValue());
        assertThat(actualEncounter.get("encounterDate"), notNullValue());
        assertThat(actualEncounter.get("encounterTime"), notNullValue());
        List<SimpleObject> actualProviders = (List<SimpleObject>) actualEncounter.get("encounterProviders");
        assertThat(actualProviders.size(), is(1));
        assertThat((String) actualProviders.get(0).get("provider"), is("Provider"));
    }
}