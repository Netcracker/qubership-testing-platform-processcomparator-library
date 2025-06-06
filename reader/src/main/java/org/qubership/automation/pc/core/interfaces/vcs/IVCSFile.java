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

package org.qubership.automation.pc.core.interfaces.vcs;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.qubership.automation.pc.core.enums.VCSFileType;

/**
 * Interface representing a file or directory in a Version Control System (VCS).
 *
 * <p>
 * This abstraction provides metadata such as revision, author, path, and content
 * access, along with structural information for directories (children).
 * </p>
 */
public interface IVCSFile {

    Date getDate();

    String getRevision();

    String getAuthor();

    String getPath();

    VCSFileType getType();

    List<IVCSFile> getChildren();

    void setChildren(List<IVCSFile> children);

    File toFile();

    InputStream getContent();

}
