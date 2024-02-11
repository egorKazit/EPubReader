package com.yk.common.service.book;

import static com.yk.common.model.book.TableOfContent.fromNavigationControl;
import static com.yk.common.utils.XmlContentHelper.getXmlContentFromInputStream;

import android.util.Log;

import com.yk.common.R;
import com.yk.common.context.ApplicationContext;
import com.yk.common.model.book.Book;
import com.yk.common.model.book.TableOfContent;
import com.yk.common.model.xml.Container;
import com.yk.common.model.xml.NavigationControl;
import com.yk.common.model.xml.Package;

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

// was partly copied from https://www.codeproject.com/Articles/592909/EPUB-Viewer-for-Android-with-Text-to-Speech
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class BookService {

    static final String META_CONTENT = "META-INF/container.xml";
    public static final String LAST_BOOK_TXT = "lastBook.txt";
    public static final String NCX = ".ncx";
    public static final  String SERVICE_TAG = "BookService";


    private static BookService bookService = null;

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
            FileInputStream fileInputStream = ApplicationContext.getContext().openFileInput(LAST_BOOK_TXT);
            return initFromPath(new BufferedReader(new InputStreamReader(fileInputStream)).readLine());
        } catch (IOException ioException) {
            Log.e(SERVICE_TAG, ApplicationContext.getContext().getString(R.string.can_not_init_book_service));
            throw new BookServiceException(ApplicationContext.getContext().getString(R.string.can_not_init_book_service), ioException);
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
        if (xmlPackage.getCover() == null)
            return null;
        try {
            return getResourceAsStream(xmlPackage.getCover());
        } catch (IOException ioException) {
            Log.e(SERVICE_TAG, ApplicationContext.getContext().getString(R.string.can_not_retrieve_cover));
            throw new BookServiceException(ApplicationContext.getContext().getString(R.string.can_not_retrieve_cover), ioException);
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
            Log.e(SERVICE_TAG, ApplicationContext.getContext().getString(R.string.can_not_read_book_file));
            throw new BookServiceException(ApplicationContext.getContext().getString(R.string.can_not_read_book_file), ioException);
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
            Log.e(SERVICE_TAG, ApplicationContext.getContext().getString(R.string.can_not_read_book_zip_file));
            throw new BookServiceException(ApplicationContext.getContext().getString(R.string.can_not_read_book_zip_file), ioException);
        }
        // set zip file
        bookServiceBuilder.bookZipFile(bookZipFile);
        // get meta content entry
        ZipEntry contentZipEntry = bookZipFile.getEntry(META_CONTENT);
        if (contentZipEntry == null) {
            throw new BookServiceException(ApplicationContext.getContext().getString(R.string.no_content_entry));
        }

        try {

            // parse and set container
            Container container = getXmlContentFromInputStream(bookZipFile.getInputStream(contentZipEntry), Container.class);
            bookServiceBuilder.container(container);

            var fullPath = container.getRootFiles().getRootFile().getFullPath();

            // build table of content
            int indexOfLastSlash = fullPath.lastIndexOf("/");
            bookServiceBuilder.rootDirectory(indexOfLastSlash != -1 ? fullPath.substring(0, indexOfLastSlash + 1) : "");

            // read content file
            ZipEntry contentFileZipEntry = bookZipFile.getEntry(fullPath);
            if (contentFileZipEntry == null) {
                throw new BookServiceException(ApplicationContext.getContext().getString(R.string.no_content_entry));
            }

            Package xmlPackage = getXmlContentFromInputStream(bookZipFile.getInputStream(contentFileZipEntry), Package.class);
            bookServiceBuilder.xmlPackage(xmlPackage);

        } catch (Exception exception) {
            Log.e(SERVICE_TAG, ApplicationContext.getContext().getString(R.string.can_not_build_content));
            throw new BookServiceException(ApplicationContext.getContext().getString(R.string.can_not_build_content), exception);
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
                    .cover(bookService.xmlPackage.getCover())
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
                .findAny().orElseThrow(() -> new BookServiceException(ApplicationContext.getContext().getString(R.string.can_not_find_chapter)))
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
                        && zipEntry.getName().endsWith(NCX)).findFirst()
                .orElseThrow(() -> new BookServiceException(ApplicationContext.getContext().getString(R.string.incorrect_configuration)));
        try {
            var navigationControl = getXmlContentFromInputStream(bookZipFile.getInputStream(tableOfContentZipEntry), NavigationControl.class);
            tableOfContent = fromNavigationControl(navigationControl, xmlPackage);
            return tableOfContent;
        } catch (Exception exception) {
            throw new BookServiceException(ApplicationContext.getContext().getString(R.string.no_table_of_content), exception);
        }
    }

}
