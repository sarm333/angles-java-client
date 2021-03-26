package com.github.angleshq.angles.api.requests;

import com.github.angleshq.angles.api.exceptions.AnglesServerException;
import com.github.angleshq.angles.api.models.Platform;
import com.github.angleshq.angles.api.models.execution.Execution;
import com.github.angleshq.angles.api.models.screenshot.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class ScreenshotRequests extends BaseRequests {

    private String basePath = "screenshot";
    private Tika tika = new Tika();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public ScreenshotRequests(String baseUrl) {
        super(baseUrl);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public Screenshot create(CreateScreenshot createScreenshot) throws IOException, AnglesServerException {
        File screenShotFile = new File(createScreenshot.getFilePath());
        String mimeType = tika.detect(screenShotFile);
        HttpEntity entity = MultipartEntityBuilder
                .create()
                .addBinaryBody("screenshot", screenShotFile, ContentType.getByMimeType(mimeType), screenShotFile.getName())
                .build();

        Map<String, String> headers = new HashMap<>();
        headers.put("view", createScreenshot.getView());
        headers.put("buildId", createScreenshot.getBuildId());
        headers.put("timestamp", sdf.format(createScreenshot.getTimestamp()));

        CloseableHttpResponse response = sendMultiPartEntity(basePath, headers, entity);
        return processResponse(response, Screenshot.class);
    }

    public Screenshot[] get(String buildId, String view, Integer limit, Integer skip) throws IOException, URISyntaxException, AnglesServerException {
        Map<String, Object> parameters = new HashMap<>();
        if (buildId != null) { parameters.put("buildId", view); }
        if (view != null) { parameters.put("view", view); }
        if (limit != null) { parameters.put("limit", limit); }
        if (skip != null) { parameters.put("skip", skip); }
        CloseableHttpResponse response = sendJSONGet(basePath, parameters);
        return processResponse(response, Screenshot[].class);
    }

    public Screenshot get(String screenshotId) throws IOException, AnglesServerException {
        CloseableHttpResponse response = sendJSONGet(basePath + "/" + screenshotId);
        return processResponse(response, Screenshot.class);
    }

    public ImageCompareResponse baselineCompare(String screenshotId) throws IOException, AnglesServerException {
        CloseableHttpResponse response = sendJSONGet(basePath + "/" + screenshotId + "/baseline/compare");
        return processResponse(response, ImageCompareResponse.class);
    }

    public Boolean delete(String screenshotId) throws IOException, AnglesServerException {
        CloseableHttpResponse response = sendDelete(basePath + "/" + screenshotId);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return true;
        } else {
            processErrorResponse(response);
        }
        return false;
    }

    public Screenshot update(String screenshotId, UpdateScreenshot updateRequest) throws IOException, AnglesServerException {
        CloseableHttpResponse response = sendJSONPut(basePath + "/" + screenshotId, updateRequest);
        return processResponse(response, Screenshot.class);
    }
}
