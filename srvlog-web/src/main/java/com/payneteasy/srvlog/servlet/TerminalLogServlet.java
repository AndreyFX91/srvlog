package com.payneteasy.srvlog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payneteasy.srvlog.data.LogData;
import com.payneteasy.srvlog.service.ILogCollector;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TerminalLogServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(TerminalLogServlet.class);

    private static final int LAST_LOG_NUMBER = 1;
    private static final String SELECT_ALL_VALUE = "All";

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    @SpringBean
    private ILogCollector logCollector;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestBody = getRequestBody(request);

        LOG.debug(requestBody);
        //TODO impl
        LogRequest logRequest = jsonMapper.readValue(requestBody, LogRequest.class);

        List<LogData> latestLogData = logCollector.loadLatest(LAST_LOG_NUMBER, logRequest.getHostId())/*generateMockProgramsLogDataList()*/.stream()
                .filter(!SELECT_ALL_VALUE.equalsIgnoreCase(logRequest.getHostName()) ? logData -> logData.getHost().equalsIgnoreCase(logRequest.getHostName()) : logData -> true)
                .filter(!SELECT_ALL_VALUE.equalsIgnoreCase(logRequest.getProgramName()) ? logData -> logData.getProgram().equalsIgnoreCase(logRequest.getProgramName()) : logData -> true)
                .filter(logData -> Objects.isNull(logRequest.getLogId()) || logData.getId().compareTo(logRequest.getLogId()) > 0)
                .sorted((l1, l2) -> l2.getDate().compareTo(l1.getDate()))
                .collect(Collectors.toList());

        String responseText = jsonMapper.writeValueAsString(latestLogData);

        LOG.debug("Response text: {}", responseText);

        response.getWriter().write(responseText);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().flush();
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

        for (int i = 0; i < LAST_LOG_NUMBER; i++) {

            LogData logToAdd = new LogData();

            logToAdd.setDate(new Date());
            logToAdd.setHost(hostName);
            logToAdd.setMessage(String.format("log message from host %s and program %s", hostName, programName));
            logToAdd.setProgram(programName);
            mockLogList.add(logToAdd);
        }

        return mockLogList;
    }

    private String getRequestBody(HttpServletRequest aRequest) {

        String result = null;
        try {
            result = getString(aRequest.getInputStream());
        } catch (IOException e){
            LOG.error(e.getMessage(), e);
        }

        return result;
    }

    private String getString(InputStream inputStream) {

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                char[] charBuffer = new char[128];
                int bytesRead;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }

        return stringBuilder.toString();
    }

    private static class LogRequest {

        private Long hostId;
        private String hostName;
        private String programName;
        private Long logId;

        public Long getHostId() {
            return hostId;
        }

        public String getHostName() {
            return hostName;
        }

        public String getProgramName() {
            return programName;
        }

        public Long getLogId() {
            return logId;
        }
    }
}
