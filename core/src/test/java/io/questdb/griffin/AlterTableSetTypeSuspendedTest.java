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

package io.questdb.griffin;

import io.questdb.*;
import io.questdb.cairo.CairoEngine;
import io.questdb.cairo.TableToken;
import io.questdb.cairo.wal.ApplyWal2TableJob;
import io.questdb.std.Chars;
import io.questdb.std.Files;
import io.questdb.std.FilesFacade;
import io.questdb.std.TestFilesFacadeImpl;
import io.questdb.std.str.LPSZ;
import io.questdb.std.str.Path;
import io.questdb.test.tools.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.questdb.griffin.AlterTableSetTypeTest.NON_WAL;
import static org.junit.Assert.*;

public class AlterTableSetTypeSuspendedTest extends AbstractAlterTableSetTypeRestartTest {

    @BeforeClass
    public static void setUpStatic() throws Exception {
        AbstractBootstrapTest.setUpStatic();
        try {
            createDummyConfiguration(PropertyKey.CAIRO_WAL_SUPPORTED.getPropertyPath() + "=true");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testWalSuspendedToNonWal() throws Exception {
        final String tableName = testName.getMethodName();
        TestUtils.assertMemoryLeak(() -> {
            final FilesFacade filesFacade = new TestFilesFacadeImpl() {
                private int attempt = 0;

                @Override
                public int openRW(LPSZ name, long opts) {
                    if (Chars.contains(name, "x.d.1") && attempt++ == 0) {
                        return -1;
                    }
                    return Files.openRW(name, opts);
                }
            };

            final Bootstrap bootstrap = new Bootstrap(null, System.getenv(), filesFacade, "-d", root.toString(), Bootstrap.SWITCH_USE_DEFAULT_LOG_FACTORY_CONFIGURATION);
            try (final ServerMain questdb = new TestServerMain(bootstrap)) {
                questdb.start();
                createTable(tableName, "WAL");

                final CairoEngine engine = questdb.getCairoEngine();
                final TableToken token = engine.getTableToken(tableName);

                try (final ApplyWal2TableJob walApplyJob = new ApplyWal2TableJob(engine, 1, 1, null)) {
                    insertInto(tableName);
                    walApplyJob.drain(0);

                    // WAL table
                    assertTrue(engine.isWalTable(token));
                    assertNumOfRows(engine, tableName, 1);
                    assertConvertFileDoesNotExist(engine, token);

                    // suspend table
                    // below should fail
                    runSqlViaPG("update " + tableName + " set x = 1111");
                    walApplyJob.drain(0);
                    assertTrue(engine.getTableSequencerAPI().isSuspended(token));
                    checkSuspended(tableName);

                    insertInto(tableName);
                    walApplyJob.drain(0);

                    // WAL table suspended, insert not applied
                    assertTrue(engine.isWalTable(token));
                    assertNumOfRows(engine, tableName, 1);
                    assertConvertFileDoesNotExist(engine, token);

                    // schedule table conversion to non-WAL
                    setType(tableName, "BYPASS WAL");
                    final Path path = assertConvertFileExists(engine, token);
                    assertConvertFileContent(path, NON_WAL);
                }
            }
            validateShutdown(tableName);

            // restart
            try (final ServerMain questdb = new TestServerMain("-d", root.toString(), Bootstrap.SWITCH_USE_DEFAULT_LOG_FACTORY_CONFIGURATION)) {
                questdb.start();

                final CairoEngine engine = questdb.getCairoEngine();
                final TableToken token = engine.getTableToken(tableName);
                assertFalse(engine.isWalTable(token));

                // insert works now
                insertInto(tableName);
                assertNumOfRows(engine, tableName, 2);

                dropTable(tableName);
            }
        });
    }
}
