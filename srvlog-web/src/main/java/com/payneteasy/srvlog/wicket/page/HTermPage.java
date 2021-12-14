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
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Secured("ROLE_ADMIN")
public class HTermPage extends BasePage {

    private static final int LOGS_ANALYSIS_QUANTITY = 100_000;

    private static final int TEST_LAST_LOG_NUMBER = 1000;

    @SpringBean
    private ILogCollector logCollector;

    public HTermPage(PageParameters pageParameters) {

        super(pageParameters, HTermPage.class);
        final HTermFilterModel filterModel = new HTermFilterModel();

        Form<HTermFilterModel> hostChoiceForm = new Form<>("hostChoice-form");
        add(hostChoiceForm);

        List<HostData> hosts = logCollector.loadHosts();
        List<LogData> lastLogs = new ArrayList<>();

        for (HostData hostData : hosts) {
            List<LogData> lastHostLogs = generateMockProgramsLogDataList();//logCollector.loadLatest(LOGS_ANALYSIS_QUANTITY, hostData.getId());
            lastLogs.addAll(lastHostLogs);
        }

        Set<String> programSet = lastLogs.stream().map(LogData::getProgram).collect(Collectors.toSet());

        DropDownChoice<HostData> hostChoices = new DropDownChoice<HostData>("choices-host", new PropertyModel<HostData>(filterModel, "hostData"), new LoadableDetachableModel<List<HostData>>() {
            @Override
            protected List<HostData> load() {
                return hosts;
            }
        }, new ChoiceRenderer<>("hostname"));

        hostChoices.setNullValid(true);
        hostChoiceForm.add(hostChoices);

        Form<HTermFilterModel> programChoiceForm = new Form<>("programChoice-form");
        add(programChoiceForm);

        DropDownChoice<String> programChoices = new DropDownChoice<>("choices-program", new PropertyModel<String>(filterModel, "programName"), new LoadableDetachableModel<List<String>>() {
            @Override
            protected List<String> load() {
                return new ArrayList<>(programSet);
            }
        }, new ChoiceRenderer<>());

        programChoices.setNullValid(true);
        programChoiceForm.add(programChoices);
    }

    public static class HTermFilterModel implements Serializable {

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

    private List<LogData> generateMockProgramsLogDataList() {

        List<LogData> fullLogList = new ArrayList<>();

        List<LogData> host1prog1LogList = generateMockProgramLogDataList("host1", "prog-1");
        List<LogData> host1prog2LogList = generateMockProgramLogDataList("host1", "prog-2");
        List<LogData> host2prog1LogList = generateMockProgramLogDataList("host2", "prog-1");
        List<LogData> host2prog2LogList = generateMockProgramLogDataList("host2", "prog-2");

        fullLogList.addAll(host1prog1LogList);
        fullLogList.addAll(host1prog2LogList);
        fullLogList.addAll(host2prog1LogList);
        fullLogList.addAll(host2prog2LogList);

        return fullLogList;
    }

    private List<LogData> generateMockProgramLogDataList(String hostName, String programName) {

        List<LogData> mockLogList = new ArrayList<>();

        for (int i = 0; i < TEST_LAST_LOG_NUMBER; i++) {

            LogData logToAdd = new LogData();

            logToAdd.setDate(new Date());
            logToAdd.setHost(hostName);
            logToAdd.setMessage(String.format("log message from host %s and program %s", hostName, programName));
            logToAdd.setProgram(programName);
            mockLogList.add(logToAdd);
        }

        return mockLogList;
    }
}
