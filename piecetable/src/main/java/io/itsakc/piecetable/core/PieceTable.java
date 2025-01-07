/**
 * Copyright [2022 - 2024] @itsakc {@link https://github.com/itsakc-me}.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.itsakc.piecetable.core;

import android.os.Build;
import android.text.style.CharacterStyle;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import io.itsakc.piecetable.text.Buffer;
import io.itsakc.piecetable.text.BufferPosition;
import io.itsakc.piecetable.util.DynamicList;
import io.itsakc.piecetable.manager.UndoRedoManager;
import io.itsakc.piecetable.util.Range;
import io.itsakc.piecetable.util.SearchResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import android.util.Log;
import android.widget.Toast;
import java.util.regex.Pattern;

/**
 * The `PieceTable` class provides an efficient structure for managing
 * and editing text by leveraging a piece table data structure.
 * It allows for incremental modifications without duplicating the
 * entire text, ensuring optimal performance during text operations.
 *
 * It supports a wide range of functionalities, including insertion,
 * deletion, and retrieval of text segments, while maintaining the
 * integrity of the original content. The class also incorporates
 * undo and redo features to facilitate easy reversal and
 * re-application of changes.
 *
 * <p> This class is designed to handle large volumes of text with
 * minimal memory overhead, making it ideal for applications requiring
 * real-time text manipulation and editing capabilities.
 *
 * Author: @itsakc
 */
public class PieceTable {

    private static final String TAG = "PieceTable";

    // Ranges of `PieceTable` chunk size.
    public static final int _CHUNK_32KB = 32 * 1024; // 32KB
    public static final int _CHUNK_64KB = 64 * 1024; // 64KB
    public static final int _CHUNK_128KB = 128 * 1024; // 128KB
    public static final int _CHUNK_256KB = 256 * 1024; // 256KB
    public static final int _CHUNK_512KB = 512 * 1024; // 512KB
    public static final int _CHUNK_1MB = 1 * 1024 * 1024; // 1MB
    public static final int _CHUNK_2MB = 2 * 1024 * 1024; // 2MB
    public static final int _CHUNK_4MB = 4 * 1024 * 1024; // 4MB
    public static final int _CHUNK_8MB = 8 * 1024 * 1024; // 8MB
    public static final int _CHUNK_16MB = 16 * 1024 * 1024; // 16MB
    public static final int _CHUNK_32MB = 32 * 1024 * 1024; // 32MB

    private boolean isThrowException = false;
    private int chunksize = _CHUNK_64KB;
    private boolean isSingleBuffer = false;

    private static PieceTable sInstance;

    private DynamicList<Buffer> buffers;
    private Piece root;
    private UndoRedoManager undoRedoManager;
    private TextModificationsListener listener;

    /**
     * Default constructor for `PieceTable`.
     * Initializes the `PieceTable` with:
     * - An empty content
     * - A default chunk size (_CHUNK_64KB)
     * - Single buffer mode disabled (isSingleBuffer = false)
     */
    public PieceTable() {
        this("", _CHUNK_64KB, false);
    }

    /**
     * Constructor for `PieceTable` with initial content.
     * Initializes the `PieceTable` with:
     * - The specified initial content
     * - The default chunk size (_CHUNK_64KB)
     * - Single buffer mode disabled (isSingleBuffer = false)
     *
     * @param content The initial content to populate the `PieceTable`.
     *                If null, the content will be treated as an empty string.
     */
    public PieceTable(CharSequence content) {
        this(content, _CHUNK_64KB, false);
    }

    /**
     * Constructor for `PieceTable` with a specified chunk size.
     * Initializes the `PieceTable` with:
     * - An empty content
     * - The specified chunk size
     * - Single buffer mode disabled (isSingleBuffer = false)
     *
     * @param chunksize The size of the chunk to be allocated for the `PieceTable`.
     *                  If chunksize is less than a certain minimum size, the default chunk size will be used.
     */
    public PieceTable(int chunksize) {
        this("", chunksize, false);
    }

    /**
     * Constructor for `PieceTable` with single buffer option.
     * Initializes the `PieceTable` with:
     * - An empty content
     * - The default chunk size (_CHUNK_64KB)
     * - The specified single buffer mode (isSingleBuffer)
     *
     * @param isSingleBuffer If true, the `PieceTable` will use a single buffer.
     *                       If false, multiple buffers may be used.
     */
    public PieceTable(boolean isSingleBuffer) {
        this("", _CHUNK_64KB, isSingleBuffer);
    }

    /**
     * Constructor for `PieceTable` with specified chunk size and single buffer option.
     * Initializes the `PieceTable` with:
     * - An empty content
     * - The specified chunk size
     * - The specified single buffer mode (isSingleBuffer)
     *
     * @param chunksize      The size of the chunk to be allocated for the `PieceTable`.
     *                       If chunksize is less than a certain minimum size, the default chunk size will be used.
     * @param isSingleBuffer If true, the `PieceTable` will use a single buffer.
     *                       If false, multiple buffers may be used.
     */
    public PieceTable(int chunksize, boolean isSingleBuffer) {
        this("", chunksize, isSingleBuffer);
    }

    /**
     * Constructor for `PieceTable` with initial content and specified chunk size.
     * Initializes the `PieceTable` with:
     * - The specified initial content
     * - The specified chunk size
     * - Single buffer mode disabled (isSingleBuffer = false)
     *
     * @param content  The initial content to populate the `PieceTable`.
     *                 If null, the content will be treated as an empty string.
     * @param chunksize The size of the chunk to be allocated for the `PieceTable`.
     *                 If chunksize is less than a certain minimum size, the default chunk size will be used.
     */
    public PieceTable(CharSequence content, int chunksize) {
        this(content, chunksize, false);
    }

    /**
     * Constructor for `PieceTable` with initial content and single buffer option.
     * Initializes the `PieceTable` with:
     * - The specified initial content
     * - The default chunk size (_CHUNK_64KB)
     * - The specified single buffer mode (isSingleBuffer)
     *
     * @param content       The initial content to populate the `PieceTable`.
     *                      If null, the content will be treated as an empty string.
     * @param isSingleBuffer If true, the `PieceTable` will use a single buffer.
     *                       If false, multiple buffers may be used.
     */
    public PieceTable(CharSequence content, boolean isSingleBuffer) {
        this(content, _CHUNK_64KB, isSingleBuffer);
    }

