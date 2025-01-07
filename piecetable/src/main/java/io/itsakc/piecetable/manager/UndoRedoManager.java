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

package io.itsakc.piecetable.manager;

import io.itsakc.piecetable.core.PieceTable;
import java.util.LinkedList;

/**
 * The UndoRedoManager class manages the undo and redo operations for 
 * a PieceTable, allowing the capture of actions and their reversal. 
 * It maintains a history of actions and supports batch editing.
 */
public class UndoRedoManager {
    
    private static final int MAX_HISTORY_SIZE = 200;
    private boolean batchEditInProgress;
    private int currentGroupId;
    private int currentPosition;
    private long previousEditTimestamp;
    private boolean hasUnlimitedHistory = false;
    private LinkedList<UndoRedoAction> actionHistory = new LinkedList<>();
    private PieceTable pieceTable;
    private UndoRedoListener listener;

    /**
     * Constructs an UndoRedoManager for the specified PieceTable.
     * 
     * @param pieceTable The PieceTable to manage undo/redo actions for.
     */
    public UndoRedoManager(PieceTable pieceTable) {
        this.pieceTable = pieceTable;
    }

    /**
     * Sets the listener for undo/redo events.
     * 
     * @param listener The listener to be notified of undo/redo actions.
     */
    public void setOnUndoRedoListener(UndoRedoListener listener) {
        this.listener = listener;
    }

    /**
     * Performs the undo operation and returns the caret position after undoing.
     * 
     * @return The caret position after undoing, or -1 if no undo is possible.
     */
    public int undo() {
        if (canUndo()) {
            UndoRedoAction lastUndoAction = actionHistory.get(currentPosition - 1);
            int group = lastUndoAction.groupId;
            do {
                UndoRedoAction action = actionHistory.get(currentPosition - 1);
                if (action.groupId != group) {
                    break;
                }
                lastUndoAction = action;
                action.performUndo();
                currentPosition--;
            } while (canUndo());
            notifyOnUndo(lastUndoAction.getUndoCaretPosition());
            return lastUndoAction.getUndoCaretPosition();
        }
        return -1;
    }

    /**
     * Performs the redo operation and returns the caret position after redoing.
     * 
     * @return The caret position after redoing, or -1 if no redo is possible.
     */
    public int redo() {
        if (canRedo()) {
            UndoRedoAction lastRedoAction = actionHistory.get(currentPosition);
            int group = lastRedoAction.groupId;
            do {
                UndoRedoAction action = actionHistory.get(currentPosition);
                if (action.groupId != group) {
                    break;
                }
                lastRedoAction = action;
                action.performRedo();
                currentPosition++;
            } while (canRedo());
            notifyOnRedo(lastRedoAction.getRedoCaretPosition());
            return lastRedoAction.getRedoCaretPosition();
        }
        return -1;
    }

    /**
     * Captures an insert action for undo/redo management.
     * 
     * @param start The starting index of the insertion.
     * @param end The ending index of the insertion.
     * @param timestamp The timestamp of the action.
     */
    public void captureInsertAction(int start, int end, long timestamp) {
        boolean wasMerged = false;
        if (canUndo()) {
            UndoRedoAction lastAction = actionHistory.get(currentPosition - 1);
            if (lastAction instanceof InsertAction && lastAction.tryMerge(start, end, timestamp)) {
                wasMerged = true;
            } else {
                lastAction.captureData(start, end);
            }
        }
        if (!wasMerged) {
            addNewAction(new InsertAction(start, end, currentGroupId));
            if (!batchEditInProgress) {
                currentGroupId++;
            }
        }
        previousEditTimestamp = timestamp;
        notifyChange(start, end, previousEditTimestamp);
    }

