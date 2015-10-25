/*******************************************************************************
 *  _  _ ___ ___     _ _
 * | \| | __/ __| __| | |__
 * | .` | _|\__ \/ _` | '_ \
 * |_|\_|_| |___/\__,_|_.__/
 *
 * Copyright (c) 2014-2015. The NFSdb project and its contributors.
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
 ******************************************************************************/

package com.nfsdb.ql;

import com.nfsdb.Journal;
import com.nfsdb.JournalWriter;
import com.nfsdb.collections.ObjList;
import com.nfsdb.io.RecordSourcePrinter;
import com.nfsdb.io.sink.StringSink;
import com.nfsdb.model.Quote;
import com.nfsdb.ql.impl.AllRowSource;
import com.nfsdb.ql.impl.JournalPartitionSource;
import com.nfsdb.ql.impl.JournalSource;
import com.nfsdb.ql.impl.ResampledSource;
import com.nfsdb.ql.ops.CountIntAggregatorFunction;
import com.nfsdb.ql.ops.FirstDoubleAggregationFunction;
import com.nfsdb.ql.ops.LastDoubleAggregationFunction;
import com.nfsdb.test.tools.AbstractTest;
import com.nfsdb.test.tools.TestUtils;
import com.nfsdb.utils.Dates;
import org.junit.Assert;
import org.junit.Test;

public class ResampledSourceTest extends AbstractTest {

