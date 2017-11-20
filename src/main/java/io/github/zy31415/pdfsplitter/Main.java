package io.github.zy31415.pdfsplitter;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PageRange;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.kernel.utils.PdfSplitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


public class Main {

    final static Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws IOException, InternalError {

        Main main = new Main();
        main.removeOutline();

        main.duplicate();

//        main.split();

    }

    private void crop() throws IOException {
        PdfReader reader = new PdfReader("temp/norton.pdf");
        PdfWriter writer = new PdfWriter("temp/norton_new.pdf");
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

            Rectangle newCropBox = new Rectangle(mediaBox.getX(), mediaBox.getY(), mediaBox.getWidth()/2F, mediaBox.getHeight());

            page.setCropBox(newCropBox);
            logger.info("[Page {}] After crop, crop box becomes: LLX - {}, LLY - {}, Height- {}, Width - {}.",
                    i, page.getCropBox().getX(), page.getCropBox().getY(), page.getCropBox().getHeight(), page.getCropBox().getWidth());
        }

        pdfDoc.close();
        writer.close();
        reader.close();
    }

    private void duplicate() throws IOException {

        PdfReader reader2 = new PdfReader("temp/norton_outline.pdf");

        PdfWriter writer = new PdfWriter("temp/norton_merged.pdf");

        PdfDocument mergedDoc = new PdfDocument(writer);

        int numPages = 200;

        PdfMerger merger = new PdfMerger(mergedDoc);

        for (int nth = 1; nth <= numPages; nth++) {
            PdfReader reader1 = new PdfReader("temp/norton_outline.pdf");
            PdfDocument doc1 = new PdfDocument(reader1);
            PdfDocument doc2 = new PdfDocument(reader2);

            merger.merge(doc1, nth, nth);
            merger.merge(doc2, nth, nth);
            doc1.close();
            reader1.close();
//            doc2.close();

        }


//        reader2.close();
        mergedDoc.close();
        writer.close();
    }

    private void removeOutline() throws IOException {
        PdfReader reader = new PdfReader("temp/norton.pdf");
        PdfWriter writer = new PdfWriter("temp/norton_outline.pdf");
        PdfDocument pdfDoc = new PdfDocument(reader, writer);

        PdfOutline outlines = pdfDoc.getOutlines(true);
        outlines.getContent().clear();
        outlines.getAllChildren().clear();

//        PdfOutline outlines = pdfDoc.getOutlines(true);
//
//        outlines.getAllChildren().clear();

//        pdfDoc.initializeOutlines();
        pdfDoc.close();
        reader.close();
        writer.close();
    }

    private void split() throws IOException {
        PdfReader reader = new PdfReader("temp/norton_outline.pdf");
        PdfDocument pdfDoc = new PdfDocument(reader);
//        pdfDoc.initializeOutlines();

//        Document doc = new Document(pdfDoc);


        List<PdfDocument> splitDocuments = new PdfSplitter(pdfDoc) {
            int partNumber = 1;

            @Override
            protected PdfWriter getNextPdfWriter(PageRange documentPageRage) {
                try {
                    logger.info("partnumber: {}", partNumber);
                    return new PdfWriter("temp/" + "splitDocument1_" + String.valueOf(partNumber++) + ".pdf");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException();
                }
            }

//        }.splitBySize(200000);
        }.splitByPageCount(1);

        for (PdfDocument doc: splitDocuments)
            doc.close();

        pdfDoc.close();
        reader.close();

    }
}
