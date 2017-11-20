package io.github.zy31415.pdfsplitter;

import com.google.common.io.Files;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;

public class PdfProcessor {

    final static Logger logger = LogManager.getLogger();

    private String inputFilePath = null;
    private String outputFilePath = null;

    private String tempWorkDir = null;

    private String fileOutlineRemoved = null;

    private String leftWindowFile = null;
    private String rightWindowFile = null;

    public PdfProcessor(String inputFilePath, String outputFilePath) throws IOException {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;

        tempWorkDir = Files.createTempDir().toString();
        logger.info("Temporory working dir is {}", tempWorkDir);

        fileOutlineRemoved = Paths.get(tempWorkDir, "outline_removed.pdf").toString();

        leftWindowFile = Paths.get(tempWorkDir, "left_window.pdf").toString();
        rightWindowFile = Paths.get(tempWorkDir, "right_window.pdf").toString();

        removeOutline();

        applyWindow(true);
        applyWindow(false);
        merge();
    }

    private void removeOutline() throws IOException {
        PdfReader reader = new PdfReader(inputFilePath);
        PdfWriter writer = new PdfWriter(fileOutlineRemoved);
        PdfDocument pdfDoc = new PdfDocument(reader, writer);

        PdfOutline outlines = pdfDoc.getOutlines(true);
        outlines.getContent().clear();
        outlines.getAllChildren().clear();

        pdfDoc.close();
        writer.close();
        reader.close();

    }

    private void applyWindow(Boolean isLeft) throws IOException {
        PdfReader reader = new PdfReader(fileOutlineRemoved);

        PdfWriter writer = isLeft ? new PdfWriter(leftWindowFile) : new PdfWriter(rightWindowFile);

        PdfDocument pdfDoc = new PdfDocument(reader, writer);

        int numPages = pdfDoc.getNumberOfPages();

        for (int i = 1; i <= numPages; i++) {
            PdfPage page = pdfDoc.getPage(i);

            Rectangle mediaBox = page.getMediaBox();
            logger.info("[Page {}] Media Box: LLX - {}, LLY - {}, Height- {}, Width - {}.",
                    i, mediaBox.getX(), mediaBox.getY(), mediaBox.getHeight(), mediaBox.getWidth());

            Rectangle cropBox = page.getCropBox();

            if (null != cropBox) {
                logger.info("[Page {}] Crop Box: LLX - {}, LLY - {}, Height- {}, Width - {}.",
                        i, cropBox.getX(), cropBox.getY(), cropBox.getHeight(), cropBox.getWidth());
            } else {
                logger.info("Crop Box is null.");
            }

            Rectangle newCropBox = isLeft ? getLeftWindow(mediaBox) : getRightWindow(mediaBox);

            page.setCropBox(newCropBox);
            logger.info("[Page {}] After crop, crop box becomes: LLX - {}, LLY - {}, Height- {}, Width - {}.",
                    i, page.getCropBox().getX(), page.getCropBox().getY(), page.getCropBox().getHeight(), page.getCropBox().getWidth());
        }

        pdfDoc.close();
        writer.close();
        reader.close();
    }

    private Rectangle getLeftWindow(Rectangle rect) {
        return new Rectangle(rect.getX(), rect.getY(), rect.getWidth()/2F, rect.getHeight());
    }

    private Rectangle getRightWindow(Rectangle rect) {
        float width = rect.getWidth()/2F;
        return new Rectangle(rect.getX() + width, rect.getY(), rect.getWidth(), rect.getHeight());
    }

    private void merge() throws IOException {

        PdfReader reader1 = new PdfReader(leftWindowFile);
        PdfDocument doc1 = new PdfDocument(reader1);

        PdfReader reader2 = new PdfReader(rightWindowFile);
        PdfDocument doc2 = new PdfDocument(reader2);

        PdfWriter writer = new PdfWriter(outputFilePath);
        PdfDocument mergedDoc = new PdfDocument(writer);

        int numPages = doc1.getNumberOfPages();

        PdfMerger merger = new PdfMerger(mergedDoc);

        for (int nth = 1; nth <= numPages; nth++) {
            merger.merge(doc1, nth, nth);
            merger.merge(doc2, nth, nth);
        }

        mergedDoc.close();
        writer.close();

        doc2.close();
        reader2.close();

        doc1.close();
        reader1.close();

    }

}
