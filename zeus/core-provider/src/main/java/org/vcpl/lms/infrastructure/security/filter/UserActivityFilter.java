package org.vcpl.lms.infrastructure.security.filter;



import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.scheduledjobs.service.BounceChargeSchedulerServiceImpl;


import java.io.IOException;
@Service
public class UserActivityFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(UserActivityFilter.class);
    @Autowired
   private PlatformSecurityContext platformSecurityContext;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if(!httpRequest.getRequestURI().contains("auth") && !(httpRequest.getRequestURI().contains("jwt")) && !(httpRequest.getRequestURI().contains("notifications")) && !(httpRequest.getRequestURI().contains("enable_business_date")))
        {
                LOG.info("User Id "+platformSecurityContext.authenticatedUser().getId()+"| Method "+httpRequest.getMethod()+"| URL "+httpRequest.getRequestURI());
        }
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
