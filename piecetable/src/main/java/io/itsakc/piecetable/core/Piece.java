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

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

/**
 * The Piece class represents a node in a binary tree structure used for 
 * managing text within a buffer. Each Piece holds information about its 
 * position in the buffer, its relationships with other Pieces, and its 
 * color for red-black tree balancing.
 */
public class Piece {
    
    public int bufferIndex;
    public int start;
    public int length;
    
    public Piece left;
    public Piece right;
    public Piece parent;

    public PieceColor color;

    /**
     * Constructs a Piece with the specified buffer index, start index, 
     * and length of the text it represents.
     * 
     * @param bufferIndex The index of the buffer that this Piece refers to.
     * @param start The starting index of the text within the buffer.
     * @param length The length of the text represented by this Piece.
     */
    public Piece(
        @IntRange (from = 0) int bufferIndex,
        @IntRange (from = 0) int start,
        @IntRange (from = 0) int length) {
            this.bufferIndex = bufferIndex;
            this.start = start;
            this.length = length;
            this.color = PieceColor.RED;
        }
    
}
