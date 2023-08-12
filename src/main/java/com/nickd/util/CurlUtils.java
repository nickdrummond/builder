package com.nickd.util;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;

public class CurlUtils {

    private static final Logger logger = LoggerFactory.getLogger(CurlUtils.class);

    public static void curl(IRI iri, File output) throws IOException {

        logger.info("Fetching: " + iri);

        ProcessBuilder pb = new ProcessBuilder(
                "curl",
                "-s",
                iri.toString());

        pb.directory(output.getParentFile());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        InputStream is = p.getInputStream();

        FileOutputStream outputStream = new FileOutputStream(output);

        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] bytes = new byte[100];
        int numberByteReaded;
        while ((numberByteReaded = bis.read(bytes, 0, 100)) != -1) {
            outputStream.write(bytes, 0, numberByteReaded);
            Arrays.fill(bytes, (byte) 0);
        }

        outputStream.flush();
        outputStream.close();
    }
}
