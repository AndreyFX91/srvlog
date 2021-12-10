package com.payneteasy.srvlog.wicket.page;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.access.annotation.Secured;

@Secured("ROLE_ADMIN")
public class HTermPage extends BasePage {

    public HTermPage(PageParameters pageParameters) {
        super(pageParameters, HTermPage.class);
    }
}
