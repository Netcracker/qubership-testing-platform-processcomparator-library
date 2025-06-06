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

import java.util.List;

import org.qubership.automation.pc.core.enums.VCSSortBy;

/**
 * Interface representing the context of a Version Control System (VCS) operation.
 *
 * <p>
 * An implementation of this interface provides metadata and file data retrieved
 * from a VCS, such as the revision, target URL, and a list of retrieved files.
 * It also allows sorting of files based on predefined sorting strategies.
 * </p>
 */
public interface IVCSContext {

    void setRevision(String revision);

    String getRevision();

    void setUrl(String url);

    String getUrl();

    void setFiles(List<IVCSFile> files);

    List<IVCSFile> getFiles();

    void sortFiles(VCSSortBy sortBy);
}
