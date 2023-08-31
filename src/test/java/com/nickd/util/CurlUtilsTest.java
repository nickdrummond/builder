package com.nickd.util;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class CurlUtilsTest {

    @Test
    public void testCurlDownload() throws IOException {
        CurlUtils.curl(IRI.create("https://starwars.fandom.com/wiki/Kassa_(episode)"), new File("test.html"));
    }
}