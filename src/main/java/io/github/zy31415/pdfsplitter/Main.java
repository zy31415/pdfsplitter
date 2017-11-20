package io.github.zy31415.pdfsplitter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class Main {

    final static Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws IOException, InternalError {
        PdfProcessor processor = new PdfProcessor("temp/norton.pdf", "temp/norton_split.pdf");
    }
}
