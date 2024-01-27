package com.aosinin.intellij.tasks.cas;

import com.aosinin.intellij.tasks.cas.resource.CASTaskBundle;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskBundle;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.generic.GenericRepository;
import com.intellij.tasks.generic.GenericRepositoryUtil;
import com.intellij.tasks.generic.ResponseType;
import com.intellij.tasks.generic.TemplateVariable;
import com.intellij.util.net.HTTPMethod;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Tag("CASGeneric")
public class CASGenericRepository extends GenericRepository {

    @SuppressWarnings({"UnusedDeclaration"})
    public CASGenericRepository() {
        super();
    }

    public CASGenericRepository(TaskRepositoryType type) {
        super(type);
    }

    public CASGenericRepository(GenericRepository other) {
        super(other);
    }

    @Override
    public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed) throws Exception {
        if (StringUtil.isEmpty(getTasksListUrl())) {
            throw new Exception(TaskBundle.message("task.list.url.configuration.parameter.is.mandatory"));
        }
        loginCAS();

        String requestUrl = GenericRepositoryUtil.substituteTemplateVariables(getTasksListUrl(), getAllTemplateVariables());
        String responseBody = executeMethod(getHttpMethod(requestUrl, getTasksListMethodType()));
        Task[] tasks = getActiveResponseHandler().parseIssues(responseBody, offset + limit);
        if (getResponseType() == ResponseType.TEXT) {
            return tasks;
        }
        if (StringUtil.isNotEmpty(getSingleTaskUrl()) && getDownloadTasksInSeparateRequests()) {
            for (int i = 0; i < tasks.length; i++) {
                tasks[i] = findTask(tasks[i].getId());
            }
        }
        return tasks;
    }

    public void loginCAS() throws Exception {
        // request the authorization page in order to further receive the execution parameter
        String loginRequestURL = getCASServerURL() + "login";
        String requestUrl = GenericRepositoryUtil.substituteTemplateVariables(loginRequestURL, getAllTemplateVariables());
        String htmlPage = executeMethod(getHttpMethod(requestUrl, HTTPMethod.GET));
        if (authorized(htmlPage)) {
            return;
        }

        // prepare and send a post request for authorization
        PostMethod postMethod = new PostMethod(requestUrl);
        List<TemplateVariable> allTemplateVariables = getAllTemplateVariables();
        for (TemplateVariable variable : allTemplateVariables) {
            if (GenericRepository.USERNAME.equals(variable.getName()) || GenericRepository.PASSWORD.equals(variable.getName())) {
                postMethod.addParameter(variable.getName(), variable.getValue());
            }
        }
        postMethod.addParameter("_eventId", "submit");
        postMethod.addParameter("execution", getExecutionParameter(htmlPage));
        configureHttpMethod(postMethod);

        htmlPage = executeMethod(postMethod);
        if (!authorized(htmlPage)) {
            throw new Exception(CASTaskBundle.message("login.error"));
        }
    }

    /**
     * Returns the {@code execution} parameter from the authorization page
     *
     * @param htmlPage html code of the page containing the parameter
     * @return execution parameter
     * @throws Exception if the parameter was not found
     */
    private String getExecutionParameter(String htmlPage) throws Exception {
        Pattern pattern = Pattern.compile("name=\"execution\" value=\"(.+?)\"");
        Matcher matcher = pattern.matcher(htmlPage);
        if (!matcher.find()) {
            throw new Exception(CASTaskBundle.message("login.error.execution"));
        }
        return matcher.group(1);
    }

    public void logoutCAS() throws Exception {
        String logoutRequestURL = getCASServerURL() + "logout";
        String requestUrl = GenericRepositoryUtil.substituteTemplateVariables(logoutRequestURL, getAllTemplateVariables());
        executeMethod(getHttpMethod(requestUrl, HTTPMethod.GET));
    }

    /**
     * Checks whether the user is authorized.
     * To understand from the content of the page that we are authorized, we are tied to a specific id.
     *
     * @param htmlPage login page
     * @return true - the user is authorized, false - not
     */
    private boolean authorized(String htmlPage) {
        return htmlPage.contains("id=\"principalId\"");
    }

    private String executeMethod(HttpMethod method) throws Exception {
        String responseBody;
        getHttpClient().executeMethod(method);
        if (method.getStatusCode() != HttpStatus.SC_OK) {
            throw new Exception(TaskBundle.message("failure.http.error", method.getStatusCode(), method.getStatusText()));
        }

        Header contentType = method.getResponseHeader("Content-Type");
        if (contentType != null && contentType.getValue().contains("charset")) {
            // ISO-8859-1 if charset wasn't specified in response
            responseBody = StringUtil.notNullize(method.getResponseBodyAsString());
        } else {
            InputStream stream = method.getResponseBodyAsStream();
            if (stream == null) {
                responseBody = "";
            } else {
                try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    responseBody = StreamUtil.readText(reader);
                }
            }
        }
        return responseBody;
    }

    private HttpMethod getHttpMethod(String requestUrl, HTTPMethod type) {
        HttpMethod method = type == HTTPMethod.GET
                ? new GetMethod(requestUrl)
                : GenericRepositoryUtil.getPostMethodFromURL(requestUrl);
        configureHttpMethod(method);
        return method;
    }

    @Nullable
    @Override
    public CancellableConnection createCancellableConnection() {
        return new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
                getIssues("", 0, 1, false);
            }

            @Override
            public void cancel() {
            }
        };
    }

    @Override
    public @NotNull CASGenericRepository clone() {
        return new CASGenericRepository(this);
    }

    public String getCASServerURL() {
        // to avoid rewriting the parent form, the Login URL field is used as the CAS server URL
        String url = getLoginUrl();
        return url.endsWith("/") ? url : url + "/";
    }
}
