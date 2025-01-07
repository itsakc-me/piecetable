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

package io.itsakc.piecetable.util;

import java.util.Objects;

/**
 * The SearchResult class represents the outcome of a search operation, 
 * encapsulating a range of text and the corresponding value found within that range.
 */
public class SearchResult {
    
    public Range range; // The range in which the value was found
    public CharSequence value; // The value found within the specified range

    /**
     * Constructs a SearchResult with the specified range and value.
     *
     * @param range The range in which the value is located.
     * @param value The value that was found within the range.
     */
    public SearchResult(Range range, CharSequence value) {
        this.range = range; 
        this.value = value; 
    }

    /**
     * Compares this SearchResult to another object for equality. Two 
     * SearchResult objects are considered equal if both their range and 
     * value are equal.
     *
     * @param o The object to compare with this SearchResult.
     * @return True if the specified object is equal to this SearchResult, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SearchResult)) {
            return false; 
        }
        SearchResult p = (SearchResult) o; 
        return Objects.equals(p.range, range) 
            && Objects.equals(p.value, value); 
    }

    /**
     * Returns a hash code value for this SearchResult. The hash code is 
     * generated using the range and value, combined to produce a unique identifier.
     *
     * @return The hash code value for this SearchResult.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(range) 
             ^ Objects.hashCode(value); 
    }

    /**
     * Returns a string representation of this SearchResult in the format 
     * "SearchResult{ range: x, value: y }", where x is the string representation 
     * of the range and y is the value.
     *
     * @return A string representation of this SearchResult.
     */
    @Override
    public String toString() {
        return "SearchResult{ range: " + range.toString() + ", value: " + value + " }"; 
    }

    /**
     * Static factory method to create a new SearchResult instance with the 
     * specified range and value.
     *
     * @param range The range in which the value is located.
     * @param value The value that was found within the range.
     * @return A new SearchResult instance.
     */
    public static SearchResult create(Range range, CharSequence value) {
        return new SearchResult(range, value); 
    }
    
}