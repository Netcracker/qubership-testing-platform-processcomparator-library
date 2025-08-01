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

package org.tigris.subversion.svnclientadapter.commandline;

import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNDirEntryWithLock;
import org.tigris.subversion.svnclientadapter.ISVNLock;

public class CmdLineRemoteDirEntryWithLock implements ISVNDirEntryWithLock {

    private final ISVNDirEntry dirEntry;
    private final ISVNLock lock;

    public CmdLineRemoteDirEntryWithLock(ISVNDirEntry dirEntry, ISVNLock lock) {
        super();
        this.dirEntry = dirEntry;
        this.lock = lock;
    }

    public ISVNDirEntry getDirEntry() {
        return dirEntry;
    }

    public ISVNLock getLock() {
        return lock;
    }

}
