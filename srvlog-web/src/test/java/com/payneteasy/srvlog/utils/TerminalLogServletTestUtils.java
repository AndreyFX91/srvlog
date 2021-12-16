package com.payneteasy.srvlog.utils;

import com.payneteasy.srvlog.data.LogData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TerminalLogServletTestUtils {

    private static final int LAST_LOG_NUMBER = 100_000;

    public static List<LogData> generateMockProgramsLogDataList() {

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

    private static List<LogData> generateMockProgramLogDataList(String hostName, String programName) {

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

}
