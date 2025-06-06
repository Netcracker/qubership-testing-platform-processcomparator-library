/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.automation.pc.reader.impl.vcs;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.qubership.automation.pc.core.enums.VCSFileType;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSFile;

/**
 * Represents a file or directory in a Version Control System (VCS).
 *
 * <p>
 * This class encapsulates metadata such as revision, author, and timestamp,
 * as well as the file content and type. It is typically used by VCS providers
 * to deliver file data during remote reads.
 * </p>
 */
public class VCSFile implements IVCSFile {

    private final VCSFileType fileType;
    private final InputStream content;
    private final String author;
    private final String revision;
    private final String path;
    private final Date date;

    private List<IVCSFile> children;

    public VCSFile(String path, String revision, String author, Date date, InputStream content, VCSFileType fileType) {
        this.path = path;
        this.revision = revision;
        this.author = author;
        this.content = content;
        this.fileType = fileType;
        this.date = date;
    }

    @Override
    public Date getDate() {
        return this.date;
    }

    @Override
    public VCSFileType getType() {
        return this.fileType;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getRevision() {
        return this.revision;
    }

    @Override
    public String getAuthor() {
        return this.author;
    }

    @Override
    public File toFile() {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream getContent() {
        return this.content;
    }

    @Override
    public List<IVCSFile> getChildren() {
        return this.children;
    }

    @Override
    public void setChildren(List<IVCSFile> children) {
        this.children = children;
    }

}
