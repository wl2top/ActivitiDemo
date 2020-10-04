package com.wl.top.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WL
 * @date 2020/10/3
 */
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class MyDruidConfig {
    private String name;
    private String username;
    private String password;
    private String url;
    private String driverClassName;
    private int initialSize;
    private int minIdle;
    private int maxActive;
    private int maxWait;
    private int timeBetweenEvictionRunsMillis;
    private int minEvictableIdleTimeMillis;
    private String validationQuery;
    private boolean testWhileIdle;
    private boolean testOnBorrow;
    private boolean testOnReturn;
    private boolean poolPreparedStatements;
    private int maxPoolPreparedStatementPerConnectionSize;
    private String filters;

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean isPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public int getMaxPoolPreparedStatementPerConnectionSize() {
        return maxPoolPreparedStatementPerConnectionSize;
    }

    public void setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
        this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }

    @Bean
    @Primary
    public DataSource dataSource() throws SQLException {
        DruidDataSource dds = new DruidDataSource();
        dds.setName(this.name);
        dds.setDriverClassName(this.driverClassName);
        dds.setUrl(this.url);
        dds.setUsername(this.username);
        dds.setPassword(this.password);
        dds.setInitialSize(this.initialSize);
        dds.setMinIdle(this.minIdle);
        dds.setMaxActive(this.maxActive);
        dds.setMaxWait(this.maxWait);
        dds.setTimeBetweenEvictionRunsMillis(this.timeBetweenEvictionRunsMillis);
        dds.setMinEvictableIdleTimeMillis(this.minEvictableIdleTimeMillis);
        dds.setValidationQuery(this.validationQuery);
        dds.setTestWhileIdle(this.testWhileIdle);
        dds.setTestOnBorrow(this.testOnBorrow);
        dds.setTestOnReturn(this.testOnReturn);
        dds.setPoolPreparedStatements(this.poolPreparedStatements);
        dds.setMaxPoolPreparedStatementPerConnectionSize(this.maxPoolPreparedStatementPerConnectionSize);
        dds.setFilters(this.filters);
        return dds;
    }

    //配置Druid的监控
    //1、配置一个管理后台的Servlet
    @Bean
    public ServletRegistrationBean statViewServlet() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
        Map<String, String> initParams = new HashMap<>();

        initParams.put("loginUsername", "admin");
        initParams.put("loginPassword", "admin");
        //默认允许所有访问
        initParams.put("allow", "");
        //拒绝192.169.15.21IP访问
        initParams.put("deny", "192.168.15.21");

        bean.setInitParameters(initParams);
        return bean;
    }


    //2、配置一个web监控的filter，用于设置哪些请求被监控，哪些被排除在外
    @Bean
    public FilterRegistrationBean webStatFilter() {
        //创建过滤器
        FilterRegistrationBean bean = new FilterRegistrationBean(new WebStatFilter());
        //设置对哪些请求url进行过滤
        //方式一 -- add形式(单个Url)
        bean.addUrlPatterns("/*");
        //方式二 -- set形式(Url集合)
        //bean.setUrlPatterns(Arrays.asList("/*"));

        //设置忽略哪些请求Url（设置对哪些请求Url不过滤）
        //方式一 -- add形式
        bean.addInitParameter("exclusions", "*.jpg,*.png,*.gif,/druid/*");
        //方式二 -- set形式
        //Map<String,String> initParams = new HashMap<>();
        //initParams.put("exclusions","*.js,*.css,/druid/*");
        //bean.setInitParameters(initParams)；

        return bean;
    }
}
