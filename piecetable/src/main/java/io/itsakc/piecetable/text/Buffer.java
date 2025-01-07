/**
 * Copyright [2022 - 2024] @itsakc {@link https://github.com/itsakc-me}.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.itsakc.piecetable.text;

import androidx.annotation.IntRange;
import io.itsakc.piecetable.util.DynamicList;

/**
 * The Buffer class implements the CharSequence interface and represents a mutable buffer of
 * characters with line tracking capabilities. It allows for text manipulation, including appending
 * and deleting characters, while keeping track of line breaks within the content.
 */
public class Buffer implements CharSequence {

    private DynamicList<Character> text;
    public DynamicList<Integer> lineStarts;
    
    /**
     * Constructs an empty Buffer with empty content.
     */
    public Buffer() {
        this("");
    }

    /**
     * Constructs an empty Buffer and adds the provided content.
     *
     * @param content the initial content for the buffer.
     */
    public Buffer(CharSequence content) {
        text = new DynamicList<>();
        lineStarts = new DynamicList<>();
        append(content);
    }

    /**
     * Appends text at the last index in the buffer.
     *
     * @param newText the text to be added.
     * @return Buffer Self instance.
     */
    public Buffer append(CharSequence newText) {
        int index = 0;
        while (index < newText.length()) {
            char charAt = newText.charAt(index);
            if (charAt == '\n') lineStarts.add(length());
            text.add(charAt);
            index++;
        }
        return this;
    }

    /**
     * Inserts text at the specified index in the buffer.
     *
     * @param start the index at which the text is to be inserted.
     * @param newText the text to be added.
     * @return Buffer Self instance.
     */
    public Buffer insert(int start, CharSequence newText) {
        int index = 0;
        while (index < newText.length()) {
            char charAt = newText.charAt(index);
            if (charAt == '\n') lineStarts.add(start + index);
            text.add((start + index), charAt);
            index++;
        }
        return this;
    }

    /**
     * Deletes text from the specified index with the specified length.
     *
     * @param start the starting index for deletion.
     * @param length the number of characters to be deleted.
     * @return Buffer Self instance.
     */
    public Buffer delete(int start, int length) {
        int index = start;
        while (index < (start + length)) {
            char charAt = charAt(start);
            if (charAt == '\n') lineStarts.remove(start);
            text.remove(start);
            index++;
        }
        return this;
    }

    /**
     * Computes the starting indices of each line from a specified index.
     *
     * @param startIndex the index from which to start computing line starts.
     */
    private void computeLineStarts(int startIndex) {
        int index = startIndex;
        while (index < length()) {
            if (charAt(index) == '\n') {
                lineStarts.add(index + 1);
            }
            index++;
        }
    }

    /**
     * Returns the length of the buffer.
     *
     * @return the length of the buffer
     */
    @Override
    public int length() {
        return text.size();
    }

    /**
     * Returns the character at the specified index.
     *
     * @param index the index of the character to be returned
     * @return the character at the specified index
     */
    @Override
    public char charAt(int index) {
        return text.get(index);
    }

    /**
     * Returns a subsequence of characters between the specified start and end indices.
     *
     * @param start the starting index of the subsequence
     * @param end the ending index of the subsequence
     * @return a CharSequence representing the subsequence
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        StringBuilder result = new StringBuilder();
        for (int index = start; index < end; index++) {
            result.append(text.get(index));
        }
        return result.toString();
    }

    /**
     * Returns a string representation of the buffer.
     *
     * @return a string representation of the buffer
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < text.size(); index++) {
            result.append(text.get(index));
        }
        return result.toString();
    }
    
}