    /**
     * Captures a delete action for undo/redo management.
     * 
     * @param start The starting index of the deletion.
     * @param end The ending index of the deletion.
     * @param timestamp The timestamp of the action.
     */
    public void captureDeleteAction(int start, int end, long timestamp) {
        boolean wasMerged = false;
        if (canUndo()) {
            UndoRedoAction lastAction = actionHistory.get(currentPosition - 1);
            if (lastAction instanceof DeleteAction && lastAction.tryMerge(start, end, timestamp)) {
                wasMerged = true;
            } else {
                lastAction.captureData(start, end);
            }
        }
        if (!wasMerged) {
            addNewAction(new DeleteAction(start, end, currentGroupId));
            if (!batchEditInProgress) {
                currentGroupId++;
            }
        }
        previousEditTimestamp = timestamp;
        notifyChange(start, end, previousEditTimestamp);
    }

    /**
     * Sets whether the undo/redo history has unlimited size.
     * 
     * @param unlimitedHistory True if unlimited history is allowed, false otherwise.
     */
    public void setUnlimitedHistory(boolean unlimitedHistory) {
        this.hasUnlimitedHistory = unlimitedHistory;
    }

    /**
     * Checks if the undo/redo history has unlimited size.
     * 
     * @return True if unlimited history is allowed, false otherwise.
     */
    public boolean hasUnlimitedHistory() {
        return hasUnlimitedHistory;
    }

    /**
     * Adds a new action to the history and manages the history size.
     * 
     * @param action The UndoRedoAction to be added.
     */
    private void addNewAction(UndoRedoAction action) {
        if (!hasUnlimitedHistory) {
            limitHistorySize();
            if (actionHistory.size() >= MAX_HISTORY_SIZE) {
                actionHistory.removeFirst();
                currentPosition--;
            }
        } else {
            limitHistorySize();
        }
        currentPosition++;
        actionHistory.add(action);
        notifyOnStackChange(actionHistory.size());
    }

    /**
     * Limits the size of the action history to the current position.
     */
    private void limitHistorySize() {
        while (actionHistory.size() > currentPosition) {
            actionHistory.removeLast();
        }
    }

    /**
     * Checks if an undo operation is possible.
     * 
     * @return True if an undo operation can be performed, false otherwise.
     */
    public final boolean canUndo() {
        return currentPosition > 0;
    }
    
    /**
     * Checks if batch editing is in progress.
     * 
     * @return True if a batch edit is ongoing, false otherwise.
     */
    public boolean isBatchEdit() {
        return batchEditInProgress;
    }
    
    /**
     * Begins a batch edit session.
     * 
     * @return True if the batch edit was successfully started.
     */
    public boolean beginBatchEdit() {
        return batchEditInProgress = true;
    }
    
    /**
     * Ends the current batch edit session.
     * 
     * @return True if the batch edit was successfully ended.
     */
    public boolean endBatchEdit() {
        currentGroupId++;
        return batchEditInProgress = false;
    }

    /**
     * Checks if a redo operation is possible.
     * 
     * @return True if a redo operation can be performed, false otherwise.
     */
    public final boolean canRedo() {
        return currentPosition < actionHistory.size();
    }

    /**
     * Notifies the listener about an undo operation.
     * 
     * @param caretPosition The caret position after undoing.
     */
    private void notifyOnUndo(int caretPosition) {
        if (listener != null) listener.onUndo(caretPosition);
    }

    /**
     * Notifies the listener about a redo operation.
     * 
     * @param caretPosition The caret position after redoing.
     */
    private void notifyOnRedo(int caretPosition) {
        if (listener != null) listener.onRedo(caretPosition);
    }

    /**
     * Notifies the listener about a change in the editor.
     * 
     * @param changedItem The item that was changed.
     * @param newValue The new value of the changed item.
     * @param timestamp The timestamp of the change.
     */
    private void notifyChange(int changedItem, int newValue, long timestamp) {
        if (listener != null) listener.onChange(changedItem, newValue, timestamp);
    }

    /**
     * Notifies the listener about a change in the action stack size.
     * 
     * @param newSize The new size of the action stack.
     */
    private void notifyOnStackChange(int newSize) {
        if (listener != null) listener.onStackChange(newSize);
    }

