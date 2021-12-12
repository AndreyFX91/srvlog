package com.payneteasy.srvlog.servlet;

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

public class TerminalLogServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(TerminalLogServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOG.debug(getRequestBody(request));
        //TODO impl
    }

    public static String getRequestBody(HttpServletRequest aRequest) {

        String result = null;
        try {
            result = getString(aRequest.getInputStream());
        } catch (IOException e){
            LOG.error(e.getMessage(), e);
        }

        return result;
    }

    private static String getString(InputStream inputStream) {

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
}
