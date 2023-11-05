package com.nickd.wiki;

import com.nickd.builder.Constants;
import com.nickd.util.CurlUtils;
import org.semanticweb.owlapi.model.IRI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class WikiCache {

    public InputStream getFromWebOrCache(IRI iri) throws IOException {
        File cacheFile = cacheFileFor(iri);
        if (!cacheFile.exists()) {
            CurlUtils.curl(iri, cacheFile);
        }
        return new FileInputStream(cacheFile);
    }

    private File cacheFileFor(IRI iri) {
        return new File(Constants.CACHES + Wiki.pageName(iri) + ".html");
    }
}
