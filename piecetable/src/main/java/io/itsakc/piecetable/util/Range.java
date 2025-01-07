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
 * The Range class represents a numeric interval defined by a start and 
 * an end value. It provides methods for equality comparison, hash code 
 * generation, and string representation of the range.
 */
public class Range {
    
    public int start;
    public int end;

    /**
     * Constructs a Range object with the specified start and end values.
     *
     * @param start The starting value of the range.
     * @param end The ending value of the range.
     */
    public Range(int start, int end) {
        this.start = start; 
        this.end = end; 
    }

    /**
     * Compares this Range to another object for equality. Two Range objects 
     * are considered equal if both their start and end values are equal.
     *
     * @param o The object to compare with this Range.
     * @return True if the specified object is equal to this Range, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Range)) {
            return false; 
        }
        Range p = (Range) o; 
        return Objects.equals(p.start, start) 
            && Objects.equals(p.end, end); 
    }

    /**
     * Returns a hash code value for this Range. The hash code is generated 
     * using the start and end values, combined to produce a unique identifier.
     *
     * @return The hash code value for this Range.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(start) 
             ^ Objects.hashCode(end); 
    }

    /**
     * Returns a string representation of this Range in the format 
     * "Range{ start: x, end: y }", where x and y are the start and end 
     * values, respectively.
     *
     * @return A string representation of this Range.
     */
    @Override
    public String toString() {
        return "Range{ start: " + start + ", end: " + end + " }"; 
    }

    /**
     * Static factory method to create a new Range instance with the 
     * specified start and end values.
     *
     * @param start The starting value of the range.
     * @param end The ending value of the range.
     * @return A new Range instance.
     */
    public static Range create(int start, int end) {
        return new Range(start, end); 
    }
    
}