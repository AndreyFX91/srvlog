package com.payneteasy.srvlog.wicket.page;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.access.annotation.Secured;

@Secured("ROLE_ADMIN")
public class OnlineLogViewerMonitorPage extends BasePage {

    public OnlineLogViewerMonitorPage(PageParameters pageParameters) {
        super(pageParameters, OnlineLogViewerMonitorPage.class);
    }
}
