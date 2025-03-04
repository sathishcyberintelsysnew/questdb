/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2023 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.cairo;

import io.questdb.std.FilesFacade;
import io.questdb.std.TestFilesFacadeImpl;

public class DefaultTestCairoConfiguration extends DefaultCairoConfiguration {
    public DefaultTestCairoConfiguration(CharSequence root) {
        super(root);
    }

    @Override
    public boolean disableColumnPurgeJob() {
        return true;
    }

    @Override
    public boolean getAllowTableRegistrySharedWrite() {
        return true;
    }

    @Override
    public FilesFacade getFilesFacade() {
        return TestFilesFacadeImpl.INSTANCE;
    }

    @Override
    public int getO3ColumnMemorySize() {
        // Reduce test memory consumption, set o3 column memory to 1MB
        return 1 << 20;
    }

    @Override
    public CharSequence getSystemTableNamePrefix() {
        return "sys.";
    }

    @Override
    public int getTableRegistryCompactionThreshold() {
        return 0;
    }

    @Override
    public boolean mangleTableDirNames() {
        return true;
    }
}
