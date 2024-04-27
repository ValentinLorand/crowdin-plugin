package com.googlecode.crowdin.maven.dao;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

class CrowdinFileDAOTest {

    private static CrowdinFileDAO crowdinDAO;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(7777));

    @BeforeEach
    public void setUp() {
        crowdinDAO = new CrowdinFileDAOImpl("http://localhost:7777","111", "secretApiKey");
        wireMockRule.start();
    }

    @Test
    void testGetDirectoryIdByName() {
        wireMockRule.stubFor(
                get("/projects/111/directories?filter=net.ihe.gazelle.tm.gazelle-tm-war")
                        .withHeader("Authorization", equalTo("Bearer secretApiKey"))
                .willReturn(ok()
                        .withBody("{\"data\":[{\"data\":{\"id\":\"123456\"}}]}"))
        );
        String folderId = crowdinDAO.getFolderIdByName("net.ihe.gazelle.tm.gazelle-tm-war");
        Assertions.assertFalse(folderId.isEmpty());
        Assertions.assertEquals("123456", folderId);
    }

    @Test
    void testCreateDirectory() {
        wireMockRule.stubFor(post("/projects/111/directories")
                        .withHeader("Authorization", equalTo("Bearer secretApiKey"))
                        .withRequestBody(equalToJson("{\"name\":\"net.ihe.gazelle.tm.gazelle-tm-war\"}"))
                .willReturn(ok().withBody("{\"data\":{\"id\":\"newDirectoryId\"}}"))
        );
        String directoryId = crowdinDAO.createFolder("net.ihe.gazelle.tm.gazelle-tm-war");
        Assertions.assertEquals("newDirectoryId", directoryId);
    }


    @Test
    void testCreateFileInDirectory() {
        wireMockRule.stubFor(post("/storages")
                .withHeader("Authorization", equalTo("Bearer secretApiKey"))
                .withHeader("Content-Type", equalTo("text/plain"))
                        .withRequestBody(equalTo("content"))
                .willReturn(created().withBody("{\"data\":{\"id\":\"newStorageId\"}}"))
        );
        wireMockRule.stubFor(post("/projects/111/files")
                .withHeader("Authorization", equalTo("Bearer secretApiKey"))
                .withRequestBody(equalTo("{\"storageId\":\"newStorageId\",\"name\":\"fileName\",\"directoryId\":\"1\"}"))
                .willReturn(created())
        );

        crowdinDAO.createFile("1","fileName","content");
//        verify(1, postRequestedFor(urlEqualTo("/storages")));
    }
}
