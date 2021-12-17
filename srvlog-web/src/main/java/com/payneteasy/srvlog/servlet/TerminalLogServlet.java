package com.payneteasy.srvlog.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payneteasy.srvlog.data.LogData;
import com.payneteasy.srvlog.service.ILogCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("terminalLogServlet")
public class TerminalLogServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(TerminalLogServlet.class);

    private static final String SELECT_ALL_VALUE = "All";

    private int logsToLoadNumber = 100_000;

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private static ILogCollector logCollector;

    public void setLogCollector(ILogCollector logCollector) {
        TerminalLogServlet.logCollector = logCollector;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        String logsToLoadParamValue = getServletContext().getInitParameter("terminal-logs-to-load-number");
        if (Objects.nonNull(logsToLoadParamValue)) logsToLoadNumber = Integer.parseInt(logsToLoadParamValue);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {

            String requestBody = getRequestBody(request);

            LOG.debug(requestBody);

            LogRequest logRequest = jsonMapper.readValue(requestBody, LogRequest.class);

            List<LogData> latestLogData = logCollector.loadLatest(
                    logsToLoadNumber, SELECT_ALL_VALUE.equalsIgnoreCase(logRequest.getHostId()) ?
                            null : Long.valueOf(logRequest.getHostId())
            )
                    .stream()
                    .filter(
                            !SELECT_ALL_VALUE.equalsIgnoreCase(logRequest.getHostName()) ?
                                    logData -> logData.getHost().equalsIgnoreCase(logRequest.getHostName()) : logData -> true
                    )
                    .filter(
                            !SELECT_ALL_VALUE.equalsIgnoreCase(logRequest.getProgramName()) ?
                                    logData -> logRequest.getProgramName().equalsIgnoreCase(logData.getProgram()) : logData -> true
                    )
                    .filter(
                            logData -> Objects.isNull(logRequest.getLogId()) || logData.getId().compareTo(logRequest.getLogId()) > 0
                    )
                    .sorted((l1, l2) -> {
                        int dateComparisonResult = l1.getDate().compareTo(l2.getDate());
                        if (dateComparisonResult != 0) {
                            return dateComparisonResult;
                        } else {
                            return l1.getId().compareTo(l2.getId());
                        }
                    })
                    .collect(Collectors.toList());

            String responseText = jsonMapper.writeValueAsString(latestLogData);

            LOG.debug("Response text: {}", responseText);

            response.getWriter().write(responseText);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().flush();

        } catch (Exception e) {
            response.getWriter().write("Internal server error");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().flush();
        }
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

        private String hostId;
        private String hostName;
        private String programName;
        private Long logId;

        public String getHostId() {
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
