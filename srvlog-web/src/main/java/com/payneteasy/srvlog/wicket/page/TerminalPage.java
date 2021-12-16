package com.payneteasy.srvlog.wicket.page;

import com.payneteasy.srvlog.data.HostData;
import com.payneteasy.srvlog.data.LogData;
import com.payneteasy.srvlog.service.ILogCollector;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.access.annotation.Secured;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Secured("ROLE_ADMIN")
public class TerminalPage extends BasePage {

    private static final int LOGS_ANALYSIS_DEPTH = 100_000;

    @SpringBean
    private ILogCollector logCollector;

    public TerminalPage(PageParameters pageParameters) {

        super(pageParameters, TerminalPage.class);
        final TerminalFilterModel filterModel = new TerminalFilterModel();

        Form<TerminalFilterModel> hostChoiceForm = new Form<>("hostChoice-form");
        add(hostChoiceForm);

        List<HostData> hosts = logCollector.loadHosts();
        List<LogData> lastLogs = new ArrayList<>();

        for (HostData hostData : hosts) {
            List<LogData> lastHostLogs = logCollector.loadLatest(LOGS_ANALYSIS_DEPTH, hostData.getId());
            lastLogs.addAll(lastHostLogs);
        }

        Set<String> programSet = lastLogs.stream().map(LogData::getProgram).collect(Collectors.toSet());
        programSet.remove(null);

        DropDownChoice<HostData> hostChoices = new DropDownChoice<>(
                "choices-host",
                new PropertyModel<>(filterModel, "hostData"),
                new LoadableDetachableModel<List<HostData>>() {
                    @Override
                    protected List<HostData> load() {
                        return hosts;
                    }
                },
                new ChoiceRenderer<>("hostname", "id")
        );

        hostChoices.setNullValid(true);
        hostChoiceForm.add(hostChoices);

        Form<TerminalFilterModel> programChoiceForm = new Form<>("programChoice-form");
        add(programChoiceForm);

        DropDownChoice<String> programChoices = new DropDownChoice<>(
                "choices-program",
                new PropertyModel<>(filterModel, "programName"),
                new LoadableDetachableModel<List<String>>() {
                    @Override
                    protected List<String> load() {
                        return new ArrayList<>(programSet);
                    }
                },
                new ChoiceRenderer<>()
        );

        programChoices.setNullValid(true);
        programChoiceForm.add(programChoices);
    }

    public static class TerminalFilterModel implements Serializable {

        private HostData hostData;
        private String programName;

        public HostData getHostData() {
            return hostData;
        }

        public void setHostData(HostData hostData) {
            this.hostData = hostData;
        }

        public String getProgramName() {
            return programName;
        }

        public void setProgramName(String programName) {
            this.programName = programName;
        }
    }
}
