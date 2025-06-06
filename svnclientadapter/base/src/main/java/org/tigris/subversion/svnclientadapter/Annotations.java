/*******************************************************************************
 * Copyright (c) 2006 svnClientAdapter project and others.
 * </p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * </p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 * Contributors:
 *     svnClientAdapter project committers - initial API and implementation
 ******************************************************************************/

package org.tigris.subversion.svnclientadapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generic implementation of the {@link ISVNAnnotations} interface.
 * </p>
 * This class represents annotations (such as author, revision, and date) for each line of a file under version control.
 * Annotations are typically filled using the {@link #addAnnotation(Annotation)} method.
 * </p>
 */
public class Annotations implements ISVNAnnotations {

    /**
     * List of annotation records, one per line.
     */
    private List annotations = new ArrayList();

    /**
     * Returns the annotation for the specified line index.
     *
     * @param i the line number
     * @return the {@link Annotation} for the specified line, or {@code null} if the index is out of bounds
     */
    protected Annotation getAnnotation(int i) {
        if (i >= this.annotations.size()) {
            return null;
        }
        return (Annotation) this.annotations.get(i);
    }

    /**
     * Appends an annotation record to the list of annotations.
     *
     * @param annotation the annotation to add; must not be {@code null}
     */
    public void addAnnotation(Annotation annotation) {
        this.annotations.add(annotation);
    }

    /**
     * {@inheritDoc}
     */
    public long getRevision(int lineNumber) {
        Annotation annotation = getAnnotation(lineNumber);
        return (annotation == null) ? -1 : annotation.getRevision();
    }

    /**
     * {@inheritDoc}
     */
    public String getAuthor(int lineNumber) {
        Annotation annotation = getAnnotation(lineNumber);
        return (annotation == null) ? null : annotation.getAuthor();
    }

    /**
     * {@inheritDoc}
     */
    public Date getChanged(int lineNumber) {
        Annotation annotation = getAnnotation(lineNumber);
        return (annotation == null) ? null : annotation.getChanged();
    }

    /**
     * {@inheritDoc}
     */
    public String getLine(int lineNumber) {
        Annotation annotation = getAnnotation(lineNumber);
        return (annotation == null) ? null : annotation.getLine();
    }

    /**
     * {@inheritDoc}
     */
    public InputStream getInputStream() {
        return new AnnotateInputStream(this);
    }

    /**
     * {@inheritDoc}
     */
    public int numberOfLines() {
        return this.annotations.size();
    }

    /**
     * Represents a single line annotation in a file, including metadata such as revision, author, and change date.
     */
    public static class Annotation {

        private long revision;
        private String author;
        private Date changed;
        private String line;

        /**
         * Constructs a new annotation.
         *
         * @param revision the revision number; if {@code -1}, a placeholder is used
         * @param author the author of the change
         * @param changed the date the line was changed
         * @param line the content of the line
         */
        public Annotation(long revision, String author, Date changed, String line) {
            if (revision == -1) {
                this.revision = 0;
                this.author = "No change";
                this.changed = new Date();
            } else {
                this.revision = revision;
                this.author = author;
                this.changed = changed;
            }
            this.line = line;
        }

        /**
         * Get author.
         *
         * @return the author of the change
         */
        public String getAuthor() {
            return author;
        }

        /**
         * Get change.
         *
         * @return the date the line was last changed
         */
        public Date getChanged() {
            return changed;
        }

        /**
         * Get content of the line.
         *
         * @return the content of the line
         */
        public String getLine() {
            return line;
        }

        /**
         * Sets the content of the line.
         *
         * @param line the new line content
         */
        public void setLine(String line) {
            this.line = line;
        }

        /**
         * Get revision number.
         *
         * @return the revision number of the change
         */
        public long getRevision() {
            return revision;
        }

        /**
         * Returns a string representation of the annotation in the format:
         * {@code revision:author:line}.
         *
         * @return string representation of the annotation
         */
        public String toString() {
            return getRevision() + ":" + getAuthor() + ":" + getLine();
        }
    }

    /**
     * An {@link InputStream} that reads annotation lines as plain text,
     * one line per read, adding newlines between them.
     */
    protected static class AnnotateInputStream extends InputStream {
        private ISVNAnnotations annotations;
        private int currentLineNumber;
        private int currentPos;
        private String currentLine;
        private int available;

        /**
         * Constructs a new {@code AnnotateInputStream}.
         *
         * @param annotations the annotations to stream; must not be {@code null}
         */
        public AnnotateInputStream(ISVNAnnotations annotations) {
            this.annotations = annotations;
            initialize();
        }

        private void initialize() {
            currentLine = annotations.getLine(0);
            currentLineNumber = 0;
            currentPos = 0;

            available = 0;
            int annotationsSize = annotations.numberOfLines();
            for (int i = 0; i < annotationsSize; i++) {
                available += annotations.getLine(i).length();
                if (i != annotationsSize - 1) {
                    available++; // account for '\n'
                }
            }
        }

        private void getNextLine() {
            currentLineNumber++;
            currentPos = 0;
            currentLine = annotations.getLine(currentLineNumber);
        }

        /**
         * Reads the next character from the annotation stream.
         *
         * @return the next character as an int, or {@code -1} if end of stream
         * @throws IOException if an I/O error occurs
         */
        public int read() throws IOException {
            if (currentLineNumber >= annotations.numberOfLines()) {
                return -1;
            }
            if (currentPos > currentLine.length()) {
                getNextLine();
                if (currentLineNumber >= annotations.numberOfLines()) {
                    return -1;
                }
            }
            int character;
            if (currentPos == currentLine.length()) {
                character = '\n';
            } else {
                character = currentLine.charAt(currentPos);
            }
            currentPos++;
            available--;
            return character;
        }

        /**
         * Returns an estimate of the number of bytes that can be read.
         *
         * @return the number of available bytes
         * @throws IOException if an I/O error occurs
         */
        public int available() throws IOException {
            return available;
        }

        /**
         * Resets the stream to the beginning.
         *
         * @throws IOException if an I/O error occurs
         */
        public synchronized void reset() throws IOException {
            initialize();
        }
    }
}
