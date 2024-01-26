package com.yk.common.service.book;

import static com.yk.common.utils.XmlContentHelper.getXmlContentFromInputStream;

import android.util.Log;

import androidx.annotation.NonNull;

import com.yk.common.context.ApplicationContext;
import com.yk.common.model.book.Book;
import com.yk.common.model.xml.Container;
import com.yk.common.model.xml.NavigationControl;
import com.yk.common.model.xml.Package;
import com.yk.common.model.book.TableOfContent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// was partly copied from https://www.codeproject.com/Articles/592909/EPUB-Viewer-for-Android-with-Text-to-Speech
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class BookService {

    static final String META_CONTENT = "META-INF/container.xml";


    private static BookService bookService = null;

    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    private Book book;
    private final String path;
    private final ZipFile bookZipFile;
    @Getter
    private final String rootDirectory;
    private final Container container;
    private final Package xmlPackage;
    private TableOfContent tableOfContent;


    public static BookService getBookService() throws BookServiceException {
        if (bookService != null) {
            return bookService;
        }
        try {
            FileInputStream fileInputStream = ApplicationContext.getContext().openFileInput("lastBook.txt");
            return initFromPath(new BufferedReader(new InputStreamReader(fileInputStream)).readLine());
        } catch (IOException ioException) {
            Log.e("BookService", "Error on Book Service initialization");
            throw new BookServiceException("Error on Book Service initialization", ioException);
        }
    }

    public boolean isEntryPresented(String resourceName) {
        return bookZipFile.getEntry(new File(rootDirectory, resourceName).getPath()) != null;
    }

    public InputStream getResourceAsStream(String resourceName) throws IOException {
        ZipEntry contentZipEntry = bookZipFile.getEntry(new File(rootDirectory, resourceName).getPath());
        return bookZipFile.getInputStream(contentZipEntry);
    }

    public InputStream getCover() throws BookServiceException {
        if (getTableOfContent().getCover() == null)
            return null;
        try {
            return getResourceAsStream(getTableOfContent().getCover());
        } catch (IOException ioException) {
            Log.e("BookService", "Error on cover retrieval");
            throw new BookServiceException("Error on cover retrieval", ioException);
        }
    }

    public static InputStream getResourceAsStreamForSingleFile(String path, String rootDirectory, String resourceName)
            throws BookServiceException {
        try {
            @SuppressWarnings("all")
            ZipFile bookZipFile = new ZipFile(path);
            ZipEntry contentZipEntry = bookZipFile.getEntry(new File(rootDirectory, resourceName).getPath());
            return bookZipFile.getInputStream(contentZipEntry);
        } catch (IOException ioException) {
            Log.e("BookService", "Error on single file reading");
            throw new BookServiceException("Error on single file reading", ioException);
        }
    }

    public static BookService buildFromPath(String path) throws BookServiceException {
        // start builder
        BookService.BookServiceBuilder bookServiceBuilder = BookService.builder();
        ZipFile bookZipFile;
        try {
            // load file
            bookZipFile = new ZipFile(path);
        } catch (IOException ioException) {
            Log.e("BookService", "Error on book zip reading");
            throw new BookServiceException("Error on book zip reading", ioException);
        }
        // set zip file
        bookServiceBuilder.bookZipFile(bookZipFile);
        // get meta content entry
        ZipEntry contentZipEntry = bookZipFile.getEntry(META_CONTENT);
        if (contentZipEntry == null) {
            throw new BookServiceException("No content entry");
        }

        Container container;
        try {

            // parse and set container
            container = getXmlContentFromInputStream(bookZipFile.getInputStream(contentZipEntry), Container.class);
            bookServiceBuilder.container(container);

            // read content file
            ZipEntry contentFileZipEntry = bookZipFile.getEntry(container.getRootFiles().getRootFile().getFullPath());
            if (contentFileZipEntry == null) {
                throw new BookServiceException("No content entry");
            }

            Package aPackage = getXmlContentFromInputStream(bookZipFile.getInputStream(contentFileZipEntry), Package.class);
            bookServiceBuilder.xmlPackage(aPackage);
        } catch (Exception exception) {
            Log.e("BookService", "Error on content building");
            throw new BookServiceException("Error on content building", exception);
        }
        // build table of content
        int indexOfLastSlash = container.getRootFiles().getRootFile().getFullPath().lastIndexOf("/");
        if (indexOfLastSlash != -1) {
            bookServiceBuilder.rootDirectory(container.getRootFiles().getRootFile().getFullPath().substring(0, indexOfLastSlash + 1));
        } else {
            bookServiceBuilder.rootDirectory("");
        }
        return bookServiceBuilder.build();
    }

    public static BookService initFromPath(String path) throws BookServiceException {
        bookService = buildFromPath(path);
        bookService.book = BookServiceHelper.uploadBookFromDatabase(path);
        if (bookService.book == null) {
            bookService.book = Book.builder().addingDate(new Date()).filePath(path)
                    .filePath(path)
                    .rootPath(bookService.getRootDirectory())
                    .title(bookService.getTitle())
                    .cover(bookService.getTableOfContent().getCover())
                    .creator(bookService.getCreator())
                    .textSize(100)
                    .build();
            BookServiceHelper.createPersistenceBook(bookService);
        }
        return bookService;
    }

    public String getTitle() {
        return xmlPackage.getMetadata().getTitle();
    }

    String getCreator() {
        return String.join(" & ", xmlPackage.getMetadata().getCreators());
    }

    public String getLanguage() {
        return xmlPackage.getMetadata().getLanguage();
    }

    public int getCurrentChapterNumber() {
        return book.getCurrentChapterNumber();
    }

    public void setCurrentChapterNumber(int currentChapter) {
        book.setCurrentChapterNumber(currentChapter);
    }

    public int getChapterByHRef(String href) throws BookServiceException {
        return tableOfContent.getSpines().stream().filter(chapter -> href.endsWith(chapter.getChapterRef()))
                .findAny().orElseThrow(() -> new BookServiceException("Chapter not found"))
                .getSpineId();
    }

    public int getCurrentChapterPosition() {
        return book.getCurrentChapterPosition();
    }

    public void setCurrentChapterPosition(int currentChapterNumber) {
        book.setCurrentChapterPosition(currentChapterNumber);
    }

    public int getTextSize() {
        return book.getTextSize();
    }

    public void setTextSize(int textSize) {
        book.setTextSize(textSize);
    }

    public TableOfContent getTableOfContent() throws BookServiceException {
        if (tableOfContent != null)
            return tableOfContent;
        ZipEntry tableOfContentZipEntry = bookZipFile.stream().filter(zipEntry -> zipEntry.getName().startsWith(rootDirectory)
                && zipEntry.getName().endsWith(".ncx")).findFirst().orElseThrow(() -> new BookServiceException("Incorrect configuration of File"));
        try {
            var navigationControl = getXmlContentFromInputStream(bookZipFile.getInputStream(tableOfContentZipEntry), NavigationControl.class);
            tableOfContent = TableOfContent.fromNavigationControl(navigationControl, xmlPackage, getCoverId());
            return tableOfContent;
        } catch (Exception exception) {
            throw new BookServiceException("Table of content can not be found", exception);
        }
    }

    @NonNull
    private String getCoverId() {
        return xmlPackage.getMetadata().getMeta().stream().filter(meta -> "cover".equals(meta.getName()))
                .findFirst().orElseGet(Package.Meta::new).getContent();

    }

}