    @Test
    public void testResampleWithCount() throws Exception {

        final String expected = "8\t1.050231933594\t0.000014963527\t2015-01-01T00:00:00.000Z\tAGK.L\n" +
                "4\t0.006688738358\t97.790924072266\t2015-01-01T00:00:00.000Z\tBP.L\n" +
                "3\t0.000020634160\t52.330078125000\t2015-01-01T00:00:00.000Z\tRRS.L\n" +
                "8\t0.000000035797\t0.004646989168\t2015-01-01T00:00:00.000Z\tBT-A.L\n" +
                "6\t0.040750414133\t1.461132109165\t2015-01-01T00:00:00.000Z\tGKN.L\n" +
                "10\t0.032379742712\t164.218750000000\t2015-01-01T00:00:00.000Z\tLLOY.L\n" +
                "9\t0.000000000000\t310.101058959961\t2015-01-01T00:00:00.000Z\tABF.L\n" +
                "3\t695.796875000000\t0.001741337735\t2015-01-01T00:00:00.000Z\tWTB.L\n" +
                "5\t1024.000000000000\t0.611244902015\t2015-01-01T00:00:00.000Z\tTLW.L\n" +
                "4\t0.000355183205\t353.683593750000\t2015-01-01T00:00:00.000Z\tADM.L\n" +
                "10\t7.977778911591\t0.519029200077\t2015-01-01T00:01:00.000Z\tGKN.L\n" +
                "9\t820.224609375000\t0.214787468314\t2015-01-01T00:01:00.000Z\tBT-A.L\n" +
                "5\t0.000000040298\t756.000000000000\t2015-01-01T00:01:00.000Z\tAGK.L\n" +
                "4\t0.000785129319\t0.009976797737\t2015-01-01T00:01:00.000Z\tLLOY.L\n" +
                "8\t0.000000718634\t258.000000000000\t2015-01-01T00:01:00.000Z\tRRS.L\n" +
                "6\t0.000000059185\t1024.000000000000\t2015-01-01T00:01:00.000Z\tADM.L\n" +
                "6\t0.000000818963\t492.000000000000\t2015-01-01T00:01:00.000Z\tABF.L\n" +
                "4\t512.000000000000\t312.000000000000\t2015-01-01T00:01:00.000Z\tTLW.L\n" +
                "3\t0.000002408446\t0.000000001387\t2015-01-01T00:01:00.000Z\tWTB.L\n" +
                "5\t0.000340424791\t896.000000000000\t2015-01-01T00:01:00.000Z\tBP.L\n" +
                "7\t0.000000010313\t272.000000000000\t2015-01-01T00:02:00.000Z\tBT-A.L\n" +
                "7\t0.003173287259\t957.521484375000\t2015-01-01T00:02:00.000Z\tABF.L\n" +
                "5\t726.000000000000\t0.000087654265\t2015-01-01T00:02:00.000Z\tGKN.L\n" +
                "6\t948.018188476563\t0.000002607587\t2015-01-01T00:02:00.000Z\tLLOY.L\n" +
                "7\t0.000288516894\t0.010696892394\t2015-01-01T00:02:00.000Z\tTLW.L\n" +
                "6\t786.804687500000\t2.103317737579\t2015-01-01T00:02:00.000Z\tRRS.L\n" +
                "4\t0.000000001803\t0.000000004889\t2015-01-01T00:02:00.000Z\tBP.L\n" +
                "8\t0.000002963134\t0.000000090628\t2015-01-01T00:02:00.000Z\tWTB.L\n" +
                "6\t708.902420043945\t703.077026367188\t2015-01-01T00:02:00.000Z\tAGK.L\n" +
                "4\t0.000000652893\t0.000000001835\t2015-01-01T00:02:00.000Z\tADM.L\n" +
                "5\t0.000000018871\t0.161021415144\t2015-01-01T00:03:00.000Z\tLLOY.L\n" +
                "8\t752.000000000000\t269.764038085938\t2015-01-01T00:03:00.000Z\tRRS.L\n" +
                "3\t107.809616088867\t0.000000000000\t2015-01-01T00:03:00.000Z\tTLW.L\n" +
                "9\t0.000000024976\t0.038886148483\t2015-01-01T00:03:00.000Z\tADM.L\n" +
                "8\t231.702758789063\t283.535179138184\t2015-01-01T00:03:00.000Z\tBP.L\n" +
                "7\t530.130859375000\t0.000019413717\t2015-01-01T00:03:00.000Z\tAGK.L\n" +
                "9\t0.000000000816\t638.767578125000\t2015-01-01T00:03:00.000Z\tGKN.L\n" +
                "5\t0.000000034833\t569.585937500000\t2015-01-01T00:03:00.000Z\tABF.L\n" +
                "2\t432.000000000000\t46.308317184448\t2015-01-01T00:03:00.000Z\tBT-A.L\n" +
                "4\t0.039086496457\t240.000000000000\t2015-01-01T00:03:00.000Z\tWTB.L\n" +
                "10\t1.459998697042\t392.000000000000\t2015-01-01T00:04:00.000Z\tBT-A.L\n" +
                "6\t0.000045827703\t0.750000000000\t2015-01-01T00:04:00.000Z\tADM.L\n" +
                "9\t384.931640625000\t0.450333625078\t2015-01-01T00:04:00.000Z\tBP.L\n" +
                "6\t794.518920898438\t0.000000005982\t2015-01-01T00:04:00.000Z\tGKN.L\n" +
                "6\t0.004665839602\t244.352966308594\t2015-01-01T00:04:00.000Z\tTLW.L\n" +
                "7\t0.005728640477\t40.062500000000\t2015-01-01T00:04:00.000Z\tWTB.L\n" +
                "5\t0.000000568244\t0.000000043666\t2015-01-01T00:04:00.000Z\tLLOY.L\n" +
                "6\t548.718750000000\t0.000008238535\t2015-01-01T00:04:00.000Z\tRRS.L\n" +
                "3\t0.000000958444\t0.000652605682\t2015-01-01T00:04:00.000Z\tABF.L\n" +
                "2\t0.000002877019\t0.000000007591\t2015-01-01T00:04:00.000Z\tAGK.L\n" +
                "3\t320.000000000000\t16.212998390198\t2015-01-01T00:05:00.000Z\tGKN.L\n" +
                "8\t0.000000010817\t0.000000009790\t2015-01-01T00:05:00.000Z\tLLOY.L\n" +
                "7\t272.000000000000\t0.000000004605\t2015-01-01T00:05:00.000Z\tRRS.L\n" +
                "7\t363.160156250000\t2.872851848602\t2015-01-01T00:05:00.000Z\tTLW.L\n" +
                "6\t24.333267211914\t196.104507446289\t2015-01-01T00:05:00.000Z\tADM.L\n" +
                "8\t0.000039814179\t0.000004861412\t2015-01-01T00:05:00.000Z\tAGK.L\n" +
                "3\t0.000000007138\t0.000000459957\t2015-01-01T00:05:00.000Z\tBT-A.L\n" +
                "7\t0.000000832384\t32.000000000000\t2015-01-01T00:05:00.000Z\tWTB.L\n" +
                "8\t148.932441711426\t0.004017438507\t2015-01-01T00:05:00.000Z\tBP.L\n" +
                "3\t0.000000434765\t0.000000011260\t2015-01-01T00:05:00.000Z\tABF.L\n" +
                "9\t37.507137298584\t508.197265625000\t2015-01-01T00:06:00.000Z\tADM.L\n" +
                "6\t0.000000382630\t1.155909627676\t2015-01-01T00:06:00.000Z\tBT-A.L\n" +
                "6\t0.000000008101\t0.630369469523\t2015-01-01T00:06:00.000Z\tBP.L\n" +
                "6\t61.015514373779\t0.000150146036\t2015-01-01T00:06:00.000Z\tABF.L\n" +
                "7\t0.002171463799\t568.654052734375\t2015-01-01T00:06:00.000Z\tGKN.L\n" +
                "5\t0.000000047101\t256.000000000000\t2015-01-01T00:06:00.000Z\tRRS.L\n" +
                "4\t0.006601167843\t246.000000000000\t2015-01-01T00:06:00.000Z\tTLW.L\n" +
                "12\t0.102180968970\t0.000018539900\t2015-01-01T00:06:00.000Z\tWTB.L\n" +
                "2\t0.000003584670\t0.000008596182\t2015-01-01T00:06:00.000Z\tAGK.L\n" +
                "3\t0.002955231466\t0.000000000000\t2015-01-01T00:06:00.000Z\tLLOY.L\n" +
                "3\t0.000005770782\t0.000003102578\t2015-01-01T00:07:00.000Z\tBP.L\n" +
                "8\t822.000000000000\t0.125839930028\t2015-01-01T00:07:00.000Z\tAGK.L\n" +
                "9\t1019.551757812500\t467.000000000000\t2015-01-01T00:07:00.000Z\tABF.L\n" +
                "6\t0.000069882813\t26.540039062500\t2015-01-01T00:07:00.000Z\tGKN.L\n" +
                "4\t1024.000000000000\t240.373519897461\t2015-01-01T00:07:00.000Z\tBT-A.L\n" +
                "6\t916.312500000000\t1023.500000000000\t2015-01-01T00:07:00.000Z\tTLW.L\n" +
                "6\t0.000000012859\t43.095703125000\t2015-01-01T00:07:00.000Z\tWTB.L\n" +
                "3\t7.250000000000\t576.000000000000\t2015-01-01T00:07:00.000Z\tLLOY.L\n" +
                "9\t28.000000000000\t170.362548828125\t2015-01-01T00:07:00.000Z\tADM.L\n" +
                "6\t551.125000000000\t1014.217071533203\t2015-01-01T00:07:00.000Z\tRRS.L\n" +
                "8\t25.775287628174\t0.000000096432\t2015-01-01T00:08:00.000Z\tBP.L\n" +
                "9\t49.693923950195\t832.000000000000\t2015-01-01T00:08:00.000Z\tWTB.L\n" +
                "8\t0.000000320372\t0.162387616932\t2015-01-01T00:08:00.000Z\tABF.L\n" +
                "6\t127.306762695313\t30.740950584412\t2015-01-01T00:08:00.000Z\tBT-A.L\n" +
                "6\t7.639264583588\t6.708699941635\t2015-01-01T00:08:00.000Z\tGKN.L\n" +
                "6\t0.000006120900\t538.000000000000\t2015-01-01T00:08:00.000Z\tTLW.L\n" +
                "5\t655.093750000000\t0.085098911077\t2015-01-01T00:08:00.000Z\tLLOY.L\n" +
                "4\t943.760406494141\t113.828125000000\t2015-01-01T00:08:00.000Z\tADM.L\n" +
                "4\t0.000006406346\t0.001021439588\t2015-01-01T00:08:00.000Z\tAGK.L\n" +
                "4\t722.000000000000\t0.000000001771\t2015-01-01T00:08:00.000Z\tRRS.L\n" +
                "11\t0.000000021755\t0.249120667577\t2015-01-01T00:09:00.000Z\tBP.L\n" +
                "8\t1.757507264614\t0.000000003206\t2015-01-01T00:09:00.000Z\tABF.L\n" +
                "9\t0.000864484580\t363.773437500000\t2015-01-01T00:09:00.000Z\tADM.L\n" +
                "5\t0.000000019996\t0.000000007078\t2015-01-01T00:09:00.000Z\tRRS.L\n" +
                "7\t0.000385815001\t0.000072461031\t2015-01-01T00:09:00.000Z\tGKN.L\n" +
                "5\t479.420410156250\t998.000000000000\t2015-01-01T00:09:00.000Z\tLLOY.L\n" +
                "3\t0.033479256555\t466.125000000000\t2015-01-01T00:09:00.000Z\tBT-A.L\n" +
                "2\t0.000006532962\t414.000000000000\t2015-01-01T00:09:00.000Z\tAGK.L\n" +
                "7\t92.858070373535\t0.000003113521\t2015-01-01T00:09:00.000Z\tWTB.L\n" +
                "3\t790.832031250000\t472.360717773438\t2015-01-01T00:09:00.000Z\tTLW.L\n" +
                "6\t0.000000000000\t0.004298945889\t2015-01-01T00:10:00.000Z\tBP.L\n" +
                "8\t0.002109512687\t30.057767391205\t2015-01-01T00:10:00.000Z\tRRS.L\n" +
                "7\t0.000000075999\t0.083750320598\t2015-01-01T00:10:00.000Z\tWTB.L\n" +
                "7\t976.000000000000\t56.306331634521\t2015-01-01T00:10:00.000Z\tGKN.L\n" +
                "6\t0.008530025836\t0.000000130447\t2015-01-01T00:10:00.000Z\tAGK.L\n" +
                "7\t0.000000005825\t0.000000093465\t2015-01-01T00:10:00.000Z\tBT-A.L\n" +
                "5\t0.004615874961\t184.494560241699\t2015-01-01T00:10:00.000Z\tADM.L\n" +
                "3\t824.000000000000\t0.042684378102\t2015-01-01T00:10:00.000Z\tTLW.L\n" +
                "5\t25.977482318878\t0.000000926976\t2015-01-01T00:10:00.000Z\tABF.L\n" +
                "6\t0.000000187427\t615.000000000000\t2015-01-01T00:10:00.000Z\tLLOY.L\n" +
                "6\t0.000000693414\t0.183049753308\t2015-01-01T00:11:00.000Z\tLLOY.L\n" +
                "8\t0.006585577736\t30.768965721130\t2015-01-01T00:11:00.000Z\tTLW.L\n" +
                "9\t732.500000000000\t256.000000000000\t2015-01-01T00:11:00.000Z\tAGK.L\n" +
                "4\t512.000000000000\t0.000008049486\t2015-01-01T00:11:00.000Z\tBP.L\n" +
                "5\t512.000000000000\t0.000000656805\t2015-01-01T00:11:00.000Z\tABF.L\n" +
                "6\t761.156250000000\t0.000000137681\t2015-01-01T00:11:00.000Z\tGKN.L\n" +
                "8\t0.805891692638\t66.382812500000\t2015-01-01T00:11:00.000Z\tADM.L\n" +
                "7\t0.001220172097\t0.000000169138\t2015-01-01T00:11:00.000Z\tRRS.L\n" +
                "4\t379.466796875000\t616.000000000000\t2015-01-01T00:11:00.000Z\tBT-A.L\n" +
                "3\t0.000000006865\t1.987107336521\t2015-01-01T00:11:00.000Z\tWTB.L\n" +
                "4\t0.117184989154\t0.000000029966\t2015-01-01T00:12:00.000Z\tBT-A.L\n" +
                "6\t436.634277343750\t719.226562500000\t2015-01-01T00:12:00.000Z\tLLOY.L\n" +
                "8\t533.000000000000\t0.532832369208\t2015-01-01T00:12:00.000Z\tAGK.L\n" +
                "7\t829.000000000000\t0.000000000000\t2015-01-01T00:12:00.000Z\tABF.L\n" +
                "9\t0.000000011423\t776.061141967774\t2015-01-01T00:12:00.000Z\tRRS.L\n" +
                "6\t0.000000017828\t0.416699528694\t2015-01-01T00:12:00.000Z\tTLW.L\n" +
                "4\t61.953857421875\t0.000004567095\t2015-01-01T00:12:00.000Z\tADM.L\n" +
                "4\t103.453125000000\t422.500000000000\t2015-01-01T00:12:00.000Z\tGKN.L\n" +
                "6\t742.443939208984\t753.273437500000\t2015-01-01T00:12:00.000Z\tBP.L\n" +
                "6\t0.000000913363\t164.603073120117\t2015-01-01T00:12:00.000Z\tWTB.L\n" +
                "7\t136.000000000000\t0.000000013173\t2015-01-01T00:13:00.000Z\tAGK.L\n" +
                "7\t0.002259623026\t0.000000331959\t2015-01-01T00:13:00.000Z\tBT-A.L\n" +
                "6\t0.000000030545\t550.949218750000\t2015-01-01T00:13:00.000Z\tTLW.L\n" +
                "3\t2.614300668240\t0.000002100232\t2015-01-01T00:13:00.000Z\tLLOY.L\n" +
                "7\t938.094116210938\t0.000010022103\t2015-01-01T00:13:00.000Z\tWTB.L\n" +
                "4\t888.000000000000\t704.000000000000\t2015-01-01T00:13:00.000Z\tRRS.L\n" +
                "5\t0.000000241424\t720.000000000000\t2015-01-01T00:13:00.000Z\tABF.L\n" +
                "5\t0.000040283783\t198.750000000000\t2015-01-01T00:13:00.000Z\tBP.L\n" +
                "6\t0.000000008656\t7.029582500458\t2015-01-01T00:13:00.000Z\tGKN.L\n" +
                "10\t344.000000000000\t1024.000000000000\t2015-01-01T00:13:00.000Z\tADM.L\n" +
                "9\t165.562500000000\t0.217311806977\t2015-01-01T00:14:00.000Z\tTLW.L\n" +
                "7\t54.772387504578\t0.000000000000\t2015-01-01T00:14:00.000Z\tLLOY.L\n" +
                "5\t640.000000000000\t512.000000000000\t2015-01-01T00:14:00.000Z\tRRS.L\n" +
                "7\t217.949218750000\t608.000000000000\t2015-01-01T00:14:00.000Z\tADM.L\n" +
                "8\t748.500000000000\t0.000000185801\t2015-01-01T00:14:00.000Z\tWTB.L\n" +
                "5\t0.000880515348\t102.726562500000\t2015-01-01T00:14:00.000Z\tBP.L\n" +
                "3\t752.000000000000\t0.000000022925\t2015-01-01T00:14:00.000Z\tABF.L\n" +
                "7\t5.263916015625\t0.000000107452\t2015-01-01T00:14:00.000Z\tAGK.L\n" +
                "6\t0.027195315808\t9.347053050995\t2015-01-01T00:14:00.000Z\tGKN.L\n" +
                "3\t0.005947815487\t0.076017290354\t2015-01-01T00:14:00.000Z\tBT-A.L\n" +
                "8\t447.960723876953\t0.000000003280\t2015-01-01T00:15:00.000Z\tRRS.L\n" +
                "6\t0.000010041262\t0.001524728257\t2015-01-01T00:15:00.000Z\tLLOY.L\n" +
                "8\t0.072611110285\t4.011615157127\t2015-01-01T00:15:00.000Z\tGKN.L\n" +
                "5\t64.000000000000\t1024.000000000000\t2015-01-01T00:15:00.000Z\tTLW.L\n" +
                "4\t592.000000000000\t0.000000817260\t2015-01-01T00:15:00.000Z\tADM.L\n" +
                "5\t0.000001396557\t0.000013820148\t2015-01-01T00:15:00.000Z\tABF.L\n" +
                "7\t256.000000000000\t0.000002778678\t2015-01-01T00:15:00.000Z\tAGK.L\n" +
                "10\t0.252962961793\t0.001536227064\t2015-01-01T00:15:00.000Z\tWTB.L\n" +
                "5\t30.968750000000\t368.000000000000\t2015-01-01T00:15:00.000Z\tBT-A.L\n" +
                "2\t465.595077514648\t0.743070840836\t2015-01-01T00:15:00.000Z\tBP.L\n" +
                "7\t1024.000000000000\t0.000703593847\t2015-01-01T00:16:00.000Z\tBP.L\n" +
                "4\t0.000004057821\t512.558593750000\t2015-01-01T00:16:00.000Z\tAGK.L\n" +
                "5\t0.000000116992\t0.000000181370\t2015-01-01T00:16:00.000Z\tTLW.L\n" +
                "4\t0.700607895851\t507.980926513672\t2015-01-01T00:16:00.000Z\tGKN.L\n" +
                "2\t0.000000005606\t585.234497070313\t2015-01-01T00:16:00.000Z\tABF.L\n" +
                "8\t710.640625000000\t0.000000072118\t2015-01-01T00:16:00.000Z\tRRS.L\n" +
                "3\t657.562500000000\t256.000000000000\t2015-01-01T00:16:00.000Z\tLLOY.L\n" +
                "4\t11.337852239609\t0.634653210640\t2015-01-01T00:16:00.000Z\tBT-A.L\n" +
                "3\t420.000000000000\t0.274923749268\t2015-01-01T00:16:00.000Z\tWTB.L\n";


        JournalWriter<Quote> w = factory.writer(Quote.class);
        long start = Dates.toMillis(2015, 1, 1);
        TestUtils.generateQuoteData(w, 1000, start, 1000);
        w.commit();

        final Journal r = factory.reader(Quote.class.getName());


        // select count(), first(ask), last(ask), sym, ts sample by sym, 1m
        ResampledSource resampledSource = new ResampledSource(
                new JournalSource(
                        new JournalPartitionSource(r.getMetadata(), false)
                        , new AllRowSource()
                )
                ,
                new ObjList<CharSequence>() {{
                        add("sym");
                }}
                ,
                new ObjList<AggregatorFunction>() {{
                    add(new CountIntAggregatorFunction("count"));
                    add(new FirstDoubleAggregationFunction(r.getMetadata().getColumn("ask")));
                    add(new LastDoubleAggregationFunction(r.getMetadata().getColumn("ask")));
                }}
                , ResampledSource.SampleBy.MINUTE
        );

        StringSink sink = new StringSink();
        RecordSourcePrinter out = new RecordSourcePrinter(sink);
        out.printCursor(resampledSource.prepareCursor(factory));
        Assert.assertEquals(expected, sink.toString());
    }
}