    /**
     * Constructor for `PieceTable` with all parameters.
     * Initializes the `PieceTable` with:
     * - The specified initial content
     * - The specified chunk size
     * - The specified single buffer mode (isSingleBuffer)
     *
     * @param content       The initial content to populate the `PieceTable`.
     *                      If null, the content will be treated as an empty string.
     * @param chunksize     The size of the chunk to be allocated for the `PieceTable`.
     *                      If chunksize is less than a certain minimum size, the default chunk size will be used.
     * @param isSingleBuffer If true, the `PieceTable` will use a single buffer.
     *                       If false, multiple buffers may be used.
     */
    public PieceTable(CharSequence content, int chunksize, boolean isSingleBuffer) {
        sInstance = this;
        setChunksize(chunksize);
        enableSingleBuffer(isSingleBuffer);
        loadInitialContent(content);
    }

    /**
    .* Enables or disables throw exception for the `PieceTable`.
     *
     * @param state If true, enables throwing exception. If false, disables it.
     */
    public synchronized void enableThrowException(boolean state) {
        this.isThrowException = state;
    }

    /**
     * Sets the chunk size for the `PieceTable`.
     *
     * @param chunksize The new size for the chunks.
     */
    public synchronized void setChunksize(int chunksize) {
        if (chunksize > _CHUNK_32MB) chunksize = _CHUNK_32MB;
        if (chunksize < _CHUNK_32KB) chunksize = _CHUNK_32KB;
        this.chunksize = chunksize;
        this.isSingleBuffer = false;
    }

    /**
     * Enables or disables single buffer mode for the `PieceTable`.
     *
     * @param state If true, enables single buffer mode. If false, disables it.
     */
    public synchronized void enableSingleBuffer(boolean state) {
        if (this.isSingleBuffer = state) {
            this.chunksize = _CHUNK_32MB;
        }
    }

    /**
     * Checks if the system is set to throw exceptions.
     *
     * @return true if the system is set to throw exceptions, false otherwise.
     */
    public synchronized boolean isThrowException() {
        return this.isThrowException;
    }

    /**
     * Retrieves the current chunk size.
     *
     * @return the current chunk size in bytes.
     */
    public synchronized int chunksize() {
        return this.chunksize;
    }

    /**
     * Checks if the system is operating in single buffer mode.
     *
     * @return true if the system is operating in single buffer mode, false otherwise.
     */
    public synchronized boolean isSingleBuffer() {
        return this.isSingleBuffer;
    }

