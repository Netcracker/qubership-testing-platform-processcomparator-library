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

import java.util.ArrayList;
import java.util.List;

import org.qubership.automation.pc.core.enums.VCSSortBy;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSContext;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSFile;

/**
 * Default implementation of the {@link IVCSContext} interface.
 *
 * <p>
 * Represents the context of a Version Control System (VCS) read operation,
 * including metadata such as the revision and URL, as well as a list of retrieved files.
 * This implementation supports sorting of files by name or modification date.
 * </p>
 */
public class VCSContext implements IVCSContext {

    private String revision;
    private String url;
    private List<IVCSFile> files = new ArrayList<>();

    @Override
    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setFiles(List<IVCSFile> files) {
        this.files = files;
    }

    @Override
    public String getRevision() {
        return this.revision;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public List<IVCSFile> getFiles() {
        return this.files;
    }

    @Override
    public void sortFiles(VCSSortBy sortBy) {
        this.files.sort((f1, f2) -> {
            int sortMod = sortBy == VCSSortBy.DATE_ASC || sortBy == VCSSortBy.NAME_ASC ? 1 : -1;
            int compareResult;
            if (sortBy == VCSSortBy.DATE_ASC || sortBy == VCSSortBy.DATE_DESC) {
                return f1.getDate().compareTo(f2.getDate()) * sortMod;
            } else {
                return f1.getPath().compareTo(f2.getPath()) * sortMod;                
            }
        });
    }

}