    /**
     * Abstract class representing an undo/redo action.
     */
    abstract class UndoRedoAction {
        public int startOffset;
        public int endOffset;
        public String actionData;
        public int groupId;
        public final long MAX_MERGE_INTERVAL = 1000000000;

        public abstract void performUndo();
        public abstract void performRedo();
        public abstract void captureData(int start, int end);
        public abstract int getUndoCaretPosition();
        public abstract int getRedoCaretPosition();
        public abstract boolean tryMerge(int start, int end, long timestamp);
    }
    

    /**
     * Represents an insert action for undo/redo management.
     */
    class InsertAction extends UndoRedoAction {
        /**
         * Constructs an InsertAction with the specified parameters.
         * 
         * @param startOffset The starting offset of the insert action.
         * @param endOffset The ending offset of the insert action.
         * @param groupId The group ID for this action.
         */
        public InsertAction(int startOffset, int endOffset, int groupId) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.groupId = groupId;
        }

        @Override
        public boolean tryMerge(int start, int end, long timestamp) {
            if (previousEditTimestamp < 0) {
                return false;
            }
            if ((timestamp - previousEditTimestamp) < MAX_MERGE_INTERVAL && start == end) {
                endOffset += end - start;
                limitHistorySize();
                return true;
            }
            return false;
        }

        @Override
        public void captureData(int start, int end) {
            actionData = pieceTable.textRange(
                this.startOffset = start,
                this.endOffset = end).toString();
        }

        @Override
        public void performUndo() {
            if (actionData == null) {
                captureData(startOffset, endOffset);
                pieceTable.delete(startOffset, endOffset);
            } else {
                pieceTable.delete(startOffset, endOffset);
            }
        }

        @Override
        public void performRedo() {
            pieceTable.insert(startOffset, actionData);
        }

        @Override
        public int getRedoCaretPosition() {
            return endOffset;
        }

        @Override
        public int getUndoCaretPosition() {
            return startOffset;
        }
    }

    /**
     * Represents a delete action for undo/redo management.
     */
    class DeleteAction extends UndoRedoAction {
        /**
         * Constructs a DeleteAction with the specified parameters.
         * 
         * @param startOffset The starting offset of the delete action.
         * @param endOffset The ending offset of the delete action.
         * @param groupId The group ID for this action.
         */
        public DeleteAction(int startOffset, int endOffset, int groupId) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.groupId = groupId;
        }

        @Override
        public boolean tryMerge(int start, int end, long timestamp) {
            if (previousEditTimestamp < 0) {
                return false;
            }
            if ((timestamp - previousEditTimestamp) < MAX_MERGE_INTERVAL && end == start) {
                startOffset = start;
                limitHistorySize();
                return true;
            }
            return false;
        }

        @Override
        public void captureData(int start, int end) {
//            long cStart = System.currentTimeMillis();
            actionData = pieceTable.textRange(
                this.startOffset = start,
                this.endOffset = end).toString();
//            long cEnd = System.currentTimeMillis();
//            throw new Error("start?: " + start + "    end?: " + end + "\nTime taken captureData?: " + (cEnd - cStart) / 1000.0 + " seconds");
        }

        @Override
        public void performUndo() {
            if (actionData == null) {
                captureData(startOffset, endOffset);
                pieceTable.insert(startOffset, actionData);
            } else {
                pieceTable.insert(startOffset, actionData);
            }
        }

        @Override
        public void performRedo() {
            pieceTable.delete(startOffset, endOffset);
        }

        @Override
        public int getRedoCaretPosition() {
            return startOffset;
        }

        @Override
        public int getUndoCaretPosition() {
            return endOffset;
        }
    }
    
    /**
     * Interface for listeners to receive undo/redo notifications.
     */
    public interface UndoRedoListener {
        void onUndo(int caretPosition);
        void onRedo(int caretPosition);
        void onChange(int changedItem, int newValue, long timestamp);
        void onStackChange(int newSize);
    }
    
}