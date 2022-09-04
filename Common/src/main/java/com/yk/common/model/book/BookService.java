package com.yk.common.model.book;

import static com.yk.common.constants.Tags.CONTAINER;
import static com.yk.common.constants.Tags.CONTENT;
import static com.yk.common.constants.Tags.CREATOR;
import static com.yk.common.constants.Tags.DESCRIPTION;
import static com.yk.common.constants.Tags.FULL_PATH;
import static com.yk.common.constants.Tags.LANGUAGE;
import static com.yk.common.constants.Tags.META;
import static com.yk.common.constants.Tags.METADATA;
import static com.yk.common.constants.Tags.NAME;
import static com.yk.common.constants.Tags.PACKAGE;
import static com.yk.common.constants.Tags.ROOT_FILE;
import static com.yk.common.constants.Tags.ROOT_FILES;
import static com.yk.common.constants.Tags.TITLE;
import static com.yk.common.utils.JsonContentHelper.getContentInJson;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.yk.common.constants.Tags;
import com.yk.common.utils.ApplicationContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
@RequiresApi(api = Build.VERSION_CODES.S)
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
    private final JSONObject contentFileHeader;
    private final JSONObject contentFileZipEntryInJson;
    private final JSONArray meta;
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
            ZipFile bookZipFile = new ZipFile(path);
            ZipEntry contentZipEntry = bookZipFile.getEntry(new File(rootDirectory, resourceName).getPath());
            return bookZipFile.getInputStream(contentZipEntry);
        } catch (IOException ioException) {
            Log.e("BookService", "Error on single file reading");
            throw new BookServiceException("Error on single file reading", ioException);
        }
    }

    public static BookService buildFromPath(String path) throws BookServiceException {
        BookService.BookServiceBuilder bookServiceBuilder = BookService.builder();
        ZipFile bookZipFile;
        try {
            bookZipFile = new ZipFile(path);
        } catch (IOException ioException) {
            Log.e("BookService", "Error on book zip reading");
            throw new BookServiceException("Error on book zip reading", ioException);
        }
        bookServiceBuilder.bookZipFile(bookZipFile);
        ZipEntry contentZipEntry = bookZipFile.getEntry(META_CONTENT);
        if (contentZipEntry == null) {
            throw new BookServiceException("No content entry");
        }
        String fullPathToContentFile;
        try {
            JSONObject contentInJson = getContentInJson(bookZipFile.getInputStream(contentZipEntry));
            fullPathToContentFile = contentInJson.getJSONObject(CONTAINER.getTag()).getJSONObject(ROOT_FILES.getTag()).getJSONObject(ROOT_FILE.getTag()).getString(FULL_PATH.getTag());

            // read content file
            ZipEntry contentFileZipEntry = bookZipFile.getEntry(fullPathToContentFile);
            if (contentFileZipEntry == null) {
                throw new BookServiceException("No content entry");
            }
            JSONObject contentFileZipEntryInJson = getContentInJson(bookZipFile.getInputStream(contentFileZipEntry));
            bookServiceBuilder.contentFileZipEntryInJson(contentFileZipEntryInJson);
            JSONObject contentFileHeader = contentFileZipEntryInJson.getJSONObject(PACKAGE.getTag()).getJSONObject(METADATA.getTag());
            bookServiceBuilder.contentFileHeader(contentFileHeader);
            bookServiceBuilder.meta(contentFileHeader.getJSONArray(META.getTag()));

        } catch (IOException | JSONException exception) {
            Log.e("BookService", "Error on content building");
            throw new BookServiceException("Error on content building", exception);
        }
        // build table of content
        int indexOfLastSlash = fullPathToContentFile.lastIndexOf("/");
        if (indexOfLastSlash != -1) {
            bookServiceBuilder.rootDirectory(fullPathToContentFile.substring(0, indexOfLastSlash + 1));
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

    public String getPath() {
        return book.getFilePath();
    }

    public String getTitle() throws BookServiceException {
        return getStringByTag(TITLE);
    }

    String getCreator() {
        return getStringByTag(CREATOR);
    }

    public String getLanguage() throws BookServiceException {
        return getStringByTag(LANGUAGE);
    }

    String getDescription() {
        return getStringByTag(DESCRIPTION);
    }

    private String getStringByTag(Tags tag) {
        try {
            return contentFileHeader.getString(tag.getTag());
        } catch (JSONException jsonException) {
            Log.e("BookService", "Error on getting value for tag " + tag.getTag());
        }
        return "";
    }

    public int getCurrentChapter() {
        return book.getCurrentChapter();
    }

    public void setCurrentChapter(int currentChapter) {
        book.setCurrentChapter(currentChapter);
    }

    public int getCurrentChapterSize() {
        return book.getCurrentChapterSize();
    }

    public void setCurrentChapterSize(int currentChapterSize) {
        book.setCurrentChapterSize(currentChapterSize);
    }

    public int getChapterByHRef(String href) {
        return tableOfContent.getSpines().stream().filter(chapter -> href.endsWith(chapter.getChapterRef()))
                .findAny().orElseThrow(RuntimeException::new)
                .getSpineId();
    }

    public int getCurrentChapterPosition() {
        return book.getCurrentChapterPosition();
    }

    public void setCurrentChapterPosition(int currentChapterPosition) {
        book.setCurrentChapterPosition(currentChapterPosition);
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
            JSONObject tableOfContentInJson = getContentInJson(bookZipFile.getInputStream(tableOfContentZipEntry));
            tableOfContent = TableOfContent.fromJson(tableOfContentInJson, contentFileZipEntryInJson, getCoverId());
            return tableOfContent;
        } catch (IOException | JSONException exception) {
            throw new BookServiceException("Table of content can not be found", exception);
        }
    }

    private String getCoverId() throws BookServiceException {
        try {
            for (int index = 0; index < meta.length(); index++) {
                if (((JSONObject) meta.get(index)).has(NAME.getTag())
                        && ((JSONObject) meta.get(index)).getString(NAME.getTag()).equals("cover")) {
                    return ((JSONObject) meta.get(index)).getString(CONTENT.getTag());
                }
            }
        } catch (JSONException jsonException) {
            throw new BookServiceException("Cover can not be read", jsonException);
        }
        return "";
    }

}