    /**
     * Load initial content into the `PieceTable`.
     *
     * @param file The initial file that contains the text content to be loaded.
     */
    public synchronized void loadInitialContent(File file) {
        StringBuilder sb = new StringBuilder();
        FileReader fr = null;
        try {
            fr = new FileReader(file);

            char[] buff = new char[1024];
            int length = 0;

            while ((length = fr.read(buff)) > 0) {
                sb.append(new String(buff, 0, length));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        loadInitialContent(sb);
    }

    /**
     * Load initial content into the `PieceTable`.
     *
     * @param content The initial text content to be loaded.
     */
    public synchronized void loadInitialContent(CharSequence content) {
        try {
        	clear();
            append(content);
            notifyContentLoaded(content);
        } catch(Exception err) {
        	handleException(err);
        }
    }

    /**
     * Adds a buffer to the end of the buffer list. This method is synchronized to ensure thread
     * safety.
     *
     * @param buffer the buffer to be added.
     */
    public synchronized void addBuffer(Buffer buffer) {
        addBuffer(buffers.size(), buffer);
    }

    /**
     * Adds a buffer at the specified index in the buffer list. If the index is invalid, the
     * operation is aborted.
     *
     * @param index the position at which the buffer should be added.
     * @param buffer the buffer to be added.
     */
    public synchronized void addBuffer(int index, Buffer buffer) {
        try {
            Piece parent = findBufferPosition(
                           findOutBuffer(index) - 1).piece;
            enableSingleBuffer(false);
            insertTextRecursively(0, buffer, parent);
        } catch (Exception err) {
            handleException(err);
        }
    }

    /**
     * Removes the specified buffer from the buffer list. This method is synchronized to ensure
     * thread safety.
     *
     * @param buffer the buffer to be removed.
     */
    public synchronized void removeBuffer(Buffer buffer) {
        removeBuffer(indexOfBuffer(buffer));
    }

    /**
     * Removes the buffer at the specified index from the buffer list. If the index is invalid, the
     * operation is aborted.
     *
     * @param index the position of the buffer to be removed.
     */
    public synchronized void removeBuffer(int index) {
        try {
            int start = findOutBuffer(index);
            int end = start + buffers.get(index).length();
            delete(start, end);
        } catch (Exception err) {
            handleException(err);
        }
    }

    /**
     * Replaces the buffer at the specified index with a new buffer. This method first removes the
     * buffer at the specified index, then adds the new buffer.
     *
     * @param index the position of the buffer to be replaced.
     * @param buffer the new buffer to be added.
     */
    public synchronized void replaceBuffer(int index, Buffer buffer) {
        try {
            removeBuffer(index);
            addBuffer(index, buffer);
        } catch (Exception err) {
            handleException(err);
        }
    }

    /**
     * Retrieves the buffer at the specified index in the buffer list.
     *
     * @param index the position of the buffer to retrieve.
     * @return the buffer at the specified index, or null if an error occurs.
     */
    public synchronized Buffer bufferAt(int index) {
        try {
            return buffers.get(index);
        } catch (Exception err) {
            handleException(err);
            return null;
        }
    }

    /**
     * Finds the index of the specified buffer in the buffer list.
     *
     * @param buffer the buffer to find.
     * @return the index of the buffer, or -1 if the buffer is not found or an error occurs.
     */
    public synchronized int indexOfBuffer(Buffer buffer) {
        try {
            return buffers.indexOf(buffer);
        } catch (Exception err) {
            handleException(err);
            return -1;
        }
    }

    /**
     * Returns the number of buffers in the buffer list.
     *
     * @return the number of buffers, or 0 if an error occurs.
     */
    public synchronized int sizeOfBuffers() {
        try {
            return buffers.size();
        } catch (Exception err) {
            handleException(err);
            return 0;
        }
    }

    /**
     * Calculates the position within a specific buffer based on the given
     * index and buffer index. This method computes the relative position
     * inside a buffer considering the buffer's maximum size.
     *
     * @param position the absolute index in the piece table.
     * @return the relative position within the specified buffer.
     */
    public synchronized int findInBuffer(int position) {
        return findInBuffer(position, (int) Math.floor(position / chunksize));
    }

    /**
     * Calculates the position within a specific buffer based on the given
     * index and buffer index. This method computes the relative position
     * inside a buffer considering the buffer's maximum size.
     *
     * @param position the absolute index in the piece table.
     * @param index the index of the buffer in the buffer list.
     * @return the relative position within the specified buffer.
     */
    public synchronized int findInBuffer(int position, int index) {
        try {
            return position - findOutBuffer(index);
        } catch(Exception err) {
        	handleException(err);
            return -1;
        }
    }

    /**
     * Calculates the absolute position in the piece table based on the given buffer index. This
     * method computes the cumulative length of buffers up to the specified index to determine the
     * start position of that buffer.
     *
     * @param index the index of the buffer in the buffer list.
     * @return the absolute start position in the piece table corresponding to the specified buffer index.
     */
    public synchronized int findOutBuffer(int index) {
        try {
        	return index * chunksize;
        } catch(Exception err) {
        	handleException(err);
            return -1;
        }
    }

    /**
     * Append text at the last position, while capturing action (force allowed).
     *
     * @param text The text to be appended.
     * @return `PieceTable` Self instance.
     */
    public synchronized PieceTable append(CharSequence text) {
        return append(text, true);
    }

    /**
     * Append text at the last position, while capturing action (if allowed).
     *
    .* @param text The text to be appended.
     * @param captureAction The state of capturing action.
     * @return `PieceTable` Self instance.
     */
    public synchronized PieceTable append(CharSequence text, boolean captureAction) {
        try {
            int index = length();
            Piece piece = findMaxPiece(root);
            Buffer buffer = buffers.get(piece.bufferIndex);

            int spaceLeft = chunksize - buffer.length();
            int toAppend = Math.min(spaceLeft, text.length());
            piece.length += toAppend;

            buffer.append(text.subSequence(0, toAppend));

            insertTextRecursively(toAppend, text, piece);

            if (captureAction && text.length() > 0) {
                undoRedoManager.captureInsertAction(index, index + text.length(), System.nanoTime());
            }
            notifyTextInserted(index, text);
            return Factory.getInstance();
        } catch(Exception err) {
        	handleException(err);
            return null;
        }
    }

    /**
     * Insert text at the specified position, while capturing action (force allowed).
     *
     * @param position The position where the text should be inserted.
     * @param text The text to be inserted.
     * @return `PieceTable` Self instance.
     */
    public synchronized PieceTable insert(int position, CharSequence text) {
        return insert(position, text, true);
    }

    /**
     * Insert text at the specified position, while capturing action (if allowed).
     *
     * @param position The position where the text should be inserted.
     * @param text The text to be inserted.
     * @param captureAction State (On/Off) of Undo/Redo action capture.
     * @return `PieceTable` Self instance.
     */
    public synchronized PieceTable insert(int position, CharSequence text, boolean captureAction) {
        try {
            BufferPosition bufferPosition = findBufferPosition(position);
            Piece piece = bufferPosition.piece;
            int remainder = bufferPosition.remainder;
            Buffer buffer = buffers.get(piece.bufferIndex);
            int bufferRemainder = findInBuffer(position, piece.bufferIndex);

            if (position == length()) return append(text, captureAction);

            int spaceLeft = chunksize - buffer.length();
            int toInsert = Math.min(spaceLeft, text.length());
            piece.length += toInsert;

            if (remainder > 0 && piece.length > remainder) root = splitPiece(root, piece, remainder);

            CharSequence afterText = buffer.subSequence(bufferRemainder, buffer.length());
            CharSequence textToInsert = text.subSequence(0, toInsert);

            buffer.insert(bufferRemainder, textToInsert);

            StringBuilder restText = new StringBuilder(text);
            if (textToInsert.length() >= spaceLeft) restText.append(afterText);

            insertTextRecursively(toInsert, restText, piece);

            if (captureAction && text.length() > 0) {
                undoRedoManager.captureInsertAction(position, position + text.length(), System.nanoTime());
            }
            notifyTextInserted(position, text);
            return Factory.getInstance();
        } catch(Exception err) {
        	handleException(err);
            return null;
        }
    }

    /**
     * Delete text within the specified range, capturing action (force allowed).
     *
     * @param range The range of text to be deleted.
     * @return `PieceTable` Self instance.
     */
    public synchronized PieceTable delete(Range range) {
        return delete(range.start, range.end);
    }

    /**
     * Delete text from start to end position, while capturing the action (force allowed).
     *
     * @param start The starting position of the text to delete.
     * @param end The ending position of the text to delete.
     * @return `PieceTable` Self instance.
     */
    public synchronized PieceTable delete(int start, int end) {
        return delete(start, end, true);
    }

    /**
     * Delete text from start to end positions, while capturing action (if allowed).
     *
     * @param start The starting position of the text to delete.
     * @param end The ending position of the text to delete.
     * @param captureAction State (On/Off) of Undo/Redo action capture.
     * @return `PieceTable` Self instance.
     */
    public synchronized PieceTable delete(int start, int end, boolean captureAction) {
        try {
            // Capture the delete action for undo/redo if required
            if (captureAction && end > start) {
                undoRedoManager.captureDeleteAction(start, end, System.nanoTime());
            }

            if (start == 0 && end == length()) {
                clear();
                return Factory.getInstance();
            }

            int current = start;
            while (current < end) {
                // Find the buffer position for the current index
                BufferPosition bufferPosition = findBufferPosition(current);
                Piece piece = bufferPosition.piece;
                int remainder = bufferPosition.remainder;
                Buffer buffer = buffers.get(piece.bufferIndex);

                int mStart = findInBuffer(current, piece.bufferIndex);
                int mLength = Math.min(end - current, piece.length);

                if (remainder == 0 && mLength == piece.length) {
                    // Delete the piece if the remainder is 0 and end is beyond the piece end
                    root = deletePiece(root, piece);
                } else {
                    // Reduce the length of the piece
                    piece.length -= mLength;
                }

                // Delete from the buffer
                buffer.delete(mStart, mLength);

                // Remove buffer if it is empty and not the original buffer
                if (buffer.length() == 0) {
                    buffers.remove(piece.bufferIndex);
                }

                current += mLength;
            }

            // Notify listeners about the text deletion
            notifyTextDeleted(start, end);
            return Factory.getInstance();
        } catch (Exception err) {
            handleException(err);
            return null;
        }
    }

    /**
     * Replace text within the specified range with new text, capturing
     * action (force allowed).
     *
     * @param range The range of text to replace.
     * @param replacement The new text to insert in place of the old text.
     * @return `PieceTable` Self instance.
     */
    public synchronized PieceTable replace(Range range, CharSequence replacement) {
        return replace(range.start, range.end, replacement);
    }

    /**
     * Replace text from start to the end of the new text, capturing action (force allowed).
     *
     * @param start The starting position of the text to replace.
     * @param replacement The new text to insert in place of the old text.
     * @return `PieceTable` Self instance.
     */
    public synchronized PieceTable replace(int start, CharSequence replacement) {
        return replace(start, (start + replacement.length()), replacement);
    }

    /**
     * Replace text from start to end with new text, while capturing action (force allowed).
     *
     * @param start The starting position of the text to replace.
     * @param end The ending position of the text to replace.
     * @param replacement The new text to insert.
     * @return `PieceTable` Self instance.
     */
    public synchronized PieceTable replace(int start, int end, CharSequence replacement) {
        return replace(start, end, replacement, true);
    }

    /**
     * Replace text from start to end with new text, while capturing action (if allowed).
     *
     * @param start The starting position of the text to replace.
     * @param end The ending position of the text to replace.
     * @param replacement The new text to insert.
     * @param captureAction State (On/Off) of Undo/Redo action capture.
     * @return `PieceTable` Self instance.
     */
    public synchronized PieceTable replace(int start, int end, CharSequence replacement, boolean captureAction) {
        try {
            delete(start, end, captureAction);
            insert(start, replacement, captureAction);
            return Factory.getInstance();
        } catch(Exception err) {
        	handleException(err);
            return null;
        }
    }

    /**
     * Performs a single search in the `PieceTable`, auto-checker for isRegex.
     *
     * @param query The search query.
     * @param caseSensitive Whether the search is case-sensitive.
     * @return The result of the search.
     */
    public synchronized SearchResult performSingleSearch(CharSequence query, boolean caseSensitive) {
        return performSingleSearch(isRegex(query), query, 0, caseSensitive);
    }

    /**
     * Performs a single search in the `PieceTable`, auto-checker for isRegex,
     * from a given position.
     *
     * @param query The search query.
     * @param startPosition the starting position to begin the search from.
     * @param caseSensitive Whether the search is case-sensitive.
     * @return The result of the search.
     */
    public synchronized SearchResult performSingleSearch(CharSequence query, int startPosition, boolean caseSensitive) {
        return performSingleSearch(isRegex(query), query, startPosition, caseSensitive);
    }

    /**
     * Performs a single search in the `PieceTable`.
     *
     * @param isRegex Whether the search performs regex search.
     * @param query The search query.
     * @param caseSensitive Whether the search is case-sensitive.
     * @return The result of the search.
     */
    public synchronized SearchResult performSingleSearch(boolean isRegex, CharSequence query, boolean caseSensitive) {
        return performSingleSearch(isRegex, query, 0, caseSensitive);
    }

    /**
     * Performs a single search for a text, either normal or regex, in
     * the `PieceTable` from a given position.
     *
     * @param isRegex Whether the search performs regex search.
     * @param query the query text to search for.
     * @param startPosition the starting position to begin the search from.
     * @param caseSensitive Whether the search should be case sensitive.
     * @return A SearchResult containing the range and value of the found text.
     */
    public synchronized SearchResult performSingleSearch(boolean isRegex, CharSequence query, int startPosition, boolean caseSensitive) {
        try {
            SearchResult result = new SearchResult(new Range(-1, -1), "");
            String queryText = query.toString();
            int currentOffset = 0;

            for (Buffer buffer : buffers) {
                String bufferText = buffer.toString();
                if (startPosition > currentOffset + buffer.length()) {
                    currentOffset += buffer.length();
                    continue;
                }

                int searchOffset = Math.max(0, startPosition - currentOffset);
                int index;

                if (isRegex) {
                    Pattern pattern = caseSensitive ?
                            Pattern.compile(queryText) :
                            Pattern.compile(queryText, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(bufferText.substring(searchOffset));

                    if (matcher.find()) {
                        index = matcher.start() + searchOffset;
                        result.range.start = currentOffset + index;
                        result.range.end = currentOffset + matcher.end();
                        result.value = matcher.group();
                    }
                } else {
                    if (caseSensitive) index = bufferText.indexOf(queryText, searchOffset);
                    else index = bufferText.toLowerCase().indexOf(queryText.toLowerCase(), searchOffset);

                    if (index != -1) {
                        result.range.start = currentOffset + index;
                        result.range.end = currentOffset + index + queryText.length();
                        result.value = queryText;
                    }
                }

                currentOffset += buffer.length();
            }

            return result;
        } catch (Exception err) {
            handleException(err);
            return null;
        }
    }

    /** Interface for multiple search results listener. */
    public interface MultiSearchListener {
        /**
         * Called when a multiple search completes.
         *
         * @param result The list of search results.
         */
        void onMultiSearch(SearchResult result, int caret);
    }

    /**
     * Performs a multiple search in the `PieceTable`, auto-checker for isRegex.
     *
     * @param query The search query.
     * @param caseSensitive Whether the search is case-sensitive.
     * @return The list of search results.
     */
    public synchronized List<SearchResult> performMultiSearch(CharSequence query, boolean caseSensitive) {
        return performMultiSearch(isRegex(query), query, 0, caseSensitive, null);
    }

    /**
     * Performs a multiple search in the `PieceTable` with a listener,
     * auto-checker for isRegex.
     *
     * @param query The search query.
     * @param caseSensitive Whether the search is case-sensitive.
     * @param listener The listener for the search results.
     * @return The list of search results.
     */
    public synchronized List<SearchResult> performMultiSearch(CharSequence query, boolean caseSensitive, MultiSearchListener listener) {
        return performMultiSearch(isRegex(query), query, 0, caseSensitive, listener);
    }

    /**
     * Performs a multiple search in the `PieceTable` from a given position,
     * auto-checker for isRegex.
     *
     * @param query The search query.
     * @param startPosition the starting position to begin the search.
     * @param caseSensitive Whether the search is case-sensitive.
     * @return The list of search results.
     */
    public synchronized List<SearchResult> performMultiSearch(CharSequence query, int startPosition, boolean caseSensitive) {
        return performMultiSearch(isRegex(query), query, startPosition, caseSensitive, null);
    }

    /**
     * Performs a multiple search in the `PieceTable` with a listener
     * from a given position, auto-checker for isRegex.
     *
     * @param query The search query.
     * @param startPosition the starting position to begin the search.
     * @param caseSensitive Whether the search is case-sensitive.
     * @param listener The listener for the search results.
     * @return The list of search results.
     */
    public synchronized List<SearchResult> performMultiSearch(CharSequence query, int startPosition, boolean caseSensitive, MultiSearchListener listener) {
        return performMultiSearch(isRegex(query), query, startPosition, caseSensitive, listener);
    }

    /**
     * Performs a multiple search in the `PieceTable`.
     *
     * @param isRegex Whether the search performs regex search.
     * @param query The search query.
     * @param caseSensitive Whether the search is case-sensitive.
     * @return The list of search results.
     */
    public synchronized List<SearchResult> performMultiSearch(boolean isRegex, CharSequence query, boolean caseSensitive) {
        return performMultiSearch(isRegex, query, caseSensitive, null);
    }

    /**
     * Performs a multiple search in the `PieceTable` with a listener.
     *
     * @param isRegex Whether the search performs regex search.
     * @param query The search query.
     * @param caseSensitive Whether the search is case-sensitive.
     * @param listener The listener for the search results.
     * @return The list of search results.
     */
    public synchronized List<SearchResult> performMultiSearch(boolean isRegex, CharSequence query, boolean caseSensitive, MultiSearchListener listener) {
        return performMultiSearch(isRegex, query, 0, caseSensitive, listener);
    }

    /**
     * Performs a multiple search in the `PieceTable` from a given position.
     *
     * @param isRegex Whether the search performs regex search.
     * @param query The search query.
     * @param startPosition the starting position to begin the search.
     * @param caseSensitive Whether the search is case-sensitive.
     * @return The list of search results.
     */
    public synchronized List<SearchResult> performMultiSearch(boolean isRegex, CharSequence query, int startPosition, boolean caseSensitive) {
        return performMultiSearch(isRegex, query, startPosition, caseSensitive, null);
    }

    /**
     * Performs a multi search for a text, either normal or regex.
     *
     * @param isRegex Whether the search performs regex search.
     * @param query the query text to search for.
     * @param startPosition the starting position to begin the search.
     * @param caseSensitive Whether the search should be case sensitive.
     * @param listener The listener for the search results.
     * @return A List of SearchResult containing all matched ranges and values.
     */
    public synchronized List<SearchResult> performMultiSearch(boolean isRegex, CharSequence query, int startPosition, boolean caseSensitive, MultiSearchListener listener) {
        try {
            List<SearchResult> results = new ArrayList<>();
            String queryText = query.toString();
            int currentOffset = 0;

            for (Buffer buffer : buffers) {
                String bufferText = buffer.toString();
                if (startPosition > currentOffset + buffer.length()) {
                    currentOffset += buffer.length();
                    continue;
                }

                int searchOffset = Math.max(0, startPosition - currentOffset);

                if (isRegex) {
                    Pattern pattern = caseSensitive ?
                            Pattern.compile(queryText) :
                            Pattern.compile(queryText, Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(bufferText.substring(searchOffset));
                    while (matcher.find()) {
                        results.add(new SearchResult(new Range(currentOffset + matcher.start() + searchOffset, currentOffset + matcher.end() + searchOffset), matcher.group()));
                    }
                } else {
                    int index = searchOffset;
                    while ((index = (caseSensitive ?
                            bufferText.indexOf(queryText, index) :
                            bufferText.toLowerCase().indexOf(
                                queryText.toLowerCase(), index))) != -1) {
                        results.add(new SearchResult(new Range(currentOffset + index, currentOffset + index + queryText.length()), queryText));
                        index += queryText.length();
                    }
                }

                currentOffset += buffer.length();
            }

            if (listener != null) {
                int caret = 0;
                while (caret < results.size()) {
                    listener.onMultiSearch(results.get(caret), caret);
                    caret++;
                }
                return null;
            }

            return results;
        } catch (Exception err) {
            handleException(err);
            return null;
        }
    }

    /**
     * Perform an undo operation.
     *
     * @return The number of changes undone.
     */
    public synchronized int undo() {
        try {
        	return undoRedoManager.undo();
        } catch(Exception err) {
        	handleException(err);
            return -1;
        }
    }

    /**
     * Perform a redo operation.
     *
     * @return The number of changes redone.
     */
    public synchronized int redo() {
        try {
        	return undoRedoManager.redo();
        } catch(Exception err) {
        	handleException(err);
            return -1;
        }
    }

    /**
     * Check if an undo operation is possible.
     *
     * @return True if an undo can be performed, false otherwise.
     */
    public synchronized boolean canUndo() {
        try {
        	return undoRedoManager.canUndo();
        } catch(Exception err) {
        	handleException(err);
            return false;
        }
    }

    /**
     * Check if a redo operation is possible.
     *
     * @return True if a redo can be performed, false otherwise.
     */
    public synchronized boolean canRedo() {
        try {
        	return undoRedoManager.canRedo();
        } catch(Exception err) {
        	handleException(err);
            return false;
        }
    }

    /**
     * Set whether the undo-redo history should be unlimited.
     *
     * @param unlimitedHistory True to set unlimited history, false otherwise.
     */
    public synchronized void setUnlimitedHistory(boolean unlimitedHistory) {
        try {
        	undoRedoManager.setUnlimitedHistory(unlimitedHistory);
        } catch(Exception err) {
        	handleException(err);
        }
    }

    /**
     * Check if the undo-redo history is unlimited.
     *
     * @return True if unlimited history is enabled, false otherwise.
     */
    public synchronized boolean isUnlimitedHistory() {
        try {
        	return undoRedoManager.hasUnlimitedHistory();
        } catch(Exception err) {
        	handleException(err);
            return false;
        }
    }

    /**
     * Check if a batch edit operation is in progress.
     *
     * @return True if batch edit is active, false otherwise.
     */
    public synchronized boolean isBatchEdit() {
        try {
        	return undoRedoManager.isBatchEdit();
        } catch(Exception err) {
        	handleException(err);
            return false;
        }
    }

    /**
     * Begin a batch edit operation.
     *
     * @return True if the batch edit started successfully, false otherwise.
     */
    public synchronized void beginBatchEdit() {
        try {
        	undoRedoManager.beginBatchEdit();
        } catch(Exception err) {
        	handleException(err);
        }
    }

    /**
     * End a batch edit operation.
     *
     * @return True if the batch edit ended successfully, false otherwise.
     */
    public synchronized void endBatchEdit() {
        try {
        	undoRedoManager.endBatchEdit();
        } catch(Exception err) {
        	handleException(err);
        }
    }

    /**
     * Returns the total number of lines in the Piece Table.
     *
     * @return The total number of lines in the Piece Table.
     */
    public synchronized int lineCount() {
        try {
        	int lineCount = 0;
            for (Buffer buffer : buffers) {
                lineCount += buffer.lineStarts.size();
            }
            return lineCount;
        } catch(Exception err) {
        	handleException(err);
            return 0;
        }
    }

    /**
     * Returns the line offset (line number) for the given character offset.
     *
     * @param charOffset The character offset for which the line offset is to be retrieved.
     * @return The line offset (line number) for the given character offset.
     */
    public synchronized int lineOffset(int charOffset) {
        try {
        	int lineIndex = 0;
            int currentOffset = 0;

            for (Buffer buffer : buffers) {
                for (int lineStart : buffer.lineStarts) {
                    if (currentOffset + lineStart > charOffset) {
                        return lineIndex;
                    }
                    lineIndex++;
                }
                currentOffset += buffer.length();
            }

            return -1;
        } catch(Exception err) {
        	handleException(err);
            return -1;
        }
    }

    /**
     * Retrieve the content of a specific line.
     *
     * @param targetLineIndex The index of the line to retrieve.
     * @return The content of the specified line as CharSequence.
     */
    public synchronized CharSequence lineContent(int targetLineIndex) {
        try {
            int currentLineIndex = 0;

            for (Buffer buffer : buffers) {
                for (int i = 0; i < buffer.lineStarts.size(); i++) {
                    if (currentLineIndex == targetLineIndex) {
                        int start = buffer.lineStarts.get(i);
                        int end = (i + 1 < buffer.lineStarts.size()) ? buffer.lineStarts.get(i + 1) - 1 : buffer.length();
                        return buffer.subSequence(start, end);
                    }
                    currentLineIndex++;
                }
            }

            return "";
        } catch(Exception err) {
        	handleException(err);
            return "";
        }
    }

    /**
     * Get the range of a specific line.
     *
     * @param targetLineIndex The index of the line whose range is to be retrieved.
     * @return A Range object representing the start and end indices of the line.
     */
    public synchronized Range lineRange(int targetLineIndex) {
        try {
            int currentLineIndex = 0;

            for (Buffer buffer : buffers) {
                for (int i = 0; i < buffer.lineStarts.size(); i++) {
                    if (currentLineIndex == targetLineIndex) {
                        int start = buffer.lineStarts.get(i);
                        int end = (i + 1 < buffer.lineStarts.size()) ? buffer.lineStarts.get(i + 1) - 1 : buffer.length();
                        return new Range(start, end);
                    }
                    currentLineIndex++;
                }
            }

            return new Range(-1, -1);
        } catch(Exception err) {
        	handleException(err);
            return new Range(-1, -1);
        }
    }

    /**
     * Get the length of a specific line.
     *
     * @param targetLineIndex The index of the line whose length is to be retrieved.
     * @return The length of the specified line.
     */
    public synchronized int lineLength(int targetLineIndex) {
        try {
        	Range range = lineRange(targetLineIndex);
            return range.end - range.start;
        } catch(Exception err) {
        	handleException(err);
            return 0;
        }
    }

    /**
     * Get a specific range of text.
     *
     * @param range The range of text to retrieve.
     * @return The text within the specified range.
     */
    public synchronized CharSequence textRange(Range range) {
        return textRange(range.start, range.end);
    }

    /**
     * Get a specific range of text defined by start and end positions.
     *
     * @param start The starting position of the text range.
     * @param end The ending position of the text range.
     * @return The text within the specified start and end positions.
     */
    public synchronized CharSequence textRange(int start, int end) {
        try {
            StringBuilder content = new StringBuilder();

            int currentOffset = 0;
            for (Buffer buffer : buffers) {
                int bufferstart = currentOffset;
                int bufferEnd = currentOffset + buffer.length();

                if (end <= bufferstart) break;
                if (start < bufferEnd) {
                    int mStart = Math.max(0, start - bufferstart);
                    int mEnd = Math.min(buffer.length(), end - bufferstart);
                    content.append(buffer.subSequence(mStart, mEnd));
                }

                currentOffset += buffer.length();
            }

            return content.toString();
        } catch(Exception err) {
        	handleException(err);
            return "";
        }
    }

    /**
     * Set a listener for text modification events.
     *
     * @param listener The listener to be notified of text modifications.
     */
    public synchronized void setOnTextModificationListener(TextModificationsListener listener) {
        try {
        	this.listener = listener;
        } catch(Exception err) {
        	handleException(err);
        }
    }

    /**
     * Set a listener for undo and redo events.
     *
     * @param listener The listener to be notified of undo/redo actions.
     */
    public synchronized void setOnUndoRedoListeners(UndoRedoManager.UndoRedoListener listener) {
        try {
        	undoRedoManager.setOnUndoRedoListener(listener);
        } catch(Exception err) {
        	handleException(err);
        }
    }

    /**
     * Get the entire text content.
     *
     * @return The full text content as CharSequence with spans.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public synchronized CharSequence text() {
        try {
            StringBuilder content = new StringBuilder();
            buffers.forEach(buffer -> content.append(buffer));
            return content;
        } catch (Exception err) {
            handleException(err);
            return "";
        }
    }

    /**
     * Get the total length of the text content.
     *
     * @return The total length of the text content.
     */
    public synchronized int length() {
        try {
            int textLength = 0;
            for (Buffer buffer : buffers) textLength += buffer.length();
            return textLength;
        } catch(Exception err) {
        	handleException(err);
            return 0;
        }
    }

    /**
     * Clear all the buffers from `PieceTable`.
     *
     * @return boolean The state of clearing.
     */
    public synchronized boolean clear() {
        try {
            this.buffers = new DynamicList<>();
            this.undoRedoManager = new UndoRedoManager(this);
            this.root = new Piece(0, 0, 0);
            buffers.add(new Buffer());
            return true;
        } catch(Exception err) {
            handleException(err);
            return false;
        }
    }

    /**
     * Returns whether the `PieceTable` is empty or not.
     *
     * @returns boolean Returns true if the buffer list is empty, false otherwise.
     */
    public synchronized boolean isEmpty() {
        try {
        	return buffers.isEmpty();
        } catch(Exception err) {
        	handleException(err);
            return false;
        }
    }

    /**
     * Appends text to new buffers if it exceeds the capacity of the current buffer.
     *
     * @param toAppend The number of characters already appended to the current buffer.
     * @param text The full text that is being processed.
     * @param parent The parent structure that receives new pieces.
     */
    private void insertTextRecursively(int toAppend, @NonNull CharSequence text, @NonNull Piece parent) {
        int bufferIndex = parent.bufferIndex + 1;
        for (int start = toAppend; start < text.length(); start += chunksize) {
            int end = Math.min(start + chunksize, text.length());
            Buffer buffer = new Buffer(text.subSequence(start, end));
            Piece piece = new Piece(bufferIndex, findOutBuffer(bufferIndex), buffer.length());
            root = insertPiece(root, parent, piece);
            buffers.add(bufferIndex, buffer);
            bufferIndex++;
        }
    }

    /**
     * Deletes a piece from the Red-Black Tree and ensures the tree remains balanced.
     *
     * @param root The root of the Red-Black Tree.
     * @param replaced The piece to be deleted.
     * @return The new root of the tree after deletion.
     */
    private Piece deletePiece(Piece root, Piece replaced) {
        if (replaced == null) return root;

        PieceColor originalColor = replaced.color;
        Piece replacement;

        if (replaced.left == null) {
            replacement = replaced.right;
            root = transplant(root, replaced, replaced.right);
        } else if (replaced.right == null) {
            replacement = replaced.left;
            root = transplant(root, replaced, replaced.left);
        } else {
            Piece successor = findMinPiece(replaced.right);
            originalColor = successor.color;
            replacement = successor.right;
            if (successor.parent == replaced) {
                if (replacement != null) {
                    replacement.parent = successor;
                }
            } else {
                root = transplant(root, successor, successor.right);
                successor.right = replaced.right;
                successor.right.parent = successor;
            }
            root = transplant(root, replaced, successor);
            successor.left = replaced.left;
            successor.left.parent = successor;
            successor.color = replaced.color;
        }

        if (originalColor == PieceColor.BLACK) {
            root = rebalanceDelete(root, replacement);
        }

        return root;
    }

    /**
     * Transplants one piece into another in the tree. This method replaces one subtree as a child
     * of its parent with another subtree.
     *
     * @param root The root of the piece table.
     * @param u The piece to be replaced.
     * @param v The piece to replace it with.
     * @return The new root of the piece table.
     */
    private Piece transplant(Piece root, Piece u, Piece v) {
        if (u.parent == null) {
            root = v;
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        if (v != null) {
            v.parent = u.parent;
        }
        return root;
    }

    /**
     * Rebalances the binary tree starting from a given piece. This method ensures that the tree
     * maintains its balanced properties after deletions, updating the root if
     * necessary.
     *
     * @param root The root of the tree.
     * @param piece The piece from which to start rebalancing.
     * @return The new root of the tree after rebalancing.
     */
    private Piece rebalanceDelete(Piece root, Piece piece) {
        Piece sibling;
        while (piece != root && (piece == null || piece.color == PieceColor.BLACK)) {
            if (piece == piece.parent.left) {
                sibling = piece.parent.right;

                if (sibling.color == PieceColor.RED) {
                    sibling.color = PieceColor.BLACK;
                    piece.parent.color = PieceColor.RED;
                    root = rotateLeft(root, piece.parent);
                    sibling = piece.parent.right;
                }

                if ((sibling.left == null || sibling.left.color == PieceColor.BLACK)
                        && (sibling.right == null || sibling.right.color == PieceColor.BLACK)) {
                    sibling.color = PieceColor.RED;
                    piece = piece.parent;
                } else {
                    if (sibling.right == null || sibling.right.color == PieceColor.BLACK) {
                        if (sibling.left != null) sibling.left.color = PieceColor.BLACK;
                        sibling.color = PieceColor.RED;
                        root = rotateRight(root, sibling);
                        sibling = piece.parent.right;
                    }

                    sibling.color = piece.parent.color;
                    piece.parent.color = PieceColor.BLACK;
                    if (sibling.right != null) sibling.right.color = PieceColor.BLACK;
                    root = rotateLeft(root, piece.parent);
                    piece = root;
                }
            } else {
                sibling = piece.parent.left;

                if (sibling.color == PieceColor.RED) {
                    sibling.color = PieceColor.BLACK;
                    piece.parent.color = PieceColor.RED;
                    root = rotateRight(root, piece.parent);
                    sibling = piece.parent.left;
                }

                if ((sibling.left == null || sibling.left.color == PieceColor.BLACK)
                        && (sibling.right == null || sibling.right.color == PieceColor.BLACK)) {
                    sibling.color = PieceColor.RED;
                    piece = piece.parent;
                } else {
                    if (sibling.left == null || sibling.left.color == PieceColor.BLACK) {
                        if (sibling.right != null) sibling.right.color = PieceColor.BLACK;
                        sibling.color = PieceColor.RED;
                        root = rotateLeft(root, sibling);
                        sibling = piece.parent.left;
                    }

                    sibling.color = piece.parent.color;
                    piece.parent.color = PieceColor.BLACK;
                    if (sibling.left != null) sibling.left.color = PieceColor.BLACK;
                    root = rotateRight(root, piece.parent);
                    piece = root;
                }
            }
        }
        if (piece != null) piece.color = PieceColor.BLACK;
        return root;
    }

    /**
     * Splits a piece into two at a specified remainder index. This method creates a new piece with
     * the remaining part of the original piece and inserts it into the tree, updating the root as
     * necessary.
     *
     * @param root The root of the tree.
     * @param piece The piece to split.
     * @param remainder The index at which to split the piece.
     * @return The new root of the tree after the split.
     */
    private Piece splitPiece(Piece root, Piece piece, int remainder) {
        Piece rightPiece = new Piece(piece.bufferIndex, remainder, piece.length - remainder);

        piece.length = remainder;

        root = insertPiece(root, piece, rightPiece);
        return root;
    }

    /**
     * Inserts a piece into the Red-Black Tree and ensures the tree remains balanced.
     *
     * @param root The root of the Red-Black Tree.
     * @param parent The parent (node) piece of the child to be insert in.
     * @param child The piece (node) to be inserted.
     * @return The new root of the tree after insertion.
     */
    public Piece insertPiece(Piece root, Piece parent, Piece child) {
        // Standard BST insertion
        parent = binaryInsert(parent, child);

        // Fix the Red-Black Tree properties
        root = rebalanceInsert(root, child);

        return root;
    }

    /**
     * Inserts a piece into the Binary Search Tree (BST) structure.
     *
     * @param parent The parent (node) of the child.
     * @param child The child (node) to be inserted.
     * @return The root of the BST after the insertion.
     */
    private Piece binaryInsert(Piece parent, Piece child) {
        // Perform standard BST insertion
        if (parent == null) return child;

        if (child.start < parent.start) {
            parent.left = binaryInsert(parent.left, child);
            parent.left.parent = parent;
        } else {
            parent.right = binaryInsert(parent.right, child);
            parent.right.parent = parent;
        }
        return parent;

    }

    /**
     * Rebalances the binary tree starting from a given piece. This method ensures that the tree
     * maintains its balanced properties after insertions, updating the root if
     * necessary.
     *
     * @param root The root of the tree.
     * @param piece The piece from which to start rebalancing.
     * @return The new root of the tree after rebalancing.
     */
    private Piece rebalanceInsert(Piece root, Piece piece) {
        Piece uncle;
        while (piece.parent != root && piece.parent.color == PieceColor.RED) {
            if (piece.parent == piece.parent.parent.left) {
                uncle = piece.parent.parent.right;
                if (uncle != null && uncle.color == PieceColor.RED) {
                    piece.parent.color = PieceColor.BLACK;
                    uncle.color = PieceColor.BLACK;
                    piece.parent.parent.color = PieceColor.RED;
                    piece = piece.parent.parent;
                } else {
                    if (piece == piece.parent.right) {
                        piece = piece.parent;
                        root = rotateLeft(root, piece);
                    }
                    piece.parent.color = PieceColor.BLACK;
                    piece.parent.parent.color = PieceColor.RED;
                    root = rotateRight(root, piece.parent.parent);
                }
            } else {
                uncle = piece.parent.parent.left;
                if (uncle != null && uncle.color == PieceColor.RED) {
                    piece.parent.color = PieceColor.BLACK;
                    uncle.color = PieceColor.BLACK;
                    piece.parent.parent.color = PieceColor.RED;
                    piece = piece.parent.parent;
                } else {
                    if (piece == piece.parent.left) {
                        piece = piece.parent;
                        root = rotateRight(root, piece);
                    }
                    piece.parent.color = PieceColor.BLACK;
                    piece.parent.parent.color = PieceColor.RED;
                    root = rotateLeft(root, piece.parent.parent);
                }
            }
            if (piece.parent == null) break;
        }
        root.color = PieceColor.BLACK;
        return root;
    }

    /**
     * Performs a left rotation on a piece. This method updates the parent and child references to
     * rotate the piece to the left, ensuring the tree structure remains valid and updating the root
     * if necessary.
     *
     * @param root The root of the tree.
     * @param piece The piece to rotate left.
     * @return The new root of the tree after the rotation.
     */
    private Piece rotateLeft(Piece root, Piece piece) {
        Piece rightChild = piece.right;
        piece.right = rightChild.left;

        if (rightChild.left != null) {
            rightChild.left.parent = piece;
        }

        rightChild.parent = piece.parent;

        if (piece.parent == null) {
            root = rightChild;
        } else if (piece == piece.parent.left) {
            piece.parent.left = rightChild;
        } else {
            piece.parent.right = rightChild;
        }

        rightChild.left = piece;
        piece.parent = rightChild;
        return root;
    }

    /**
     * Performs a right rotation on a piece. This method updates the parent and child references to
     * rotate the piece to the right, ensuring the tree structure remains valid and updating the
     * root if necessary.
     *
     * @param root The root of the tree.
     * @param piece The piece to rotate right.
     * @return The new root of the tree after the rotation.
     */
    private Piece rotateRight(Piece root, Piece piece) {
        Piece leftChild = piece.left;
        piece.left = leftChild.right;

        if (leftChild.right != null) {
            leftChild.right.parent = piece;
        }

        leftChild.parent = piece.parent;

        if (piece.parent == null) {
            root = leftChild;
        } else if (piece == piece.parent.right) {
            piece.parent.right = leftChild;
        } else {
            piece.parent.left = leftChild;
        }

        leftChild.right = piece;
        piece.parent = leftChild;
        return root;
    }

    /**
     * Finds the piece with the smallest value in the piece table. This method traverses the tree
     * starting from the given piece and continuously moves to the left child until it reaches the
     * leftmost node, which contains the smallest value.
     *
     * @param piece the root piece from which to start the search
     * @return the piece with the smallest value, or null if the tree is empty
     */
    private Piece findMinPiece(Piece piece) {
        while (piece.left != null) piece = piece.left;
        return piece;
    }

    /**
     * Finds the piece with the largest value in the piece table. This method traverses the tree
     * starting from the given piece and continuously moves to the right child until it reaches the
     * rightmost node, which contains the largest value.
     *
     * @param piece the root piece from which to start the search
     * @return the piece with the largest value, or null if the tree is empty
     */
    private Piece findMaxPiece(Piece piece) {
        while (piece.right != null) piece = piece.right;
        return piece;
    }

    /**
     * Find the buffer position for a specific character position.
     *
     * @param position The character position to find.
     * @return A BufferPosition object containing the found piece and offset.
     */
    private BufferPosition findBufferPosition(int position) {
        Piece foundPiece = findPiece(null, root, position);
        return new BufferPosition(foundPiece, position - foundPiece.start);
    }

    /**
     * Find the piece containing the specified position.
     *
     * @param parent The parent piece.
     * @param child The current child piece.
     * @param position The position to find.
     * @return The piece containing the specified position.
     */
    private Piece findPiece(Piece parent, Piece child, int position) {
        if (child == null) return parent;
        else if (child.start > position) return findPiece(child, child.left, position);
        else if (position >= child.start + child.length) return findPiece(child, child.right, position);
        else return child;
    }

    /**
     * Check if a query is a valid regex pattern.
     *
     * @param query The query to check.
     * @return True if the query is a valid regex, false otherwise.
     */
    private boolean isRegex(CharSequence query) {
        try {
            Pattern.compile(query.toString());
            return true;
        } catch (PatternSyntaxException err) {
            Log.e(TAG, "Not a valid 'Regex': " + err.getMessage());
            return false;
        }
    }

    /**
     * Handles exceptions by either throwing them or logging them based on the configuration.
     *
     * @param err the exception to handle
     */
    private void handleException(Exception err) {
        final String TYPE = err.getClass()
                                .getSimpleName()
                                .concat(": ");
        if (isThrowException) {
            throw new Error(TYPE + err.getMessage());
        } else {
            Log.e(TAG, TYPE + err.getMessage());
        }
    }

    /**
     * Notify listeners that content has been loaded.
     *
     * @param content The content that has been loaded.
     */
    private void notifyContentLoaded(CharSequence content) {
        if (listener != null) listener.onContentLoaded(content);
    }

    /**
     * Notify listeners that text has been inserted.
     *
     * @param start The starting position of the inserted text.
     * @param text The inserted text.
     */
    private void notifyTextInserted(int start, CharSequence text) {
        if (listener != null) listener.onTextInserted(start, text);
    }

    /**
     * Notify listeners that text has been deleted.
     *
     * @param start The starting position of the deleted text.
     * @param end The ending position of the deleted text.
     */
    private void notifyTextDeleted(int start, int end) {
        if (listener != null) listener.onTextDeleted(start, end);
    }

    /**
     * Factory class for creating and managing instances of `PieceTable`.
     */
    public static class Factory {

        /**
         * Gets the singleton instance of `PieceTable`.
         * Ensures that only one instance of `PieceTable` is created.
         *
         * @return the singleton instance of `PieceTable`.
         */
        public static synchronized PieceTable getInstance() {
            if (sInstance == null) {
                sInstance = new PieceTable();
            }
            return sInstance;
        }

        /**
         * Creates and returns a new instance of `PieceTable`.
         * Each call to this method will return a new, independent instance.
         *
         * @return a new instance of `PieceTable`.
         */
        public static synchronized PieceTable createInstance() {
            return new PieceTable();
        }
    }
    
    /** Interface for text modifications listener. */
    public interface TextModificationsListener {
        /**
         * Called when content is loaded.
         *
         * @param content The loaded content.
         */
        void onContentLoaded(CharSequence content);

        /**
         * Called when text is inserted.
         *
         * @param start The start position.
         * @param text The text inserted.
         */
        void onTextInserted(int start, CharSequence text);

        /**
         * Called when text is deleted.
         *
         * @param start The start position.
         * @param end The end position.
         */
        void onTextDeleted(int start, int end);
    }
    
}
