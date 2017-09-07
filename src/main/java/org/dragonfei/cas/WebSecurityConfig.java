package org.dragonfei.cas;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

/**
 * Created by longfei on 17-9-7.
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    @Value("${server.port}")
    private String port;
    private String SSO_URL = "http://dragonfei.com:8080/cas-server-webapp-3.5.2/";

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.exceptionHandling().authenticationEntryPoint(getCasAuthenticationEntryPoint())
                .and().addFilter(casAuthenticationFilter())
                .addFilterBefore(singleSignOutFilter(),CasAuthenticationFilter.class)
                .addFilterBefore(logoutFilter(),LogoutFilter.class)
                .authorizeRequests()
                .antMatchers("/js/**","/css/**","/imgs/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/login").permitAll()
                .and().logout().permitAll();
    }

    public CasAuthenticationEntryPoint getCasAuthenticationEntryPoint(){
        CasAuthenticationEntryPoint point = new CasAuthenticationEntryPoint();
        point.setLoginUrl(SSO_URL + "login");
        point.setServiceProperties(serviceProperties());
        return point;
    }


    private ServiceProperties serviceProperties(){
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService("http://dragonfei.com:"+port+"/login/cas");
        serviceProperties.setSendRenew(false);
        return serviceProperties;
    }

    public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        filter.setAuthenticationManager(authenticationManager());
        return filter;
    }

    public SingleSignOutFilter singleSignOutFilter(){
        SingleSignOutFilter filter = new SingleSignOutFilter();
        filter.setCasServerUrlPrefix(SSO_URL);
        filter.setIgnoreInitConfiguration(true);
        return filter;
    }

    public LogoutFilter logoutFilter(){
        LogoutFilter logoutFilter = new LogoutFilter(SSO_URL + "logout?service=http://dragonfei.com:"+port+"/",
                new SecurityContextLogoutHandler());
        return logoutFilter;
    }

    @Bean
    public CasAuthenticationProvider casAuthenticationProvider(){
        CasAuthenticationProvider provider = new CasAuthenticationProvider();
        provider.setTicketValidator(cas20ServiceTicketValidator());
        provider.setServiceProperties(serviceProperties());
        provider.setKey("an_id_for_this_auth_provider_only");
        provider.setAuthenticationUserDetailsService(userDetailsByNameServiceWrapper());
        return provider;
    }

    /**
     * 当CAS认证成功时, Spring Security会自动调用此类对用户进行授权
     */
    private UserDetailsByNameServiceWrapper userDetailsByNameServiceWrapper() {
        UserDetailsByNameServiceWrapper wrapper = new UserDetailsByNameServiceWrapper();
        wrapper.setUserDetailsService(userDetailsService);
        return wrapper;
    }

    private Cas20ServiceTicketValidator cas20ServiceTicketValidator(){
        Cas20ServiceTicketValidator validator = new Cas20ServiceTicketValidator(SSO_URL);
        return validator;
    }

    /**
     * 用于实现单点登出功能
     */
    @Bean
    public ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> singleSignOutHttpSessionListener() {
        ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> listener = new ServletListenerRegistrationBean<>();
        listener.setEnabled(true);
        listener.setListener(new SingleSignOutHttpSessionListener());
        listener.setOrder(1);
        return listener;
    }
}
