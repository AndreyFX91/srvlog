package com.payneteasy.srvlog.wicket.page;

import com.payneteasy.srvlog.data.HostData;
import com.payneteasy.srvlog.service.ILogCollector;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.springframework.security.access.annotation.Secured;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Secured("ROLE_ADMIN")
public class HTermPage extends BasePage {

    @SpringBean
    private ILogCollector logCollector;
    private final WebMarkupContainer holderListView;

    public HTermPage(PageParameters pageParameters) {

        super(pageParameters, HTermPage.class);
        final HTermFilterModel filterModel = new HTermFilterModel();

        //Hosts choice form
        Form<HTermFilterModel> hostChoiceForm = new Form<>("hostChoice-form");
        add(hostChoiceForm);
        DropDownChoice<HostData> hostChoices = new DropDownChoice<HostData>("choices-host",new PropertyModel<HostData>(filterModel, "hostData"), new LoadableDetachableModel<List<HostData>>() {
            @Override
            protected List<HostData> load() {
                return logCollector.loadHosts();
            }
        }, new ChoiceRenderer<>("hostname"));
        hostChoices.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(holderListView);
            }
        });
        hostChoices.setNullValid(true);
        hostChoiceForm.add(hostChoices);

        //TODO procedure to get programs available for online log by hostname
        //Program choice form
        Form<HTermFilterModel> programChoiceForm = new Form<>("programChoice-form");
        add(programChoiceForm);
        DropDownChoice<String> programChoices = new DropDownChoice<>("choices-program", new PropertyModel<>(filterModel, "programData"), new LoadableDetachableModel<List<String>>() {
            @Override
            protected List<String> load() {
                return getMockProgramList();
            }
        }, new ChoiceRenderer<>());
        programChoices.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(holderListView);
            }
        });
        programChoices.setNullValid(true);
        programChoiceForm.add(programChoices);

        holderListView = new WebMarkupContainer("holder-search-log-data");
        holderListView.setOutputMarkupId(true);
        add(holderListView);
    }

    private List<String> getMockProgramList() {
        return Arrays.asList("prog-1", "prog-2", "prog-3");
    }

    public class HTermFilterModel implements Serializable {

        private HostData hostData;
        private String programData;

        public HostData getHostData() {
            return hostData;
        }

        public void setHostData(HostData hostData) {
            this.hostData = hostData;
        }

        public String getProgramData() {
            return programData;
        }

        public void setProgramData(String programData) {
            this.programData = programData;
        }
    }
}
