/*
Copyright 2016 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.twitter.hraven.datasource;

import java.io.IOException;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;

import com.twitter.hraven.Constants;
import com.twitter.hraven.JobKey;
import com.twitter.hraven.QualifiedJobId;

/**
 * Service to access the {@link Constants#HISTORY_BY_JOBID_TABLE}.
 *
 */
public class JobHistoryByIdService {

  private JobKeyConverter jobKeyConv = new JobKeyConverter();
  private QualifiedJobIdConverter jobIdConv = new QualifiedJobIdConverter();

  // private final Configuration conf;
  private final Connection hbaseConnection;

  /**
   * Opens a new connection to HBase server and opens connections to the tables.
   *
   * User is responsible for calling {@link #close()} when finished using this
   * service.
   * @param hbaseConnection TODO
   *
   * @throws IOException
   */
  public JobHistoryByIdService(Connection hbaseConnection) throws IOException {
    this.hbaseConnection = hbaseConnection;
  }

  /**
   * Returns the JobKey for the job_history table, stored for this job ID, or
   * {@code null} if not found.
   * @param jobId the cluster and job ID combination to look up
   * @return the JobKey instance stored, or {@code null} if not found
   * @throws IOException if thrown by the HBase client
   */
  public JobKey getJobKeyById(QualifiedJobId jobId) throws IOException {
    byte[] indexKey = jobIdConv.toBytes(jobId);

    Get g = new Get(indexKey);
    g.addColumn(Constants.INFO_FAM_BYTES, Constants.ROWKEY_COL_BYTES);
    Table historyByJobIdTable = null;

    try {
      historyByJobIdTable = hbaseConnection
          .getTable(TableName.valueOf(Constants.HISTORY_BY_JOBID_TABLE));
      Result r = historyByJobIdTable.get(g);
      if (r != null && !r.isEmpty()) {
        byte[] historyKey =
            r.getValue(Constants.INFO_FAM_BYTES, Constants.ROWKEY_COL_BYTES);
        if (historyKey != null && historyKey.length > 0) {
          return jobKeyConv.fromBytes(historyKey);
        }
      }
    } finally {
      if (historyByJobIdTable != null) {
        historyByJobIdTable.close();
      }
    }
    return null;
  }

  /**
   * Create the secondary indexes records cluster!jobId->jobKey.
   *
   * @param jobKey
   * @throws IOException if the entry cannot be written.
   */
  public void writeIndexes(JobKey jobKey) throws IOException {
    // Defensive coding
    if (jobKey != null) {
      Table historyByJobIdTable = null;

      try {
        historyByJobIdTable = hbaseConnection
            .getTable(TableName.valueOf(Constants.HISTORY_BY_JOBID_TABLE));
        byte[] jobKeyBytes = jobKeyConv.toBytes(jobKey);
        byte[] rowKeyBytes = jobIdConv.toBytes(
            new QualifiedJobId(jobKey.getCluster(), jobKey.getJobId()));

        // Insert (or update) row with jobid as the key
        Put p = new Put(rowKeyBytes);
        p.addColumn(Constants.INFO_FAM_BYTES, Constants.ROWKEY_COL_BYTES,
            jobKeyBytes);
        historyByJobIdTable.put(p);
      } finally {
        if (historyByJobIdTable != null) {
          historyByJobIdTable.close();
        }
      }
    }
  }

}
