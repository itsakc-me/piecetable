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

package io.itsakc.piecetable.text;

import io.itsakc.piecetable.core.Piece;
import java.util.Objects;

/**
 * The BufferPosition class represents a position within a buffer, 
 * encapsulating a reference to a specific Piece and the remaining 
 * character count within that Piece. It provides methods to manage 
 * equality, hashing, and representation of the position.
 */
public class BufferPosition {
    
    public Piece piece;
    public int remainder;

    /**
     * Constructs a BufferPosition with the specified Piece and remainder.
     * 
     * @param piece The Piece associated with this BufferPosition.
     * @param remainder The remaining character count within the Piece.
     */
    public BufferPosition(Piece piece, int remainder) {
        this.piece = piece;
        this.remainder = remainder;
    }

    /**
     * Compares this BufferPosition to another object for equality.
     * 
     * @param o The object to compare with.
     * @return True if the specified object is equal to this BufferPosition, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BufferPosition)) {
            return false;
        }
        BufferPosition p = (BufferPosition) o;
        return Objects.equals(p.piece, piece)
            && Objects.equals(p.remainder, remainder);
    }

    /**
     * Returns a hash code value for this BufferPosition.
     * 
     * @return The hash code for this BufferPosition.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(piece)
             ^ Objects.hashCode(remainder);
    }

    /**
     * Returns a string representation of this BufferPosition in the format 
     * "BufferPosition{ piece: x, remainder: y }", where x is the string representation 
     * of the piece and y is the remainder.
     * 
     * @return A string describing the piece and remainder in this BufferPosition.
     */
    @Override
    public String toString() {
        return "BufferPosition{ piece: " + piece.toString() + ", remainder: " + remainder + " }";
    }

    /**
     * Creates and returns a new BufferPosition with the specified Piece and remainder.
     * 
     * @param piece The Piece associated with the new BufferPosition.
     * @param remainder The remaining character count within the Piece.
     * @return A new BufferPosition instance.
     */
    public static BufferPosition create(Piece piece, int remainder) {
        return new BufferPosition(piece, remainder);
    }
    
}